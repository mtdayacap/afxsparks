package mtd.quant.afxsparks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value="config.properties")
public class ApplicationProperties {
  
  @Autowired
  private Environment env;
  
  public String getProperty(String propName){
    return env.getProperty(propName);
  }
  
}
