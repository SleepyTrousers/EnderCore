package com.enderio.core.common.util;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraftforge.common.UsernameCache;

public class PlayerUtil {

  public static @Nullable UUID getPlayerUUID(String username) {
    for (Map.Entry<UUID, String> entry : UsernameCache.getMap().entrySet()) {
      if (entry.getValue().equalsIgnoreCase(username)) {
        return entry.getKey();
      }
    }
    return null;
  }

  public static @Nullable UUID getPlayerUIDUnstable(String possibleUUID) {
    if (possibleUUID == null || possibleUUID.isEmpty())
      return null;
    UUID uuid = null;
    try {
      uuid = UUID.fromString(possibleUUID);
    } catch (IllegalArgumentException e) {
      uuid = getPlayerUUID(possibleUUID);
    }
    return uuid;
  }
}
