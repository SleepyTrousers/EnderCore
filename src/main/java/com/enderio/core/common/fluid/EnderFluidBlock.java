package com.enderio.core.common.fluid;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

// 1.12 PORTING NOTE: Instead of overriding this and applying behaviours, override EnderFluid
public class EnderFluidBlock extends FlowingFluidBlock {
  private float fogColorRed = 1f;
  private float fogColorGreen = 1f;
  private float fogColorBlue = 1f;

  protected EnderFluidBlock(Supplier<? extends EnderFlowingFluid> fluidSupplier, AbstractBlock.Properties builder, int fogColor) {
    super(fluidSupplier, builder);

    // Darken fog color to fit the fog rendering
    float dim = 1;
    while (getFogColorRed() > .2f || getFogColorGreen() > .2f || getFogColorBlue() > .2f) {
      setFogColorRed((fogColor >> 16 & 255) / 255f * dim);
      setFogColorGreen((fogColor >> 8 & 255) / 255f * dim);
      setFogColorBlue((fogColor & 255) / 255f * dim);
      dim *= .9f;
    }
  }

  public float getFogColorRed() {
    return fogColorRed;
  }

  public void setFogColorRed(float fogColorRed) {
    this.fogColorRed = fogColorRed;
  }

  public float getFogColorGreen() {
    return fogColorGreen;
  }

  public void setFogColorGreen(float fogColorGreen) {
    this.fogColorGreen = fogColorGreen;
  }

  public float getFogColorBlue() {
    return fogColorBlue;
  }

  public void setFogColorBlue(float fogColorBlue) {
    this.fogColorBlue = fogColorBlue;
  }

  @Override public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
    // Pass event.
    ((EnderFlowingFluid) getFluid()).onEntityCollision(state, worldIn, pos, entityIn);
    super.onEntityCollision(state, worldIn, pos, entityIn);
  }
}
