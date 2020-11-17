package com.enderio.core.common.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.vecmath.Vector3d;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class Util {

  public static @Nullable Block getBlockFromItemId(@Nonnull ItemStack itemId) {
    Item item = itemId.getItem();
    if (item instanceof BlockItem) {
      return ((BlockItem) item).getBlock();
    }
    return null;
  }

  public static @Nonnull ItemStack consumeItem(@Nonnull ItemStack stack) {
    if (stack.getItem() instanceof PotionItem) {
      if (stack.getCount() == 1) {
        return new ItemStack(Items.GLASS_BOTTLE);
      } else {
        stack.split(1);
        return stack;
      }
    }
    if (stack.getCount() == 1) {
      if (stack.getItem().hasContainerItem(stack)) {
        return stack.getItem().getContainerItem(stack);
      } else {
        return ItemStack.EMPTY;
      }
    } else {
      stack.split(1);
      return stack;
    }
  }

  public static void giveExperience(@Nonnull PlayerEntity thePlayer, float experience) {
    int intExp = (int) experience;
    float fractional = experience - intExp;
    if (fractional > 0.0F) {
      if ((float) Math.random() < fractional) {
        ++intExp;
      }
    }
    while (intExp > 0) {
      int j = ExperienceOrbEntity.getXPSplit(intExp);
      intExp -= j;
      thePlayer.world.addEntity(new ExperienceOrbEntity(thePlayer.world, thePlayer.getPosX(), thePlayer.getPosY() + 0.5D, thePlayer.getPosZ() + 0.5D, j));
    }
  }

  public static ItemEntity createDrop(@Nonnull World world, @Nonnull ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    if (stack.isEmpty()) {
      return null;
    }
    if (doRandomSpread) {
      float f1 = 0.7F;
      double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      ItemEntity entityitem = new ItemEntity(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickupDelay();
      return entityitem;
    } else {
      ItemEntity entityitem = new ItemEntity(world, x, y, z, stack);
      entityitem.setMotion(0, 0, 0);
      entityitem.setNoPickupDelay();
      return entityitem;
    }
  }

  public static void dropItems(@Nonnull World world, @Nonnull ItemStack stack, @Nonnull BlockPos pos, boolean doRandomSpread) {
    dropItems(world, stack, pos.getX(), pos.getY(), pos.getZ(), doRandomSpread);
  }

  public static void dropItems(@Nonnull World world, @Nonnull ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    if (stack.isEmpty()) {
      return;
    }

    ItemEntity entityitem = createEntityItem(world, stack, x, y, z, doRandomSpread);
    world.addEntity(entityitem);
  }

  public static ItemEntity createEntityItem(@Nonnull World world, @Nonnull ItemStack stack, double x, double y, double z) {
    return createEntityItem(world, stack, x, y, z, true);
  }

  public static @Nonnull ItemEntity createEntityItem(@Nonnull World world, @Nonnull ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    ItemEntity entityitem;
    if (doRandomSpread) {
      float f1 = 0.7F;
      double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      entityitem = new ItemEntity(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickupDelay();
    } else {
      entityitem = new ItemEntity(world, x, y, z, stack);
      entityitem.setMotion(0, 0, 0);
      entityitem.setNoPickupDelay();
    }
    return entityitem;
  }

  public static void dropItems(@Nonnull World world, @Nonnull ItemStack stack, int x, int y, int z, boolean doRandomSpread) {
    if (stack.isEmpty()) {
      return;
    }

    if (doRandomSpread) {
      float f1 = 0.7F;
      double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      ItemEntity entityitem = new ItemEntity(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickupDelay();
      world.addEntity(entityitem);
    } else {
      ItemEntity entityitem = new ItemEntity(world, x + 0.5, y + 0.5, z + 0.5, stack);
      entityitem.setMotion(0,0,0);
      entityitem.setNoPickupDelay();
      world.addEntity(entityitem);
    }
  }

  public static void dropItems(@Nonnull World world, ItemStack[] inventory, int x, int y, int z, boolean doRandomSpread) {
    if (inventory == null) {
      return;
    }
    for (ItemStack stack : inventory) {
      if (!stack.isEmpty()) {
        dropItems(world, stack.copy(), x, y, z, doRandomSpread);
      }
    }
  }

  public static void dropItems(@Nonnull World world, @Nonnull IInventory inventory, int x, int y, int z, boolean doRandomSpread) {
    for (int l = 0; l < inventory.getSizeInventory(); ++l) {
      ItemStack items = inventory.getStackInSlot(l);

      if (!items.isEmpty()) {
        dropItems(world, items.copy(), x, y, z, doRandomSpread);
      }
    }
  }

  public static boolean dumpModObjects(@Nonnull File file) {

    StringBuilder sb = new StringBuilder();
    for (Object key : ForgeRegistries.BLOCKS.getKeys()) {
      if (key != null) {
        sb.append(key.toString());
        sb.append("\n");
      }
    }
    for (Object key : ForgeRegistries.ITEMS.getKeys()) {
      if (key != null) {
        sb.append(key.toString());
        sb.append("\n");
      }
    }

    try {
      Files.write(sb, file, Charsets.UTF_8);
      return true;
    } catch (IOException e) {
      Log.warn("Error dumping ore dictionary entries: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  public static @Nonnull ItemStack decrStackSize(@Nonnull IInventory inventory, int slot, int size) {
    ItemStack item = inventory.getStackInSlot(slot);
    if (!item.isEmpty()) {
      if (item.getCount() <= size) {
        ItemStack result = item;
        inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
        inventory.markDirty();
        return result;
      }
      ItemStack split = item.split(size);
      inventory.markDirty();
      return split;
    }
    return ItemStack.EMPTY;
  }

  public static @Nonnull net.minecraft.util.math.vector.Vector3d getEyePosition(@Nonnull PlayerEntity player) {
    double y = player.getPosY();
    y += player.getEyeHeight();
    return new net.minecraft.util.math.vector.Vector3d(player.getPosX(), y, player.getPosZ());
  }

  public static @Nonnull Vector3d getEyePositionEio(@Nonnull PlayerEntity player) {
    Vector3d res = new Vector3d(player.getPosX(), player.getPosY(), player.getPosZ());
    res.y += player.getEyeHeight();
    return res;
  }

  public static @Nonnull Vector3d getLookVecEio(@Nonnull PlayerEntity player) {
    net.minecraft.util.math.vector.Vector3d lv = player.getLookVec();
    return new Vector3d(lv.x, lv.y, lv.z);
  }

  // Code adapted from World.rayTraceBlocks to return all
  // collided blocks
  public static @Nonnull List<BlockRayTraceResult> raytraceAll(@Nonnull World world, RayTraceContext context) {
    return doRayTrace(context, (p_217297_1_, p_217297_2_) -> {
      BlockState blockstate = world.getBlockState(p_217297_2_);
      FluidState fluidstate = world.getFluidState(p_217297_2_);
      net.minecraft.util.math.vector.Vector3d vector3d = p_217297_1_.getStartVec();
      net.minecraft.util.math.vector.Vector3d vector3d1 = p_217297_1_.getEndVec();
      VoxelShape voxelshape = p_217297_1_.getBlockShape(blockstate, world, p_217297_2_);
      BlockRayTraceResult blockraytraceresult = world.rayTraceBlocks(vector3d, vector3d1, p_217297_2_, voxelshape, blockstate);
      VoxelShape voxelshape1 = p_217297_1_.getFluidShape(fluidstate, world, p_217297_2_);
      BlockRayTraceResult blockraytraceresult1 = voxelshape1.rayTrace(vector3d, vector3d1, p_217297_2_);
      double d0 = blockraytraceresult == null ? Double.MAX_VALUE : p_217297_1_.getStartVec().squareDistanceTo(blockraytraceresult.getHitVec());
      double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : p_217297_1_.getStartVec().squareDistanceTo(blockraytraceresult1.getHitVec());
      return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
    }, (p_217302_0_) -> {
      net.minecraft.util.math.vector.Vector3d vector3d = p_217302_0_.getStartVec().subtract(p_217302_0_.getEndVec());
      return BlockRayTraceResult.createMiss(p_217302_0_.getEndVec(), Direction.getFacingFromVector(vector3d.x, vector3d.y, vector3d.z), new BlockPos(p_217302_0_.getEndVec()));
    });
  }

  private static List<BlockRayTraceResult> doRayTrace(RayTraceContext context, BiFunction<RayTraceContext, BlockPos, BlockRayTraceResult> rayTracer, Function<RayTraceContext, BlockRayTraceResult> missFactory) {
    List<BlockRayTraceResult> result = new ArrayList<BlockRayTraceResult>();

    net.minecraft.util.math.vector.Vector3d vector3d = context.getStartVec();
    net.minecraft.util.math.vector.Vector3d vector3d1 = context.getEndVec();
    if (vector3d.equals(vector3d1)) {
      return result;
    } else {
      double d0 = MathHelper.lerp(-1.0E-7D, vector3d1.x, vector3d.x);
      double d1 = MathHelper.lerp(-1.0E-7D, vector3d1.y, vector3d.y);
      double d2 = MathHelper.lerp(-1.0E-7D, vector3d1.z, vector3d.z);
      double d3 = MathHelper.lerp(-1.0E-7D, vector3d.x, vector3d1.x);
      double d4 = MathHelper.lerp(-1.0E-7D, vector3d.y, vector3d1.y);
      double d5 = MathHelper.lerp(-1.0E-7D, vector3d.z, vector3d1.z);
      int i = MathHelper.floor(d3);
      int j = MathHelper.floor(d4);
      int k = MathHelper.floor(d5);
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(i, j, k);
      BlockRayTraceResult t = rayTracer.apply(context, blockpos$mutable);
      if (t != null) {
        result.add(t);
      } else {
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        int l = MathHelper.signum(d6);
        int i1 = MathHelper.signum(d7);
        int j1 = MathHelper.signum(d8);
        double d9 = l == 0 ? Double.MAX_VALUE : (double)l / d6;
        double d10 = i1 == 0 ? Double.MAX_VALUE : (double)i1 / d7;
        double d11 = j1 == 0 ? Double.MAX_VALUE : (double)j1 / d8;
        double d12 = d9 * (l > 0 ? 1.0D - MathHelper.frac(d3) : MathHelper.frac(d3));
        double d13 = d10 * (i1 > 0 ? 1.0D - MathHelper.frac(d4) : MathHelper.frac(d4));
        double d14 = d11 * (j1 > 0 ? 1.0D - MathHelper.frac(d5) : MathHelper.frac(d5));

        while(d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
          if (d12 < d13) {
            if (d12 < d14) {
              i += l;
              d12 += d9;
            } else {
              k += j1;
              d14 += d11;
            }
          } else if (d13 < d14) {
            j += i1;
            d13 += d10;
          } else {
            k += j1;
            d14 += d11;
          }

          BlockRayTraceResult t1 = rayTracer.apply(context, blockpos$mutable.setPos(i, j, k));
          if (t1 != null) {
            result.add(t1);
          }
        }

        if (result.isEmpty())
          result.add(missFactory.apply(context));
      }
    }

    return result;
  }

  public static @Nullable Direction getDirFromOffset(int xOff, int yOff, int zOff) {
    if (xOff != 0 && yOff == 0 && zOff == 0) {
      return xOff < 0 ? Direction.WEST : Direction.EAST;
    }
    if (zOff != 0 && yOff == 0 && xOff == 0) {
      return zOff < 0 ? Direction.NORTH : Direction.SOUTH;
    }
    if (yOff != 0 && xOff == 0 && zOff == 0) {
      return yOff < 0 ? Direction.DOWN : Direction.UP;
    }
    return null;
  }

  public static @Nonnull Direction getFacingFromEntity(@Nonnull LivingEntity entity) {
    int heading = MathHelper.floor(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
    switch (heading) {
    case 0:
      return Direction.NORTH;
    case 1:
      return Direction.EAST;
    case 2:
      return Direction.SOUTH;
    case 3:
    default:
      return Direction.WEST;
    }

  }

  public static int getProgressScaled(int scale, @Nonnull IProgressTile tile) {
    return (int) (tile.getProgress() * scale);
  }

  public static void writeFacingToNBT(@Nonnull CompoundNBT nbtRoot, @Nonnull String name, @Nonnull Direction dir) {
    short val = -1;
    val = (short) dir.ordinal();
    nbtRoot.putShort(name, val);
  }

  public static @Nullable Direction readFacingFromNBT(@Nonnull CompoundNBT nbtRoot, @Nonnull String name) {
    short val = -1;
    if (nbtRoot.contains(name)) {
      val = nbtRoot.getShort(name);
    }
    if (val > 0) {
      return Direction.values()[val];
    }
    return null;
  }

}
