package mtd.quant.afxsparks.event;

import static mtd.quant.afxsparks.Constants.TICK;
import static mtd.quant.afxsparks.Constants.SEMICOLON;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mtd.quant.afxsparks.json.DateAdapter;

/**
 * Stores information of the (best) bid/ask and the trade time
 * 
 * @author mdayacap
 *
 */
@XmlRootElement
public class TickEvent extends Event {
  
  private String instrument;

  @XmlElement(name = "time", required = true)
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date time;
  
  private double bid;
  
  private double ask;

  public TickEvent() {
    super(TICK);
  }

  public String getInstrument() {
    return instrument;
  }

  public void setInstrument(String instrument) {
    this.instrument = instrument;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public double getBid() {
    return bid;
  }

  public void setBid(double bid) {
    this.bid = bid;
  }

  public double getAsk() {
    return ask;
  }

  public void setAsk(double ask) {
    this.ask = ask;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Tick[");
    
    sb.append("instrument=");
    sb.append(getInstrument());
    sb.append(SEMICOLON);
    
    sb.append("time=");
    sb.append(getTime());
    sb.append(SEMICOLON);
    
    sb.append("bid=");
    sb.append(getBid());
    sb.append(SEMICOLON);
    
    sb.append("ask=");
    sb.append(getAsk());
    
    sb.append("]");
    return sb.toString();
  }
  
  
}
