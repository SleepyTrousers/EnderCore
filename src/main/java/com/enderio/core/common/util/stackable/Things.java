package com.enderio.core.common.util.stackable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.NNIterator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.Block;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
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
public class Things extends Ingredient {
  static final @Nonnull Map<String, IThing> aliases = new HashMap<String, IThing>();

  private static final @Nonnull List<Things> values = new ArrayList<Things>();

  public Things(String... names) {
    init(names);
  }

  public Things() {
    // for subclasses
  }

  protected void init(String... names) {
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

  public @Nonnull Things addAll(@Nonnull NNList<String> names) {
    for (String name : names) {
      add(name);
    }
    return this;
  }

  public @Nonnull Things add(@Nullable Item item) {
    if (item != null) {
      nameList.add("item:" + item.getRegistryName());
      add(new ItemThing(item));
    }
    return this;
  }

  public @Nonnull Things add(@Nullable ItemStack itemStack) { // sic!
    if (itemStack != null && !itemStack.isEmpty()) {
      nameList.add("item:" + itemStack.getItem().getRegistryName());
      add(new ItemStackThing(itemStack));
    }
    return this;
  }

  public @Nonnull Things add(@Nullable Block block) {
    if (block != null) {
      nameList.add("block:" + block.getRegistryName());
      add(new BlockThing(block));
    }
    return this;
  }

  public @Nonnull Things add(@Nullable String name) {
    if (name != null) {
      nameList.add(name);
      add(new StringThing(name));
    }
    return this;
  }

  public @Nonnull Things add(@Nullable IProducer producer) {
    if (producer != null) {
      // TODO: Better way to serialize this?
      Block block = producer.getBlock();
      if (block != null) {
        nameList.add("block:" + block.getRegistryName());
      }
      Item item = producer.getItem();
      if (item != null) {
        nameList.add("item:" + item.getRegistryName());
      }
      add(new ProducerThing(producer));
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
      if (net.minecraft.block.Block.REGISTRY.containsKey(resourceLocation)) {
        Block block = net.minecraft.block.Block.REGISTRY.getObject(resourceLocation);
        nameList.add("block:" + block.getRegistryName());
      }
      // this ugly thing seems to be what Forge wants you to use
      Item item = net.minecraft.item.Item.REGISTRY.getObject(resourceLocation);
      if (item != null) {
        nameList.add("item:" + item.getRegistryName());
      }
      add(new ResourceThing(resourceLocation));
    }
    return this;
  }

  public @Nonnull Things addOredict(@Nullable String name) {
    if (name != null) {
      nameList.add("oredict:" + name);
      add(new OreThing(name));
    }
    return this;
  }

  public @Nonnull Things add(@Nullable Things otherThings) {
    if (otherThings != null) {
      nameList.addAll(otherThings.nameList);
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
      itemIds.clear();
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

  private final @Nonnull NNList<ItemStack> itemStackListRaw = new NNList<ItemStack>();

  /**
   * Returns a list of item stacks for this Thing. This does NOT expand items that are defined using the wildcard value for the item damage.
   */
  public NNList<ItemStack> getItemStacksRaw() {
    if (itemStackListRaw.isEmpty()) {
      for (IThing thing : things) {
        itemStackListRaw.addAll(thing.getItemStacks());
      }
    }
    return itemStackListRaw;
  }

  private final @Nonnull NNList<ItemStack> itemStackList = new NNList<ItemStack>();

  /**
   * Returns a list of item stacks for this Thing. This expands items that are defined using the wildcard value for the item damage.
   */
  public @Nonnull NNList<ItemStack> getItemStacks() {
    if (itemStackList.isEmpty()) {
      for (ItemStack stack : getItemStacksRaw()) {
        if (stack.isEmpty()) {
          // NOP
        } else if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
          stack.getItem().getSubItems(CreativeTabs.SEARCH, itemStackList);
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

  private final @Nonnull NNList<String> nameList = new NNList<>();

  public @Nonnull NNList<String> getNameList() {
    return nameList;
  }

  // Ingredient

  @Override
  public @Nonnull ItemStack[] getMatchingStacks() {
    return getItemStacks().toArray(new ItemStack[0]);
  }

  @Override
  public boolean apply(@Nullable ItemStack p_apply_1_) {
    return contains(p_apply_1_);
  }

  private final @Nonnull IntList itemIds = new IntArrayList();

  @Override
  public @Nonnull IntList getValidItemStacksPacked() {
    if (itemIds.isEmpty()) {
      for (NNIterator<ItemStack> itr = getItemStacks().fastIterator(); itr.hasNext();) {
        itemIds.add(RecipeItemHelper.pack(itr.next()));
      }
    }
    return itemIds;
  }

  @Override
  protected void invalidate() {
    NNList<String> names = nameList.copy();
    nameList.clear();
    things.clear();
    addAll(names);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

}
