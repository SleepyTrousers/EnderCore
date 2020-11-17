package com.enderio.core.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.FluidUtil;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ToolType;

import java.util.List;

public abstract class BlockEnder<T extends TileEntityBase> extends Block {

  protected final @Nullable Class<? extends T> teClass;

  protected BlockEnder(@Nullable Class<? extends T> teClass) {
    this(teClass, AbstractBlock.Properties.create(Material.IRON));
  }

  protected BlockEnder(@Nullable Class<? extends T> teClass, @Nonnull Material mat) {
    this(teClass, AbstractBlock.Properties.create(mat, mat.getColor()));
  }

  protected BlockEnder(@Nullable Class<? extends T> teClass, @Nonnull Material mat, MaterialColor matColor) {
    this(teClass, AbstractBlock.Properties.create(mat, matColor));
  }

  protected BlockEnder(@Nullable Class<? extends T> teClass, AbstractBlock.Properties properties) {
    super(properties.hardnessAndResistance(0.5f).sound(SoundType.METAL).harvestLevel(0).harvestTool(ToolType.PICKAXE));
    this.teClass = teClass;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return teClass != null;
  }

  @Override
  public PushReaction getPushReaction(BlockState state) {
    // Some mods coremod vanilla to ignore this condition, so let's try to enforce it.
    // If this doesn't work, we need code to blow up the block when it detects it was moved...
    return teClass != null ? PushReaction.BLOCK : super.getPushReaction(state);
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    if (teClass != null) {
      try {
        T te = teClass.newInstance();
        // Seems to be done for us now: https://github.com/MinecraftForge/MinecraftForge/blob/f54998a6b7f2815798f30ee1674b22f52e003abc/patches/minecraft/net/minecraft/world/World.java.patch#L145
//        te.setWorldCreate(world);
        te.init();
        return te;
      } catch (Exception e) {
        throw new RuntimeException("Could not create tile entity for block " + getTranslationKey() + " for class " + teClass, e);
      }
    }
    throw new RuntimeException(
            "Cannot create a TileEntity for a block that doesn't have a TileEntity. This is not a problem with EnderCore, this is caused by the caller.");
  }


  /* Subclass Helpers */

  @Override
  public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
    if (player.isSneaking()) {
      return ActionResultType.FAIL;
    }

    TileEntity te = getTileEntity(worldIn, pos);
    if (te instanceof ITankAccess) {
      if (FluidUtil.fillInternalTankFromPlayerHandItem(worldIn, pos, player, handIn, (ITankAccess) te)) {
        return ActionResultType.PASS;
      }
      if (FluidUtil.fillPlayerHandItemFromInternalTank(worldIn, pos, player, handIn, (ITankAccess) te)) {
        return ActionResultType.PASS;
      }
    }

    return openGui(worldIn, pos, player, hit.getFace());
  }


  protected ActionResultType openGui(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity entityPlayer, @Nonnull Direction side) {
    return ActionResultType.FAIL;
  }

  @Override
  public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
    if (willHarvest) {
      return true;
    }
    return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
  }

  @Override
  public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
    super.harvestBlock(worldIn, player, pos, state, te, stack);
    worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
  }

  // TODO: I think drops are now handled in JSON
//  @Override
//  public final void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull BlockAccess world, @Nonnull BlockPos pos, @Nonnull BlockState state,
//      int fortune) {
//    final T te = getTileEntity(world, pos);
//    final ItemStack drop = getNBTDrop(world, pos, state, fortune, te);
//    if (drop != null) {
//      drops.add(drop);
//    }
//    getExtraDrops(drops, world, pos, state, fortune, te);
//  }

//  @Override
//  public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
//    if (player.world.isRemote && player.isCreative() && Screen.hasControlDown()) {
//      ItemStack nbtDrop = getNBTDrop(world, pos, state, 0, getTileEntity(world, pos));
//      if (nbtDrop != null) {
//        return nbtDrop;
//      }
//    }
//    return processPickBlock(state, target, world, pos, player, super.getPickBlock(state, target, world, pos, player));
//  }


//  protected @Nonnull ItemStack processPickBlock(@Nonnull BlockState state, @Nonnull RayTraceResult target, @Nonnull IBlockReader world, @Nonnull BlockPos pos,
//      @Nonnull PlayerEntity player, @Nonnull ItemStack pickBlock) {
//    return pickBlock;
//  }

  // See above todo
//  public @Nullable ItemStack getNBTDrop(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, int fortune, @Nullable T te) {
//    ItemStack itemStack = new ItemStack(this, 1, damageDropped(state));
//    processDrop(world, pos, te, itemStack);
//    return itemStack;
//  }

  protected final void processDrop(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nullable T te, @Nonnull ItemStack drop) {
    if (te != null) {
      te.writeCustomNBT(drop);
    }
  }

  public void getExtraDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, int fortune,
      @Nullable T te) {
  }

  @Override
  public final void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull LivingEntity placer,
      @Nonnull ItemStack stack) {
    onBlockPlaced(worldIn, pos, state, placer, stack);
    T te = getTileEntity(worldIn, pos);
    if (te != null) {
      te.readCustomNBT(stack);
      onBlockPlaced(worldIn, pos, state, placer, te);
    }
  }

  public void onBlockPlaced(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull LivingEntity placer,
      @Nonnull ItemStack stack) {
  }

  public void onBlockPlaced(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull LivingEntity placer, @Nonnull T te) {
  }

  /**
   * Tries to load this block's TileEntity if it exists. Will create the TileEntity if it doesn't yet exist.
   * <p>
   * <strong>This will crash if used in any other thread than the main (client or server) thread!</strong>
   *
   */
  protected @Nullable T getTileEntity(@Nonnull IBlockReader world, @Nonnull BlockPos pos) {
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
  protected @Nullable T getTileEntitySafe(@Nonnull IBlockReader world, @Nonnull BlockPos pos) {
    if (world instanceof ChunkRenderCache) {
      final Class<? extends T> teClass2 = teClass;
      if (teClass2 != null) {
        TileEntity te = ((ChunkRenderCache) world).getTileEntity(pos, Chunk.CreateEntityType.CHECK);
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
  public static @Nullable TileEntity getAnyTileEntitySafe(@Nonnull IBlockReader world, @Nonnull BlockPos pos) {
    return getAnyTileEntitySafe(world, pos, TileEntity.class);
  }

  /**
   * Tries to load any block's TileEntity if it exists. Will not create the TileEntity when used in a render thread with the correct IBlockAccess. Will not
   * cause chunk loads. Also works with interfaces as the class parameter.
   *
   */
  @SuppressWarnings("unchecked")
  public static @Nullable <Q> Q getAnyTileEntitySafe(@Nonnull IBlockReader world, @Nonnull BlockPos pos, Class<Q> teClass) {
    TileEntity te = null;
    if (world instanceof ChunkRenderCache) {
      te = ((ChunkRenderCache) world).getTileEntity(pos, Chunk.CreateEntityType.CHECK);
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
  public static @Nullable <Q> Q getAnyTileEntity(@Nonnull IBlockReader world, @Nonnull BlockPos pos, Class<Q> teClass) {
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
      return world.getGameTime() % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval);
    }
  }

  protected boolean shouldDoWorkThisTick(@Nonnull World world, @Nonnull BlockPos pos, int interval, int offset) {
    T te = getTileEntity(world, pos);
    if (te == null) {
      return (world.getGameTime() + offset) % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval, offset);
    }
  }

  public Class<? extends T> getTeClass() {
    return teClass;
  }

//  // wrapper because vanilla null-annotations are wrong
//  @SuppressWarnings("null")
//  @Override
//  public @Nonnull Block setCreativeTab(@Nullable CreativeTabs tab) {
//    return super.setCreativeTab(tab);
//  }

//  public void setShape(IShape<T> shape) {
//    this.shape = shape;
//  }


  // TODO: This shit defo needs redoing, so for now I'm commenting it out

//  @Override
//  public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
//    if (shape != null) {
//      T te = getTileEntitySafe(worldIn, pos);
//      if (te != null) {
//        return shape.getBlockFaceShape(worldIn, state, pos, face, te);
//      } else {
//        return shape.getBlockFaceShape(worldIn, state, pos, face);
//      }
//    }
//    return super.getShape(state, worldIn, pos, context);
//  }
//
//  @Override
//  public final @Nonnull BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos,
//      @Nonnull EnumFacing face) {
//    if (shape != null) {
//      T te = getTileEntitySafe(worldIn, pos);
//      if (te != null) {
//        return shape.getBlockFaceShape(worldIn, state, pos, face, te);
//      } else {
//        return shape.getBlockFaceShape(worldIn, state, pos, face);
//      }
//    }
//    return super.getBlockFaceShape(worldIn, state, pos, face);
//  }
//
//  private IShape<T> shape = null;
//
//  public static interface IShape<T> {
//    @Nonnull
//    BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face);
//
//    default @Nonnull BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos,
//        @Nonnull EnumFacing face, @Nonnull T te) {
//      return getBlockFaceShape(worldIn, state, pos, face);
//    }
//  }
//
//  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape allFaces) {
//    return new IShape<T>() {
//      @Override
//      @Nonnull
//      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
//        return allFaces;
//      }
//    };
//  }
//
//  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape upDown, @Nonnull BlockFaceShape allSides) {
//    return new IShape<T>() {
//      @Override
//      @Nonnull
//      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
//        return face == EnumFacing.UP || face == EnumFacing.DOWN ? upDown : allSides;
//      }
//    };
//  }
//
//  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape down, @Nonnull BlockFaceShape up, @Nonnull BlockFaceShape allSides) {
//    return new IShape<T>() {
//      @Override
//      @Nonnull
//      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
//        return face == EnumFacing.UP ? up : face == EnumFacing.DOWN ? down : allSides;
//      }
//    };
//  }
//
//  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape... faces) {
//    return new IShape<T>() {
//      @SuppressWarnings("null")
//      @Override
//      @Nonnull
//      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
//        return faces[face.ordinal()];
//      }
//    };
//  }

}