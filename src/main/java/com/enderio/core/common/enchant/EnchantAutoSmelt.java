package com.enderio.core.common.enchant;

import javax.annotation.Nullable;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.enchant.IAdvancedEnchant;
import com.enderio.core.common.config.ConfigHandler;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class EnchantAutoSmelt extends Enchantment implements IAdvancedEnchant {
  private static EnchantAutoSmelt INSTANCE;

  /**
   * Can be null prior to registration, or if disabled
   */
  @Nullable
  public static EnchantAutoSmelt instance() {
    return INSTANCE;
  }

  private EnchantAutoSmelt(int id) {
    // The ResourceLocation is mostly irrelevant, it's just a key to retreive the enchantment with
    super(id, new ResourceLocation(EnderCore.DOMAIN, "autosmelt"), 2, EnumEnchantmentType.BREAKABLE);
  }

  @Override
  public int getMaxEnchantability(int level) {
    return super.getMaxEnchantability(level) + 30;
  }

  @Override
  public int getMinEnchantability(int level) {
    return super.getMinEnchantability(level);
  }

  @Override
  public int getMaxLevel() {
    return 1;
  }

  @Override
  public boolean canApply(ItemStack stack) {
    return type.canEnchantItem(stack.getItem()) && !(stack.getItem() instanceof ItemArmor);
  }

  @Override
  public String getName() {
    return "enchantment.autosmelt";
  }

  @Override
  public boolean isAllowedOnBooks() {
    return ConfigHandler.allowAutoSmelt;
  }

  @Override
  public String[] getTooltipDetails(ItemStack stack) {
    return new String[] { EnderCore.lang.localize("enchantment.autosmelt.tooltip", false) };
  }

  public static void register() {
    if (ConfigHandler.allowAutoSmelt) {
      INSTANCE = new EnchantAutoSmelt(ConfigHandler.enchantIDAutoSmelt);
      FMLInterModComms.sendMessage("EnderIO", "recipe:enchanter",
          "<enchantment name=\"" + EnderCore.DOMAIN +  ":autosmelt\" costPerLevel=\"30\">\n<itemStack oreDictionary=\"blockCoal\" number=\"32\"/>\n</enchantment>");
      Enchantment.addToBookList(INSTANCE);
    }
  }

  @Override
  public boolean canApplyTogether(Enchantment ench) {
    return super.canApplyTogether(ench) && ench.effectId != silkTouch.effectId
        && ((ench.effectId != fortune.effectId) || ConfigHandler.allowAutoSmeltWithFortune);
  }

}
