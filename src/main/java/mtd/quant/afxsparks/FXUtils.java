package mtd.quant.afxsparks;

public class FXUtils {

  public static String extractBaseCurrency(String currencyPair) {
    return currencyPair.substring(0, 3);
  }

  public static String extractQuoteCurrency(String currencyPair){
    return currencyPair.substring(3);
  }
  
  /**
   * Convert instrument symbol from [Base][Quote] to [Base]_[Quote].
   * E.g. GBPUSD to GBP_USD
   * 
   * */
  public static String separateBaseQuoteSymbolWithUnderscore(String pair){
    return pair.substring(0, 3) + "_" + pair.substring(3);
  }
}
