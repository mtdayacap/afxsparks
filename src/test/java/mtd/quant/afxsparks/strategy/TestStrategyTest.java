package mtd.quant.afxsparks.strategy;

import static mtd.quant.afxsparks.Constants.BUY;
import static mtd.quant.afxsparks.Constants.MARKET_ORDER;
import static mtd.quant.afxsparks.Constants.SELL;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mtd.quant.afxsparks.AFXSparksConfig;
import mtd.quant.afxsparks.data.DataConfig;
import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.event.SignalEvent;
import mtd.quant.afxsparks.event.TickEvent;

@RunWith(PowerMockRunner.class)
public class TestStrategyTest {

  private static final String CURRENCY_PAIR = "GBPUSD";

  private Strategy testStrategy;

  private Queue<Event> eventQueue;

  private List<String> pairs;

  private Date now;

  private TickEvent event;

  @Before
  public void setUp() {
    eventQueue = new ConcurrentLinkedQueue<>();
    
    pairs = new ArrayList<>();
    pairs.add(CURRENCY_PAIR);
    
    testStrategy = new TestStrategy();
    Whitebox.setInternalState(testStrategy, "pairs", pairs);
    Whitebox.setInternalState(testStrategy, "eventQueue", eventQueue);
    
    now = new Date();
    event = new TickEvent(); // TODO Please check this in the future if you need to inject this.
    event.setTime(now);

  }

  @Test
  public void testBuySignal() {
    // WHEN
    int prevTick = 5;
    Whitebox.setInternalState(testStrategy, "tick", prevTick);
    Whitebox.setInternalState(testStrategy, "invested", false);

    // Execute
    testStrategy.calculateSignals(event);
    SignalEvent signalEvent = (SignalEvent) eventQueue.poll();

    // THEN
    assertEquals(pairs.get(0), signalEvent.getInstrument());
    assertEquals(BUY, signalEvent.getSide());
    assertEquals(MARKET_ORDER, signalEvent.getOrderType());
    assertEquals(now, signalEvent.getTime());
    Integer tick = (Integer) Whitebox.getInternalState(testStrategy, "tick");
    assertEquals(prevTick + 1, tick.intValue());
  }

  @Test
  public void testSellSignal() {
    // WHEN
    int prevTick = 5;
    Whitebox.setInternalState(testStrategy, "tick", prevTick);
    Whitebox.setInternalState(testStrategy, "invested", true);

    // EXEC
    testStrategy.calculateSignals(event);
    SignalEvent signalEvent = (SignalEvent) eventQueue.poll();

    // THEN
    assertEquals(pairs.get(0), signalEvent.getInstrument());
    assertEquals(SELL, signalEvent.getSide());
    assertEquals(MARKET_ORDER, signalEvent.getOrderType());
    assertEquals(now, signalEvent.getTime());
    Integer tick = (Integer) Whitebox.getInternalState(testStrategy, "tick");
    assertEquals(prevTick + 1, tick.intValue());

  }

  @Test
  public void testMaintainPosition() {
    // WHEN
    int prevTick = 4;
    Whitebox.setInternalState(testStrategy, "tick", prevTick);
    Whitebox.setInternalState(testStrategy, "invested", true);

    // EXEC
    testStrategy.calculateSignals(event);
    SignalEvent signalEvent = (SignalEvent) eventQueue.poll();

    // THEN
    assertTrue((signalEvent == null));
    Integer tick = (Integer) Whitebox.getInternalState(testStrategy, "tick");
    assertEquals(prevTick + 1, tick.intValue());
  }
}
