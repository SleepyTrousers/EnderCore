package com.enderio.core.common;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.FluidUtil;
import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;

public abstract class BlockEnder<T extends TileEntityBase> extends Block {

  protected final @Nullable Class<? extends T> teClass;

  protected BlockEnder(@Nullable Class<? extends T> teClass) {
    this(teClass, new Material(MapColor.IRON));
  }

  protected BlockEnder(@Nullable Class<? extends T> teClass, @Nonnull Material mat) {
    super(mat);
    this.teClass = teClass;

    setHardness(0.5F);
    setSoundType(SoundType.METAL);
    setHarvestLevel("pickaxe", 0);
  }

  @Override
  public boolean hasTileEntity(@Nonnull IBlockState state) {
    return teClass != null;
  }

  @Override
  public @Nonnull TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
    if (teClass != null) {
      try {
        T te = teClass.newInstance();
        te.setWorldCreate(world);
        te.init();
        return te;
      } catch (Exception e) {
        throw new RuntimeException("Could not create tile entity for block " + getLocalizedName() + " for class " + teClass, e);
      }
    }
    throw new RuntimeException(
        "Cannot create a TileEntity for a block that doesn't have a TileEntity. This is not a problem with EnderCore, this is caused by the caller.");
  }

  /* Subclass Helpers */

  @Override
  public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn,
      @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
    if (playerIn.isSneaking()) {
      return false;
    }

    TileEntity te = getTileEntity(worldIn, pos);
    if (te instanceof ITankAccess) {
      if (FluidUtil.fillInternalTankFromPlayerHandItem(worldIn, pos, playerIn, hand, (ITankAccess) te)) {
        return true;
      }
      if (FluidUtil.fillPlayerHandItemFromInternalTank(worldIn, pos, playerIn, hand, (ITankAccess) te)) {
        return true;
      }
    }

    return openGui(worldIn, pos, playerIn, side);
  }

  protected boolean openGui(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer entityPlayer, @Nonnull EnumFacing side) {
    return false;
  }

  public boolean doNormalDrops(IBlockAccess world, BlockPos pos) {
    return true;
  }

  @Override
  public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
    if (willHarvest) {
      return true;
    }
    return super.removedByPlayer(state, world, pos, player, willHarvest);
  }

  @Override
  public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te,
      @Nonnull ItemStack stack) {
    super.harvestBlock(worldIn, player, pos, state, te, stack);
    worldIn.setBlockToAir(pos);
  }

  @Override
  public @Nonnull List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
    if (doNormalDrops(world, pos)) {
      return super.getDrops(world, pos, state, fortune);
    }
    return Lists.newArrayList(getNBTDrop(world, pos, getTileEntity(world, pos)));
  }

  public ItemStack getNBTDrop(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable T te) {
    int meta = damageDropped(world.getBlockState(pos));
    ItemStack itemStack = new ItemStack(this, 1, meta);
    processDrop(world, pos, te, itemStack);
    return itemStack;
  }

  protected void processDrop(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable T te, @Nonnull ItemStack drop) {
    if (te != null) {
      final NBTTagCompound tag = new NBTTagCompound();
      te.writeCustomNBT(NBTAction.ITEM, tag);
      if (!tag.hasNoTags()) {
        drop.setTagCompound(tag);
      }
    }
  }

  @Override
  public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer,
      @Nonnull ItemStack stack) {
    if (stack.hasTagCompound()) {
      T te = getTileEntity(worldIn, pos);
      if (te != null) {
        te.readCustomNBT(NBTAction.ITEM, NullHelper.notnullM(stack.getTagCompound(), "tag compound vanished"));
      }
    }
  }

  /**
   * Tries to load this block's TileEntity if it exists. Will create the TileEntity if it doesn't yet exist.
   * <p>
   * <strong>This will crash if used in any other thread than the main (client or server) thread!</strong>
   *
   */
  protected @Nullable T getTileEntity(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    final Class<? extends T> teClass2 = teClass;
    if (teClass2 != null) {
      TileEntity te = world.getTileEntity(pos);
      if (teClass2.isInstance(te)) {
        return teClass2.cast(te);
      }
    }
    return null;
  }

  /**
   * Tries to load this block's TileEntity if it exists. Will not create the TileEntity when used in a render thread with the correct IBlockAccess.
   *
   */
  protected @Nullable T getTileEntitySafe(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    if (world instanceof ChunkCache) {
      final Class<? extends T> teClass2 = teClass;
      if (teClass2 != null) {
        TileEntity te = ((ChunkCache) world).getTileEntity(pos, EnumCreateEntityType.CHECK);
        if (teClass2.isInstance(te)) {
          return teClass2.cast(te);
        }
      }
      return null;
    } else {
      return getTileEntity(world, pos);
    }
  }

  /**
   * Tries to load any block's TileEntity if it exists. Will not create the TileEntity when used in a render thread with the correct IBlockAccess. Will not
   * cause chunk loads.
   *
   */
  public static @Nullable TileEntity getAnyTileEntitySafe(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    return getAnyTileEntitySafe(world, pos, TileEntity.class);
  }

  /**
   * Tries to load any block's TileEntity if it exists. Will not create the TileEntity when used in a render thread with the correct IBlockAccess. Will not
   * cause chunk loads. Also works with interfaces as the class parameter.
   *
   */
  @SuppressWarnings("unchecked")
  public static @Nullable <Q> Q getAnyTileEntitySafe(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, Class<Q> teClass) {
    TileEntity te = null;
    if (world instanceof ChunkCache) {
      te = ((ChunkCache) world).getTileEntity(pos, EnumCreateEntityType.CHECK);
    } else if (world instanceof World) {
      if (((World) world).isBlockLoaded(pos)) {
        te = world.getTileEntity(pos);
      }
    } else {
      te = world.getTileEntity(pos);
    }
    if (teClass == null) {
      return (Q) te;
    }
    if (teClass.isInstance(te)) {
      return teClass.cast(te);
    }
    return null;
  }

  /**
   * Tries to load any block's TileEntity if it exists. Not suitable for tasks outside the main thread. Also works with interfaces as the class parameter.
   *
   */
  @SuppressWarnings("unchecked")
  public static @Nullable <Q> Q getAnyTileEntity(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, Class<Q> teClass) {
    TileEntity te = world.getTileEntity(pos);
    if (teClass == null) {
      return (Q) te;
    }
    if (teClass.isInstance(te)) {
      return teClass.cast(te);
    }
    return null;
  }

  protected boolean shouldDoWorkThisTick(@Nonnull World world, @Nonnull BlockPos pos, int interval) {
    T te = getTileEntity(world, pos);
    if (te == null) {
      return world.getTotalWorldTime() % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval);
    }
  }

  protected boolean shouldDoWorkThisTick(@Nonnull World world, @Nonnull BlockPos pos, int interval, int offset) {
    T te = getTileEntity(world, pos);
    if (te == null) {
      return (world.getTotalWorldTime() + offset) % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval, offset);
    }
  }

  public Class<? extends T> getTeClass() {
    return teClass;
  }

  // wrapper because vanilla null-annotations are wrong
  @SuppressWarnings("null")
  @Override
  public @Nonnull Block setCreativeTab(@Nullable CreativeTabs tab) {
    return super.setCreativeTab(tab);
  }

}