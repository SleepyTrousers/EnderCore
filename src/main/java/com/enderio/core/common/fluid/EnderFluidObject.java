package com.enderio.core.common.fluid;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.BucketItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;

/**
 * A fluid's different registry objects, all kept together.
 * @param <StillFlowingFluid> The still flowing fluid class.
 * @param <FlowingFluid> The flowing fluid class.
 */
public class EnderFluidObject<FluidBlock extends EnderFluidBlock, Bucket extends BucketItem, StillFlowingFluid extends EnderFlowingFluid, FlowingFluid extends EnderFlowingFluid> {
  @Nonnull private final RegistryObject<FluidBlock> block;
  @Nonnull private final RegistryObject<Bucket> bucket;
  @Nonnull private final RegistryObject<StillFlowingFluid> still;
  @Nonnull private final RegistryObject<FlowingFluid> flowing;

  public EnderFluidObject(RegistryObject<FluidBlock> block, RegistryObject<Bucket> bucket, RegistryObject<StillFlowingFluid> still, RegistryObject<FlowingFluid> flowing) {
    this.block = block;
    this.bucket = bucket;
    this.still = still;
    this.flowing = flowing;
  }

  /**
   * Get the fluid block
   */
  @Nonnull public RegistryObject<FluidBlock> getBlock() {
    return block;
  }

  /**
   * Get the bucket item
   */
  @Nonnull public RegistryObject<Bucket> getBucket() {
    return bucket;
  }

  /**
   * Get the still fluid.
   */
  @Nonnull public RegistryObject<StillFlowingFluid> getStill() {
    return still;
  }

  /**
   * Get the flowing fluid.
   * @return
   */
  @Nonnull public RegistryObject<FlowingFluid> getFlowing() {
    return flowing;
  }

  /**
   * Helper for setting the render layer for this fluid.
   * @param type The render layer for this fluid.
   */
  @OnlyIn(Dist.CLIENT)
  public void setRenderLayer(RenderType type) {
    RenderTypeLookup.setRenderLayer(block.get(), type);
    RenderTypeLookup.setRenderLayer(still.get(), type);
    RenderTypeLookup.setRenderLayer(flowing.get(), type);
  }
}
