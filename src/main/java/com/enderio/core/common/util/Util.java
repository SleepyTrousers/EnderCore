package com.enderio.core.common.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.vecmath.Vector3d;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class Util {

  public static Block getBlockFromItemId(ItemStack itemId) {
    Item item = itemId.getItem();
    if (item instanceof ItemBlock) {
      return ((ItemBlock) item).block;
    }
    return null;
  }

  public static ItemStack consumeItem(ItemStack stack) {
    if (stack.getItem() instanceof ItemPotion) {
      if (stack.stackSize == 1) {
        return new ItemStack(Items.glass_bottle);
      } else {
        stack.splitStack(1);
        return stack;
      }
    }
    if (stack.stackSize == 1) {
      if (stack.getItem().hasContainerItem(stack)) {
        return stack.getItem().getContainerItem(stack);
      } else {
        return null;
      }
    } else {
      stack.splitStack(1);
      return stack;
    }
  }

  public static void giveExperience(EntityPlayer thePlayer, float experience) {
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
      thePlayer.worldObj.spawnEntityInWorld(new EntityXPOrb(thePlayer.worldObj, thePlayer.posX, thePlayer.posY + 0.5D, thePlayer.posZ + 0.5D, j));
    }
  }

  public static BlockCoord canPlaceItem(ItemStack stack, Block blockToBePlaced, EntityPlayer player, World world, BlockPos pos, EnumFacing side) {

    if (stack == null || stack.stackSize == 0 || blockToBePlaced == null) {
      return null;
    }

    IBlockState bs = world.getBlockState(pos);
    Block block = bs.getBlock();
    if (!block.isReplaceable(world, pos)) {
      pos = pos.offset(side);
    }

    if (!player.canPlayerEdit(pos, side, stack)) {
      return null;
    } else if (pos.getY() == 255 && blockToBePlaced.getMaterial().isSolid()) {
      return null;
    }
    if (world.canBlockBePlaced(blockToBePlaced, pos, false, side, (Entity) null, stack)) {
      return new BlockCoord(pos);
    }
    return null;
  }

  public static EntityItem createDrop(World world, ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    if (stack == null || stack.stackSize <= 0) {
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

  public static void dropItems(World world, ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
    if (stack == null || stack.stackSize <= 0) {
      return;
    }

    EntityItem entityitem = createEntityItem(world, stack, x, y, z, doRandomSpread);
    world.spawnEntityInWorld(entityitem);

  }

  public static EntityItem createEntityItem(World world, ItemStack stack, double x, double y, double z) {
    return createEntityItem(world, stack, x, y, z, true);
  }

  public static EntityItem createEntityItem(World world, ItemStack stack, double x, double y, double z, boolean doRandomSpread) {
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

  public static void dropItems(World world, ItemStack stack, int x, int y, int z, boolean doRandomSpread) {
    if (stack == null || stack.stackSize <= 0) {
      return;
    }

    if (doRandomSpread) {
      float f1 = 0.7F;
      double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
      EntityItem entityitem = new EntityItem(world, x + d, y + d1, z + d2, stack);
      entityitem.setDefaultPickupDelay();
      world.spawnEntityInWorld(entityitem);
    } else {
      EntityItem entityitem = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack);
      entityitem.motionX = 0;
      entityitem.motionY = 0;
      entityitem.motionZ = 0;
      entityitem.setNoPickupDelay();
      world.spawnEntityInWorld(entityitem);
    }

  }

  public static void dropItems(World world, ItemStack[] inventory, int x, int y, int z, boolean doRandomSpread) {
    if (inventory == null) {
      return;
    }
    for (ItemStack stack : inventory) {
      if (stack != null && stack.stackSize > 0) {
        dropItems(world, stack.copy(), x, y, z, doRandomSpread);
      }
    }
  }

  public static void dropItems(World world, IInventory inventory, int x, int y, int z, boolean doRandomSpread) {
    for (int l = 0; l < inventory.getSizeInventory(); ++l) {
      ItemStack items = inventory.getStackInSlot(l);

      if (items != null && items.stackSize > 0) {
        dropItems(world, inventory.getStackInSlot(l).copy(), x, y, z, doRandomSpread);
      }
    }
  }

  public static boolean dumpModObjects(File file) {

    StringBuilder sb = new StringBuilder();
    for (Object key : Block.blockRegistry.getKeys()) {
      if (key != null) {
        sb.append(key.toString());
        sb.append("\n");
      }
    }
    for (Object key : Item.itemRegistry.getKeys()) {
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

  public static boolean dumpOreNames(File file) {

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

  public static ItemStack decrStackSize(IInventory inventory, int slot, int size) {
    ItemStack item = inventory.getStackInSlot(slot);
    if (item != null) {
      if (item.stackSize <= size) {
        ItemStack result = item;
        inventory.setInventorySlotContents(slot, null);
        inventory.markDirty();
        return result;
      }
      ItemStack split = item.splitStack(size);
      if (item.stackSize == 0) {
        inventory.setInventorySlotContents(slot, null);
      }
      inventory.markDirty();
      return split;
    }
    return null;
  }

  public static Vec3 getEyePosition(EntityPlayer player) {
    double y = player.posY;
    y += player.getEyeHeight();
    return new Vec3(player.posX, y, player.posZ);
  }

  public static Vector3d getEyePositionEio(EntityPlayer player) {
    Vector3d res = new Vector3d(player.posX, player.posY, player.posZ);
    res.y += player.getEyeHeight();
    return res;
  }

  public static Vector3d getLookVecEio(EntityPlayer player) {
    Vec3 lv = player.getLookVec();
    return new Vector3d(lv.xCoord, lv.yCoord, lv.zCoord);
  }

  public static boolean isEquipped(EntityPlayer player, Class<? extends Item> class1) {
    if (player == null || player.inventory == null || player.inventory.getCurrentItem() == null) {
      return false;
    }
    // player.inventory.getCurrentItem().getClass().getItem().isAssignableFrom(class1)
    return class1.isAssignableFrom(player.inventory.getCurrentItem().getItem().getClass());
  }

  public static boolean isType(ItemStack stack, Class<?> class1) {
    if (stack == null || class1 == null) {
      return false;
    }
    return class1.isAssignableFrom(stack.getItem().getClass());
  }

  // Code adapted from World.func_147447_a (rayTraceBlocks) to return all
  // collided blocks
  public static List<MovingObjectPosition> raytraceAll(World world, Vec3 startVec, Vec3 endVec, boolean includeLiquids) {

    List<MovingObjectPosition> result = new ArrayList<MovingObjectPosition>();
    boolean p_147447_4_ = false;
    boolean p_147447_5_ = false;

    if (!Double.isNaN(startVec.xCoord) && !Double.isNaN(startVec.yCoord) && !Double.isNaN(startVec.zCoord)) {
      if (!Double.isNaN(endVec.xCoord) && !Double.isNaN(endVec.yCoord) && !Double.isNaN(endVec.zCoord)) {

        int endX = MathHelper.floor_double(endVec.xCoord);
        int endY = MathHelper.floor_double(endVec.yCoord);
        int endZ = MathHelper.floor_double(endVec.zCoord);
        int startX = MathHelper.floor_double(startVec.xCoord);
        int startY = MathHelper.floor_double(startVec.yCoord);
        int startZ = MathHelper.floor_double(startVec.zCoord);
        BlockPos pos = new BlockPos(startX, startY, startZ);
        IBlockState bs = world.getBlockState(pos);
        Block block = bs.getBlock();

        if ((!p_147447_4_ || block.getCollisionBoundingBox(world, pos, bs) != null) && block.canCollideCheck(bs, includeLiquids)) {
          // if ((!p_147447_4_ || block.getCollisionBoundingBoxFromPool(world,
          // l, i1, j1) != null) && block.canCollideCheck(k1, includeLiquids)) {
          MovingObjectPosition movingobjectposition = block.collisionRayTrace(world, new BlockPos(startX, startY, startZ), startVec, endVec);
          if (movingobjectposition != null) {
            result.add(movingobjectposition);
          }
        }

        MovingObjectPosition movingobjectposition2 = null;
        int k1 = 200;

        while (k1-- >= 0) {
          if (Double.isNaN(startVec.xCoord) || Double.isNaN(startVec.yCoord) || Double.isNaN(startVec.zCoord)) {
            return null;
          }

          if (startX == endX && startY == endY && startZ == endZ) {
            if (p_147447_5_) {
              result.add(movingobjectposition2);
            } else {
              return result;
            }
          }

          boolean flag6 = true;
          boolean flag3 = true;
          boolean flag4 = true;
          double d0 = 999.0D;
          double d1 = 999.0D;
          double d2 = 999.0D;

          if (endX > startX) {
            d0 = startX + 1.0D;
          } else if (endX < startX) {
            d0 = startX + 0.0D;
          } else {
            flag6 = false;
          }

          if (endY > startY) {
            d1 = startY + 1.0D;
          } else if (endY < startY) {
            d1 = startY + 0.0D;
          } else {
            flag3 = false;
          }

          if (endZ > startZ) {
            d2 = startZ + 1.0D;
          } else if (endZ < startZ) {
            d2 = startZ + 0.0D;
          } else {
            flag4 = false;
          }

          double d3 = 999.0D;
          double d4 = 999.0D;
          double d5 = 999.0D;
          double d6 = endVec.xCoord - startVec.xCoord;
          double d7 = endVec.yCoord - startVec.yCoord;
          double d8 = endVec.zCoord - startVec.zCoord;

          if (flag6) {
            d3 = (d0 - startVec.xCoord) / d6;
          }
          if (flag3) {
            d4 = (d1 - startVec.yCoord) / d7;
          }
          if (flag4) {
            d5 = (d2 - startVec.zCoord) / d8;
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
            enumfacing = endX > startX ? EnumFacing.WEST : EnumFacing.EAST;
            startVec = new Vec3(d0, startVec.yCoord + d7 * d3, startVec.zCoord + d8 * d3);
          } else if (d4 < d5) {
            enumfacing = endY > startY ? EnumFacing.DOWN : EnumFacing.UP;
            startVec = new Vec3(startVec.xCoord + d6 * d4, d1, startVec.zCoord + d8 * d4);
          } else {
            enumfacing = endZ > startZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
            startVec = new Vec3(startVec.xCoord + d6 * d5, startVec.yCoord + d7 * d5, d2);
          }

          startX = MathHelper.floor_double(startVec.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
          startY = MathHelper.floor_double(startVec.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
          startZ = MathHelper.floor_double(startVec.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
          pos = new BlockPos(startX, startY, startZ);
          IBlockState bs1 = world.getBlockState(pos);
          Block block1 = bs.getBlock();

          if (!p_147447_4_ || block1.getCollisionBoundingBox(world, new BlockPos(startX, startY, startZ), bs1) != null) {
            if (block1.canCollideCheck(bs1, includeLiquids)) {
              MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(world, new BlockPos(startX, startY, startZ), startVec, endVec);
              if (movingobjectposition1 != null) {
                result.add(movingobjectposition1);
              }
            } else {
              movingobjectposition2 = new MovingObjectPosition(startVec, enumfacing, pos);
            }
          }
        }
        if (p_147447_5_) {
          result.add(movingobjectposition2);
        } else {
          return result;
        }
      } else {
        return result;
      }
    } else {
      return result;
    }
    return result;
  }

  public static EnumFacing getDirFromOffset(int xOff, int yOff, int zOff) {
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

  public static int getProgressScaled(int scale, IProgressTile tile) {
    return (int) (tile.getProgress() * scale);
  }

  // copied from WAILA source to avoid API dependency
  public static String WailaStyle = "\u00A4";
  public static String WailaIcon = "\u00A5";
  public static String TAB = WailaStyle + WailaStyle + "a";
  public static String ALIGNRIGHT = WailaStyle + WailaStyle + "b";
  public static String ALIGNCENTER = WailaStyle + WailaStyle + "c";
  public static String HEART = WailaStyle + WailaIcon + "a";
  public static String HHEART = WailaStyle + WailaIcon + "b";
  public static String EHEART = WailaStyle + WailaIcon + "c";

}
