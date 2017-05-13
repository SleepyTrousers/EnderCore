package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDictionaryHelper {

  public static final String INGOT_COPPER = "ingotCopper";
  public static final String INGOT_TIN = "ingotTin";
  public static final String DUST_ENDERPEARL = "dustEnderPearl";
  public static final String INGOT_ENDERIUM = "ingotEnderium";

  public static boolean isRegistered(String name) {
    if (!OreDictionary.getOres(name).isEmpty()) {
      return true;
    }
    return false;
  }

  public static boolean hasCopper() {
    return isRegistered(INGOT_COPPER);
  }

  public static boolean hasTin() {
    return isRegistered(INGOT_TIN);
  }

  public static boolean hasEnderPearlDust() {
    return isRegistered(DUST_ENDERPEARL);
  }

  private OreDictionaryHelper() {
  }

  public static boolean hasEnderium() {
    return isRegistered(INGOT_ENDERIUM);
  }

  public static String[] getOreNames(@Nonnull ItemStack stack) {
    int[] ids = OreDictionary.getOreIDs(stack);
    String[] ret = new String[ids.length];
    for (int i = 0; i < ids.length; i++) {
      ret[i] = OreDictionary.getOreName(ids[i]);
    }
    return ret;
  }

  public static boolean hasName(@Nonnull ItemStack stack, String oreName) {
    return ArrayUtils.contains(getOreNames(stack), oreName);
  }

}
