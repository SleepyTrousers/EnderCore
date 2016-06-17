package com.enderio.core.common.enchant;

import javax.annotation.Nullable;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.enchant.IAdvancedEnchant;
import com.enderio.core.common.config.ConfigHandler;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class EnchantAutoSmelt extends Enchantment implements IAdvancedEnchant {
  private static EnchantAutoSmelt INSTANCE;

  /**
   * Can be null prior to registration, or if disabled
   */
  @Nullable
  public static EnchantAutoSmelt instance() {
    return INSTANCE;
  }

  private EnchantAutoSmelt() {
    super(Rarity.UNCOMMON, EnumEnchantmentType.BREAKABLE, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
    setRegistryName("autosmelt");
  }

  @Override
  public int getMaxEnchantability(int level) {
    return 60;
  }

  @Override
  public int getMinEnchantability(int level) {
    return 15;
  }

  @Override
  public int getMaxLevel() {
    return 1;
  }

  @Override
  public boolean canApply(ItemStack stack) {
    return type.canEnchantItem(stack.getItem()) && !(stack.getItem() instanceof ItemArmor) && !(stack.getItem() instanceof ItemSword)
        && !(stack.getItem() instanceof ItemBow) && !(stack.getItem() instanceof ItemFishingRod);
  }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack) {
    return canApply(stack);
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
      INSTANCE = new EnchantAutoSmelt();
      GameRegistry.register(INSTANCE);
      FMLInterModComms.sendMessage("EnderIO", "recipe:enchanter",
          "<enchantment name=\"" + EnderCore.DOMAIN +  ":autosmelt\">\n<itemStack oreDictionary=\"blockCoal\" number=\"32\"/>\n</enchantment>");
    }
  }

  @Override
  public boolean canApplyTogether(Enchantment ench) {
    return super.canApplyTogether(ench) && ench!= Enchantments.SILK_TOUCH
        && ((ench!= Enchantments.FORTUNE) || ConfigHandler.allowAutoSmeltWithFortune);
  }

}
