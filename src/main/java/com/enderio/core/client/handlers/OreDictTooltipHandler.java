package com.enderio.core.client.handlers;

import static com.enderio.core.common.config.ConfigHandler.showOredictTooltips;
import static com.enderio.core.common.config.ConfigHandler.showRegistryNameTooltips;

import org.lwjgl.input.Keyboard;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

@Handler
public class OreDictTooltipHandler {
  @SubscribeEvent
  public void onItemTooltip(ItemTooltipEvent event) {
    boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    boolean debugMode = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
    boolean doRegistry = showRegistryNameTooltips == 3 ? debugMode : showRegistryNameTooltips == 2 ? shiftDown : showRegistryNameTooltips == 1;
    boolean doOredict = showOredictTooltips == 3 ? debugMode : showOredictTooltips == 2 ? shiftDown : showOredictTooltips == 1;

    if (doRegistry) {
      event.getToolTip().add(Item.REGISTRY.getNameForObject(event.getItemStack().getItem()).toString());
    }

    if (doOredict) {
      int[] ids = OreDictionary.getOreIDs(event.getItemStack());

      if (ids.length > 0) {
        event.getToolTip().add(EnderCore.lang.localize("tooltip.oreDictNames"));
        for (int i : ids) {
          event.getToolTip().add("  - " + OreDictionary.getOreName(i));
        }
      }
    }
  }
}
