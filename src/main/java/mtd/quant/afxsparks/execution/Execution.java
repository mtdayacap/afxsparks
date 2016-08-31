package mtd.quant.afxsparks.execution;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import mtd.quant.afxsparks.FXUtils;
import mtd.quant.afxsparks.event.Event;
import mtd.quant.afxsparks.event.OrderEvent;

@Component
public class Execution {

  private static Logger log = Logger.getLogger(Execution.class);
  
  private String apiDomain;
  
  private String accessToken;
  
  private String accountId;
  
  private RestTemplate client;
  
  public Execution(String apiDomain, String accessToken, String accountId){
    this.apiDomain = apiDomain;
    this.accessToken = accessToken;
    this.accountId = accountId;
    
    this.client = new RestTemplate();
    this.client.getMessageConverters().add(new FormHttpMessageConverter());
  }
  
  public boolean executeOrder(Event event){
    OrderEvent orderEvent = (OrderEvent) event;
    StringBuilder url = new StringBuilder();
    url.append("https://").append(apiDomain).append("/v1/accounts/").append(accountId).append("/orders");
    
    // Build form
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("instrument", FXUtils.separateBaseQuoteSymbolWithUnderscore(orderEvent.getInstrument()));
    params.add("units", orderEvent.getUnitsAsStringAndInteger());
    params.add("type", orderEvent.getOrderType());
    params.add("side", orderEvent.getSide());
    
    log.info("params="+params);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // application/x-www-form-urlencoded
    headers.add("Authorization", "Bearer " + this.accessToken);
    
    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String,String>>(params, headers);
    
    log.info("Execute order: " + orderEvent);
    
    ResponseEntity<String> response = client.exchange(url.toString(), HttpMethod.POST, requestEntity, String.class);

    boolean isSuccess = false;
    if (response.getStatusCode() == HttpStatus.OK) {
      log.info(response.getBody());
      isSuccess = true;
    } else {
      log.warn(response.getBody());
    }
    
    return isSuccess;
  }
}
