package com.enderio.core.common.util;

import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;

import com.enderio.core.EnderCore;

public class EnderStringUtils {

    /**
     * Formats a string and number for use in GUIs and tooltips
     * 
     * @param prefix      - The string to put before the formatted number
     * @param suffix      - The string to put after the formatted number
     * @param amnt        - The number to be formatted
     * @param useDecimals - Whether or not to use decimals in the representation
     * @param formatK     - Whether or not to format the thousands
     * @return A string numeric formatted to use SI suffixes
     */
    public static String formatString(String prefix, String suffix, long amnt, boolean useDecimals, boolean formatK) {
        if (formatK && Long.toString(amnt).length() < 7 && Long.toString(amnt).length() > 3) {
            return formatSmallerNumber(prefix, suffix, amnt, useDecimals);
        }

        switch (Long.toString(amnt).length()) {
            case 7:
                prefix += Long.toString(amnt).substring(0, 1)
                        + (useDecimals ? "." + Long.toString(amnt).substring(1, 3) : "")
                        + "M"
                        + suffix;
                return prefix;
            case 8:
                prefix += Long.toString(amnt).substring(0, 2)
                        + (useDecimals ? "." + Long.toString(amnt).substring(2, 4) : "")
                        + "M"
                        + suffix;
                return prefix;
            case 9:
                prefix += Long.toString(amnt).substring(0, 3)
                        + (useDecimals ? "." + Long.toString(amnt).substring(3, 5) : "")
                        + "M"
                        + suffix;
                return prefix;
            case 10:
                prefix += Long.toString(amnt).substring(0, 1)
                        + (useDecimals ? "." + Long.toString(amnt).substring(1, 3) : "")
                        + "B"
                        + suffix;
                return prefix;
            case 11:
                prefix += Long.toString(amnt).substring(0, 2)
                        + (useDecimals ? "." + Long.toString(amnt).substring(2, 4) : "")
                        + "B"
                        + suffix;
                return prefix;
            case 12:
                prefix += Long.toString(amnt).substring(0, 3)
                        + (useDecimals ? "." + Long.toString(amnt).substring(3, 5) : "")
                        + "B"
                        + suffix;
                return prefix;
            case 13:
                prefix += Long.toString(amnt).substring(0, 1)
                        + (useDecimals ? "." + Long.toString(amnt).substring(1, 5) : "")
                        + "T"
                        + suffix;
                return prefix;
            default:
                prefix += "" + amnt + suffix;
                return prefix;
        }
    }

    /**
     * Formats a string and number for use in GUIs and tooltips
     * 
     * @param prefix      - The string to put before the formatted number
     * @param suffix      - The string to put after the formatted number
     * @param amnt        - The number to be formatted
     * @param useDecimals - Whether or not to use decimals in the representation
     * @return A string numeric formatted to use SI suffixes
     */
    public static String formatString(String prefix, String suffix, long amnt, boolean useDecimals) {
        return formatString(prefix, suffix, amnt, useDecimals, false);
    }

    private static String formatSmallerNumber(String prefix, String suffix, long amnt, boolean useDecimals) {
        switch (Long.toString(amnt).length()) {
            case 4:
                prefix += Long.toString(amnt).substring(0, 1)
                        + (useDecimals ? "." + Long.toString(amnt).substring(1, 3) : "")
                        + "K"
                        + suffix;
                return prefix;
            case 5:
                prefix += Long.toString(amnt).substring(0, 2)
                        + (useDecimals ? "." + Long.toString(amnt).substring(2, 4) : "")
                        + "K"
                        + suffix;
                return prefix;
            case 6:
                prefix += Long.toString(amnt).substring(0, 3)
                        + (useDecimals ? "." + Long.toString(amnt).substring(3, 5) : "")
                        + "K"
                        + suffix;
                return prefix;
        }
        return "";
    }

    /**
     * Returns a color for the number passed, based on its percentage of the max
     * 
     * @param num The number to compare
     * @param max The max number
     * 
     * @return if num {@literal <}= 10% of max : RED<br>
     *         if 10% {@literal <} num {@literal <}= 25% of max: GOLD (orange-ish)<br>
     *         if num {@literal >} 25% of max: GREEN
     */
    public static EnumChatFormatting getColorFor(double num, double max) {
        if (num / max <= .1) return EnumChatFormatting.RED;
        else if (num / max <= .25) return EnumChatFormatting.GOLD;
        else return EnumChatFormatting.GREEN;
    }

    public static String getEffectNameWithLevel(PotionEffect effect) {
        String name = EnderCore.lang.localize(effect.getEffectName(), false);

        if (effect.getAmplifier() > 0) {
            name += " " + EnderCore.lang.localize("enchantment.level." + (effect.getAmplifier() + 1), false);
        }

        return name;
    }
}
