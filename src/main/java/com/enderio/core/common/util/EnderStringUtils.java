package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.TextFormatting;

public class EnderStringUtils {
  /**
   * Formats a string and number for use in GUIs and tooltips
   *
   * @param prefix
   *          - The string to put before the formatted number
   * @param suffix
   *          - The string to put after the formatted number
   * @param amnt
   *          - The number to be formatted
   * @param useDecimals
   *          - Whether or not to use decimals in the representation
   * @param formatK
   *          - Whether or not to format the thousands
   * @return A string numeric formatted to use SI suffixes
   */
  public static @Nonnull String formatString(String prefix, String suffix, long amnt, boolean useDecimals, boolean formatK) {
    if (formatK && Long.toString(amnt).length() < 7 && Long.toString(amnt).length() > 3) {
      return formatSmallerNumber(prefix, suffix, amnt, useDecimals);
    }

    switch (Long.toString(amnt).length()) {
    case 7:
      return prefix + Long.toString(amnt).substring(0, 1) + (useDecimals ? "." + Long.toString(amnt).substring(1, 3) : "") + "M" + suffix;
    case 8:
      return prefix + Long.toString(amnt).substring(0, 2) + (useDecimals ? "." + Long.toString(amnt).substring(2, 4) : "") + "M" + suffix;
    case 9:
      return prefix + Long.toString(amnt).substring(0, 3) + (useDecimals ? "." + Long.toString(amnt).substring(3, 5) : "") + "M" + suffix;
    case 10:
      return prefix + Long.toString(amnt).substring(0, 1) + (useDecimals ? "." + Long.toString(amnt).substring(1, 3) : "") + "B" + suffix;
    case 11:
      return prefix + Long.toString(amnt).substring(0, 2) + (useDecimals ? "." + Long.toString(amnt).substring(2, 4) : "") + "B" + suffix;
    case 12:
      return prefix + Long.toString(amnt).substring(0, 3) + (useDecimals ? "." + Long.toString(amnt).substring(3, 5) : "") + "B" + suffix;
    case 13:
      return prefix + Long.toString(amnt).substring(0, 1) + (useDecimals ? "." + Long.toString(amnt).substring(1, 5) : "") + "T" + suffix;
    default:
      return prefix + "" + amnt + suffix;
    }
  }

  /**
   * Formats a string and number for use in GUIs and tooltips
   *
   * @param prefix
   *          - The string to put before the formatted number
   * @param suffix
   *          - The string to put after the formatted number
   * @param amnt
   *          - The number to be formatted
   * @param useDecimals
   *          - Whether or not to use decimals in the representation
   * @return A string numeric formatted to use SI suffixes
   */
  public static @Nonnull String formatString(String prefix, String suffix, long amnt, boolean useDecimals) {
    return formatString(prefix, suffix, amnt, useDecimals, false);
  }

  private static @Nonnull String formatSmallerNumber(String prefix, String suffix, long amnt, boolean useDecimals) {
    switch (Long.toString(amnt).length()) {
    case 4:
      return prefix + Long.toString(amnt).substring(0, 1) + (useDecimals ? "." + Long.toString(amnt).substring(1, 3) : "") + "K" + suffix;
    case 5:
      return prefix + Long.toString(amnt).substring(0, 2) + (useDecimals ? "." + Long.toString(amnt).substring(2, 4) : "") + "K" + suffix;
    case 6:
      return prefix + Long.toString(amnt).substring(0, 3) + (useDecimals ? "." + Long.toString(amnt).substring(3, 5) : "") + "K" + suffix;
    }
    return "";
  }

  /**
   * Returns a color for the number passed, based on its percentage of the max
   *
   * @param num
   *          The number to compare
   * @param max
   *          The max number
   *
   * @return if num {@literal <}= 10% of max : RED<br>
   *         if 10% {@literal <} num {@literal <}= 25% of max: GOLD (orange-ish)<br>
   *         if num {@literal >} 25% of max: GREEN
   */
  public static @Nonnull TextFormatting getColorFor(double num, double max) {
    if (num / max <= .1)
      return TextFormatting.RED;
    else if (num / max <= .25)
      return TextFormatting.GOLD;
    else
      return TextFormatting.GREEN;
  }

  public static @Nonnull String getEffectNameWithLevel(@Nonnull EffectInstance effect) {
    String name = EnderCore.lang.localize(effect.getEffectName(), false);

    if (effect.getAmplifier() > 0) {
      name += " " + EnderCore.lang.localize("enchantment.level." + (effect.getAmplifier() + 1), false);
    }

    return name;
  }
}
