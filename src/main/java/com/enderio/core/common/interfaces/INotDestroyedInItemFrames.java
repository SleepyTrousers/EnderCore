package com.enderio.core.common.interfaces;

import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

/**
 * Items implementing this interface will be handled in a special way when inserted into an item frame so that no {@link PlayerDestroyItemEvent} is fired.
 *
 */
public interface INotDestroyedInItemFrames {

}
