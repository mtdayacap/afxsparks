package mtd.quant.afxsparks.data.poc;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class OrderExecutionPOC {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void test() {
    String authToken = "d1960a56f85fa4c3ca3af54837cfc8cd-29ee0e4edd02c91d3047ea7397fdcc3d";
    String instruments = "GBP_USD";
    String accountId = "5737269";
    String url = "https://api-fxpractice.oanda.com/v1/accounts/" + accountId + "/orders";

    RestTemplate client = new RestTemplate();
    client.getMessageConverters().add(new FormHttpMessageConverter());

    MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
    params.add("instrument", instruments);
    params.add("units", "5");
    params.add("type", "market");
    params.add("side", "buy");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // application/x-www-form-urlencoded
    headers.add("Authorization", "Bearer " + authToken);

    HttpEntity<MultiValueMap<String, String>> entity =
        new HttpEntity<MultiValueMap<String, String>>(params, headers);

    ResponseEntity<String> response = client.exchange(url, HttpMethod.POST, entity, String.class);

    System.out.println(response.getBody());
  }
}
