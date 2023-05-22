package com.enderio.core.common.imc.handlers;

import com.enderio.core.api.common.imc.IMC;
import com.enderio.core.common.handlers.RightClickCropHandler;
import com.enderio.core.common.handlers.RightClickCropHandler.PlantInfo;
import com.enderio.core.common.imc.IMCRegistry.IMCBase;

import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

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

        if (data.length != 3) {
            return;
        }

        PlantInfo plantinfo = new PlantInfo(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
        plantinfo.init();
        RightClickCropHandler.INSTANCE.addCrop(plantinfo);
    }
}
