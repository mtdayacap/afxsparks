package mtd.quant.afxsparks.data.poc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

public class StreamingFXPricesSpringWebPOC {

  @Test
  public void test() {
    String authToken = "d1960a56f85fa4c3ca3af54837cfc8cd-29ee0e4edd02c91d3047ea7397fdcc3d";
    String instruments = "GBP_USD";
    String accountId = "5737269";
    String url = "https://stream-fxpractice.oanda.com/v1/prices?accountId=" + accountId
        + "&instruments=" + instruments;

    RestTemplate client = new RestTemplate();
    RequestCallback callback = new RequestCallback() {

      @Override
      public void doWithRequest(ClientHttpRequest request) throws IOException {
        request.getHeaders().add("Authorization", "Bearer " + authToken);
      }
    };

    client
        .execute(url, HttpMethod.GET, callback, new ResponseExtractor<ResponseEntity<String>>() {

          @Override
          public ResponseEntity<String> extractData(ClientHttpResponse response)
              throws IOException {

            if (response.getStatusCode() == HttpStatus.OK) {

              InputStream is = response.getBody();
              BufferedReader reader = new BufferedReader(new InputStreamReader(is));

              String line = null;
              while ((line = reader.readLine()) != null) {
                System.out.println(line);
                Object obj = JSONValue.parse(line);
                JSONObject tick = (JSONObject) obj;

                if (tick.containsKey("tick")) {
                  tick = (JSONObject) tick.get("tick");
                }

                // ignore heartbeats
                if (tick.containsKey("instrument")) {
                  System.out.println("-------");

                  String instrument = tick.get("instrument").toString();
                  String time = tick.get("time").toString();
                  double bid = Double.parseDouble(tick.get("bid").toString());
                  double ask = Double.parseDouble(tick.get("ask").toString());

                  System.out.println(instrument);
                  System.out.println(time);
                  System.out.println(bid);
                  System.out.println(ask);
                }

              }

            } else {
              System.out.println(response.getStatusCode());
            }

            return new ResponseEntity<String>(response.getStatusCode());
          }
        }).getStatusCode();

  }

}
