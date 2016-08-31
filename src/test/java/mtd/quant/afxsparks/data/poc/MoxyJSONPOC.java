package mtd.quant.afxsparks.data.poc;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.junit.Test;

import mtd.quant.afxsparks.event.TickEvent;

public class MoxyJSONPOC {

  @Test
  public void test() throws JAXBException {
    String tickJson =
        "{\"tick\":{\"instrument\":\"USD_JPY\",\"time\":\"2015-11-06T21:59:58.986750Z\",\"bid\":123.083,\"ask\":123.233}}";


    JAXBContext jc = JAXBContext.newInstance(TickEvent.class);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
    unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);
    StreamSource json = new StreamSource(new StringReader(tickJson));
    TickEvent tick = unmarshaller.unmarshal(json, TickEvent.class).getValue();
    
    System.out.println("instrument = " + tick.getInstrument());
    System.out.println("time = " + tick.getTime());
    System.out.println("bid = " + tick.getBid());
    System.out.println("ask = " + tick.getAsk());
  }

}
