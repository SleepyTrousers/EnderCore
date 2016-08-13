package com.enderio.core.common.transform;

import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.event.AnvilMaxCostEvent;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.MinecraftForge;

public class EnderCoreMethods {
  
  public static int getMaxAnvilCost(Object source) {
    int maxCost = ConfigHandler.invisibleMode == 1 ? 40 : ConfigHandler.anvilMaxLevel;
    AnvilMaxCostEvent event = new AnvilMaxCostEvent(source, maxCost);    
    MinecraftForge.EVENT_BUS.post(event);    
    return event.getMaxAnvilCost();    
  }

  // mostly copied from ContainerFurnace
  public static ItemStack transferStackInSlot(ContainerFurnace inv, EntityPlayer p_82846_1_, int p_82846_2_) {
    ItemStack itemstack = null;
    Slot slot = inv.inventorySlots.get(p_82846_2_);

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
        slot = inv.inventorySlots.get(k);
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
        slot = inv.inventorySlots.get(k);
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

  public static interface IOverlayRenderAware {
    public void renderItemOverlayIntoGUI(ItemStack stack, int xPosition, int yPosition);
  }

  public static void renderItemOverlayIntoGUI(ItemStack stack, int xPosition, int yPosition) {
    if (stack != null && stack.getItem() instanceof IOverlayRenderAware) {
      ((IOverlayRenderAware) stack.getItem()).renderItemOverlayIntoGUI(stack, xPosition, yPosition);
    }
  }

  public static interface IElytraFlyingProvider {
    public boolean isElytraFlying(EntityLivingBase entity, ItemStack itemstack);
  }

  public static boolean isElytraFlying(EntityLivingBase entity) {
    if (!entity.isRiding() && !entity.isInWater() && !entity.isInLava()) {
      ItemStack itemstack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
      if (itemstack != null && itemstack.getItem() instanceof IElytraFlyingProvider) {
        return ((IElytraFlyingProvider) itemstack.getItem()).isElytraFlying(entity, itemstack);
      }
    }
    return false;
  }

  public static interface ICreeperTarget {
    /**
     * Determine if the given creeper should blow up when nearby.
     * <p>
     * Note that the creeper stills tracks the target, even if this returns false.
     * 
     * @param swellingCreeper
     *          The creeper that wants to explode
     * @return True if the creeper is allowed to explode, false otherwise.
     */
    boolean isCreeperTarget(EntityCreeper swellingCreeper);
  }

  public static boolean isCreeperTarget(EntityCreeper swellingCreeper, EntityLivingBase entitylivingbase) {
    if (entitylivingbase instanceof ICreeperTarget) {
      return ((ICreeperTarget) entitylivingbase).isCreeperTarget(swellingCreeper);
    }
    return true;
  }

}
