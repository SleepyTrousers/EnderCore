package com.enderio.core.common.transform;

import javax.annotation.Nonnull;

import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.event.AnvilMaxCostEvent;
import com.enderio.core.common.event.ItemGUIRenderEvent;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class EnderCoreMethods {

  public static int getMaxAnvilCost(Object source) {
    int maxCost = ConfigHandler.invisibleMode == 1 ? 40 : ConfigHandler.anvilMaxLevel;
    AnvilMaxCostEvent event = new AnvilMaxCostEvent(source, maxCost);
    MinecraftForge.EVENT_BUS.post(event);
    return event.getMaxAnvilCost();
  }

  // copied from ContainerFurnace, changes marked
  public static @Nonnull ItemStack transferStackInSlot(@Nonnull ContainerFurnace inv, @Nonnull EntityPlayer playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = inv.inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      if (index == 2) {
        if (!mergeItemStack(inv, itemstack1, 3, 39, true)) {
          return ItemStack.EMPTY;
        }

        slot.onSlotChange(itemstack1, itemstack);
      } else if (index != 1 && index != 0) {
        if (TileEntityFurnace.isItemFuel(itemstack1) && mergeItemStack(inv, itemstack1, 1, 2, false)) { // HL: added this case
          // NOP - if we can move an item into the fuel slot, we're happy and done. Otherwise try to move it into the input slot, then stop moving it.
        } else if (!FurnaceRecipes.instance().getSmeltingResult(itemstack1).isEmpty()) {
          if (!mergeItemStack(inv, itemstack1, 0, 1, false)) {
            return ItemStack.EMPTY;
          }
        } else if (TileEntityFurnace.isItemFuel(itemstack1)) {
          if (!mergeItemStack(inv, itemstack1, 1, 2, false)) {
            return ItemStack.EMPTY;
          }
        } else if (index >= 3 && index < 30) {
          if (!mergeItemStack(inv, itemstack1, 30, 39, false)) {
            return ItemStack.EMPTY;
          }
        } else if (index >= 30 && index < 39 && !mergeItemStack(inv, itemstack1, 3, 30, false)) {
          return ItemStack.EMPTY;
        }
      } else if (!mergeItemStack(inv, itemstack1, 3, 39, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }

      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }

      slot.onTake(playerIn, itemstack1);
    }

    return itemstack;
  }

  // copied from Container, unchanged
  private static boolean mergeItemStack(@Nonnull Container inv, @Nonnull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
    boolean flag = false;
    int i = startIndex;

    if (reverseDirection) {
      i = endIndex - 1;
    }

    if (stack.isStackable()) {
      while (!stack.isEmpty()) {
        if (reverseDirection) {
          if (i < startIndex) {
            break;
          }
        } else if (i >= endIndex) {
          break;
        }

        Slot slot = inv.inventorySlots.get(i);
        ItemStack itemstack = slot.getStack();

        if (!itemstack.isEmpty() && itemstack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata())
            && ItemStack.areItemStackTagsEqual(stack, itemstack)) {
          int j = itemstack.getCount() + stack.getCount();
          int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

          if (j <= maxSize) {
            stack.setCount(0);
            itemstack.setCount(j);
            slot.onSlotChanged();
            flag = true;
          } else if (itemstack.getCount() < maxSize) {
            stack.shrink(maxSize - itemstack.getCount());
            itemstack.setCount(maxSize);
            slot.onSlotChanged();
            flag = true;
          }
        }

        if (reverseDirection) {
          --i;
        } else {
          ++i;
        }
      }
    }

    if (!stack.isEmpty()) {
      if (reverseDirection) {
        i = endIndex - 1;
      } else {
        i = startIndex;
      }

      while (true) {
        if (reverseDirection) {
          if (i < startIndex) {
            break;
          }
        } else if (i >= endIndex) {
          break;
        }

        Slot slot1 = inv.inventorySlots.get(i);
        ItemStack itemstack1 = slot1.getStack();

        if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
          if (stack.getCount() > slot1.getSlotStackLimit()) {
            slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
          } else {
            slot1.putStack(stack.splitStack(stack.getCount()));
          }

          slot1.onSlotChanged();
          flag = true;
          break;
        }

        if (reverseDirection) {
          --i;
        } else {
          ++i;
        }
      }
    }

    return flag;
  }

  public static interface IOverlayRenderAware {
    public void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition);
  }

  public static interface IUnderlayRenderAware {
    public void renderItemAndEffectIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition);
  }

  public static void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
    if (!stack.isEmpty()) {
      if (stack.getItem() instanceof IOverlayRenderAware) {
        ((IOverlayRenderAware) stack.getItem()).renderItemOverlayIntoGUI(stack, xPosition, yPosition);
      }
      MinecraftForge.EVENT_BUS.post(new ItemGUIRenderEvent.Post(stack, xPosition, yPosition));
    }
  }

  public static void renderItemAndEffectIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
    if (!stack.isEmpty()) {
      if (stack.getItem() instanceof IUnderlayRenderAware) {
        ((IUnderlayRenderAware) stack.getItem()).renderItemAndEffectIntoGUI(stack, xPosition, yPosition);
      }
      MinecraftForge.EVENT_BUS.post(new ItemGUIRenderEvent.Pre(stack, xPosition, yPosition));
    }
  }

  public static interface IElytraFlyingProvider {
    public boolean isElytraFlying(@Nonnull EntityLivingBase entity, @Nonnull ItemStack itemstack, boolean shouldStop);
  }

  // Note: isRiding() and isInWater() are cheap getters, isInLava() is an expensive volumetric search
  public static boolean isElytraFlying(@Nonnull EntityLivingBase entity) {
    ItemStack itemstack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
    if (itemstack.getItem() instanceof IElytraFlyingProvider) {
      return ((IElytraFlyingProvider) itemstack.getItem()).isElytraFlying(entity, itemstack,
          entity.onGround || entity.isRiding() || entity.isInWater() || isInLavaSafe(entity));
    }
    return false;
  }

  // non-chunkloading copy of Entity.isInLava()
  public static boolean isInLavaSafe(@Nonnull Entity entity) {
    return isMaterialInBBSafe(entity.world, entity.getEntityBoundingBox().expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D),
        Material.LAVA);
  }

  // non-chunkloading copy of World.isMaterialInBB()
  public static boolean isMaterialInBBSafe(@Nonnull World world, @Nonnull AxisAlignedBB bb, @Nonnull Material materialIn) {
    int i = MathHelper.floor(bb.minX);
    int j = MathHelper.ceil(bb.maxX);
    int k = MathHelper.floor(bb.minY);
    int l = MathHelper.ceil(bb.maxY);
    int i1 = MathHelper.floor(bb.minZ);
    int j1 = MathHelper.ceil(bb.maxZ);
    BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

    for (int k1 = i; k1 < j; ++k1) {
      for (int l1 = k; l1 < l; ++l1) {
        for (int i2 = i1; i2 < j1; ++i2) {
          blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
          if (world.isBlockLoaded(blockpos$pooledmutableblockpos, false) && world.getBlockState(blockpos$pooledmutableblockpos).getMaterial() == materialIn) {
            blockpos$pooledmutableblockpos.release();
            return true;
          }
        }
      }
    }

    blockpos$pooledmutableblockpos.release();
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
    boolean isCreeperTarget(@Nonnull EntityCreeper swellingCreeper);
  }

  public static boolean isCreeperTarget(@Nonnull EntityCreeper swellingCreeper, @Nonnull EntityLivingBase entitylivingbase) {
    if (entitylivingbase instanceof ICreeperTarget) {
      return ((ICreeperTarget) entitylivingbase).isCreeperTarget(swellingCreeper);
    }
    return true;
  }

}
