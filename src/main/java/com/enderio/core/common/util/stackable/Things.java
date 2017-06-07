package com.enderio.core.common.util.stackable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.EnderCore;
import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;

/**
 * This class is a way to hold lists of configurable items and blocks.
 * <p>
 * It can be fed with item names, items, item stacks, block names, blocks and ore dictionary names.
 * <p>
 * Those can then be used to check if an item, item stack or block is in a list, or to fetch a list in item, item stack or block form.
 * <p>
 * Non-existing things are silently ignored. Adding things in the pre-init phase (i.e. before all modded things exist in the game) is safe.
 *
 */
public class Things {
  static final @Nonnull Map<String, IThing> aliases = new HashMap<String, IThing>();

  private static final @Nonnull List<Things> values = new ArrayList<Things>();

  public Things(String... names) {
    for (String string : names) {
      add(string);
    }
    if (inPreInit) {
      values.add(this);
    }
  }

  private static boolean inPreInit = true;

  public static void init(@Nullable FMLInitializationEvent event) {
    inPreInit = false;
    for (Things element : values) {
      element.bake();
    }
    values.clear();
  }

  private final @Nonnull List<IThing> things = new ArrayList<IThing>();

  public @Nonnull Things add(@Nullable Item item) {
    if (item != null) {
      add(new ItemThing(item));
    }
    return this;
  }

  public @Nonnull Things add(@Nullable ItemStack itemStack) { // sic!
    if (itemStack != null && !itemStack.isEmpty()) {
      add(new ItemStackThing(itemStack));
    }
    return this;
  }

  public @Nonnull Things add(@Nullable Block block) {
    if (block != null) {
      add(new BlockThing(block));
    }
    return this;
  }

  public @Nonnull Things add(@Nullable String name) {
    if (name != null) {
      add(new StringThing(name));
    }
    return this;
  }

  public static void addAlias(@Nullable String name, @Nullable String value) {
    if (name != null && value != null) {
      aliases.put(name, new StringThing(value));
    }
  }

  public @Nonnull Things add(@Nullable ResourceLocation resourceLocation) {
    if (resourceLocation != null) {
      add(new ResourceThing(resourceLocation));
    }
    return this;
  }

  public @Nonnull Things addOredict(@Nullable String name) {
    if (name != null) {
      add(new OreThing(name));
    }
    return this;
  }

  public @Nonnull Things add(@Nullable Things otherThings) {
    if (otherThings != null) {
      for (IThing thing : otherThings.things) {
        add(thing);
      }
    }
    return this;
  }

  private void add(@Nullable IThing thing) {
    IThing baked;
    if (!inPreInit && thing != null) {
      baked = thing.bake();
    } else {
      baked = thing;
    }
    if (baked != null) {
      things.add(baked);
      itemList.clear();
      itemStackListRaw.clear();
      itemStackList.clear();
      blockList.clear();
    }
  }

  private void bake() {
    for (int i = 0; i < things.size(); i++) {
      IThing thing = things.get(i);
      IThing bakedThing = thing.bake();
      if (bakedThing != null) {
        things.set(i, bakedThing);
      } else {
        things.remove(i);
        i--;
      }
    }
  }

  public boolean contains(@Nullable Item item) {
    for (IThing thing : things) {
      if (thing.is(item)) {
        return true;
      }
    }
    return false;
  }

  public boolean contains(@Nullable ItemStack itemStack) { // sic!
    for (IThing thing : things) {
      if (thing.is(itemStack)) {
        return true;
      }
    }
    return false;
  }

  public boolean contains(@Nullable Block block) {
    for (IThing thing : things) {
      if (thing.is(block)) {
        return true;
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return things.isEmpty();
  }

  private final @Nonnull NNList<Item> itemList = new NNList<Item>();

  public NNList<Item> getItems() {
    if (itemList.isEmpty()) {
      for (IThing thing : things) {
        itemList.addAll(thing.getItems());
      }
    }
    return itemList;
  }

  private NNList<ItemStack> itemStackListRaw = null;

  public NNList<ItemStack> getItemStacksRaw() {
    if (itemStackListRaw == null) {
      itemStackListRaw = new NNList<ItemStack>();
      for (IThing thing : things) {
        itemStackListRaw.addAll(thing.getItemStacks());
      }
    }
    return itemStackListRaw;
  }

  private final @Nonnull NNList<ItemStack> itemStackList = new NNList<ItemStack>();

  /**
   * Returns a list of item stacks for this Thing. Please note that items that are defined using the wildcard value for the item damage may not return the
   * complete list on standalone servers
   */
  public @Nonnull NNList<ItemStack> getItemStacks() {
    if (itemStackList.isEmpty()) {
      for (ItemStack stack : getItemStacksRaw()) {
        if (stack.isEmpty()) {
          // NOP
        } else if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
          EnderCore.proxy.getSubItems(stack.getItem(), EnderCore.proxy.getCreativeTab(stack), itemStackList);
        } else {
          itemStackList.add(stack);
        }
      }
    }
    return itemStackList;
  }

  private final @Nonnull NNList<Block> blockList = new NNList<Block>();

  public @Nonnull NNList<Block> getBlocks() {
    if (blockList.isEmpty()) {
      for (IThing thing : things) {
        blockList.addAll(thing.getBlocks());
      }
    }
    return blockList;
  }

  public NNList<Object> getRecipeObjects() {
    NNList<Object> result = new NNList<Object>();
    for (IThing thing : things) {
      Object recipeObject = thing.getRecipeObject();
      if (recipeObject != null) {
        result.add(recipeObject);
      }
    }
    return result;
  }

}
