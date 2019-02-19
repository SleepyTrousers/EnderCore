package com.enderio.core.common.tweaks;

import com.enderio.core.common.util.Log;
import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SlabRecipes2 extends Tweak {

  public static final SlabRecipes2 INSTANCE = new SlabRecipes2();

  SlabRecipes2() {
    super("slabToBlockRecipesAutomatic", "Adds recipes to turn any two slabs back into a full block by scanning existing recipes and guessing a lot");
  }

  @Override
  public void load() {
    registerSlabToBlock();
  }

  @Override
  public boolean enabledByDefault() {
    return false;
  }

  private static void registerSlabToBlock() {
    RECIPES: for (IRecipe recipe : ForgeRegistries.RECIPES) {
      if ((recipe.getRecipeOutput().getItem() instanceof ItemSlab || Block.getBlockFromItem(recipe.getRecipeOutput().getItem()) instanceof BlockSlab)
          && !"minecraft".equals(recipe.getRecipeOutput().getItem().getRegistryName().getResourceDomain())) {
        if (recipe.getRecipeOutput().getCount() != 6) {
          Log.info("Rejected anti-slab recipe for " + recipe.getRecipeOutput().copy() + " because output size is not 6");
          continue;
        }
        NNList<ItemStack> stacks = new NNList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
          stacks.addAll(ingredient.getMatchingStacks());
        }
        ItemStack input = null;
        int count = 0;
        for (ItemStack stack : stacks) {
          if (stack != null && !stack.isEmpty()) {
            if (input == null) {
              input = stack;
              count++;
            } else if (input.getItem() == stack.getItem() && input.getItemDamage() == stack.getItemDamage()) {
              count++;
            } else {
              Log.info("Rejected anti-slab recipe for " + recipe.getRecipeOutput().copy() + " because " + input + " is not " + stack);
              continue RECIPES;
            }
          }
        }
        if (input != null && count == 3) {
          Ingredient ingredient = Ingredient.fromStacks(recipe.getRecipeOutput().copy().splitStack(1));
          ForgeRegistries.RECIPES.register(new ShapelessRecipes("", input, new NNList<>(ingredient, ingredient))

              .setRegistryName(recipe.getRecipeOutput().getItem().getRegistryName().getResourcePath() + "_" + recipe.getRecipeOutput().getItemDamage() + "_to_"
                  + input.getItem().getRegistryName().getResourcePath())

          );
          Log.info("Added anti-slab recipe for " + recipe.getRecipeOutput().copy() + " back to " + input);
        } else if (input != null) {
          Log.info("Rejected anti-slab recipe for " + recipe.getRecipeOutput().copy() + " back to " + input + " because count is " + count);
        }
      }

    }

  }

}
