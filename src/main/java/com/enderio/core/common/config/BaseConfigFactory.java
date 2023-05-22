package com.enderio.core.common.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.enderio.core.client.config.BaseConfigGui;

import cpw.mods.fml.client.IModGuiFactory;

public class BaseConfigFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {
        ;
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return BaseConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

}
