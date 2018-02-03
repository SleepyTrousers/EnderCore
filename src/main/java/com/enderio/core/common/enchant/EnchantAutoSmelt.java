package com.enderio.core.common.enchant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.enchant.IAdvancedEnchant;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.util.NullHelper;
import com.google.common.base.Predicate;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class EnchantAutoSmelt extends Enchantment implements IAdvancedEnchant {

  private static EnchantAutoSmelt INSTANCE;

  private static final EnumEnchantmentType ENCH_TYPE = EnumHelper.addEnchantmentType("EC_AUTOSMELT", new Predicate<Item>() {

    @Override
    public boolean apply(@Nullable Item item) {
      return NullHelper.notnullM(item, "EnumEnchantmentType.canEnchantItem(null)").isDamageable() && !(item instanceof ItemArmor)
          && !(item instanceof ItemSword) && !(item instanceof ItemBow) && !(item instanceof ItemFishingRod);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      return super.equals(obj);
    }

  });

  /**
   * Can be null prior to registration, or if disabled
   */
  @Nullable
  public static EnchantAutoSmelt instance() {
    return INSTANCE;
  }

  private EnchantAutoSmelt() {
    super(Rarity.RARE, ENCH_TYPE, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
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
  public @Nonnull String getName() {
    return "enchantment.autosmelt";
  }

  @Override
  public boolean isAllowedOnBooks() {
    return ConfigHandler.allowAutoSmelt;
  }

  @Override
  public @Nonnull String[] getTooltipDetails(@Nonnull ItemStack stack) {
    return new String[] { EnderCore.lang.localize("enchantment.autosmelt.tooltip", false) };
  }

  public static void register() {
    if (ConfigHandler.allowAutoSmelt) {
      INSTANCE = new EnchantAutoSmelt();
      ForgeRegistries.ENCHANTMENTS.register(INSTANCE); // FIXME
      FMLInterModComms.sendMessage("enderio", "recipe:xml",
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?><recipes>"
              + "<recipe name=\"Enchanter: endercore:autosmelt\" required=\"true\" disabled=\"false\"><enchanting>"
              + "<input name=\"oredict:blockCoal\" amount=\"32\"/><enchantment name=\"" + EnderCore.DOMAIN
              + ":autosmelt\" costMultiplier=\"1\"/></enchanting></recipe></recipes>");
    }
  }

  @Override
  public boolean canApplyTogether(@Nonnull Enchantment ench) {
    return super.canApplyTogether(ench) && ench != Enchantments.SILK_TOUCH;
  }

}
