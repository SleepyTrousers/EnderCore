package com.enderio.core.client.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.enderio.core.EnderCore;
import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.api.client.gui.IResourceTooltipProvider;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.util.ItemUtil;
import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.enderio.core.common.config.ConfigHandler.showDurabilityTooltips;

@Handler
public class SpecialTooltipHandler {

  public interface ITooltipCallback extends IAdvancedTooltipProvider {
    boolean shouldHandleItem(@Nonnull ItemStack item);
  }

  private static final @Nonnull List<ITooltipCallback> callbacks = new ArrayList<>();

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
      addInformation(ttp, evt.getItemStack(), evt.getEntityPlayer(), getTooltip(evt), flag);
    } else if (evt.getItemStack().getItem() instanceof IResourceTooltipProvider) {
      addInformation((IResourceTooltipProvider) evt.getItemStack().getItem(), evt, flag);
    } else {
      Block blk = Block.getBlockFromItem(evt.getItemStack().getItem());
      if (blk instanceof IAdvancedTooltipProvider) {
        addInformation((IAdvancedTooltipProvider) blk, evt.getItemStack(), evt.getEntityPlayer(), getTooltip(evt), flag);
      } else if (blk instanceof IResourceTooltipProvider) {
        addInformation((IResourceTooltipProvider) blk, evt, flag);
      }
    }

    for (ITooltipCallback callback : callbacks) {
      if (callback.shouldHandleItem(evt.getItemStack())) {
        addInformation(callback, evt.getItemStack(), evt.getEntityPlayer(), getTooltip(evt), flag);
      }
    }
  }

  public static boolean showDurability(boolean shiftDown) {
    return showDurabilityTooltips == 3 ? Minecraft.getMinecraft().gameSettings.advancedItemTooltips
        : showDurabilityTooltips == 2 ? shiftDown : showDurabilityTooltips == 1;
  }

  public static NNList<String> getAllTooltips(ItemStack stack) {
    NNList<String> list = new NNList<>();

    if (stack.getItem() instanceof IAdvancedTooltipProvider) {
      IAdvancedTooltipProvider tt = (IAdvancedTooltipProvider) stack.getItem();
      tt.addCommonEntries(stack, Minecraft.getMinecraft().player, list, false);
      tt.addBasicEntries(stack, Minecraft.getMinecraft().player, list, false);
      tt.addDetailedEntries(stack, Minecraft.getMinecraft().player, list, false);
    } else if (stack.getItem() instanceof IResourceTooltipProvider) {
      String name = ((IResourceTooltipProvider) stack.getItem()).getUnlocalizedNameForTooltip(stack);
      addCommonTooltipFromResources(list, name);
      addBasicTooltipFromResources(list, name);
      addDetailedTooltipFromResources(list, name);
    } else {
      Block blk = Block.getBlockFromItem(stack.getItem());
      if (blk instanceof IAdvancedTooltipProvider) {
        IAdvancedTooltipProvider tt = (IAdvancedTooltipProvider) blk;
        tt.addCommonEntries(stack, Minecraft.getMinecraft().player, list, false);
        tt.addBasicEntries(stack, Minecraft.getMinecraft().player, list, false);
        tt.addDetailedEntries(stack, Minecraft.getMinecraft().player, list, false);
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
        callback.addCommonEntries(stack, Minecraft.getMinecraft().player, list, false);
        callback.addBasicEntries(stack, Minecraft.getMinecraft().player, list, false);
        callback.addDetailedEntries(stack, Minecraft.getMinecraft().player, list, false);
      }
    }

    return list;
  }

  public static void addDurabilityTooltip(@Nonnull List<String> toolTip, @Nonnull ItemStack itemStack) {
    if (!itemStack.isItemStackDamageable()) {
      return;
    }
    Item item = itemStack.getItem();
    if (item instanceof ItemTool || item instanceof ItemArmor || item instanceof ItemSword || item instanceof ItemHoe || item instanceof ItemBow
        || item instanceof ItemShears) {
      toolTip.add(ItemUtil.getDurabilityString(itemStack));
    }
  }

  public static void addInformation(@Nonnull IResourceTooltipProvider item, @Nonnull ItemTooltipEvent evt, boolean flag) {
    addInformation(item, evt.getItemStack(), evt.getEntityPlayer(), getTooltip(evt), flag);
  }

  private static @Nonnull List<String> getTooltip(@Nonnull ItemTooltipEvent event) {
    List<String> toolTip = event.getToolTip();
    if (toolTip == null) {
      throw new NullPointerException("How should we add a tooltip into a null list???");
    }
    return toolTip;
  }

  public static void addInformation(@Nonnull IResourceTooltipProvider tt, @Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer,
      @Nonnull List<String> list, boolean flag) {
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

  public static void addInformation(@Nonnull IAdvancedTooltipProvider tt, @Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer,
      @Nonnull List<String> list, boolean flag) {
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

  private static final @Nonnull List<String> throwaway = new ArrayList<String>();

  private static boolean hasDetailedTooltip(@Nonnull IResourceTooltipProvider tt, @Nonnull ItemStack stack) {
    throwaway.clear();
    String name = tt.getUnlocalizedNameForTooltip(stack);
    addDetailedTooltipFromResources(throwaway, name);
    return !throwaway.isEmpty();
  }

  private static boolean hasDetailedTooltip(@Nonnull IAdvancedTooltipProvider tt, @Nonnull ItemStack stack, @Nullable EntityPlayer player, boolean flag) {
    throwaway.clear();
    tt.addDetailedEntries(stack, player, throwaway, flag);
    return !throwaway.isEmpty();
  }

  public static void addShowDetailsTooltip(@Nonnull List<String> list) {
    list.add(TextFormatting.WHITE + "" + TextFormatting.ITALIC + EnderCore.lang.localize("tooltip.showDetails"));
  }

  public static void addDetailedTooltipFromResources(@Nonnull List<String> list, @Nonnull String unlocalizedName) {
    addMultilineTooltip(list, unlocalizedName, ".tooltip.detailed");
  }

  public static void addBasicTooltipFromResources(@Nonnull List<String> list, @Nonnull String unlocalizedName) {
    addMultilineTooltip(list, unlocalizedName, ".tooltip.basic");
  }

  public static void addCommonTooltipFromResources(@Nonnull List<String> list, @Nonnull String unlocalizedName) {
    addMultilineTooltip(list, unlocalizedName, ".tooltip.common");
  }

  public static void addMultilineTooltip(@Nonnull List<String> list, @Nonnull String... keyParts) {
    String key = String.join("", keyParts);
    if (EnderCore.lang.canLocalizeExact(key + ".line1")) {
      // compat
      addTooltipFromResources(list, key + ".line");
    } else if (EnderCore.lang.canLocalizeExact(key)) {
      list.addAll(Arrays.asList(EnderCore.lang.localizeListExact(key)));
    }
  }

  @Deprecated
  public static void addTooltipFromResources(@Nonnull List<String> list, @Nullable /* for String.concat() */ String keyBase) {
    boolean done = false;
    int line = 1;
    while (!done) {
      String key = keyBase + line;
      String val = EnderCore.lang.localizeExact(key);
      if (val.trim().length() < 0 || val.equals(key) || line > 12) {
        done = true;
      } else {
        list.add(val);
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
      unlocalizedNameForTooltip = itemstack.getItem().getTranslationKey(itemstack);
    }
    return unlocalizedNameForTooltip;
  }

  public static void addCommonTooltipFromResources(@Nonnull List<String> list, @Nonnull ItemStack itemstack) {
    if (itemstack.isEmpty()) {
      return;
    }
    addCommonTooltipFromResources(list, getUnlocalizedNameForTooltip(itemstack));
  }

  public static void addDetailedTooltipFromResources(@Nonnull List<String> list, @Nonnull ItemStack itemstack) {
    if (itemstack.isEmpty()) {
      return;
    }
    addDetailedTooltipFromResources(list, getUnlocalizedNameForTooltip(itemstack));
  }

  private SpecialTooltipHandler() {
  }

  public static boolean showAdvancedTooltips() {
    return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
  }

}
