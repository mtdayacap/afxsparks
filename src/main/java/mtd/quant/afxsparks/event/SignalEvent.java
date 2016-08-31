package mtd.quant.afxsparks.event;

import static mtd.quant.afxsparks.Constants.SIGNAL;
import static mtd.quant.afxsparks.Constants.SEMICOLON;

import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * Used to transmit orders to the execution handler and thus contains the 
 * instrument, the number of units to trade, the order type ("market" 
 * or "limit") and the "side" (i.e. "buy" and "sell")
 * 
 * @author mdayacap
 *
 */
@Component
public class SignalEvent extends Event{
  
  private String instrument;
  
  private String orderType;
  
  private String side;
  
  private Date time;

  public SignalEvent(){
    super(SIGNAL);
  }
  
  public SignalEvent(String instrument, String orderType, String side, Date time) {
    super(SIGNAL);
    this.instrument = instrument;
    this.orderType = orderType;
    this.side = side;
    this.time = time;
  }

  public String getInstrument() {
    return instrument;
  }

  public void setInstrument(String instrument) {
    this.instrument = instrument;
  }

  public String getOrderType() {
    return orderType;
  }

  public void setOrderType(String orderType) {
    this.orderType = orderType;
  }

  public String getSide() {
    return side;
  }

  public void setSide(String side) {
    this.side = side;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("SignalEvent[");
    
    sb.append("instrument=");
    sb.append(getInstrument());
    sb.append(SEMICOLON);
    
    sb.append("orderType=");
    sb.append(getOrderType());
    sb.append(SEMICOLON);
    
    sb.append("side=");
    sb.append(getSide());
    sb.append(SEMICOLON);

    sb.append("time=");
    sb.append(getTime());
    sb.append(SEMICOLON);
    
    return sb.toString();
  }
  
  
}
