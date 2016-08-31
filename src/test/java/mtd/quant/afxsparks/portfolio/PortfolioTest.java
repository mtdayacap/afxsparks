package mtd.quant.afxsparks.portfolio;

import static mtd.quant.afxsparks.Constants.BUY;
import static mtd.quant.afxsparks.Constants.MARKET_ORDER;
import static mtd.quant.afxsparks.Constants.SELL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import mtd.quant.afxsparks.Constants;
import mtd.quant.afxsparks.MathUtils;
import mtd.quant.afxsparks.data.Price;
import mtd.quant.afxsparks.data.StreamingForexPrices;
import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.event.OrderEvent;
import mtd.quant.afxsparks.event.SignalEvent;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {Position.class})
public class PortfolioTest {

  private static final String GBP = "GBP";

  private static final String CURRENCY_PAIR = "GBPUSD";

  @Mock
  private StreamingForexPrices pricesMock;

  private Portfolio portfolio;

  private SignalEvent signalEvent;

  private double equity;

  private double riskPerTrade;

  private double tradeUnits;

  private Map<String, Position> positions;

  private Price price;
  
  private Queue<Event> eventQueue;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    equity = 100000.0;
    riskPerTrade = 0.02;
    tradeUnits = MathUtils.roundHalfDown(equity * riskPerTrade, 2);
    double leverage = 20.0;

    portfolio = new Portfolio(GBP, leverage, equity, riskPerTrade);
    positions = (HashMap<String, Position>) Whitebox.getInternalState(portfolio, "positions");

    signalEvent = new SignalEvent(CURRENCY_PAIR, MARKET_ORDER, BUY, new Date());

    price = new Price();
    Mockito.when(pricesMock.getInstrumentPrice(signalEvent.getInstrument())).thenReturn(price);
    Whitebox.setInternalState(portfolio, "prices", pricesMock);
    
    eventQueue = new ConcurrentLinkedQueue<>();
    Whitebox.setInternalState(portfolio, "eventQueue", eventQueue);
  }

  @Test
  public void add_new_position_currency_pair_has_no_position() {
    // WHEN
    StreamingForexPrices pricesMock = Mockito.mock(StreamingForexPrices.class);
    double bid = 1.51214;
    double ask = 1.51225;
    Price price = new Price(signalEvent.getInstrument(), bid, ask);
    Mockito.when(pricesMock.getInstrumentPrice(signalEvent.getInstrument())).thenReturn(price);
    Whitebox.setInternalState(portfolio, "prices", pricesMock);

    // EXECUTE
    portfolio.executeSignal(signalEvent);
    Position actPosition = positions.get(signalEvent.getInstrument());
    OrderEvent actOrderEvent = (OrderEvent) eventQueue.poll();

    // THEN
    BigDecimal equity = new BigDecimal("100000.0");
    BigDecimal riskPerTrade = new BigDecimal("0.02");
    BigDecimal tradePerUnits = equity.multiply(riskPerTrade);
    double units = tradePerUnits.doubleValue();
    double exposure = tradePerUnits.doubleValue();

    Position expPosition = new Position(BUY, CURRENCY_PAIR, units, exposure, ask, bid);
    OrderEvent expOrderEvent = new OrderEvent(BUY, units, CURRENCY_PAIR, MARKET_ORDER);

    assertEquals(expPosition, actPosition);
    assertEquals(expOrderEvent, actOrderEvent);
  }

  @Test
  public void add_position_units_position_exists_and_same_side() {
    // WHEN
    double addPrice = 1.51225; // ask
    double removePrice = 1.51214; // buy

    // Add initial position
    double exposure = tradeUnits;
    Position initPosition = new Position(signalEvent.getSide(), signalEvent.getInstrument(),
        tradeUnits, exposure, addPrice, removePrice);
    positions.put(initPosition.getCurrencyPair(), initPosition);

    // Comes in the new bid and ask price
    price.setInstrument(signalEvent.getInstrument());
    price.setBid(1.51204);
    price.setAsk(1.51215);

    // EXECUTE
    portfolio.executeSignal(signalEvent);
    Position actPosition = positions.get(CURRENCY_PAIR);
    double actUnits = actPosition.getUnits();
    double actAvgPrice = actPosition.getAveragePrice();
    double actExposure = actPosition.getExposure();
    double actProfitBase = actPosition.getProfitBase();
    double actProfitPercentage = actPosition.getProfitPercentage();

    // THEN
    BigDecimal initUnits = new BigDecimal("2000.00000");
    BigDecimal initAddPrice = new BigDecimal("1.51225");
    BigDecimal newUnits = new BigDecimal("2000.00000");
    BigDecimal newAddPrice = new BigDecimal("1.51215");
    BigDecimal initExposure = new BigDecimal("2000.00000");
    BigDecimal newExposure = new BigDecimal("2000.00000");
    BigDecimal remPrice = new BigDecimal("1.51204");
    BigDecimal mul = new BigDecimal("1.0"); // Positive multiplier for LONG position

    BigDecimal newTotalCost =
        (initAddPrice.multiply(initUnits)).add((newAddPrice.multiply(newUnits)));
    BigDecimal expUnits = initUnits.add(newUnits);
    // avgPrice = Summation(UnitPrice * Units) / TotalUnits NOTE: Per Position
    BigDecimal expAvgPrice = newTotalCost.divide(expUnits);
    BigDecimal expExposure = initExposure.add(newExposure);
    // currentPrice - sell/ask price
    // avgPrice - buy/bid price
    // pips = (currentPrice - avgPrice) * multiplier
    // if LONG multiplier = 1 else SHORT multiplier = -1
    BigDecimal pips = remPrice.subtract(expAvgPrice).multiply(mul);
    // profitBase = pips * (exposure / currentPrice)
    BigDecimal expProfitBase =
        expExposure.divide(remPrice, BigDecimal.ROUND_HALF_DOWN).multiply(pips);
    // profitPerc = (profitBase / exposure) * 100
    BigDecimal expProfitPerc = expProfitBase.divide(expExposure, BigDecimal.ROUND_HALF_DOWN)
        .multiply(new BigDecimal("100"));
    // Round half down and convert to double
    double expAvgPriceD = expAvgPrice.setScale(5, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    double expProfitBaseD = expProfitBase.setScale(5, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    double expProfitPercD = expProfitPerc.setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();

    assertEquals(expUnits.doubleValue(), actUnits, 0);
    assertEquals(expExposure.doubleValue(), actExposure, 0);
    assertEquals(expAvgPriceD, actAvgPrice, 0);
    assertEquals(expProfitBaseD, actProfitBase, 0);
    assertEquals(expProfitPercD, actProfitPercentage, 0);
  }

  @Test
  public void remove_position_units_if_position_exists_with_diff_sides_and_less_units() {
    // WHEN
    // If currency pair has position
    // If new position has diff side with current position
    // If new position has less units with the current position
    // Then remove the new position units

    // set-up current position
    double averagePrice = 1.51215;
    double currentPrice = 1.51204;
    double exposure = MathUtils.roundHalfDown(tradeUnits * 2, 5); // 4000.0
    double units = MathUtils.roundHalfDown(exposure, 2);
    Position currentPosition = new Position(signalEvent.getSide(), signalEvent.getInstrument(),
        units, exposure, averagePrice, currentPrice);
    positions.put(currentPosition.getCurrencyPair(), currentPosition);

    // set-up the latest prices
    price.setInstrument(signalEvent.getInstrument());
    price.setBid(1.51214); // let's sell this because the selling price went up!

    // EXECUTE
    signalEvent.setSide(SELL);
    portfolio.executeSignal(signalEvent);
    Position position = positions.get(CURRENCY_PAIR);
    double actUnits = position.getUnits();
    double actExposure = position.getExposure();
    double actAvgPrice = position.getAveragePrice();
    double actCurrentPrice = position.getCurrentPrice();
    double actProfitBase = position.getProfitBase();
    double actProfitPerc = position.getProfitPercentage();
    double actBalance = portfolio.getBalance();

    // THEN
    BigDecimal initUnits = new BigDecimal("4000.00000");
    BigDecimal removeUnits = new BigDecimal("2000.00000");
    // Same as initial units as they were bought in base currency
    BigDecimal initExposure = new BigDecimal("4000.00000");
    BigDecimal initBalance = new BigDecimal("100000.00000");
    BigDecimal remPrice = new BigDecimal("1.51214");
    BigDecimal mul = new BigDecimal("1.0");
    BigDecimal expAvgPrice = new BigDecimal("1.51215");
    BigDecimal expTotalUnits = initUnits.subtract(removeUnits);
    BigDecimal expExposure = initExposure.subtract(removeUnits);
    BigDecimal pips = remPrice.subtract(expAvgPrice).multiply(mul);
    BigDecimal expProfitBase =
        expExposure.divide(remPrice, BigDecimal.ROUND_HALF_DOWN).multiply(pips);
    BigDecimal expProfitPerc = expProfitBase.divide(expExposure, BigDecimal.ROUND_HALF_DOWN)
        .multiply(new BigDecimal("100"));
    BigDecimal expBalance = initBalance.add(expProfitBase).setScale(5, BigDecimal.ROUND_HALF_DOWN);

    expExposure = expExposure.setScale(5, BigDecimal.ROUND_HALF_DOWN);
    expProfitBase = expProfitBase.setScale(5, BigDecimal.ROUND_HALF_DOWN);
    expProfitPerc = expProfitPerc.setScale(2, BigDecimal.ROUND_HALF_DOWN);

    assertEquals(expTotalUnits.doubleValue(), actUnits, 0);
    assertEquals(expExposure.doubleValue(), actExposure, 0);
    assertEquals(expAvgPrice.doubleValue(), actAvgPrice, 0);
    assertEquals(remPrice.doubleValue(), actCurrentPrice, 0);
    assertEquals(expProfitBase.doubleValue(), actProfitBase, 0);
    assertEquals(expProfitPerc.doubleValue(), actProfitPerc, 0);
    assertEquals(expBalance.doubleValue(), actBalance, 0);
  }

  @Test
  public void close_position_if_has_position_equal_units_but_diff_sides() {
    // currency pair has position
    // current position has diff side with the new position
    // current position has equal units with the new position

    // WHEN
    // current position
    double averagePrice = 1.51215;
    double currentPrice = 1.51204;
    double exp = MathUtils.roundHalfDownToPipDecimalPlaces(tradeUnits);
    Position currentPosition = new Position(signalEvent.getSide(), signalEvent.getInstrument(),
        tradeUnits, exp, averagePrice, currentPrice);
    positions.put(currentPosition.getCurrencyPair(), currentPosition);
    
    // latest price
    price.setInstrument(signalEvent.getInstrument());
    price.setBid(1.51214);
    
    // EXECUTE
    signalEvent.setSide(SELL);
    portfolio.executeSignal(signalEvent);
    OrderEvent expEvent = (OrderEvent) eventQueue.poll();
    assertNull(positions.get(CURRENCY_PAIR));
    double actBalance = (double) Whitebox.getInternalState(portfolio, "balance");

    // THEN
    BigDecimal removePrice = new BigDecimal("1.51214");
    BigDecimal avgPrice = new BigDecimal("1.51215");
    BigDecimal exposure = new BigDecimal("2000.00000");
    BigDecimal initBalance = new BigDecimal("100000.00000");
    BigDecimal pips = removePrice.subtract(avgPrice).multiply(new BigDecimal("1.0"));
    BigDecimal profitBase = exposure.divide(removePrice, BigDecimal.ROUND_HALF_DOWN).multiply(pips);
    BigDecimal expBalance = initBalance.add(profitBase).setScale(5, BigDecimal.ROUND_HALF_DOWN);

    assertEquals(expEvent.getSide(), SELL);
    assertEquals(expEvent.getUnits(), tradeUnits, 0);
    assertEquals(expEvent.getInstrument(), CURRENCY_PAIR);
    assertEquals(expEvent.getOrderType(), Constants.MARKET_ORDER);
    assertEquals(expBalance.doubleValue(), actBalance, 0);
  }
  
}
