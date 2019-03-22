package com.enderio.core.common.imc;

import java.util.List;

import com.enderio.core.common.imc.handlers.IMCRightClickCrop;
import com.google.common.collect.Lists;

import net.minecraftforge.fml.InterModComms.IMCMessage;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;

public class IMCRegistry {
  public interface IIMC {
    String getKey();

    void act(IMCMessage msg);
  }

  public static abstract class IMCBase implements IIMC {
    private String key;

    public IMCBase(String key) {
      this.key = key;
    }

    @Override
    public String getKey() {
      return key;
    }
  }

  public static final IMCRegistry INSTANCE = new IMCRegistry();

  private List<IIMC> handlers = Lists.newArrayList();

  private IMCRegistry() {
  }

  public void addIMCHandler(IIMC handler) {
    handlers.add(handler);
  }

  public void handleEvent(InterModProcessEvent event) {
    for (IIMC handler : handlers) {
      event.getIMCStream().filter(msg -> msg.getMethod().equals(handler.getKey())).forEach(handler::act);
    }
  }

  public void init() {
    addIMCHandler(new IMCRightClickCrop());
  }
}
