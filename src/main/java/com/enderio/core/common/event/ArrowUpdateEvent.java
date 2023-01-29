package com.enderio.core.common.event;

import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

import cpw.mods.fml.common.eventhandler.Cancelable;

/**
 * Fires each tick that a Projectile or Arrow is updated. <br>
 * This event is in EntityArrow.onUpdate().<br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link cpw.mods.fml.common.eventhandler.Event.HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 **/
public class ArrowUpdateEvent extends EntityEvent {

    public ArrowUpdateEvent(Entity entity) {
        super(entity);
    }
}
