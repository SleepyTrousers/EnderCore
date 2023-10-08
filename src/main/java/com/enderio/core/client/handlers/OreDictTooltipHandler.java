package com.enderio.core.client.handlers;

import static com.enderio.core.common.config.ConfigHandler.showOredictTooltips;
import static com.enderio.core.common.config.ConfigHandler.showRegistryNameTooltips;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.oredict.OreDictionary;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Handler
public class OreDictTooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        boolean shiftDown = ClientHandler.isShiftDown();
        boolean debugMode = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
        boolean doRegistry = showRegistryNameTooltips == 3 ? debugMode
                : showRegistryNameTooltips == 2 ? shiftDown : showRegistryNameTooltips == 1;
        boolean doOredict = showOredictTooltips == 3 ? debugMode
                : showOredictTooltips == 2 ? shiftDown : showOredictTooltips == 1;

        if (doRegistry) {
            event.toolTip.add(Item.itemRegistry.getNameForObject(event.itemStack.getItem()));
        }

        if (doOredict) {
            int[] ids = OreDictionary.getOreIDs(event.itemStack);

            if (ids.length > 0) {
                event.toolTip.add(EnderCore.lang.localize("tooltip.oreDictNames"));
                for (int i : ids) {
                    event.toolTip.add("  - " + OreDictionary.getOreName(i));
                }
            }
        }
    }
}
