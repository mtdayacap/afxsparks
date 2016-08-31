package mtd.quant.afxsparks.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import mtd.quant.afxsparks.ApplicationProperties;
import mtd.quant.afxsparks.PropertiesConstants;
import mtd.quant.afxsparks.event.TickEvent;

@Configuration
@ComponentScan
public class DataConfig {

  private static final Logger log = Logger.getLogger(DataConfig.class);

  @Autowired
  private ApplicationProperties applicationProperties;
  
  @Resource
  private List<String> pairs;

  private String streamDomain;

  private String accountId;

  private String accessToken;


  @PostConstruct
  public void init() {
    this.streamDomain = applicationProperties.getProperty(PropertiesConstants.STREAM_DOMAIN);
    this.accountId = applicationProperties.getProperty(PropertiesConstants.ACCOUNT_ID);
    this.accessToken = applicationProperties.getProperty(PropertiesConstants.ACCESS_TOKEN);
  }
  
  @Bean
  public StreamingForexPrices prices() {

    StreamingForexPrices prices = null;
    JAXBContext jaxbCtx = null;
    Unmarshaller unmarshaller = null;
    try {

      jaxbCtx = JAXBContext.newInstance(TickEvent.class);
      unmarshaller = jaxbCtx.createUnmarshaller();
      unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
      unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);

      Map<String, Price> priceLookup = new HashMap<String, Price>();
      prices = new StreamingForexPricesImpl(streamDomain, accessToken, accountId, pairs, 
          unmarshaller, priceLookup);
    } catch (JAXBException e) {
      log.error("Failed setting-up TickEvent JSON unmarshaller", e);
    }

    return prices;
  }

}
