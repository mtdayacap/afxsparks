package mtd.quant.afxsparks.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import mtd.quant.afxsparks.FXUtils;
import mtd.quant.afxsparks.event.TickEvent;

public abstract class AbstractStreamingForexPrices extends PriceHandlerImpl
    implements StreamingForexPrices {

  private static final Logger log = Logger.getLogger(AbstractStreamingForexPrices.class);

  private String accessToken;

  private String accountId;

  private RestTemplate client;

  private String domain;

  private List<String> pairs;

  // JSON
  private Unmarshaller unmarshaller;

  public AbstractStreamingForexPrices(String domain, String accesToken, String accountId,
      List<String> pairs, Unmarshaller unmarshaller, Map<String, Price> prices) {
    super(prices);

    this.domain = domain;
    this.accessToken = accesToken;
    this.accountId = accountId;
    this.pairs = pairs;
    this.unmarshaller = unmarshaller;

    // Init Rest client
    this.client = new RestTemplate();
  }

  public AbstractStreamingForexPrices() {
    super();
  }

  public void streamToQueue() {
    String instruments = formInstruments(pairs);
    String url =
        "https://" + domain + "/v1/prices?accountId=" + accountId + "&instruments=" + instruments;
    RequestCallback callback = new RequestCallback() {

      @Override
      public void doWithRequest(ClientHttpRequest request) throws IOException {

        request.getHeaders().add("Authorization", "Bearer " + accessToken);
      }
    };

    client.execute(url, HttpMethod.GET, callback, new TickResponseExtractor());
  }

  private String formInstruments(List<String> pairs) {

    StringBuilder sb = new StringBuilder();
    for (String pair : pairs) {
      String intrument = FXUtils.separateBaseQuoteSymbolWithUnderscore(pair);
      sb.append(intrument + ",");
    }
    String instruments = sb.toString();
    return instruments.substring(0, instruments.length() - 1); // Remove comma at the end
  }

  protected abstract void extractTickData(BufferedReader responseReader) throws IOException;

  protected TickEvent unmarshalTickEvent(String jsonStr) {
    StreamSource jsonSource = new StreamSource(new StringReader(jsonStr));
    try {
      return unmarshaller.unmarshal(jsonSource, TickEvent.class).getValue();
    } catch (JAXBException e) {
      log.error("Failed unmarshalling tick reponse.", e);
    }

    return null;
  }

  private class TickResponseExtractor implements ResponseExtractor<ResponseEntity<String>> {

    @Override
    public ResponseEntity<String> extractData(ClientHttpResponse response) throws IOException {
      if (response.getStatusCode() != HttpStatus.OK) {
        return new ResponseEntity<String>(response.getStatusCode());
      }

      InputStream is = response.getBody();
      BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));

      extractTickData(responseReader);

      return new ResponseEntity<String>(response.getStatusCode());
    }


  }
}
