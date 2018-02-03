package com.enderio.core.common.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class AnvilMaxCostEvent extends Event {

  private int maxAnvilCost;
  
  private final Object source;

  public AnvilMaxCostEvent(Object source, int maxAnvilCost) {  
    this.maxAnvilCost = maxAnvilCost;
    this.source = source;
  }

  public int getMaxAnvilCost() {
    return maxAnvilCost;
  }

  public void setMaxAnvilCost(int maxAnvilCost) {
    this.maxAnvilCost = maxAnvilCost;
  }

  public Object getSource() {
    return source;
  }
  
}
