package mtd.quant.afxsparks.data;

import static mtd.quant.afxsparks.Constants.TICK;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Queue;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mtd.quant.afxsparks.AFXSparksConfig;
import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.event.TickEvent;

@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DataConfigTest.class, AFXSparksConfig.class})
public class StreamingForexPricesIT {

  @Autowired
  private StreamingForexPrices prices;

  @Resource
  private Queue<Event> eventQueue;

  @Test
  public void testStreamForexPrice() {
    Date startTime = new Date(System.currentTimeMillis());

    prices.streamToQueue();
    TickEvent event = (TickEvent) eventQueue.poll();
    Date tickTime = event.getTime();

    // TickEvent chekcing
    assertEquals(TICK, event.getType());
    assertNotNull(event.getInstrument());

    // Different time zones. HKT leading.
    assertTrue((startTime.compareTo(tickTime) >= 0));

    assertNotEquals(0, event.getAsk(), 0);
    assertNotNull(event.getAsk());

    assertNotEquals(0, event.getBid(), 0);
    assertNotNull(event.getBid());

    // Prices checking
    Price price = prices.getInstrumentPrice(event.getInstrument().replace("_", ""));

    // TickEvent instrument format has "_" separator between the base and quote currency
    String instrument = event.getInstrument().replace("_", "");
    assertEquals(instrument, price.getInstrument());
    assertEquals(event.getBid(), price.getBid(), 0);
    assertEquals(event.getAsk(), price.getAsk(), 0);
    // Inverted Price
    // Invert instrument name i.e. GBPUSD --> USDGBP
    String invInstrumentName = instrument.substring(3) + instrument.substring(0, 3);
    // Get reciprocal of bid and ask prices.
    // Round half down to 4 decimal places.
    double invBid = new BigDecimal((1.00000 / event.getBid()))
        .setScale(5, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    double invAsk = new BigDecimal((1.00000 / event.getAsk()))
        .setScale(5, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    Price invPrice = prices.getInstrumentPrice(invInstrumentName);

    assertEquals(invInstrumentName, invPrice.getInstrument());
    assertEquals(invBid, invPrice.getBid(), 0);
    assertEquals(invAsk, invPrice.getAsk(), 0);
  }

}
