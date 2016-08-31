package mtd.quant.afxsparks.execution;

import static org.junit.Assert.*;
import static mtd.quant.afxsparks.Constants.BUY;
import static mtd.quant.afxsparks.Constants.MARKET_ORDER;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mtd.quant.afxsparks.AFXSparksConfig;
import mtd.quant.afxsparks.event.OrderEvent;

@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AFXSparksConfig.class})
public class ExecutionIT {

  @Autowired
  private Execution execution;
  
  @Resource
  List<String> pairs;

  @Before
  public void setUp() throws Exception {}

  @Test
  public void order_execution() {
    // WHEN
    OrderEvent orderEvent = new OrderEvent(BUY, 5, pairs.get(0), MARKET_ORDER);
    
    // EXECUTE
    boolean successful = execution.executeOrder(orderEvent);
    
    // THEN
    assertTrue(successful);
  }

}
