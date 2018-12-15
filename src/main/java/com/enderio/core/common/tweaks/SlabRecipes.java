package com.enderio.core.common.tweaks;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NullHelper;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class SlabRecipes extends Tweak {
  private static final String[] slabEndingsWood = { "WoodOak", "WoodSpruce", "WoodBirch", "WoodJungle", "WoodAcacia", "WoodDarkOak" };
  private static final String[] slabEndingsStone = { "Stone", "Sandstone", "Cobblestone", "Bricks", "StoneBricks", "NetherBrick", "Quartz" };
  private static final Block[] slabResults = { Blocks.STONE, Blocks.SANDSTONE, Blocks.COBBLESTONE, Blocks.BRICK_BLOCK, Blocks.STONEBRICK, Blocks.NETHER_BRICK,
      Blocks.QUARTZ_BLOCK };

  public static final SlabRecipes INSTANCE = new SlabRecipes();

  SlabRecipes() {
    super("slabToBlockRecipes", "Adds recipes to turn any two slabs back into a full block");
  }

  @Override
  public void load() {
    registerSlabToBlock();
  }

  private static void addSlabRecipe(@Nonnull ItemStack stack, @Nonnull String slab) {
    ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null, stack, slab, slab)
        .setRegistryName(slab + "_to_" + NullHelper.notnullM(stack.getItem().getRegistryName(), "found unregistered item").getPath()));
  }

  private static void registerSlabToBlock() {
    for (int j = 0; j < slabEndingsWood.length; j++) {
      addSlabRecipe(new ItemStack(Blocks.PLANKS, 1, j), "slab" + slabEndingsWood[j]);
    }

    for (int j = 0; j < slabEndingsStone.length; j++) {
      addSlabRecipe(new ItemStack(NullHelper.notnullM(slabResults[j], "Blocks are missing from the game")), "slab" + slabEndingsStone[j]);
    }
  }

}
