package com.enderio.core.common.enchant;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.enchant.IAdvancedEnchant;
import com.enderio.core.common.config.ConfigHandler;

import cpw.mods.fml.common.event.FMLInterModComms;

public class EnchantAutoSmelt extends Enchantment implements IAdvancedEnchant {

    public static final EnchantAutoSmelt INSTANCE = new EnchantAutoSmelt(ConfigHandler.enchantIDAutoSmelt);

    private EnchantAutoSmelt(int id) {
        super(id, 2, EnumEnchantmentType.breakable);
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

    public void register() {
        if (ConfigHandler.allowAutoSmelt) {
            FMLInterModComms.sendMessage(
                    "EnderIO",
                    "recipe:enchanter",
                    "<enchantment name=\"enchantment.autosmelt\" costPerLevel=\"30\">\n<itemStack oreDictionary=\"blockCoal\" number=\"32\"/>\n</enchantment>");
        } else {
            Enchantment.enchantmentsList[this.effectId] = null;
        }
    }

    public boolean canApplyTogether(Enchantment ench) {
        return super.canApplyTogether(ench) && ench.effectId != silkTouch.effectId
                && ((ench.effectId != fortune.effectId) || ConfigHandler.allowAutoSmeltWithFortune);
    }

}
