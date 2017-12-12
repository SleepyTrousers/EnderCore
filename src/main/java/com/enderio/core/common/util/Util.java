package com.enderio.core.common.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.vecmath.Vector3d;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class Util {

  public static @Nullable Block getBlockFromItemId(@Nonnull ItemStack itemId) {
    Item item = itemId.getItem();
    if (item instanceof ItemBlock) {
      return ((ItemBlock) item).getBlock();
    }
    return null;
  }

  public static @Nonnull ItemStack consumeItem(@Nonnull ItemStack stack) {
    if (stack.getItem() instanceof ItemPotion) {
      if (stack.getCount() == 1) {
        return new ItemStack(Items.GLASS_BOTTLE);
      } else {
        stack.splitStack(1);
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
      stack.splitStack(1);
      return stack;
    }
  }

  public static void giveExperience(@Nonnull EntityPlayer thePlayer, float experience) {
    int intExp = (int) experience;
    float fractional = experience - intExp;
    if (fractional > 0.0F) {
      if ((float) Math.random() < fractional) {
        ++intExp;
      }
    }
    while (intExp > 0) {
      int j = EntityXPOrb.getXPSplit(intExp);
      intExp -= j;
      thePlayer.world.spawnEntity(new EntityXPOrb(thePlayer.world, thePlayer.posX, thePlayer.posY + 0.5D, thePlayer.posZ + 0.5D, j));
    }
  }

  public static EntityItem createDrop(@Nonnull World world, @Nonnull ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    if (stack.isEmpty()) {
      return null;
    }
    if (doRandomSpread) {
      float f1 = 0.7F;
      double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      EntityItem entityitem = new EntityItem(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickupDelay();
      return entityitem;
    } else {
      EntityItem entityitem = new EntityItem(world, x, y, z, stack);
      entityitem.motionX = 0;
      entityitem.motionY = 0;
      entityitem.motionZ = 0;
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

    EntityItem entityitem = createEntityItem(world, stack, x, y, z, doRandomSpread);
    world.spawnEntity(entityitem);
  }

  public static EntityItem createEntityItem(@Nonnull World world, @Nonnull ItemStack stack, double x, double y, double z) {
    return createEntityItem(world, stack, x, y, z, true);
  }

  public static @Nonnull EntityItem createEntityItem(@Nonnull World world, @Nonnull ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    EntityItem entityitem;
    if (doRandomSpread) {
      float f1 = 0.7F;
      double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      entityitem = new EntityItem(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickupDelay();
    } else {
      entityitem = new EntityItem(world, x, y, z, stack);
      entityitem.motionX = 0;
      entityitem.motionY = 0;
      entityitem.motionZ = 0;
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
      EntityItem entityitem = new EntityItem(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickupDelay();
      world.spawnEntity(entityitem);
    } else {
      EntityItem entityitem = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack);
      entityitem.motionX = 0;
      entityitem.motionY = 0;
      entityitem.motionZ = 0;
      entityitem.setNoPickupDelay();
      world.spawnEntity(entityitem);
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
    for (Object key : Block.REGISTRY.getKeys()) {
      if (key != null) {
        sb.append(key.toString());
        sb.append("\n");
      }
    }
    for (Object key : Item.REGISTRY.getKeys()) {
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

  public static boolean dumpOreNames(@Nonnull File file) {

    try {
      String[] oreNames = OreDictionary.getOreNames();
      Files.write(Joiner.on("\n").join(oreNames), file, Charsets.UTF_8);
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
      ItemStack split = item.splitStack(size);
      inventory.markDirty();
      return split;
    }
    return ItemStack.EMPTY;
  }

  public static @Nonnull Vec3d getEyePosition(@Nonnull EntityPlayer player) {
    double y = player.posY;
    y += player.getEyeHeight();
    return new Vec3d(player.posX, y, player.posZ);
  }

  public static @Nonnull Vector3d getEyePositionEio(@Nonnull EntityPlayer player) {
    Vector3d res = new Vector3d(player.posX, player.posY, player.posZ);
    res.y += player.getEyeHeight();
    return res;
  }

  public static @Nonnull Vector3d getLookVecEio(@Nonnull EntityPlayer player) {
    Vec3d lv = player.getLookVec();
    return new Vector3d(lv.x, lv.y, lv.z);
  }

  // Code adapted from World.rayTraceBlocks to return all
  // collided blocks
  public static @Nonnull List<RayTraceResult> raytraceAll(@Nonnull World world, @Nonnull Vec3d startVector, @Nonnull Vec3d endVec, boolean includeLiquids) {
    boolean ignoreBlockWithoutBoundingBox = true;
    Vec3d startVec = startVector;

    List<RayTraceResult> result = new ArrayList<RayTraceResult>();

    if (!Double.isNaN(startVec.x) && !Double.isNaN(startVec.y) && !Double.isNaN(startVec.z)) {
      if (!Double.isNaN(endVec.x) && !Double.isNaN(endVec.y) && !Double.isNaN(endVec.z)) {
        int i = MathHelper.floor(endVec.x);
        int j = MathHelper.floor(endVec.y);
        int k = MathHelper.floor(endVec.z);
        int l = MathHelper.floor(startVec.x);
        int i1 = MathHelper.floor(startVec.y);
        int j1 = MathHelper.floor(startVec.z);
        BlockPos blockpos = new BlockPos(l, i1, j1);
        IBlockState iblockstate = world.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB)
            && block.canCollideCheck(iblockstate, includeLiquids)) {
          @Nonnull
          RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, startVec, endVec);
          result.add(raytraceresult);
        }

        int k1 = 200;

        while (k1-- >= 0) {
          if (Double.isNaN(startVec.x) || Double.isNaN(startVec.y) || Double.isNaN(startVec.z)) {
            return new ArrayList<RayTraceResult>();
          }

          if (l == i && i1 == j && j1 == k) {
            return result;
          }

          boolean flag2 = true;
          boolean flag = true;
          boolean flag1 = true;
          double d0 = 999.0D;
          double d1 = 999.0D;
          double d2 = 999.0D;

          if (i > l) {
            d0 = l + 1.0D;
          } else if (i < l) {
            d0 = l + 0.0D;
          } else {
            flag2 = false;
          }

          if (j > i1) {
            d1 = i1 + 1.0D;
          } else if (j < i1) {
            d1 = i1 + 0.0D;
          } else {
            flag = false;
          }

          if (k > j1) {
            d2 = j1 + 1.0D;
          } else if (k < j1) {
            d2 = j1 + 0.0D;
          } else {
            flag1 = false;
          }

          double d3 = 999.0D;
          double d4 = 999.0D;
          double d5 = 999.0D;
          double d6 = endVec.x - startVec.x;
          double d7 = endVec.y - startVec.y;
          double d8 = endVec.z - startVec.z;

          if (flag2) {
            d3 = (d0 - startVec.x) / d6;
          }

          if (flag) {
            d4 = (d1 - startVec.y) / d7;
          }

          if (flag1) {
            d5 = (d2 - startVec.z) / d8;
          }

          if (d3 == -0.0D) {
            d3 = -1.0E-4D;
          }

          if (d4 == -0.0D) {
            d4 = -1.0E-4D;
          }

          if (d5 == -0.0D) {
            d5 = -1.0E-4D;
          }

          EnumFacing enumfacing;

          if (d3 < d4 && d3 < d5) {
            enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
            startVec = new Vec3d(d0, startVec.y + d7 * d3, startVec.z + d8 * d3);
          } else if (d4 < d5) {
            enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
            startVec = new Vec3d(startVec.x + d6 * d4, d1, startVec.z + d8 * d4);
          } else {
            enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
            startVec = new Vec3d(startVec.x + d6 * d5, startVec.y + d7 * d5, d2);
          }

          l = MathHelper.floor(startVec.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
          i1 = MathHelper.floor(startVec.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
          j1 = MathHelper.floor(startVec.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
          blockpos = new BlockPos(l, i1, j1);
          IBlockState iblockstate1 = world.getBlockState(blockpos);
          Block block1 = iblockstate1.getBlock();

          if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL
              || iblockstate1.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) {
            if (block1.canCollideCheck(iblockstate1, includeLiquids)) {
              @Nonnull
              RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(world, blockpos, startVec, endVec);
              result.add(raytraceresult1);
            }
          }
        }

        return result;
      } else {
        return result;
      }
    } else {
      return result;
    }
  }

  public static @Nullable EnumFacing getDirFromOffset(int xOff, int yOff, int zOff) {
    if (xOff != 0 && yOff == 0 && zOff == 0) {
      return xOff < 0 ? EnumFacing.WEST : EnumFacing.EAST;
    }
    if (zOff != 0 && yOff == 0 && xOff == 0) {
      return zOff < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH;
    }
    if (yOff != 0 && xOff == 0 && zOff == 0) {
      return yOff < 0 ? EnumFacing.DOWN : EnumFacing.UP;
    }
    return null;
  }

  public static @Nonnull EnumFacing getFacingFromEntity(@Nonnull EntityLivingBase entity) {
    int heading = MathHelper.floor(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
    switch (heading) {
    case 0:
      return EnumFacing.NORTH;
    case 1:
      return EnumFacing.EAST;
    case 2:
      return EnumFacing.SOUTH;
    case 3:
    default:
      return EnumFacing.WEST;
    }

  }

  public static int getProgressScaled(int scale, @Nonnull IProgressTile tile) {
    return (int) (tile.getProgress() * scale);
  }

  public static void writeFacingToNBT(@Nonnull NBTTagCompound nbtRoot, @Nonnull String name, @Nonnull EnumFacing dir) {
    short val = -1;
    val = (short) dir.ordinal();
    nbtRoot.setShort(name, val);
  }

  public static @Nullable EnumFacing readFacingFromNBT(@Nonnull NBTTagCompound nbtRoot, @Nonnull String name) {
    short val = -1;
    if (nbtRoot.hasKey(name)) {
      val = nbtRoot.getShort(name);
    }
    if (val > 0) {
      return EnumFacing.values()[val];
    }
    return null;
  }

}
