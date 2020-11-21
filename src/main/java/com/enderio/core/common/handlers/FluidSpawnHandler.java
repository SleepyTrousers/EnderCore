package com.enderio.core.common.handlers;

import com.enderio.core.common.fluid.EnderFluidBlock;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FluidSpawnHandler {
  @SubscribeEvent
  public static void onEntitySpawn(LivingSpawnEvent.CheckSpawn evt) {
    if (evt.getResult() != Event.Result.DENY
        && EntitySpawnPlacementRegistry
            .getPlacementType(evt.getEntity().getType()) == EntitySpawnPlacementRegistry.PlacementType.IN_WATER
        && evt.getWorld().getBlockState(evt.getEntityLiving().getPosition()).getBlock() instanceof EnderFluidBlock) {
      evt.setResult(Event.Result.DENY);
    }
  }
}
