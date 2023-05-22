package com.enderio.core.common.enchant;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.enchant.IAdvancedEnchant;
import com.enderio.core.common.config.ConfigHandler;

import cpw.mods.fml.common.event.FMLInterModComms;

public class EnchantXPBoost extends Enchantment implements IAdvancedEnchant {

    public static final EnchantXPBoost INSTANCE = new EnchantXPBoost(ConfigHandler.enchantIDXPBoost);

    private EnchantXPBoost(int id) {
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
        return 3;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        return type.canEnchantItem(stack.getItem()) && !(stack.getItem() instanceof ItemArmor);
    }

    @Override
    public String getName() {
        return "enchantment.xpboost";
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ConfigHandler.allowXPBoost;
    }

    @Override
    public String[] getTooltipDetails(ItemStack stack) {
        return new String[] { EnderCore.lang.localize("enchantment.xpboost.tooltip", false) };
    }

    public void register() {
        if (ConfigHandler.allowXPBoost) {
            FMLInterModComms.sendMessage(
                    "EnderIO",
                    "recipe:enchanter",
                    "<enchantment name=\"enchantment.xpboost\" costPerLevel=\"4\">\n<itemStack oreDictionary=\"ingotGold\" number=\"16\"/>\n</enchantment>");
        } else {
            Enchantment.enchantmentsList[this.effectId] = null;
        }
    }
}
