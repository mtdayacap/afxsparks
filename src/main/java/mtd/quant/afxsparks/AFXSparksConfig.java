package mtd.quant.afxsparks;

import static mtd.quant.afxsparks.PropertiesConstants.BASE_CURRENCY;
import static mtd.quant.afxsparks.PropertiesConstants.ACCESS_TOKEN;
import static mtd.quant.afxsparks.PropertiesConstants.ACCOUNT_ID;
import static mtd.quant.afxsparks.PropertiesConstants.API_DOMAIN;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.execution.Execution;
import mtd.quant.afxsparks.portfolio.Portfolio;

@Configuration
@ComponentScan
public class AFXSparksConfig {

  @Autowired
  private ApplicationProperties appProperties;

  @Bean
  public List<String> pairs() {
    String pairsStr = appProperties.getProperty(PropertiesConstants.PAIRS);
    return Arrays.asList(pairsStr.split(PropertiesConstants.PAIRS_DELIMITER));
  }

  @Bean
  public Queue<Event> eventQueue() {
    return new ConcurrentLinkedQueue<Event>();
  }

  @Bean
  public Execution execution() {
    return new Execution(appProperties.getProperty(API_DOMAIN),
        appProperties.getProperty(ACCESS_TOKEN), appProperties.getProperty(ACCOUNT_ID));
  }

  @Bean
  public Portfolio portfolio() {
    String baseCurrency = appProperties.getProperty(BASE_CURRENCY);
    double leverage = 20;
    double equity = 100000.0;
    double riskPerTrade = 0.02;
    return new Portfolio(baseCurrency, leverage, equity, riskPerTrade);
  }
}
