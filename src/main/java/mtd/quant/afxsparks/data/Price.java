package mtd.quant.afxsparks.data;

import org.springframework.stereotype.Component;

@Component
public class Price {

  private String instrument;

  private double bid;

  private double ask;

  public Price() {}

  public Price(String instrument, double bid, double ask) {
    this.instrument = instrument;
    this.bid = bid;
    this.ask = ask;
  }

  public String getInstrument() {
    return instrument;
  }

  public void setInstrument(String instrument) {
    this.instrument = instrument;
  }

  /**
   * The price of the base currency that the 
   * market is willing to buy (Selling price of the base currency).
   */
  public double getBid() {
    return bid;
  }

  public void setBid(double bid) {
    this.bid = bid;
  }

  /**
   * The price of the base currency that the
   * market is willing to sell (Buying price of the base currency) 
   */
  public double getAsk() {
    return ask;
  }

  public void setAsk(double ask) {
    this.ask = ask;
  }


}
