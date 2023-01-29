package com.enderio.core.client.handlers;

import static com.enderio.core.common.config.ConfigHandler.showDurabilityTooltips;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import com.enderio.core.EnderCore;
import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.api.client.gui.IResourceTooltipProvider;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.util.ItemUtil;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Handler
public enum SpecialTooltipHandler {

    INSTANCE;

    public interface ITooltipCallback extends IAdvancedTooltipProvider {

        boolean shouldHandleItem(ItemStack item);
    }

    private final List<ITooltipCallback> callbacks = Lists.newArrayList();

    public void addCallback(ITooltipCallback callback) {
        callbacks.add(callback);
    }

    @SubscribeEvent
    public void addTooltip(ItemTooltipEvent evt) {
        if (evt.itemStack == null) {
            return;
        }

        boolean shiftDown = showAdvancedTooltips();
        boolean debugMode = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
        boolean doDurability = showDurabilityTooltips == 3 ? debugMode
                : showDurabilityTooltips == 2 ? shiftDown : showDurabilityTooltips == 1;

        if (doDurability) {
            addDurabilityTooltip(evt.toolTip, evt.itemStack);
        }

        if (evt.itemStack.getItem() instanceof IAdvancedTooltipProvider) {
            IAdvancedTooltipProvider ttp = (IAdvancedTooltipProvider) evt.itemStack.getItem();
            addInformation(ttp, evt.itemStack, evt.entityPlayer, evt.toolTip, false);
            return;
        } else if (evt.itemStack.getItem() instanceof IResourceTooltipProvider) {
            addInformation((IResourceTooltipProvider) evt.itemStack.getItem(), evt);
            return;
        }

        Block blk = Block.getBlockFromItem(evt.itemStack.getItem());
        if (blk instanceof IAdvancedTooltipProvider) {
            addInformation((IAdvancedTooltipProvider) blk, evt.itemStack, evt.entityPlayer, evt.toolTip, false);
            return;
        } else if (blk instanceof IResourceTooltipProvider) {
            addInformation((IResourceTooltipProvider) blk, evt);
            return;
        }

        for (ITooltipCallback callback : callbacks) {
            if (callback.shouldHandleItem(evt.itemStack)) {
                addInformation(callback, evt.itemStack, evt.entityPlayer, evt.toolTip, false);
            }
        }
    }

    public void addDurabilityTooltip(List<String> toolTip, ItemStack itemStack) {
        if (!itemStack.isItemStackDamageable()) {
            return;
        }
        Item item = itemStack.getItem();
        if (item instanceof ItemTool || item instanceof ItemArmor
                || item instanceof ItemSword
                || item instanceof ItemHoe
                || item instanceof ItemBow) {
            toolTip.add(ItemUtil.getDurabilityString(itemStack));
        }
    }

    public void addInformation(IResourceTooltipProvider item, ItemTooltipEvent evt) {
        addInformation(item, evt.itemStack, evt.entityPlayer, evt.toolTip);
    }

    public void addInformation(IResourceTooltipProvider tt, ItemStack itemstack, EntityPlayer entityplayer, List list) {
        String name = tt.getUnlocalizedNameForTooltip(itemstack);
        if (showAdvancedTooltips()) {
            addCommonTooltipFromResources(list, name);
            addDetailedTooltipFromResources(list, name);
        } else {
            addBasicTooltipFromResources(list, name);
            addCommonTooltipFromResources(list, name);
            if (hasDetailedTooltip(tt, itemstack)) {
                addShowDetailsTooltip(list);
            }
        }
    }

    public void addInformation(IAdvancedTooltipProvider tt, ItemStack itemstack, EntityPlayer entityplayer, List list,
            boolean flag) {
        tt.addCommonEntries(itemstack, entityplayer, list, flag);
        if (showAdvancedTooltips()) {
            tt.addDetailedEntries(itemstack, entityplayer, list, flag);
        } else {
            tt.addBasicEntries(itemstack, entityplayer, list, flag);
            if (hasDetailedTooltip(tt, itemstack, entityplayer, flag)) {
                addShowDetailsTooltip(list);
            }
        }
    }

    private static final List<String> throwaway = new ArrayList<String>();

    private static boolean hasDetailedTooltip(IResourceTooltipProvider tt, ItemStack stack) {
        throwaway.clear();
        String name = tt.getUnlocalizedNameForTooltip(stack);
        addDetailedTooltipFromResources(throwaway, name);
        return !throwaway.isEmpty();
    }

    private static boolean hasDetailedTooltip(IAdvancedTooltipProvider tt, ItemStack stack, EntityPlayer player,
            boolean flag) {
        throwaway.clear();
        tt.addDetailedEntries(stack, player, throwaway, flag);
        return !throwaway.isEmpty();
    }

    public static void addShowDetailsTooltip(List list) {
        list.add(
                EnumChatFormatting.WHITE + ""
                        + EnumChatFormatting.ITALIC
                        + EnderCore.lang.localize("tooltip.showDetails"));
    }

    public static boolean showAdvancedTooltips() {
        return ClientHandler.isShiftDown();
    }

    public static void addDetailedTooltipFromResources(List list, String unlocalizedName) {
        addTooltipFromResources(list, unlocalizedName.concat(".tooltip.detailed.line"));
    }

    public static void addBasicTooltipFromResources(List list, String unlocalizedName) {
        addTooltipFromResources(list, unlocalizedName.concat(".tooltip.basic.line"));
    }

    public static void addCommonTooltipFromResources(List list, String unlocalizedName) {
        addTooltipFromResources(list, unlocalizedName.concat(".tooltip.common.line"));
    }

    public static void addTooltipFromResources(List list, String keyBase) {
        boolean done = false;
        int line = 1;
        while (!done) {
            String key = keyBase + line;
            String val = EnderCore.lang.localizeExact(key);
            if (val == null || val.trim().length() < 0 || val.equals(key) || line > 12) {
                done = true;
            } else {
                list.add(val);
                line++;
            }
        }
    }

    private static String getUnlocalizedNameForTooltip(ItemStack itemstack) {
        String unlocalizedNameForTooltip = null;
        if (itemstack.getItem() instanceof IResourceTooltipProvider) {
            unlocalizedNameForTooltip = ((IResourceTooltipProvider) itemstack.getItem())
                    .getUnlocalizedNameForTooltip(itemstack);
        }
        if (unlocalizedNameForTooltip == null) {
            unlocalizedNameForTooltip = itemstack.getItem().getUnlocalizedName(itemstack);
        }
        return unlocalizedNameForTooltip;
    }

    public static void addCommonTooltipFromResources(List list, ItemStack itemstack) {
        if (itemstack.getItem() == null) {
            return;
        }
        addCommonTooltipFromResources(list, getUnlocalizedNameForTooltip(itemstack));
    }

    public static void addDetailedTooltipFromResources(List list, ItemStack itemstack) {
        if (itemstack.getItem() == null) {
            return;
        }
        addDetailedTooltipFromResources(list, getUnlocalizedNameForTooltip(itemstack));
    }

}
