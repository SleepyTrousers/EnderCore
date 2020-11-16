package com.enderio.core.client.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


import com.enderio.core.EnderCore;
import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.api.client.gui.IResourceTooltipProvider;
import com.enderio.core.common.util.ItemUtil;
import com.enderio.core.common.util.NNList;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.BooleanUtils;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber
public class SpecialTooltipHandler {

  public interface ITooltipCallback extends IAdvancedTooltipProvider {
    boolean shouldHandleItem(@Nonnull ItemStack item);
  }

  private static final @Nonnull List<ITooltipCallback> callbacks = Lists.newArrayList();

  public static void addCallback(@Nonnull ITooltipCallback callback) {
    callbacks.add(callback);
  }

  @SubscribeEvent
  public static void addTooltip(ItemTooltipEvent evt) {
    if (evt.getItemStack().isEmpty()) {
      return;
    }

    final boolean flag = showAdvancedTooltips();
    boolean doDurability = showDurability(evt.getFlags().isAdvanced());

    if (doDurability) {
      addDurabilityTooltip(getTooltip(evt), evt.getItemStack());
    }

    if (evt.getItemStack().getItem() instanceof IAdvancedTooltipProvider) {
      IAdvancedTooltipProvider ttp = (IAdvancedTooltipProvider) evt.getItemStack().getItem();
      addInformation(ttp, evt.getItemStack(), evt.getPlayer(), getTooltip(evt), flag);
    } else if (evt.getItemStack().getItem() instanceof IResourceTooltipProvider) {
      addInformation((IResourceTooltipProvider) evt.getItemStack().getItem(), evt, flag);
    } else {
      Block blk = Block.getBlockFromItem(evt.getItemStack().getItem());
      if (blk instanceof IAdvancedTooltipProvider) {
        addInformation((IAdvancedTooltipProvider) blk, evt.getItemStack(), evt.getPlayer(), getTooltip(evt), flag);
      } else if (blk instanceof IResourceTooltipProvider) {
        addInformation((IResourceTooltipProvider) blk, evt, flag);
      }
    }

    for (ITooltipCallback callback : callbacks) {
      if (callback.shouldHandleItem(evt.getItemStack())) {
        addInformation(callback, evt.getItemStack(), evt.getPlayer(), getTooltip(evt), flag);
      }
    }
  }

  //Show durability on item tooltips.", "0 - Off", "1 - Always on", "2 - Only with shift", "3 - Only in debug mode
  private static int showDurabilityTooltips = 1;

  public static boolean showDurability(boolean shiftDown) {
    return showDurabilityTooltips == 3 ? Minecraft.getInstance().gameSettings.advancedItemTooltips
        : showDurabilityTooltips == 2 ? shiftDown : showDurabilityTooltips == 1;
  }

  public static NNList<ITextComponent> getAllTooltips(ItemStack stack) {
    NNList<ITextComponent> list = new NNList<>();

    if (stack.getItem() instanceof IAdvancedTooltipProvider) {
      IAdvancedTooltipProvider tt = (IAdvancedTooltipProvider) stack.getItem();
      tt.addCommonEntries(stack, Minecraft.getInstance().player, list, false);
      tt.addBasicEntries(stack, Minecraft.getInstance().player, list, false);
      tt.addDetailedEntries(stack, Minecraft.getInstance().player, list, false);
    } else if (stack.getItem() instanceof IResourceTooltipProvider) {
      String name = ((IResourceTooltipProvider) stack.getItem()).getUnlocalizedNameForTooltip(stack);
      addCommonTooltipFromResources(list, name);
      addBasicTooltipFromResources(list, name);
      addDetailedTooltipFromResources(list, name);
    } else {
      Block blk = Block.getBlockFromItem(stack.getItem());
      if (blk instanceof IAdvancedTooltipProvider) {
        IAdvancedTooltipProvider tt = (IAdvancedTooltipProvider) blk;
        tt.addCommonEntries(stack, Minecraft.getInstance().player, list, false);
        tt.addBasicEntries(stack, Minecraft.getInstance().player, list, false);
        tt.addDetailedEntries(stack, Minecraft.getInstance().player, list, false);
      } else if (blk instanceof IResourceTooltipProvider) {
        IResourceTooltipProvider tt = (IResourceTooltipProvider) blk;
        String name = tt.getUnlocalizedNameForTooltip(stack);
        addCommonTooltipFromResources(list, name);
        addBasicTooltipFromResources(list, name);
        addDetailedTooltipFromResources(list, name);
      }
    }

    for (ITooltipCallback callback : callbacks) {
      if (callback.shouldHandleItem(stack)) {
        callback.addCommonEntries(stack, Minecraft.getInstance().player, list, false);
        callback.addBasicEntries(stack, Minecraft.getInstance().player, list, false);
        callback.addDetailedEntries(stack, Minecraft.getInstance().player, list, false);
      }
    }

    return list;
  }

  public static void addDurabilityTooltip(@Nonnull List<ITextComponent> toolTip, @Nonnull ItemStack itemStack) {
    /*if (!itemStack.isItemStackDamageable()) {
      return;
    }*/

    Item item = itemStack.getItem();
    if (item instanceof ToolItem || item instanceof ArmorItem || item instanceof SwordItem || item instanceof HoeItem || item instanceof BowItem
        || item instanceof ShearsItem) {
      toolTip.add(new StringTextComponent(ItemUtil.getDurabilityString(itemStack)));
    }
  }

  public static void addInformation(@Nonnull IResourceTooltipProvider item, @Nonnull ItemTooltipEvent evt, boolean flag) {
    addInformation(item, evt.getItemStack(), evt.getPlayer(), getTooltip(evt), flag);
  }

  private static @Nonnull List<ITextComponent> getTooltip(@Nonnull ItemTooltipEvent event) {
    List<ITextComponent> toolTip = event.getToolTip();
    if (toolTip == null) {
      throw new NullPointerException("How should we add a tooltip into a null list???");
    }
    return toolTip;
  }

  public static void addInformation(@Nonnull IResourceTooltipProvider tt, @Nonnull ItemStack itemstack, @Nullable PlayerEntity entityplayer,
      @Nonnull List<ITextComponent> list, boolean flag) {
    String name = tt.getUnlocalizedNameForTooltip(itemstack);
    if (flag) {
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

  public static void addInformation(@Nonnull IAdvancedTooltipProvider tt, @Nonnull ItemStack itemstack, @Nullable PlayerEntity entityplayer,
      @Nonnull List<ITextComponent> list, boolean flag) {
    tt.addCommonEntries(itemstack, entityplayer, list, false);
    if (flag) {
      tt.addDetailedEntries(itemstack, entityplayer, list, false);
    } else {
      tt.addBasicEntries(itemstack, entityplayer, list, false);
      if (hasDetailedTooltip(tt, itemstack, entityplayer, false)) {
        addShowDetailsTooltip(list);
      }
    }
  }

  private static final @Nonnull List<ITextComponent> throwaway = new ArrayList<ITextComponent>();

  private static boolean hasDetailedTooltip(@Nonnull IResourceTooltipProvider tt, @Nonnull ItemStack stack) {
    throwaway.clear();
    String name = tt.getUnlocalizedNameForTooltip(stack);
    addDetailedTooltipFromResources(throwaway, name);
    return !throwaway.isEmpty();
  }

  private static boolean hasDetailedTooltip(@Nonnull IAdvancedTooltipProvider tt, @Nonnull ItemStack stack, @Nullable PlayerEntity player, boolean flag) {
    throwaway.clear();
    tt.addDetailedEntries(stack, player, throwaway, flag);
    return !throwaway.isEmpty();
  }

  public static void addShowDetailsTooltip(@Nonnull List<ITextComponent> list) {
    list.add(new StringTextComponent(TextFormatting.WHITE + "" + TextFormatting.ITALIC + EnderCore.lang.localize("tooltip.showDetails")));
  }

  public static void addDetailedTooltipFromResources(@Nonnull List<ITextComponent> list, @Nonnull String unlocalizedName) {
    addTooltipFromResources(list, unlocalizedName.concat(".tooltip.detailed.line"));
  }

  public static void addBasicTooltipFromResources(@Nonnull List<ITextComponent> list, @Nonnull String unlocalizedName) {
    addTooltipFromResources(list, unlocalizedName.concat(".tooltip.basic.line"));
  }

  public static void addCommonTooltipFromResources(@Nonnull List<ITextComponent> list, @Nonnull String unlocalizedName) {
    addTooltipFromResources(list, unlocalizedName.concat(".tooltip.common.line"));
  }

  public static void addTooltipFromResources(@Nonnull List<ITextComponent> list, @Nullable /* for String.concat() */ String keyBase) {
    boolean done = false;
    int line = 1;
    while (!done) {
      String key = keyBase + line;
      String val = EnderCore.lang.localizeExact(key);
      if (val.trim().length() < 0 || val.equals(key) || line > 12) {
        done = true;
      } else {
        list.add(new StringTextComponent(val));
        line++;
      }
    }
  }

  private static @Nonnull String getUnlocalizedNameForTooltip(@Nonnull ItemStack itemstack) {
    String unlocalizedNameForTooltip = null;
    if (itemstack.getItem() instanceof IResourceTooltipProvider) {
      unlocalizedNameForTooltip = ((IResourceTooltipProvider) itemstack.getItem()).getUnlocalizedNameForTooltip(itemstack);
    }
    if (unlocalizedNameForTooltip == null) {
      unlocalizedNameForTooltip = itemstack.getItem().getName().toString();
    }
    return unlocalizedNameForTooltip;
  }

  public static void addCommonTooltipFromResources(@Nonnull List<ITextComponent> list, @Nonnull ItemStack itemstack) {
    if (itemstack.isEmpty()) {
      return;
    }
    addCommonTooltipFromResources(list, getUnlocalizedNameForTooltip(itemstack));
  }

  public static void addDetailedTooltipFromResources(@Nonnull List<ITextComponent> list, @Nonnull ItemStack itemstack) {
    if (itemstack.isEmpty()) {
      return;
    }
    addDetailedTooltipFromResources(list, getUnlocalizedNameForTooltip(itemstack));
  }

  private SpecialTooltipHandler() {
  }

  public static boolean showAdvancedTooltips() {
    return BooleanUtils.toBoolean(GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) || BooleanUtils.toBoolean(GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT));
    //return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
  }

}
