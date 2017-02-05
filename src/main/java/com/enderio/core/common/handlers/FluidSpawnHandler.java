package com.enderio.core.common.handlers;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.fluid.BlockFluidEnder;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Handler
public class FluidSpawnHandler {

  @SubscribeEvent
  public static void onEntitySpawn(LivingSpawnEvent.CheckSpawn evt) {
    if (evt.getResult() != Result.DENY
        && EntitySpawnPlacementRegistry
            .getPlacementForEntity(evt.getEntity().getClass()) == EntityLiving.SpawnPlacementType.IN_WATER
        && evt.getWorld().getBlockState(evt.getEntityLiving().getPosition()).getBlock() instanceof BlockFluidEnder) {
      evt.setResult(Result.DENY);
    }
    return;
  }

  private FluidSpawnHandler() {
  }

}
