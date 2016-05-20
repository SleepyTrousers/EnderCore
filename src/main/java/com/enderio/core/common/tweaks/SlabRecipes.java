package com.enderio.core.common.tweaks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
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

  private void registerSlabToBlock() {
    for (int i = 0; i <= 1; i++) {
      String[] arr = i == 0 ? slabEndingsWood : slabEndingsStone;
      Block[] results = i == 1 ? slabResults : null;
      for (int j = 0; j < arr.length; j++) {
        GameRegistry
            .addRecipe(new ShapelessOreRecipe(i == 0 ? new ItemStack(Blocks.PLANKS, 1, j) : new ItemStack(results[j]), "slab" + arr[j], "slab" + arr[j]));
      }
    }
  }
}
