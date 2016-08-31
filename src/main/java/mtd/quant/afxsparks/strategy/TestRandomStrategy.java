package mtd.quant.afxsparks.strategy;

import static mtd.quant.afxsparks.Constants.BUY;
import static mtd.quant.afxsparks.Constants.SELL;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.event.OrderEvent;

@Component
public class TestRandomStrategy implements Strategy {

  @Resource
  private Queue<Event> eventQueue;
  
  private String instrument;
  
  private int tick;

  private double units;

  private String orderType;
  
  public TestRandomStrategy(String instrument, double units, String orderType) {
    this.instrument = instrument;
    this.units = units;
    this.orderType = orderType;
    this.tick = 0;
  }

  @Override
  public void calculateSignals(Event event) {
    OrderEvent orderEvent = null;
    List<String> randomBuySellList = Arrays.asList(BUY, SELL);
    Random random = new Random();
    if (this.tick % 5 == 0) {
      String randomSide = randomBuySellList.get(random.nextInt(randomBuySellList.size()));
      orderEvent = new OrderEvent(randomSide, this.units, this.instrument, this.orderType);
      eventQueue.add(orderEvent);
    }
    this.tick++;
  }
}
