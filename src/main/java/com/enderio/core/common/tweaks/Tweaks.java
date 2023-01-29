package com.enderio.core.common.tweaks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;

public class Tweaks {

    /**
     * As of yet unused, but might be nice in the future to have a list
     */
    private static final List<Tweak> tweaks = new ArrayList<Tweak>();

    public static void loadIngameTweaks() {
    // @formatter:off
        tweaks.add(new Tweak("fixBedSound", "Makes the bed stepSound wool instead of stone") {
            @Override
            public void load() { Blocks.bed.setStepSound(Block.soundTypeCloth); }
        });
        
        tweaks.add(new Tweak("changeBoatStackSize", "Makes boats stack to 16") {
            @Override
            public void load() { Items.boat.setMaxStackSize(16); }
        });
        
        tweaks.add(new Tweak("fixPackedIceTool", "Allows packed ice to be mined with a pickaxe") {
            @Override
            public void load() { Blocks.packed_ice.setHarvestLevel("pickaxe", 0); }
        });
    }
    
    public static void loadNonIngameTweaks()
    {
        tweaks.add(new SlabRecipes());
        tweaks.add(new InfiniBow());

        tweaks.add(new Tweak("bookToPaperRecipe", "Adds shapeless recipe from 1 book to 2 paper") {
            @Override
            public void load() { GameRegistry.addShapelessRecipe(new ItemStack(Items.paper, 2), Items.book); }
        });
        
        tweaks.add(new Tweak("shapelessPaperRecipe","Adds a shapeless recipe for paper") {
            @Override
            public void load() { GameRegistry.addShapelessRecipe(new ItemStack(Items.paper, 3), Items.reeds, Items.reeds, Items.reeds); }
        });
    }    
}
