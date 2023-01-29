package com.enderio.core.common;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDict {

    public static void registerVanilla() {
        safeRegister("barsIron", Blocks.iron_bars);
        safeRegister("blockHopper", Blocks.hopper);
        safeRegister("blockObsidian", Blocks.obsidian);
        safeRegister("itemNetherStar", Items.nether_star);
        safeRegister("gravel", Blocks.gravel);
        safeRegister("chestWood", Blocks.chest);
        safeRegister("craftingTableWood", Blocks.crafting_table);
        safeRegister("itemCoal", Items.coal);
        safeRegister("itemCharcoal", new ItemStack(Items.coal, 1, 1));
        safeRegister("pearlEnder", Items.ender_pearl);
        safeRegister("pearlEnderEye", Items.ender_eye);
        safeRegister("itemBlazeRod", Items.blaze_rod);
        safeRegister("itemBlazePowder", Items.blaze_powder);
        safeRegister("itemClay", Items.clay_ball);
        safeRegister("itemFlint", Items.flint);
        safeRegister("itemGhastTear", Items.ghast_tear);
        safeRegister("dustGunpowder", Items.gunpowder);
        safeRegister("itemLeather", Items.leather);
        safeRegister("slabWoodOak", new ItemStack(Blocks.wooden_slab, 1, 0));
        safeRegister("slabWoodSpruce", new ItemStack(Blocks.wooden_slab, 1, 1));
        safeRegister("slabWoodBirch", new ItemStack(Blocks.wooden_slab, 1, 2));
        safeRegister("slabWoodJungle", new ItemStack(Blocks.wooden_slab, 1, 3));
        safeRegister("slabWoodAcacia", new ItemStack(Blocks.wooden_slab, 1, 4));
        safeRegister("slabWoodDarkOak", new ItemStack(Blocks.wooden_slab, 1, 5));
        safeRegister("slabStone", new ItemStack(Blocks.stone_slab, 1, 0));
        safeRegister("slabSandstone", new ItemStack(Blocks.stone_slab, 1, 1));
        safeRegister("slabCobblestone", new ItemStack(Blocks.stone_slab, 1, 3));
        safeRegister("slabBricks", new ItemStack(Blocks.stone_slab, 1, 4));
        safeRegister("slabStoneBricks", new ItemStack(Blocks.stone_slab, 1, 5));
        safeRegister("slabNetherBrick", new ItemStack(Blocks.stone_slab, 1, 6));
        safeRegister("slabQuartz", new ItemStack(Blocks.stone_slab, 1, 7));
    }

    public static void safeRegister(String name, Block block) {
        safeRegister(name, Item.getItemFromBlock(block));
    }

    public static void safeRegister(String name, Item item) {
        safeRegister(name, new ItemStack(item));
    }

    public static void safeRegister(String name, ItemStack stack) {
        if (!isRegistered(stack, OreDictionary.getOres(name))) OreDictionary.registerOre(name, stack);
    }

    private static boolean isRegistered(ItemStack stack, ArrayList<ItemStack> toCheck) {
        for (ItemStack check : toCheck) {
            if (stack != null && stack.getItem() == check.getItem()
                    && (stack.getItemDamage() == check.getItemDamage()
                            || stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)) {
                return true;
            }
        }
        return false;
    }

    private OreDict() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
