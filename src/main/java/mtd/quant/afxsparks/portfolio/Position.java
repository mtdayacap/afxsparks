package mtd.quant.afxsparks.portfolio;

import static mtd.quant.afxsparks.portfolio.PositionTypeEnum.LONG;
import static mtd.quant.afxsparks.portfolio.PositionTypeEnum.SHORT;

import static mtd.quant.afxsparks.Constants.LONG_POSITION;
import static mtd.quant.afxsparks.MathUtils.roundHalfDown;
import static mtd.quant.afxsparks.MathUtils.roundHalfDownToPipDecimalPlaces;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mtd.quant.afxsparks.FXUtils;
import mtd.quant.afxsparks.data.Price;
import mtd.quant.afxsparks.data.StreamingForexPrices;

/**
 * Implementation of the Position as of QSForex Day 2
 * 
 * @author mike
 *
 */
@Component
public class Position {

  @Autowired
  private StreamingForexPrices prices;
  
  // Open position price
  private double averagePrice;

  // Closing position price
  private double currentPrice;

  private String baseCurrency;

  // market
  private String currencyPair;


  private double exposure;

  private String homeCurrency;

  private String market;

  private PositionTypeEnum position;

  @Deprecated
  // side
  private String positionType;


  private double profitBase;

  private double profitPercentage;

  private String quoteCurrency;

  private String quoteHomeCurrencyPair;

  private double units;

  public Position() {}

  public Position(PositionTypeEnum positionType, String market, double units, double exposure,
      double bid, double ask) {
    this.position = positionType;
    this.market = market;
    this.units = units;
    this.exposure = exposure;

    // Long or short
    // Long  - Buy quote currency from the market - ask price
    // Short - Sell quote currency from the market - bid price
    // bid - The price the trader is willing to buy from the market
    // ask - The price the trader is willing to sell from the market
    if (this.position == LONG) {
      this.averagePrice = ask;  
      this.currentPrice = bid;

    } else {
      this.averagePrice = bid; 
      this.currentPrice = ask;

    }
    
    this.profitBase = calculateProfitBase(this.exposure);
    this.profitPercentage = calculateProfitPercentage(this.exposure);
  }

  @Deprecated
  public Position(String positionType, String market, double units, double exposure, double bid,
      double ask) {
    this.positionType = positionType;
    this.currencyPair = market;
    this.units = units;
    this.exposure = exposure;

    // Long or short

    this.averagePrice = bid;
    this.currentPrice = ask;

    calculateProfitBase();
    calculateProfitPercentage();
  }

  public double calculatePips() {
    double mult = 0;
    if (this.position == LONG) {
      mult = 1.0;

    } else if (this.position == SHORT) {
      mult = -1.0;

    }

    return roundHalfDownToPipDecimalPlaces(mult * (currentPrice - averagePrice));
  }

  public double getAveragePrice() {
    return averagePrice;
  }

  public String getBaseCurrency() {
    return baseCurrency;
  }

  public String getCurrencyPair() {
    return this.currencyPair;
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public double getExposure() {
    return exposure;
  }

  public String getPositionType() {
    return this.positionType;
  }

  
  
  public double getProfitBase() {
    return profitBase;
  }

  public double getProfitPercentage() {
    return profitPercentage;
  }

  public String getQuoteCurrency() {
    return quoteCurrency;
  }

  public String getQuoteHomeCurrencyPair() {
    return this.quoteCurrency;
  }

  public double getUnits() {
    return units;
  }

  public void setAveragePrice(double averagePrice) {
    this.averagePrice = averagePrice;
  }

  public void setExposure(double exposure) {
    this.exposure = exposure;
  }

  public void setUnits(double units) {
    this.units = units;
  }

  @Deprecated
  public void updatePositionPrice() {
    Price price = prices.getInstrumentPrice(currencyPair);
    if (positionType.equals(LONG_POSITION)) {
      this.currentPrice = price.getBid();
    } else {
      this.currentPrice = price.getAsk();
    }
    
    calculateProfitBase();
    calculateProfitPercentage();
  }

  @Deprecated
  public void updatePositionPrice(double currentPrice) {
    this.currentPrice = currentPrice;
    this.profitBase = calculateProfitBase();
    this.profitPercentage = calculateProfitPercentage();
  }

  public void updatePositionPrice(double bid, double ask, double exposure) {
    if (this.position == LONG) {
      this.currentPrice = bid;
      
    } else if (this.position == SHORT) {
      this.currentPrice = ask;
      
    }
    
    this.profitBase = calculateProfitBase(exposure);
    this.profitPercentage = calculateProfitPercentage(exposure);
  }

  @Deprecated
  private double calculateProfitBase() {
    double pips = calculatePips();
    double units = (exposure / currentPrice);
    double profitBase = pips * units;
    return roundHalfDownToPipDecimalPlaces(profitBase);
  }

  private double calculateProfitBase(double exposure) {
     double pips = calculatePips();
     double units = (exposure / this.currentPrice);
     double profitBase = pips * units;
     return roundHalfDownToPipDecimalPlaces(profitBase);
  }

  @Deprecated
  private double calculateProfitPercentage() {
    double profitPerc = (profitBase / exposure) * 100;
    return roundHalfDown(profitPerc, 2);
  }

  private double calculateProfitPercentage(double exposure) {
    double profitPerc = (this.profitBase / exposure) * 100;
    return roundHalfDown(profitPerc, 2);
  }

  private void setUpCurrencies() {
    this.baseCurrency = FXUtils.extractBaseCurrency(currencyPair);
    this.quoteCurrency = FXUtils.extractQuoteCurrency(currencyPair);
    this.quoteHomeCurrencyPair = quoteCurrency + homeCurrency;

    Price price = prices.getInstrumentPrice(currencyPair);
    if (positionType.equals(LONG_POSITION)) {
      this.averagePrice = price.getAsk();
      this.currentPrice = price.getBid();
    } else {
      this.averagePrice = price.getBid();
      this.currentPrice = price.getAsk();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    } else if (obj == this) {
      return true;
    }

    Position position = (Position) obj;
    return new EqualsBuilder().append(position.getPositionType(), getPositionType())
        .append(position.getCurrencyPair(), getCurrencyPair())
        .append(position.getUnits(), getUnits()).append(position.getExposure(), getExposure())
        .append(position.getAveragePrice(), getAveragePrice())
        .append(position.getCurrentPrice(), getCurrentPrice())
        .append(position.getExposure(), getExposure())
        .append(position.getProfitBase(), getProfitBase())
        .append(position.getProfitPercentage(), getProfitPercentage()).isEquals();
  }
}
