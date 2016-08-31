package mtd.quant.afxsparks.portfolio;

import static mtd.quant.afxsparks.MathUtils.roundHalfDown;
import static mtd.quant.afxsparks.MathUtils.roundHalfDownToPipDecimalPlaces;
import static mtd.quant.afxsparks.Constants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mtd.quant.afxsparks.Constants;
import mtd.quant.afxsparks.data.StreamingForexPrices;
import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.event.OrderEvent;
import mtd.quant.afxsparks.event.SignalEvent;
import mtd.quant.afxsparks.event.TickEvent;

@Component
public class Portfolio {

  private static Logger log = Logger.getLogger(Portfolio.class);
  
  private double balance;

  private String baseCurrency;

  private double equity;

  @Resource
  private Queue<Event> eventQueue;

  private double leverage;

  private Map<String, Position> positions;

  @Autowired
  private StreamingForexPrices prices;

  private double riskPerTrade;

  private double tradeUnits;

  public Portfolio() {}

  public Portfolio(double equity) {
    this.equity = equity;
  }

  public Portfolio(String baseCurrency, double leverage, double equity, double riskPerTrade) {
    this.baseCurrency = baseCurrency;
    this.leverage = leverage;
    this.equity = equity;
    this.riskPerTrade = riskPerTrade;
    this.balance = equity;
    this.tradeUnits = calcRiskPositionSize();
    this.positions = new HashMap<>();
  }

  public void executeSignal(Event event) {
    SignalEvent signalEvent = (SignalEvent) event;
    double exposure = this.tradeUnits;
    double averagePrice = this.prices.getInstrumentPrice(signalEvent.getInstrument()).getAsk();
    double currentPrice = this.prices.getInstrumentPrice(signalEvent.getInstrument()).getBid();
    Position position = new Position(signalEvent.getSide(), signalEvent.getInstrument(),
        this.tradeUnits, exposure, averagePrice, currentPrice);

    // if there is no position then create one
    if (noCurrencyPairPositionExists(position.getCurrencyPair())) {
      addNewPosition(position);
      OrderEvent orderEvent =
          new OrderEvent(BUY, position.getUnits(), position.getCurrencyPair(), "market");
      eventQueue.add(orderEvent);
    }
    // if a position exists add or remove position
    else {
      Position currentPosition = positions.get(position.getCurrencyPair());
      // check if sides are equal
      if (position.getPositionType().equals(currentPosition.getPositionType())) {
        addPositionUnits(position);
      }
      // Different sides
      // Check if the units close the position
      else {
        // if units are equal then close out the position
        if (position.getUnits() == currentPosition.getUnits()) {
          closePosition(position.getCurrencyPair(), position.getCurrentPrice());
          OrderEvent orderEvent =
              new OrderEvent(SELL, position.getUnits(), position.getCurrencyPair(), "market");
          eventQueue.add(orderEvent);
        }
        // if units are less than the current units then remove position
        else if (position.getUnits() < currentPosition.getUnits()) {
          removePositionUnits(position.getCurrencyPair(), position.getUnits(),
              position.getCurrentPrice());
        }
        // if units > current units
        // close out the current position
        // and create a new opposing position for the remaining units
        else {
          // Close out the position first
          double remainingUnits = position.getUnits() - currentPosition.getUnits();
          closePosition(position.getCurrencyPair(), position.getCurrentPrice());

          // Create the new opposing Position with the remaining units
          String newSide = null;
          if (position.getPositionType().equals(BUY)) {
            newSide = BUY;
          } else {
            newSide = SELL;
          }
          Position newOpposingPosition =
              new Position(newSide, position.getCurrencyPair(), remainingUnits,
                  position.getExposure(), position.getAveragePrice(), position.getCurrentPrice());
          addNewPosition(newOpposingPosition);
        }
      }
    }

    log.info("Balance: " + this.balance);
  }

  public double getBalance() {
    return balance;
  }

  public void updatePortfolio(TickEvent event) {
    String pair = event.getInstrument();
    if (positions.containsKey(pair)) {
      Position position = positions.get(pair);
      position.updatePositionPrice();
    }
  }

  private void addNewPosition(Position newPosition) {
    positions.put(newPosition.getCurrencyPair(), newPosition);
  }

  private void addPositionUnits(Position position) {
    if (noCurrencyPairPositionExists(position.getCurrencyPair())) {
      return;
    }

    Position currentPosition = positions.get(position.getCurrencyPair());
    double newTotalUnits = currentPosition.getUnits() + position.getUnits();
    double newTotalCost = (currentPosition.getUnits() * currentPosition.getAveragePrice())
        + (position.getAveragePrice() * position.getUnits());
    double newAvgCost = newTotalCost / newTotalUnits;
    currentPosition.setExposure(
        roundHalfDownToPipDecimalPlaces(position.getExposure() + currentPosition.getExposure()));
    currentPosition.setAveragePrice(roundHalfDownToPipDecimalPlaces(newAvgCost));
    currentPosition.setUnits(roundHalfDown(newTotalUnits, 2));
    currentPosition.updatePositionPrice(position.getCurrentPrice());
  }

  private double calcRiskPositionSize() {
    double riskPositionSize = equity * riskPerTrade;
    return roundHalfDown(riskPositionSize, 2);
  }

  private void closePosition(String currencyPair, double removePrice) {
    if (noCurrencyPairPositionExists(currencyPair)) {
      return;
    }

    Position position = positions.remove(currencyPair);
    position.updatePositionPrice(removePrice);
    double profitNLosses = (position.calculatePips() * position.getExposure()) / removePrice;
    this.balance = roundHalfDownToPipDecimalPlaces(this.balance + profitNLosses);
  }

  private boolean noCurrencyPairPositionExists(String currencyPair) {
    return !positions.containsKey(currencyPair);
  }

  private void removePositionUnits(String currencyPair, double units, double removePrice) {
    if (noCurrencyPairPositionExists(currencyPair)) {
      return;
    }

    Position position = positions.get(currencyPair);
    // subtract units
    double totalUnits = position.getUnits() - units;
    position.setUnits(roundHalfDown(totalUnits, 2));
    // Units used here is in Base currency.
    // Currency used to buy currency is base currency
    // Hence, units was directly subtracted from exposure --> exposure - units
    position.setExposure(roundHalfDownToPipDecimalPlaces(position.getExposure() - units));
    position.updatePositionPrice(removePrice);
    double profitNLoss = position.calculatePips() * (units / removePrice);
    this.balance = roundHalfDownToPipDecimalPlaces(this.balance + profitNLoss);
  }



}
