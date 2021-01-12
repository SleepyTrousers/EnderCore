package com.enderio.core.common.fluid;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

// 1.12 PORTING NOTE: Instead of overriding this and applying behaviours, override EnderFluid

/**
 * Represents a fluid in block form.
 * This contains information about the fog color.
 */
public class EnderFluidBlock extends FlowingFluidBlock {
  private float fogColorRed = 1f;
  private float fogColorGreen = 1f;
  private float fogColorBlue = 1f;

  public EnderFluidBlock(Supplier<? extends EnderFlowingFluid> fluidSupplier, AbstractBlock.Properties builder, int fogColor) {
    super(fluidSupplier, builder);

    // Darken fog color to fit the fog rendering
    float dim = 1;
    while (this.fogColorRed > .2f || this.fogColorGreen > .2f || this.fogColorBlue > .2f) {
      this.fogColorRed = ((fogColor >> 16) & 0xFF) / 255f * dim;
      this.fogColorGreen = ((fogColor >> 8) & 0xFF) / 255f * dim;
      this.fogColorBlue = (fogColor & 0xFF) / 255f * dim;
      dim *= .9f;
    }
  }

  public float getFogColorRed() {
    return fogColorRed;
  }

  public float getFogColorGreen() {
    return fogColorGreen;
  }

  public float getFogColorBlue() {
    return fogColorBlue;
  }

  @Override public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
    // Pass event through. Makes custom behaviour more simple to implement.
    ((EnderFlowingFluid) getFluid()).onEntityCollision(state, worldIn, pos, entityIn);
    super.onEntityCollision(state, worldIn, pos, entityIn);
  }
}
