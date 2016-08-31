package mtd.quant.afxsparks.data;

import java.util.Map;

import mtd.quant.afxsparks.MathUtils;

public class PriceHandlerImpl implements PriceHandler {

  private Map<String, Price> prices;

  public PriceHandlerImpl(Map<String, Price> prices) {
    this.prices = prices;
  }

  public PriceHandlerImpl() {}

  public Price getInstrumentPrice(String instrument) {
    return prices.get(instrument);
  }

  protected void updatePrices(String instrument, double bid, double ask) {
    instrument = instrument.replace("_", "");
    Price price = new Price(instrument, bid, ask);
    Price invPrice = invertPrices(instrument, bid, ask);
    addPrice(price);
    addPrice(invPrice);
  }

  protected void addPrice(Price price) {
    prices.put(price.getInstrument(), price);
  }

  protected Price invertPrices(String instrument, double bid, double ask) {
    String invInstrument = instrument.substring(3) + instrument.subSequence(0, 3);
    double invBid = MathUtils.roundHalfDown((1.00000 / bid), 5);
    double invAsk = MathUtils.roundHalfDown((1.00000 / ask), 5);
    return new Price(invInstrument, invBid, invAsk);
  }

}
