package mtd.quant.afxsparks.trading;

import static mtd.quant.afxsparks.Constants.SIGNAL;
import static mtd.quant.afxsparks.Constants.ORDER;
import static mtd.quant.afxsparks.Constants.TICK;

import java.util.Queue;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.execution.Execution;
import mtd.quant.afxsparks.portfolio.Portfolio;
import mtd.quant.afxsparks.strategy.Strategy;

@Component
public class Trading implements Runnable {

  private Logger log = Logger.getLogger(Trading.class);

  @Autowired
  private Strategy testStrategy;

  @Autowired
  private Execution execution;

  @Autowired
  private Portfolio portfolio;
  
  @Resource
  private Queue<Event> eventQueue;

  public Trading(){}
  
  @Override
  public void run() {
    log.info("Let's start trading! Goodluck!!! :)");
    Event event = null;
    while (true) {
      event = eventQueue.poll();
      if (event == null) {
        continue;
      } 
      
      log.info(event);
      if (event.getType().equals(TICK)) {
        testStrategy.calculateSignals(event);
        
      } else if (event.getType().equals(ORDER)) {
        execution.executeOrder(event);
        
      } else if (event.getType().equals(SIGNAL)) {
        portfolio.executeSignal(event);
        
      }
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        log.error(e);
      }
    }
  }

}
