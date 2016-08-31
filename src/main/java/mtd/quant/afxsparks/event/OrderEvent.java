package mtd.quant.afxsparks.event;

import static mtd.quant.afxsparks.Constants.ORDER;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderEvent extends Event {

  public String instrument;

  public String side;

  public double units;

  public String orderType;

  public OrderEvent() {
    super(ORDER);
  }

  public OrderEvent(String side, double units, String instrument, String orderType) {
    super(ORDER);
    this.instrument = instrument;
    this.side = side;
    this.units = units;
    this.orderType = orderType;
  }

  public String getInstrument() {
    return instrument;
  }

  public void setInstrument(String instrument) {
    this.instrument = instrument;
  }

  public String getSide() {
    return side;
  }

  public void setSide(String side) {
    this.side = side;
  }

  public double getUnits() {
    return units;
  }

  public void setUnits(int units) {
    this.units = units;
  }

  public String getOrderType() {
    return orderType;
  }

  public void setOrderType(String orderType) {
    this.orderType = orderType;
  }

  public String getUnitsAsStringAndInteger() {
    return Integer.toString(new Double(getUnits()).intValue());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("OrderEvent=[");
    sb.append("instrument=").append(getInstrument());
    sb.append(";side=").append(getSide());
    sb.append(";units=").append(getUnitsAsStringAndInteger());
    sb.append(";orderType=").append(getOrderType());
    sb.append("]");
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != getClass()) {
      return false;

    } else if (obj == this) {
      return true;

    }

    OrderEvent orderEvent = (OrderEvent) obj;
    return new EqualsBuilder().append(orderEvent.getInstrument(), getInstrument())
        .append(orderEvent.getSide(), getSide()).append(orderEvent.getType(), getType())
        .append(orderEvent.getOrderType(), getOrderType()).append(orderEvent.getUnits(), getUnits())
        .isEquals();
  }


}
