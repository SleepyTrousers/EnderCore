package com.enderio.core.common.imc.handlers;

import com.enderio.core.api.common.imc.IMC;
import com.enderio.core.common.handlers.RightClickCropHandler;
import com.enderio.core.common.handlers.RightClickCropHandler.IPlantInfo;
import com.enderio.core.common.handlers.RightClickCropHandler.LegacyPlantInfo;
import com.enderio.core.common.imc.IMCRegistry.IMCBase;
import com.enderio.core.common.util.Log;

import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCRightClickCrop extends IMCBase {
  public IMCRightClickCrop() {
    super(IMC.ADD_RIGHT_CLICK_CROP);
  }

  @Override
  public void act(IMCMessage msg) {
    if (!msg.isStringMessage()) {
      return;
    }

    String[] data = msg.getStringValue().split("\\|");

    if (data.length != 4) {
      Log.error("Got invalid right click crop IMC, incorrect number of arguments: " + msg.getStringValue());
      return;
    }

    IPlantInfo plantinfo = new LegacyPlantInfo(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
    plantinfo.init("received in IMC message from " + msg.getSender());
    RightClickCropHandler.INSTANCE.addCrop(plantinfo);
  }
}
