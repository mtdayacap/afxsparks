package mtd.quant.afxsparks.event;

import org.springframework.stereotype.Component;

@Component
public class Event {
  
  private String type;

  public Event(){
  }
  
  public Event(String event){
    setType(event);
  }
  
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
