package com.enderio.core.common.fluid;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.BucketItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;

/**
 * An EnderCore fluid.
 * This simply keeps all fluid registry objects nicely tucked in one.
 * @param <SourceFlowingFluid> The source flowing fluid class.
 * @param <FlowingFluid> The flowing fluid class.
 */
public class EnderFluid<FluidBlock extends EnderFluidBlock, Bucket extends BucketItem, SourceFlowingFluid extends EnderFlowingFluid, FlowingFluid extends EnderFlowingFluid> {
  private final RegistryObject<FluidBlock> block;
  private final RegistryObject<Bucket> bucket;
  private final RegistryObject<SourceFlowingFluid> still;
  private final RegistryObject<FlowingFluid> flowing;

  public EnderFluid(RegistryObject<FluidBlock> block, RegistryObject<Bucket> bucket, RegistryObject<SourceFlowingFluid> still, RegistryObject<FlowingFluid> flowing) {
    this.block = block;
    this.bucket = bucket;
    this.still = still;
    this.flowing = flowing;
  }

  public RegistryObject<FluidBlock> getBlock() {
    return block;
  }

  public RegistryObject<Bucket> getBucket() {
    return bucket;
  }

  public RegistryObject<SourceFlowingFluid> getStill() {
    return still;
  }

  public RegistryObject<FlowingFluid> getFlowing() {
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
