package com.enderio.core.common.util.stackable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.NNIterator;
import com.enderio.core.common.util.stackable.IThing.Zwieback;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.Block;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

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
    if (beforeLoadComplete) {
      values.add(this);
    }
  }

  private static boolean inPreInit = true;
  private static boolean beforeLoadComplete = true;

  public static void init(@Nullable FMLInitializationEvent event) {
    inPreInit = false;
    for (Things element : values) {
      element.bake();
    }
    values.clear();
  }

  public static void init(@Nullable FMLLoadCompleteEvent event) {
    beforeLoadComplete = false;
    for (Things element : values) {
      element.doublebake();
    }
    values.clear();
  }

  private final @Nonnull List<IThing> things = new ArrayList<IThing>();
  private NBTTagCompound nbt = null;
  private int size = 1;

  public void setNbt(NBTTagCompound nbt) {
    this.nbt = nbt;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public @Nonnull Things add(@Nullable Item item) {
    if (item != null) {
      nameList.add("item:" + item.getRegistryName());
      add(new ItemThing(item));
    }
    return this;
  }

  public Things add(NNList<?> list) {
    for (Object o : list) {
      if (o instanceof String) {
        add((String) o);
      } else if (o instanceof Item) {
        add((Item) o);
      } else if (o instanceof Block) {
        add((Block) o);
      } else if (o instanceof IProducer) {
        add((IProducer) o);
      } else if (o instanceof ItemStack) {
        add((ItemStack) o);
      } else if (o instanceof ResourceLocation) {
        add((ResourceLocation) o);
      } else if (o instanceof Things) {
        add((Things) o);
      } else if (o instanceof NNList) {
        add((NNList<?>) o);
      } else {
        throw new RuntimeException("Class " + o.getClass() + " is not suitable for Things");
      }
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
    if (thing != null) {
      if (!inPreInit) {
        for (IThing baked : thing.bake()) {
          things.add(baked);
        }
      } else {
        things.add(thing);
      }
    }
    cleanCachedValues();
  }

  private void cleanCachedValues() {
    itemList = null;
    itemStackListRaw = null;
    itemStackList = null;
    blockList = null;
    itemIds = null;
  }

  private void bake() {
    NNList<IThing> temp = new NNList<>();
    for (IThing thing : things) {
      for (IThing baked : thing.bake()) {
        temp.add(baked);
      }
    }
    things.clear();
    things.addAll(temp);
    cleanCachedValues();
  }

  private void doublebake() {
    for (int i = 0; i < things.size(); i++) {
      IThing thing = things.get(i);
      if (thing instanceof Zwieback) {
        IThing bakedThing = ((Zwieback) thing).rebake();
        if (bakedThing != null) {
          things.set(i, bakedThing);
        } else {
          things.remove(i);
          i--;
        }
        cleanCachedValues();
      }
    }
  }

  public boolean contains(@Nullable Item item) {
    if (item != Items.AIR) {
      for (IThing thing : things) {
        if (thing.is(item)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean contains(@Nullable ItemStack itemStack) { // sic!
    if (itemStack != null && !itemStack.isEmpty()) {
      for (IThing thing : things) {
        if (thing.is(itemStack) && (nbt == null || nbt.equals(itemStack.getTagCompound()))) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean contains(@Nullable Block block) {
    if (block != Blocks.AIR) {
      for (IThing thing : things) {
        if (thing.is(block)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return things.isEmpty();
  }

  public boolean isValid() {
    if (inPreInit) {
      Things temp = new Things();
      temp.add(this.getNameList());
      temp.bake();
      return !temp.getItemStacksRaw().isEmpty();
    } else {
      return !getItemStacksRaw().isEmpty();
    }
  }

  public boolean isPotentiallyValid() {
    if (isValid()) {
      return true;
    }
    if (beforeLoadComplete) {
      for (IThing thing : things) {
        if (thing instanceof Zwieback) {
          return true;
        }
      }
    }
    return false;
  }

  private @Nullable NNList<Item> itemList = null;

  public @Nonnull NNList<Item> getItems() {
    NNList<Item> list = itemList;
    if (list == null || list.isEmpty() || beforeLoadComplete) {
      list = new NNList<>();
      for (IThing thing : things) {
        list.addAll(thing.getItems());
      }
    }
    return itemList = list;
  }

  private @Nullable NNList<ItemStack> itemStackListRaw = null;

  /**
   * Returns a list of item stacks for this Thing. Items in this list may have the wildcard meta. List may contain empty stacks.
   */
  public @Nonnull NNList<ItemStack> getItemStacksRaw() {
    NNList<ItemStack> list = itemStackListRaw;
    if (list == null || list.isEmpty() || beforeLoadComplete) {
      list = new NNList<>();
      for (IThing thing : things) {
        list.addAll(thing.getItemStacks());
      }
      applyNBT(list);
    }
    return itemStackListRaw = list;
  }

  private void applyNBT(NNList<ItemStack> list) {
    if (nbt != null || size > 1) {
      for (int i = 0; i < list.size(); i++) {
        ItemStack stack = list.get(i).copy();
        if (nbt != null) {
          stack.setTagCompound(nbt.copy());
        }
        if (size > 1) {
          stack.setCount(size);
        }
        list.set(i, stack);
      }
    }
  }

  private @Nullable NNList<ItemStack> itemStackList = null;

  /**
   * Returns a list of item stacks for this Thing. Wildcard values are expanded to a full list of all available sub-items. Empty stacks are stripped.
   * 
   * Please note that items that are defined using the wildcard value for the item damage may not return the complete list on dedicated servers
   */
  public @Nonnull NNList<ItemStack> getItemStacks() {
    NNList<ItemStack> list = itemStackList;
    if (list == null || list.isEmpty() || beforeLoadComplete) {
      list = new NNList<>();
      for (ItemStack stack : getItemStacksRaw()) {
        if (stack.isEmpty()) {
          // NOP
        } else if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
          stack.getItem().getSubItems(CreativeTabs.SEARCH, list);
        } else {
          list.add(stack);
        }
      }
      applyNBT(list);
    }
    return itemStackList = list;
  }

  public @Nonnull ItemStack getItemStack() {
    NNList<ItemStack> itemStacks = getItemStacks();
    return itemStacks.isEmpty() ? ItemStack.EMPTY : itemStacks.get(0);
  }

  private @Nullable NNList<Block> blockList = null;

  public @Nonnull NNList<Block> getBlocks() {
    NNList<Block> list = blockList;
    if (list == null || list.isEmpty() || beforeLoadComplete) {
      list = new NNList<>();
      for (IThing thing : things) {
        list.addAll(thing.getBlocks());
      }
    }
    return blockList = list;
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

  private @Nullable IntList itemIds = null;

  @Override
  public @Nonnull IntList getValidItemStacksPacked() {
    IntList list = itemIds;
    if (list == null || list.isEmpty() || beforeLoadComplete) {
      list = new IntArrayList();
      for (NNIterator<ItemStack> itr = getItemStacks().fastIterator(); itr.hasNext();) {
        list.add(RecipeItemHelper.pack(itr.next()));
      }
    }
    return itemIds = list;
  }

  @Override
  protected void invalidate() {
    NNList<String> names = nameList.copy();
    nameList.clear();
    things.clear();
    add(names);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @SuppressWarnings("null")
  public @Nonnull Ingredient asIngredient() {
    NNList<Ingredient> ings = new NNList<>();
    for (IThing thing : things) {
      if (thing instanceof OreThing) {
        if (nbt != null) {
          ings.add(new OreIngredientNBT(((OreThing) thing).getName(), nbt));
        } else {
          ings.add(new OreIngredient(((OreThing) thing).getName()) {
            @Override
            public String toString() {
              return "OreIngredient " + Arrays.toString(getMatchingStacks());
            }
          });
        }
      } else {
        NNList<ItemStack> sublist = new NNList<>();
        for (ItemStack stack : thing.getItemStacks()) {
          if (stack.isEmpty()) {
            // NOP
          } else if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            stack.getItem().getSubItems(CreativeTabs.SEARCH, sublist);
          } else {
            sublist.add(stack);
          }
        }
        applyNBT(sublist);
        for (ItemStack itemStack : sublist) {
          if (nbt != null) {
            ings.add(new IngredientNBT(itemStack) {
              @Override
              public String toString() {
                return "IngredientNBT " + Arrays.toString(getMatchingStacks());
              }
            });
          } else {
            ings.add(new Ingredient(itemStack) {
              @Override
              public String toString() {
                return "Ingredient " + Arrays.toString(getMatchingStacks());
              }
            });
          }
        }
      }
    }

    if (ings.isEmpty()) {
      return this;
    } else if (ings.size() == 1) {
      return ings.get(0);
    } else {
      return new CompoundIngredient(ings) {
        @Override
        public String toString() {
          return "CompoundIngredient " + Arrays.toString(getMatchingStacks());
        }
      };
    }
  }

  private static class OreIngredientNBT extends OreIngredient {

    private final @Nonnull NBTTagCompound nbt;

    public OreIngredientNBT(@Nonnull String ore, @Nonnull NBTTagCompound nbt) {
      super(ore);
      this.nbt = nbt;
    }

    @Override
    public boolean apply(@Nullable ItemStack input) {
      return super.apply(input) && input != null && nbt.equals(input.getTagCompound());
    }

    @Override
    @Nonnull
    public ItemStack[] getMatchingStacks() {
      final ItemStack[] matchingStacks = super.getMatchingStacks();
      for (ItemStack itemStack : matchingStacks) {
        itemStack.setTagCompound(nbt.copy());
      }
      return matchingStacks;
    }

    @Override
    public boolean isSimple() {
      return false;
    }

    @Override
    public String toString() {
      return "OreIngredientNBT(" + nbt + ") " + Arrays.toString(getMatchingStacks());
    }

  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Things [");
    builder.append("things=");
    builder.append(things);
    builder.append(", ");
    builder.append("size=");
    builder.append(size);
    builder.append(", ");
    if (nbt != null) {
      builder.append("nbt=");
      builder.append(nbt);
    }
    builder.append("]");
    return builder.toString();
  }

}
