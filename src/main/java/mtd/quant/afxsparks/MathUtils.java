package mtd.quant.afxsparks;

import java.math.BigDecimal;

public class MathUtils {
  
  public static double roundHalfDown(double value, int places){
    return new BigDecimal(value).setScale(places, BigDecimal.ROUND_HALF_DOWN).doubleValue();
  }

  /**
   * 
   * */
  public static double roundHalfDownToPipDecimalPlaces(double value) {
    return roundHalfDown(value, 5);
  }
}
