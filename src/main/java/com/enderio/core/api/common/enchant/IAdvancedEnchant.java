package com.enderio.core.api.common.enchant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.EnderCore;
import com.google.common.base.Predicate;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

/**
 * Allows your enchants to have some flavor or description text underneath them
 */
public interface IAdvancedEnchant {

  public static final EnumEnchantmentType ALL = EnumHelper.addEnchantmentType("EC_REALLY_ALL", new Predicate<Item>() {

    @Override
    public boolean apply(@Nullable Item input) {
      return true;
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
   * Get the detail for this itemstack
   *
   * @param stack
   * @return a list of <code>String</code>s to be bulleted under the enchantment
   */
  default @Nonnull String[] getTooltipDetails(@Nonnull ItemStack stack) {
    return EnderCore.lang.splitList(EnderCore.lang.localizeExact("description." + ((Enchantment) this).getName()));
  }

}
