package com.enderio.core.common.util;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.EnderCore;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.button.CycleButton.ICycleEnum;
import com.enderio.core.client.render.EnderWidget;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.oredict.OreDictionary;

public enum DyeColor implements ICycleEnum {

  BLACK,
  RED,
  GREEN,
  BROWN,
  BLUE,
  PURPLE,
  CYAN,
  SILVER,
  GRAY,
  PINK,
  LIME,
  YELLOW,
  LIGHT_BLUE,
  MAGENTA,
  ORANGE,
  WHITE;

  public static final String[] DYE_ORE_NAMES = { "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray",
      "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite" };

  public static final String[] DYE_ORE_UNLOCAL_NAMES = {

      "item.fireworksCharge.black", "item.fireworksCharge.red", "item.fireworksCharge.green", "item.fireworksCharge.brown", "item.fireworksCharge.blue",
      "item.fireworksCharge.purple", "item.fireworksCharge.cyan", "item.fireworksCharge.silver", "item.fireworksCharge.gray", "item.fireworksCharge.pink",
      "item.fireworksCharge.lime", "item.fireworksCharge.yellow", "item.fireworksCharge.lightBlue", "item.fireworksCharge.magenta",
      "item.fireworksCharge.orange", "item.fireworksCharge.white"

  };

  public static final IWidgetIcon[] DYE_ICONS = {

      EnderWidget.COLOR_BLACK, EnderWidget.COLOR_RED, EnderWidget.COLOR_GREEN, EnderWidget.COLOR_BROWN, EnderWidget.COLOR_BLUE, EnderWidget.COLOR_PURPLE,
      EnderWidget.COLOR_CYAN, EnderWidget.COLOR_SILVER, EnderWidget.COLOR_GRAY, EnderWidget.COLOR_PINK, EnderWidget.COLOR_LIME, EnderWidget.COLOR_YELLOW,
      EnderWidget.COLOR_LIGHTBLUE, EnderWidget.COLOR_MAGENTA, EnderWidget.COLOR_ORANGE, EnderWidget.COLOR_WHITE

  };

  public static @Nonnull DyeColor getNext(@Nonnull DyeColor col) {
    int ord = col.ordinal() + 1;
    if (ord >= DyeColor.values().length) {
      ord = 0;
    }
    return NullHelper.first(DyeColor.values()[ord], DyeColor.BLACK);
  }

  public static @Nullable DyeColor getColorFromDye(@Nonnull ItemStack dye) {
    if (dye.isEmpty()) {
      return null;
    }
    int[] oreIDs = OreDictionary.getOreIDs(dye);
    for (int i = 0; i < DYE_ORE_NAMES.length; i++) {
      int dyeID = OreDictionary.getOreID(DYE_ORE_NAMES[i]);
      for (int oreId : oreIDs) {
        if (dyeID == oreId) {
          return DyeColor.values()[i];
        }
      }
    }
    return null;
  }

  public static @Nonnull DyeColor fromIndex(int index) {
    return NullHelper.first(DyeColor.values()[MathHelper.clamp(index, 0, DyeColor.values().length - 1)], DyeColor.BLACK);
  }

  private DyeColor() {
  }

  public int getColor() {
    return ItemDye.DYE_COLORS[ordinal()];
  }

  public @Nonnull String getName() {
    return EnumDyeColor.values()[ordinal()].getName();
  }

  public @Nonnull String getLocalisedName() {
    return EnderCore.lang.localizeExact(NullHelper.notnull(DYE_ORE_UNLOCAL_NAMES[ordinal()], "Data corruption"), false);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  @Nonnull
  public IWidgetIcon getIcon() {
    return NullHelper.first(DYE_ICONS[ordinal()], EnderWidget.COLOR_BLACK);
  }

  @Override
  @Nonnull
  public List<String> getTooltipLines() {
    return new NNList<>(getLocalisedName());
  }
}