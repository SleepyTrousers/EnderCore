package com.enderio.core.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.FluidUtil;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;

public abstract class BlockEnder<T extends TileEntityBase> extends Block {

  protected final @Nullable Class<? extends T> teClass;

  protected BlockEnder(@Nullable Class<? extends T> teClass) {
    this(teClass, new Material(MapColor.IRON), MapColor.IRON);
  }

  protected BlockEnder(@Nullable Class<? extends T> teClass, @Nonnull Material mat) {
    this(teClass, mat, mat.getMaterialMapColor());
  }

  protected BlockEnder(@Nullable Class<? extends T> teClass, @Nonnull Material mat, MapColor mapColor) {
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
  public @Nonnull EnumPushReaction getMobilityFlag(@Nonnull IBlockState state) {
    // Some mods coremod vanilla to ignore this condition, so let's try to enforce it.
    // If this doesn't work, we need code to blow up the block when it detects it was moved...
    return teClass != null ? EnumPushReaction.BLOCK : super.getMobilityFlag(state);
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
  public final void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state,
      int fortune) {
    final T te = getTileEntity(world, pos);
    final ItemStack drop = getNBTDrop(world, pos, state, fortune, te);
    if (drop != null) {
      drops.add(drop);
    }
    getExtraDrops(drops, world, pos, state, fortune, te);
  }

  /**
   * override {@link #processPickBlock(IBlockState, RayTraceResult, World, BlockPos, EntityPlayer, ItemStack)} instead if possible
   */
  // TODO: 1.13 add 'final'
  @Override
  public @Nonnull ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos,
      @Nonnull EntityPlayer player) {
    if (player.capabilities.isCreativeMode && GuiScreen.isCtrlKeyDown()) {
      ItemStack nbtDrop = getNBTDrop(world, pos, state, 0, getTileEntity(world, pos));
      if (nbtDrop != null) {
        return nbtDrop;
      }
    }
    return processPickBlock(state, target, world, pos, player, super.getPickBlock(state, target, world, pos, player));
  }

  protected @Nonnull ItemStack processPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos,
      @Nonnull EntityPlayer player, @Nonnull ItemStack pickBlock) {
    return pickBlock;
  }

  public @Nullable ItemStack getNBTDrop(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune, @Nullable T te) {
    ItemStack itemStack = new ItemStack(this, 1, damageDropped(state));
    processDrop(world, pos, te, itemStack);
    return itemStack;
  }

  protected final void processDrop(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable T te, @Nonnull ItemStack drop) {
    if (te != null) {
      te.writeCustomNBT(drop);
    }
  }

  public void getExtraDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune,
      @Nullable T te) {
  }

  @Override
  public final void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer,
      @Nonnull ItemStack stack) {
    onBlockPlaced(worldIn, pos, state, placer, stack);
    T te = getTileEntity(worldIn, pos);
    if (te != null) {
      te.readCustomNBT(stack);
      onBlockPlaced(worldIn, pos, state, placer, te);
    }
  }

  public void onBlockPlaced(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer,
      @Nonnull ItemStack stack) {
  }

  public void onBlockPlaced(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull T te) {
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

  public void setShape(IShape<T> shape) {
    this.shape = shape;
  }

  @Override
  public final @Nonnull BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos,
      @Nonnull EnumFacing face) {
    if (shape != null) {
      T te = getTileEntitySafe(worldIn, pos);
      if (te != null) {
        return shape.getBlockFaceShape(worldIn, state, pos, face, te);
      } else {
        return shape.getBlockFaceShape(worldIn, state, pos, face);
      }
    }
    return super.getBlockFaceShape(worldIn, state, pos, face);
  }

  private IShape<T> shape = null;

  public static interface IShape<T> {
    @Nonnull
    BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face);

    default @Nonnull BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos,
        @Nonnull EnumFacing face, @Nonnull T te) {
      return getBlockFaceShape(worldIn, state, pos, face);
    }
  }

  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape allFaces) {
    return new IShape<T>() {
      @Override
      @Nonnull
      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return allFaces;
      }
    };
  }

  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape upDown, @Nonnull BlockFaceShape allSides) {
    return new IShape<T>() {
      @Override
      @Nonnull
      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return face == EnumFacing.UP || face == EnumFacing.DOWN ? upDown : allSides;
      }
    };
  }

  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape down, @Nonnull BlockFaceShape up, @Nonnull BlockFaceShape allSides) {
    return new IShape<T>() {
      @Override
      @Nonnull
      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return face == EnumFacing.UP ? up : face == EnumFacing.DOWN ? down : allSides;
      }
    };
  }

  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape... faces) {
    return new IShape<T>() {
      @SuppressWarnings("null")
      @Override
      @Nonnull
      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return faces[face.ordinal()];
      }
    };
  }

}