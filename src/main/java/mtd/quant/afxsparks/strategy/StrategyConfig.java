package mtd.quant.afxsparks.strategy;

import static mtd.quant.afxsparks.Constants.MARKET_ORDER;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class StrategyConfig {
  
  @Resource
  private List<String> pairs;
  
  @Bean
  public Strategy testStrategy(){
    return new TestStrategy();
  }
  
  @Bean
  public Strategy testRandomStrategy(){
    String instrument = pairs.get(0);
    String orderType = MARKET_ORDER;
    double units = 5;
    return new TestRandomStrategy(instrument, units, orderType);
  }
}
