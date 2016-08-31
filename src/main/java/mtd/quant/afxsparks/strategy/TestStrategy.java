package mtd.quant.afxsparks.strategy;

import static mtd.quant.afxsparks.Constants.MARKET_ORDER;
import static mtd.quant.afxsparks.Constants.BUY;
import static mtd.quant.afxsparks.Constants.SELL;

import java.util.List;
import java.util.Queue;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.event.SignalEvent;
import mtd.quant.afxsparks.event.TickEvent;

@Component
public class TestStrategy implements Strategy {

  @Resource
  private List<String> pairs;

  @Resource
  private Queue<Event> eventQueue;

  private boolean invested;

  private int tick;

  public TestStrategy() {
    this.tick = 0;
    this.invested = false;
  }


  public void calculateSignals(Event event) {
    TickEvent tickEvent = (TickEvent) event;
    if ((tick % 5) == 0) {
      SignalEvent signalEvent = null;
      if (!isInvested()) {
        // Buy
        signalEvent = new SignalEvent(pairs.get(0), MARKET_ORDER, BUY, tickEvent.getTime());
        invested = true;

      } else {
        // Sell
        signalEvent = new SignalEvent(pairs.get(0), MARKET_ORDER, SELL, tickEvent.getTime());
        this.invested = false;

      }
      eventQueue.add(signalEvent);

    }
    tickIncrement();
  }

  private boolean isInvested() {
    return invested;
  }

  private void tickIncrement() {
    tick++;
  }

}
