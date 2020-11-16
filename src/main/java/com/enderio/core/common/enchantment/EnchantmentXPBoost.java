package com.enderio.core.common.enchantment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.enchantment.IAdvancedEnchantment;
import com.enderio.core.common.util.NullHelper;
import com.google.common.base.Predicate;

import com.google.common.base.Supplier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// TODO: Move into EIO Tools module
@EventBusSubscriber(modid = EnderCore.MODID)
public class EnchantmentXPBoost extends Enchantment implements IAdvancedEnchantment {

  private static EnchantmentXPBoost INSTANCE;

  private static final EnchantmentType ENCH_TYPE = EnchantmentType.create("EC_XPBOOST", new Predicate<Item>() {

    @Override
    public boolean apply(@Nullable Item item) {
      return NullHelper.notnullM(item, "EnumEnchantmentType.canEnchantItem(null)").isDamageable() && !(item instanceof ArmorItem)
          && !(item instanceof FishingRodItem);
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
  public static EnchantmentXPBoost instance() {
    return INSTANCE;
  }

  private EnchantmentXPBoost() {
    // The ResourceLocation is mostly irrelevant, it's just a key to retrieve the enchantment with
    super(Rarity.UNCOMMON, ENCH_TYPE, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND });
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
    // TODO: Config:
//    return ConfigHandler.allowXPBoost;
    return true;
  }

  @Override
  public boolean canApply(@Nonnull ItemStack stack) {
    // TODO: Config:
//    return ConfigHandler.allowXPBoost && super.canApply(stack);
    return super.canApply(stack);
  }

  @Override
  public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack) {
    // TODO: Config:
//    return ConfigHandler.allowXPBoost && super.canApplyAtEnchantingTable(stack);
    return super.canApplyAtEnchantingTable(stack);
  }

  @Override
  public @Nonnull String[] getTooltipDetails(@Nonnull ItemStack stack) {
    return new String[] { EnderCore.lang.localize("enchantment.xpboost.tooltip", false) };
  }

  @SubscribeEvent
  public static void register(@Nonnull RegistryEvent.Register<Enchantment> event) {
    // TODO: Config:
//    if (ConfigHandler.allowXPBoost) {
      INSTANCE = new EnchantmentXPBoost();
      event.getRegistry().register(INSTANCE);
      Supplier<String> supplier = new Supplier<String>() {
        @Override
        public String get() {
          return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><recipes>"
                  + "<recipe name=\"Enchanter: endercore:xpboost\" required=\"true\" disabled=\"false\"><enchanting>"
                  + "<input name=\"oredict:ingotGold\" amount=\"16\"/><enchantment name=\"" + EnderCore.DOMAIN
                  + ":xpboost\" costMultiplier=\"1\"/></enchanting></recipe></recipes>";
        }
      };
      InterModComms.sendTo("enderio", "recipe:xml", supplier);
//    }
  }
}
