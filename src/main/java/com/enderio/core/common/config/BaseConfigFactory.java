package com.enderio.core.common.config;

import java.util.Set;

import com.enderio.core.client.config.BaseConfigGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class BaseConfigFactory implements IModGuiFactory {
  @Override
  public void initialize(Minecraft minecraftInstance) {
    ;
  }

  @Override
  public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
    return null;
  }

  @Override
  public boolean hasConfigGui() {
    return true;
  }

  @Override
  public GuiScreen createConfigGui(GuiScreen parentScreen) {
    return new BaseConfigGui(parentScreen);
  }

}
