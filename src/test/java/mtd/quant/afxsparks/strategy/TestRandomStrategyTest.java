package mtd.quant.afxsparks.strategy;

import static mtd.quant.afxsparks.Constants.MARKET_ORDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Queue;

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
import org.springframework.util.StringUtils;

import mtd.quant.afxsparks.AFXSparksConfig;
import mtd.quant.afxsparks.data.DataConfig;
import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.event.OrderEvent;
import mtd.quant.afxsparks.event.TickEvent;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AFXSparksConfig.class, StrategyConfig.class, DataConfig.class})
public class TestRandomStrategyTest {

  @Autowired
  private Strategy testRandomStrategy;

  @Resource
  private Queue<Event> eventQueue;

  @Resource
  private List<String> pairs;

  private Date now;

  private TickEvent event;

  @Before
  public void setUp() throws Exception {
    now = new Date();
    event = new TickEvent();
    event.setTime(now);
  }

  @Test
  public void buy_or_sell_every_fifth_tick() {
    // WHEN
    int prevTick = 5;
    Whitebox.setInternalState(testRandomStrategy, "tick", prevTick);

    // EXECUTE
    testRandomStrategy.calculateSignals(event);
    OrderEvent orderEvent = (OrderEvent) eventQueue.poll();

    // THEN
    assertTrue(!StringUtils.isEmpty(orderEvent.getSide())); // Buy or Sell
    assertEquals(pairs.get(0), orderEvent.getInstrument()); // First Pair
    assertEquals(5.0, orderEvent.getUnits(), 0); // 5 units
    assertEquals(MARKET_ORDER, orderEvent.getOrderType()); // Constant to MARKET order type
    int currentTick = (Integer) Whitebox.getInternalState(testRandomStrategy, "tick");
    assertEquals(prevTick + 1, currentTick);
  }

  @Test
  public void no_order_if_tick_not_divisible_by_five() {
    // WHEN
    int prevTick = 6;
    Whitebox.setInternalState(testRandomStrategy, "tick", prevTick);

    // EXECUTE
    testRandomStrategy.calculateSignals(event);
    OrderEvent orderEvent = (OrderEvent) eventQueue.poll();

    // THEN
    assertNull(orderEvent);
    int currentTick = (Integer) Whitebox.getInternalState(testRandomStrategy, "tick");
    assertEquals(prevTick + 1, currentTick);
  }
}
