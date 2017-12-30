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
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class EnchantXPBoost extends Enchantment implements IAdvancedEnchant {

  private static EnchantXPBoost INSTANCE;

  private static final EnumEnchantmentType ENCH_TYPE = EnumHelper.addEnchantmentType("EC_XPBOOST", new Predicate<Item>() {

    @Override
    public boolean apply(@Nullable Item item) {
      return NullHelper.notnullM(item, "EnumEnchantmentType.canEnchantItem(null)").isDamageable() && !(item instanceof ItemArmor)
          && !(item instanceof ItemFishingRod);
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
  public static EnchantXPBoost instance() {
    return INSTANCE;
  }

  private EnchantXPBoost() {
    // The ResourceLocation is mostly irrelevant, it's just a key to retrieve the enchantment with
    super(Rarity.UNCOMMON, ENCH_TYPE, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND });
    setRegistryName("xpboost");
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
    return 3;
  }

  @Override
  public boolean canApplyTogether(@Nonnull Enchantment ench) {
    return super.canApplyTogether(ench) && ench != Enchantments.SILK_TOUCH;
  }

  @Override
  public @Nonnull String getName() {
    return "enchantment.xpboost";
  }

  @Override
  public boolean isAllowedOnBooks() {
    return ConfigHandler.allowXPBoost;
  }

  @Override
  public @Nonnull String[] getTooltipDetails(@Nonnull ItemStack stack) {
    return new String[] { EnderCore.lang.localize("enchantment.xpboost.tooltip", false) };
  }

  public static void register() {
    if (ConfigHandler.allowXPBoost) {
      INSTANCE = new EnchantXPBoost();
      GameRegistry.register(INSTANCE);
      FMLInterModComms.sendMessage("enderio", "recipe:xml",
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?><recipes>"
              + "<recipe name=\"Enchanter: endercore:xpboost\" required=\"true\" disabled=\"false\"><enchanting>"
              + "<input name=\"oredict:ingotGold\" amount=\"16\"/><enchantment name=\"" + EnderCore.DOMAIN
              + ":xpboost\" costMultiplier=\"1\"/></enchanting></recipe></recipes>");
    }
  }
}
