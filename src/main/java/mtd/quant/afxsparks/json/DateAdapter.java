package mtd.quant.afxsparks.json;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

  private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
  
  @Override
  public Date unmarshal(String v) throws Exception {
    return format.parse(v);
  }

  @Override
  public String marshal(Date v) throws Exception {
    return format.format(v);
  }

}
