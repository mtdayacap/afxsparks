package mtd.quant.afxsparks.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamForexPricesProcess implements Runnable {

  @Autowired
  private StreamingForexPrices prices;
  
  @Override
  public void run() {
    prices.streamToQueue();
  }

}
