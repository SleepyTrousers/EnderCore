package com.enderio.core.common.fluid;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Deferred EnderFluid Registry.
 * Allows easier registration of custom fluids.
 */
public class DeferredEnderFluidRegistry {
  private final DeferredRegister<Item> ITEMS;
  private final DeferredRegister<Block> BLOCKS;
  private final DeferredRegister<Fluid> FLUIDS;

  public DeferredEnderFluidRegistry(String modid) {
    ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
    BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
    FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, modid);
  }

  /**
   * Register a custom fluid.
   * @param name Fluid name
   * @param still Still fluid
   * @param flowing Flowing fluid
   * @return EnderFluid container
   */
  public <StillFlowingFluid extends EnderFlowingFluid, FlowingFluid extends EnderFlowingFluid> EnderFluid<EnderFluidBlock, BucketItem, StillFlowingFluid, FlowingFluid> register(
      String name, Supplier<StillFlowingFluid> still, Supplier<FlowingFluid> flowing, AbstractBlock.Properties properties, int fogColor) {
    return register(name, still, flowing, getDefaultBlock(still, properties, fogColor), getDefaultBucket(name, still));
  }

  /**
   * Register a custom fluid.
   * @param name Fluid name
   * @param still Still fluid
   * @param flowing Flowing fluid
   * @param block Flowing fluid block
   * @return EnderFluid container
   */
  public <FluidBlock extends EnderFluidBlock, StillFlowingBlock extends EnderFlowingFluid, FlowingFluid extends EnderFlowingFluid> EnderFluid<FluidBlock, BucketItem, StillFlowingBlock, FlowingFluid> register(
      String name, Supplier<StillFlowingBlock> still, Supplier<FlowingFluid> flowing, Supplier<FluidBlock> block) {
    return register(name, still, flowing, block, getDefaultBucket(name, still));
  }

  /**
   * Register a custom fluid.
   * @param name Fluid name
   * @param still Still fluid
   * @param flowing Flowing fluid
   * @param bucket Bucket item
   * @return EnderFluid container
   */
  public <Bucket extends BucketItem, StillFlowingFluid extends EnderFlowingFluid, FlowingFluid extends EnderFlowingFluid> EnderFluid<EnderFluidBlock, Bucket, StillFlowingFluid, FlowingFluid> register(
      String name, Supplier<StillFlowingFluid> still, Supplier<FlowingFluid> flowing, AbstractBlock.Properties properties, int fogColor,
      Supplier<Bucket> bucket) {
    return register(name, still, flowing, getDefaultBlock(still, properties, fogColor), bucket);
  }

  /**
   * Register a custom fluid.
   * @param name Fluid name
   * @param still Still fluid
   * @param flowing Flowing fluid
   * @param block Flowing fluid block
   * @param bucket Bucket item
   * @return EnderFluid container
   */
  public <FluidBlock extends EnderFluidBlock, Bucket extends BucketItem, StillFlowingFluid extends EnderFlowingFluid, FlowingFluid extends EnderFlowingFluid> EnderFluid<FluidBlock, Bucket, StillFlowingFluid, FlowingFluid> register(
      String name, Supplier<StillFlowingFluid> still, Supplier<FlowingFluid> flowing, Supplier<FluidBlock> block, Supplier<Bucket> bucket) {
    RegistryObject<EnderFluidBlock> regBlock = BLOCKS.register(name + "_block", block);
    RegistryObject<BucketItem> regBucket = ITEMS.register(name + "_bucket", bucket);
    RegistryObject<StillFlowingFluid> regStill = FLUIDS.register(name, still);
    RegistryObject<FlowingFluid> regFlowing = FLUIDS.register(name + "_flowing", flowing);
    return new EnderFluid(regBlock, regBucket, regStill, regFlowing);
  }

  public void register(IEventBus bus) {
    BLOCKS.register(bus);
    FLUIDS.register(bus);
    ITEMS.register(bus);
  }

  private Supplier<BucketItem> getDefaultBucket(String fluidName, Supplier<? extends EnderFlowingFluid> still) {
    return () -> new BucketItem(still, new Item.Properties().containerItem(Items.BUCKET).maxStackSize(1).group(ItemGroup.MISC));
  }

  private Supplier<EnderFluidBlock> getDefaultBlock(Supplier<? extends EnderFlowingFluid> still, AbstractBlock.Properties properties, int fogColor) {
    return () -> new EnderFluidBlock(still, properties, fogColor);
  }
}
