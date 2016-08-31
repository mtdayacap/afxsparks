package mtd.quant.afxsparks.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Resource;
import javax.xml.bind.Unmarshaller;

import org.springframework.stereotype.Component;

import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.event.TickEvent;

@Component
public class StreamingForexPricesImplStub extends AbstractStreamingForexPrices {

  @Resource
  private Queue<Event> eventQueue;

  public StreamingForexPricesImplStub(){
    super();
  }
  
  public StreamingForexPricesImplStub(String domain, String accesToken, String accountId,
      List<String> pairs, Unmarshaller unmarshaller, Map<String, Price> prices) {
    super(domain, accesToken, accountId, pairs, unmarshaller, prices);
  }
  
  @Override
  protected void extractTickData(BufferedReader reader) throws IOException {
    String line = null;
    while ((line = reader.readLine()) != null) {
      // Ignore heartbeats
      if (line.contains("instrument")) {
        TickEvent tickEvent = unmarshalTickEvent(line);
        eventQueue.add(tickEvent);
        updatePrices(tickEvent.getInstrument(), tickEvent.getBid(), tickEvent.getAsk());
        break;
      }
    }
  }

}
