package com.enderio.core.common.fluid;

import com.enderio.core.common.util.NullHelper;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

public abstract class EnderFlowingFluid extends ForgeFlowingFluid {
  private final boolean flowUpward;

  protected EnderFlowingFluid(Properties properties) {
    super(properties);
    flowUpward = getAttributes().getDensity() < 0;
  }

  @Override protected boolean canDisplace(FluidState state, IBlockReader world, BlockPos pos, Fluid fluidIn, Direction direction) {
    BlockState bs = NullHelper.notnullF(world, "canDisplace() called without world")
        .getBlockState(NullHelper.notnullF(pos, "canDisplace() called without pos"));
    if (bs.getMaterial().isLiquid()) {
      return false;
    }
    return super.canDisplace(state, world, pos, fluidIn, direction);
  }

  @Override protected void flowAround(IWorld worldIn, BlockPos pos, FluidState stateIn) {
    if (!flowUpward) {
      super.flowAround(worldIn, pos, stateIn);
    } else {
      if (!stateIn.isEmpty()) {
        BlockState blockstate = worldIn.getBlockState(pos);
        BlockPos blockpos = pos.up();
        BlockState blockstate1 = worldIn.getBlockState(blockpos);
        FluidState fluidstate = this.calculateCorrectFlowingState(worldIn, blockpos, blockstate1);
        if (this.canFlow(worldIn, pos, blockstate, Direction.UP, blockpos, blockstate1, worldIn.getFluidState(blockpos), fluidstate.getFluid())) {
          this.flowInto(worldIn, blockpos, blockstate1, Direction.UP, fluidstate);
        }
      }
    }
  }

  // Same as super but with upward flows.
  @Override public Vector3d getFlow(IBlockReader blockReader, BlockPos pos, FluidState fluidState) {
    double d0 = 0.0D;
    double d1 = 0.0D;
    BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      blockpos$mutable.setAndMove(pos, direction);
      FluidState fluidstate = blockReader.getFluidState(blockpos$mutable);
      if (this.isSameOrEmpty(fluidstate)) {
        float f = fluidstate.getHeight();
        float f1 = 0.0F;
        if (f == 0.0F) {
          if (!blockReader.getBlockState(blockpos$mutable).getMaterial().blocksMovement()) {
            BlockPos blockpos = blockpos$mutable.down();
            FluidState fluidstate1 = blockReader.getFluidState(blockpos);
            if (this.isSameOrEmpty(fluidstate1)) {
              f = fluidstate1.getHeight();
              if (f > 0.0F) {
                f1 = fluidState.getHeight() - (f - 0.8888889F);
              }
            }
          }
        } else if (f > 0.0F) {
          f1 = fluidState.getHeight() - f;
        }

        if (f1 != 0.0F) {
          d0 += (double) ((float) direction.getXOffset() * f1);
          d1 += (double) ((float) direction.getZOffset() * f1);
        }
      }
    }

    Vector3d vector3d = new Vector3d(d0, 0.0D, d1);
    if (fluidState.get(FALLING)) {
      for (Direction direction1 : Direction.Plane.HORIZONTAL) {
        blockpos$mutable.setAndMove(pos, direction1);
        if (this.causesDownwardCurrent(blockReader, blockpos$mutable, direction1) || this
            .causesDownwardCurrent(blockReader, blockpos$mutable.up(), direction1)) {
          vector3d = vector3d.normalize().add(0.0D, flowUpward ? 6.0D : -6.0D, 0.0D);
          break;
        }
      }
    }

    return vector3d.normalize();
  }

  // The same as the original, except it allows upward flows.
  @Override protected FluidState calculateCorrectFlowingState(IWorldReader worldIn, BlockPos pos, BlockState blockStateIn) {
    int i = 0;
    int j = 0;

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      BlockPos blockpos = pos.offset(direction);
      BlockState blockstate = worldIn.getBlockState(blockpos);
      FluidState fluidstate = blockstate.getFluidState();
      if (fluidstate.getFluid().isEquivalentTo(this) && this.doesSideHaveHoles(direction, worldIn, pos, blockStateIn, blockpos, blockstate)) {
        if (fluidstate.isSource() && net.minecraftforge.event.ForgeEventFactory
            .canCreateFluidSource(worldIn, blockpos, blockstate, this.canSourcesMultiply())) {
          ++j;
        }

        i = Math.max(i, fluidstate.getLevel());
      }
    }

    if (j >= 2) {
      BlockState blockstate1 = worldIn.getBlockState(flowUpward ? pos.up() : pos.down());
      FluidState fluidstate1 = blockstate1.getFluidState();
      if (blockstate1.getMaterial().isSolid() || this.isSameAs(fluidstate1)) {
        return this.getStillFluidState(false);
      }
    }

    BlockPos blockpos1 = flowUpward ? pos.down() : pos.up();
    BlockState blockstate2 = worldIn.getBlockState(blockpos1);
    FluidState fluidstate2 = blockstate2.getFluidState();
    if (!fluidstate2.isEmpty() && fluidstate2.getFluid().isEquivalentTo(this) && this
        .doesSideHaveHoles(flowUpward ? Direction.DOWN : Direction.UP, worldIn, pos, blockStateIn, blockpos1, blockstate2)) {
      return this.getFlowingFluidState(8, true);
    } else {
      int k = i - this.getLevelDecreasePerBlock(worldIn);
      return k <= 0 ? Fluids.EMPTY.getDefaultState() : this.getFlowingFluidState(k, false);
    }
  }

  // Here comes some hateful private copying!!!!

  private static final Field field_212756_e_ref;

  static {
    field_212756_e_ref = ObfuscationReflectionHelper.findField(FlowingFluid.class, "field_212756_e");
  }

  private boolean doesSideHaveHoles(Direction p_212751_1_, IBlockReader p_212751_2_, BlockPos p_212751_3_, BlockState p_212751_4_, BlockPos p_212751_5_,
      BlockState p_212751_6_) {
    Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey> object2bytelinkedopenhashmap;
    if (!p_212751_4_.getBlock().isVariableOpacity() && !p_212751_6_.getBlock().isVariableOpacity()) {
      try {
        object2bytelinkedopenhashmap = ((ThreadLocal<Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey>>) field_212756_e_ref.get(this)).get();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        return false;
      }
    } else {
      object2bytelinkedopenhashmap = null;
    }

    Block.RenderSideCacheKey block$rendersidecachekey;
    if (object2bytelinkedopenhashmap != null) {
      block$rendersidecachekey = new Block.RenderSideCacheKey(p_212751_4_, p_212751_6_, p_212751_1_);
      byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$rendersidecachekey);
      if (b0 != 127) {
        return b0 != 0;
      }
    } else {
      block$rendersidecachekey = null;
    }

    VoxelShape voxelshape1 = p_212751_4_.getCollisionShape(p_212751_2_, p_212751_3_);
    VoxelShape voxelshape = p_212751_6_.getCollisionShape(p_212751_2_, p_212751_5_);
    boolean flag = !VoxelShapes.doAdjacentCubeSidesFillSquare(voxelshape1, voxelshape, p_212751_1_);
    if (object2bytelinkedopenhashmap != null) {
      if (object2bytelinkedopenhashmap.size() == 200) {
        object2bytelinkedopenhashmap.removeLastByte();
      }

      object2bytelinkedopenhashmap.putAndMoveToFirst(block$rendersidecachekey, (byte) (flag ? 1 : 0));
    }

    return flag;
  }

  private boolean isSameOrEmpty(FluidState state) {
    return state.isEmpty() || state.getFluid().isEquivalentTo(this);
  }

  private boolean isSameAs(FluidState stateIn) {
    return stateIn.getFluid().isEquivalentTo(this) && stateIn.isSource();
  }

  // ROVER: Added this so we no longer need to override EnderFluidBlock.
  public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {

  }

  public static class Flowing extends EnderFlowingFluid {
    public Flowing(EnderFlowingFluid.Properties properties) {
      super(properties);
      setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 7));
    }

    protected void fillStateContainer(StateContainer.Builder<Fluid, FluidState> builder) {
      super.fillStateContainer(builder);
      builder.add(LEVEL_1_8);
    }

    public int getLevel(FluidState state) {
      return state.get(LEVEL_1_8);
    }

    public boolean isSource(FluidState state) {
      return false;
    }
  }

  public static class Source extends EnderFlowingFluid {
    public Source(EnderFlowingFluid.Properties properties) {
      super(properties);
    }

    public int getLevel(FluidState state) {
      return 8;
    }

    public boolean isSource(FluidState state) {
      return true;
    }
  }

  /**
   * Wrapper around ForgeFlowingFluid.Properties.
   * This basically grabs the viscosity and sets tick speed to viscosity / 200 as default.
   */
  public static class Properties extends ForgeFlowingFluid.Properties {
    private static final Field viscosity;

    static {
      viscosity = ObfuscationReflectionHelper.findField(FluidAttributes.Builder.class, "viscosity");
    }

    public Properties(Supplier<? extends EnderFlowingFluid> still, Supplier<? extends EnderFlowingFluid> flowing, FluidAttributes.Builder attributes) {
      super(still, flowing, attributes);
      try {
        tickRate(((int) viscosity.get(attributes)) / 200);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    // REEEEEEEE!

    public Properties canMultiply() {
      super.canMultiply();
      return this;
    }

    public Properties bucket(Supplier<? extends Item> bucket) {
      super.bucket(bucket);
      return this;
    }

    public Properties block(Supplier<? extends FlowingFluidBlock> block) {
      super.block(block);
      return this;
    }

    public Properties slopeFindDistance(int slopeFindDistance) {
      super.slopeFindDistance(slopeFindDistance);
      return this;
    }

    public Properties levelDecreasePerBlock(int levelDecreasePerBlock) {
      super.levelDecreasePerBlock(levelDecreasePerBlock);
      return this;
    }

    public Properties explosionResistance(float explosionResistance) {
      super.explosionResistance(explosionResistance);
      return this;
    }

    public Properties tickRate(int tickRate) {
      super.tickRate(tickRate);
      return this;
    }
  }
}
