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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
        return new ItemStack(Items.GLASS_BOTTLE);
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

  public static BlockCoord canPlaceItem(ItemStack stack, IBlockState blockToBePlaced, EntityPlayer player, World world, BlockPos pos, EnumFacing side) {

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
    if (world.canBlockBePlaced(blockToBePlaced.getBlock(), pos, false, side, player, stack)) {      
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

  public static void dropItems(World world, ItemStack stack, BlockPos pos, boolean doRandomSpread) {
    dropItems(world,stack,pos.getX(),pos.getY(),pos.getZ(), doRandomSpread);
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
        dropItems(world, items.copy(), x, y, z, doRandomSpread);
      }
    }
  }

  public static boolean dumpModObjects(File file) {

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

  public static Vec3d getEyePosition(EntityPlayer player) {
    double y = player.posY;
    y += player.getEyeHeight();
    return new Vec3d(player.posX, y, player.posZ);
  }

  public static Vector3d getEyePositionEio(EntityPlayer player) {
    Vector3d res = new Vector3d(player.posX, player.posY, player.posZ);
    res.y += player.getEyeHeight();
    return res;
  }

  public static Vector3d getLookVecEio(EntityPlayer player) {
    Vec3d lv = player.getLookVec();
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

  // Code adapted from World.rayTraceBlocks to return all
  // collided blocks
  public static List<RayTraceResult> raytraceAll(World world, Vec3d startVec, Vec3d endVec, boolean includeLiquids) {    
    boolean ignoreBlockWithoutBoundingBox = true;
    
    List<RayTraceResult> result = new ArrayList<RayTraceResult>();
    
    if (!Double.isNaN(startVec.xCoord) && !Double.isNaN(startVec.yCoord) && !Double.isNaN(startVec.zCoord)) {
      if (!Double.isNaN(endVec.xCoord) && !Double.isNaN(endVec.yCoord) && !Double.isNaN(endVec.zCoord)) {
        int i = MathHelper.floor_double(endVec.xCoord);
        int j = MathHelper.floor_double(endVec.yCoord);
        int k = MathHelper.floor_double(endVec.zCoord);
        int l = MathHelper.floor_double(startVec.xCoord);
        int i1 = MathHelper.floor_double(startVec.yCoord);
        int j1 = MathHelper.floor_double(startVec.zCoord);
        BlockPos blockpos = new BlockPos(l, i1, j1);
        IBlockState iblockstate = world.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB)
            && block.canCollideCheck(iblockstate, includeLiquids)) {
          RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, startVec, endVec);

          if (raytraceresult != null) {
            result.add(raytraceresult);
          }
        }

        int k1 = 200;

        while (k1-- >= 0) {
          if (Double.isNaN(startVec.xCoord) || Double.isNaN(startVec.yCoord) || Double.isNaN(startVec.zCoord)) {
            return null;
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
            d0 = (double) l + 1.0D;
          } else if (i < l) {
            d0 = (double) l + 0.0D;
          } else {
            flag2 = false;
          }

          if (j > i1) {
            d1 = (double) i1 + 1.0D;
          } else if (j < i1) {
            d1 = (double) i1 + 0.0D;
          } else {
            flag = false;
          }

          if (k > j1) {
            d2 = (double) j1 + 1.0D;
          } else if (k < j1) {
            d2 = (double) j1 + 0.0D;
          } else {
            flag1 = false;
          }

          double d3 = 999.0D;
          double d4 = 999.0D;
          double d5 = 999.0D;
          double d6 = endVec.xCoord - startVec.xCoord;
          double d7 = endVec.yCoord - startVec.yCoord;
          double d8 = endVec.zCoord - startVec.zCoord;

          if (flag2) {
            d3 = (d0 - startVec.xCoord) / d6;
          }

          if (flag) {
            d4 = (d1 - startVec.yCoord) / d7;
          }

          if (flag1) {
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
            enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
            startVec = new Vec3d(d0, startVec.yCoord + d7 * d3, startVec.zCoord + d8 * d3);
          } else if (d4 < d5) {
            enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
            startVec = new Vec3d(startVec.xCoord + d6 * d4, d1, startVec.zCoord + d8 * d4);
          } else {
            enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
            startVec = new Vec3d(startVec.xCoord + d6 * d5, startVec.yCoord + d7 * d5, d2);
          }

          l = MathHelper.floor_double(startVec.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
          i1 = MathHelper.floor_double(startVec.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
          j1 = MathHelper.floor_double(startVec.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
          blockpos = new BlockPos(l, i1, j1);
          IBlockState iblockstate1 = world.getBlockState(blockpos);
          Block block1 = iblockstate1.getBlock();

          if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL
              || iblockstate1.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) {
            if (block1.canCollideCheck(iblockstate1, includeLiquids)) {
              RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(world, blockpos, startVec, endVec);

              if (raytraceresult1 != null) {
                result.add(raytraceresult1);
              }
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

  public static void writeFacingToNBT(NBTTagCompound nbtRoot, String name, EnumFacing dir) {
    if(nbtRoot == null || name == null) {
      return;
    }
    short val = -1;
    if(dir != null) {
      val = (short)dir.ordinal();
    }
    nbtRoot.setShort(name, val);        
  }
  
  public static EnumFacing readFacingFromNBT(NBTTagCompound nbtRoot, String name) {
    if(nbtRoot == null || name == null) {
      return null;
    }
    short val = -1;
    if(nbtRoot.hasKey(name)) {
      val = nbtRoot.getShort(name);
    }
    if(val > 0) {
      return EnumFacing.values()[val];
    }
    return null;        
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
