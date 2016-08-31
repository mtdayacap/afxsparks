package mtd.quant.afxsparks.portfolio;

import static mtd.quant.afxsparks.Constants.SHORT_POSITION;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import mtd.quant.afxsparks.Constants;
import mtd.quant.afxsparks.data.Price;
import mtd.quant.afxsparks.data.StreamingForexPrices;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest(value = {StreamingForexPrices.class, Position.class})
public class PositionTest {

  @Mock(name = "prices")
  private StreamingForexPrices pricesMock;

  @InjectMocks
  private Position position;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  public void testUpdatePositionPriceIfLong() {
    // WHEN
    String GBPUSD = "GBPUSD";
    Price price = new Price(GBPUSD, 1.56777, 1.56779);
    Mockito.when(pricesMock.getInstrumentPrice(GBPUSD)).thenReturn(price);
    Whitebox.setInternalState(position, "currencyPair", GBPUSD);
    Whitebox.setInternalState(position, "currentPrice", 1.56778);
    Whitebox.setInternalState(position, "positionType", Constants.LONG_POSITION);

    // EXECUTE
    position.updatePositionPrice();

    // THEN
    // Long position
    assertEquals(price.getBid(), position.getCurrentPrice(), 0);
    // TODO Checking of updated profit base
    // TODO Checking of updated profit percentage
  }

  public void testUpdatePositionPriceIfShort() {
    // WHEN
    String GBPUSD = "GBPUSD";
    Price price = new Price(GBPUSD, 1.56777, 1.56779);
    Mockito.when(pricesMock.getInstrumentPrice(GBPUSD)).thenReturn(price);
    Whitebox.setInternalState(position, "currencyPair", GBPUSD);
    Whitebox.setInternalState(position, "currentPrice", 1.56778);
    Whitebox.setInternalState(position, "positionType", Constants.SHORT_POSITION);

    // EXECUTE
    position.updatePositionPrice();

    // THEN
    // Long position
    assertEquals(price.getAsk(), position.getCurrentPrice(), 0);
    // TODO Checking of updated profit base
    // TODO Checking of updated profit percentage
  }

  @Test
  public void update_position_price() {
    // WHEN
    double currPrice = 1.50025;
    double avePrice =  1.50014;
    double exposure = 2000;
    Whitebox.setInternalState(position, "positionType", SHORT_POSITION);
    Whitebox.setInternalState(position, "averagePrice", avePrice);
    Whitebox.setInternalState(position, "exposure", exposure);

    // EXECUTE
    position.updatePositionPrice(currPrice);

    // THEN
    double expProfitBase = -0.14664;
    double expProfitPerc = -0.01;
    
    double actProfitBase = (Double)Whitebox.getInternalState(position, "profitBase");
    double actProfitPerc = (Double)Whitebox.getInternalState(position, "profitPercentage");
    
    assertEquals(expProfitBase, actProfitBase, 0);
    assertEquals(expProfitPerc, actProfitPerc, 0);
  }
}
