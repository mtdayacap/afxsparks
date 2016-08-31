package mtd.quant.afxsparks.strategy;

import mtd.quant.afxsparks.event.Event;

public interface Strategy {
  void calculateSignals(Event event);
}
