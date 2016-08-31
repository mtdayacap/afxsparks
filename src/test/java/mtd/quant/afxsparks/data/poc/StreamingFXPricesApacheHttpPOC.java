package mtd.quant.afxsparks.data.poc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;

public class StreamingFXPricesApacheHttpPOC {

  @SuppressWarnings("deprecation")
  @Test
  public void test() throws ParseException, IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();

    try {

      // Set these variables to whatever personal ones are preferred
      String domain = "https://stream-fxpractice.oanda.com";
      String access_token = "d1960a56f85fa4c3ca3af54837cfc8cd-29ee0e4edd02c91d3047ea7397fdcc3d";
      String account_id = "5737269";
      String instruments = "EUR_USD,USD_JPY,EUR_JPY";

      HttpUriRequest httpGet = new HttpGet(
          domain + "/v1/prices?accountId=" + account_id + "&instruments=" + instruments);
      httpGet.setHeader(new BasicHeader("Authorization", "Bearer " + access_token));

      System.out.println("Executing request: " + httpGet.getRequestLine());

      HttpResponse resp = httpClient.execute(httpGet);
      HttpEntity entity = (HttpEntity) resp.getEntity();

      if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
        InputStream stream = ((HttpEntity) entity).getContent();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));

        while ((line = br.readLine()) != null) {

          Object obj = JSONValue.parse(line);
          JSONObject tick = (JSONObject) obj;

          // unwrap if necessary
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
        // print error message
        String responseString = EntityUtils.toString((org.apache.http.HttpEntity) entity, "UTF-8");
        System.out.println(responseString);
      }

    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

}
