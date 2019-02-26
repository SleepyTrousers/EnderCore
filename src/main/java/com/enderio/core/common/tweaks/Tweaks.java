package com.enderio.core.common.tweaks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.enderio.core.common.config.AbstractConfigHandler.RestartReqs;
import com.enderio.core.common.config.ConfigHandler;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class Tweaks {

  private static final Set<Tweak> ingameTweaks = new HashSet<Tweak>();
  private static final Set<Tweak> permanentTweaks = new HashSet<Tweak>();
  private static final Set<Tweak> lateTweaks = new HashSet<Tweak>();

  static {
    // @formatter:off

    // Tweaks that can be turned on/off at will
    ingameTweaks.add(new Tweak("changeBoatStackSize", "Makes boats stack to 16", RestartReqs.NONE) {
      @Override
      public void load() { Items.BOAT.setMaxStackSize(16); }

      @Override
      public void unload() { Items.BOAT.setMaxStackSize(1); }
    });

    ingameTweaks.add(new Tweak("fixPackedIceTool", "Allows packed ice to be mined with a pickaxe", RestartReqs.NONE) {
      @Override
      public void load() { Blocks.PACKED_ICE.setHarvestLevel("pickaxe", 0); }

      @Override
      @SuppressWarnings("null")
      public void unload() { Blocks.PACKED_ICE.setHarvestLevel(null, -1); }
    });

    ingameTweaks.add(new Tweak("fluidContainerBottles", "Makes water bottles normal fluid containers") {
      @Override
      public void load() { 
        MinecraftForge.EVENT_BUS.register(BottleFluidCapability.class); 
      }

      @Override
      protected void unload() {
        MinecraftForge.EVENT_BUS.unregister(BottleFluidCapability.class);
      }
    });

    ingameTweaks.add(new InfiniBow());

    // Tweaks that require a reboot to be toggled and need to be loaded late
    lateTweaks.add(new SlabRecipesAutomatic());

    // Tweaks that require a reboot to be toggled
    permanentTweaks.add(new SlabRecipes());
    permanentTweaks.add(new BottleFluidCapability.BottleTweak());

    permanentTweaks.add(new Tweak("bookToPaperRecipe", "Adds shapeless recipe from 1 book to 2 paper") {
      @Override
      public void load() { 
        ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null, new ItemStack(Items.PAPER, 2), Items.BOOK).setRegistryName("book_to_paper")); 
      }
    });

    permanentTweaks.add(new Tweak("shapelessPaperRecipe", "Adds a shapeless recipe for paper") {
      @Override
      public void load() { 
        ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null, new ItemStack(Items.PAPER, 3), Items.REEDS, Items.REEDS, Items.REEDS).setRegistryName("shapeless_paper")); 
      }
    });

// @formatter:on
  }

  public static void loadIngameTweaks() {
    load(ingameTweaks);
  }

  public static void loadNonIngameTweaks() {
    load(permanentTweaks);
    for (Tweak tweak : lateTweaks) {
      // need to put that in earlier than loadLateTweaks()
      ConfigHandler.instance().addBooleanFor(tweak);
    }
  }

  // this needs to run after all other mods had the chance to add their recipes
  public static void loadLateTweaks() {
    load(lateTweaks);
  }

  private static void load(Collection<Tweak> tweaks) {
    for (Tweak tweak : tweaks) {
      if (ConfigHandler.instance().addBooleanFor(tweak)) {
        tweak.enable();
      } else {
        tweak.disable();
      }
    }
  }
}
