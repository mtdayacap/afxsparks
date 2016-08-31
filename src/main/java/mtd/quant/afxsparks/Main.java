package mtd.quant.afxsparks;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import mtd.quant.afxsparks.data.DataConfig;
import mtd.quant.afxsparks.data.StreamForexPricesProcess;
import mtd.quant.afxsparks.strategy.StrategyConfig;
import mtd.quant.afxsparks.trading.Trading;

public class Main {
  public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
        AFXSparksConfig.class, DataConfig.class, StrategyConfig.class);
    
    Trading fxTrading = ctx.getBean(Trading.class);
    StreamForexPricesProcess fxPricesStreaming = ctx.getBean(StreamForexPricesProcess.class);
    
    Thread fxTradingThread = new Thread(fxTrading, "fxTrading");
    Thread fxPricesStreamingThread = new Thread(fxPricesStreaming, "fxPricesStreaming");
    
    fxTradingThread.start();
    fxPricesStreamingThread.start();
    
    ctx.close();
  }
}
