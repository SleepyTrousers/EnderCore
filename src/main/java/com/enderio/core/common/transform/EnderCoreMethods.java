package com.enderio.core.common.transform;

import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.event.ArrowUpdateEvent;
import com.enderio.core.common.event.ItemStackEvent.ItemEnchantabilityEvent;
import com.enderio.core.common.event.ItemStackEvent.ItemRarityEvent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;

public class EnderCoreMethods {
  public static boolean hasVoidParticles(WorldType type, boolean hasSky) {
    if (ConfigHandler.disableVoidFog == 0) {
      return type != WorldType.FLAT && !hasSky;
    } else if (ConfigHandler.disableVoidFog == 1) {
      return type != WorldType.FLAT && type != WorldType.DEFAULT && !hasSky;
    } else {
      return false;
    }
  }

  public static int getMaxAnvilCost() {
    return ConfigHandler.anvilMaxLevel;
  }

  public static int getItemEnchantability(ItemStack stack, int base) {
    ItemEnchantabilityEvent event = new ItemEnchantabilityEvent(stack, base);
    MinecraftForge.EVENT_BUS.post(event);
    return event.enchantability;
  }

  public static EnumRarity getItemRarity(ItemStack stack) {
    ItemRarityEvent event = new ItemRarityEvent(stack, stack.getItem().getRarity(stack));
    MinecraftForge.EVENT_BUS.post(event);
    return event.rarity;
  }

  public static void onArrowUpdate(EntityArrow entity) {
    MinecraftForge.EVENT_BUS.post(new ArrowUpdateEvent(entity));
  }

  // mostly copied from ContainerFurnace
  public static ItemStack transferStackInSlot(ContainerFurnace inv, EntityPlayer p_82846_1_, int p_82846_2_) {
    ItemStack itemstack = null;
    Slot slot = (Slot) inv.inventorySlots.get(p_82846_2_);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      if (p_82846_2_ == 2) {
        if (!mergeItemStack(inv, itemstack1, 3, 39, true)) {
          return null;
        }

        slot.onSlotChange(itemstack1, itemstack);
      } else if (p_82846_2_ != 1 && p_82846_2_ != 0) {
        // I have moved this check to the beginning
        if (TileEntityFurnace.isItemFuel(itemstack1)) {
          if (!mergeItemStack(inv, itemstack1, 1, 2, false)) {
            // Nest this inside so that if the above fails it will
            // attempt to do the input slot
            if (FurnaceRecipes.instance().getSmeltingResult(itemstack1) != null) {
              if (!mergeItemStack(inv, itemstack1, 0, 1, false)) {
                return null;
              }
            }
          }
        } else if (FurnaceRecipes.instance().getSmeltingResult(itemstack1) != null) {
          if (!mergeItemStack(inv, itemstack1, 0, 1, false)) {
            return null;
          }
        } else if (p_82846_2_ >= 3 && p_82846_2_ < 30) {
          if (!mergeItemStack(inv, itemstack1, 30, 39, false)) {
            return null;
          }
        } else if (p_82846_2_ >= 30 && p_82846_2_ < 39 && !mergeItemStack(inv, itemstack1, 3, 30, false)) {
          return null;
        }
      } else if (!mergeItemStack(inv, itemstack1, 3, 39, false)) {
        return null;
      }

      if (itemstack1.stackSize == 0) {
        slot.putStack((ItemStack) null);
      } else {
        slot.onSlotChanged();
      }

      if (itemstack1.stackSize == itemstack.stackSize) {
        return null;
      }

      slot.onPickupFromSlot(p_82846_1_, itemstack1);
    }

    return itemstack;
  }

  // copied from Container
  private static boolean mergeItemStack(Container inv, ItemStack p_75135_1_, int p_75135_2_, int p_75135_3_, boolean p_75135_4_) {
    boolean flag1 = false;
    int k = p_75135_2_;

    if (p_75135_4_) {
      k = p_75135_3_ - 1;
    }

    Slot slot;
    ItemStack itemstack1;

    if (p_75135_1_.isStackable()) {
      while (p_75135_1_.stackSize > 0 && (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_)) {
        slot = (Slot) inv.inventorySlots.get(k);
        itemstack1 = slot.getStack();

        if (itemstack1 != null && itemstack1.getItem() == p_75135_1_.getItem()
            && (!p_75135_1_.getHasSubtypes() || p_75135_1_.getItemDamage() == itemstack1.getItemDamage())
            && ItemStack.areItemStackTagsEqual(p_75135_1_, itemstack1)) {
          int l = itemstack1.stackSize + p_75135_1_.stackSize;

          if (l <= p_75135_1_.getMaxStackSize()) {
            p_75135_1_.stackSize = 0;
            itemstack1.stackSize = l;
            slot.onSlotChanged();
            flag1 = true;
          } else if (itemstack1.stackSize < p_75135_1_.getMaxStackSize()) {
            p_75135_1_.stackSize -= p_75135_1_.getMaxStackSize() - itemstack1.stackSize;
            itemstack1.stackSize = p_75135_1_.getMaxStackSize();
            slot.onSlotChanged();
            flag1 = true;
          }
        }

        if (p_75135_4_) {
          --k;
        } else {
          ++k;
        }
      }
    }

    if (p_75135_1_.stackSize > 0) {
      if (p_75135_4_) {
        k = p_75135_3_ - 1;
      } else {
        k = p_75135_2_;
      }

      while (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_) {
        slot = (Slot) inv.inventorySlots.get(k);
        itemstack1 = slot.getStack();

        if (itemstack1 == null) {
          slot.putStack(p_75135_1_.copy());
          slot.onSlotChanged();
          p_75135_1_.stackSize = 0;
          flag1 = true;
          break;
        }

        if (p_75135_4_) {
          --k;
        } else {
          ++k;
        }
      }
    }

    return flag1;
  }
}
