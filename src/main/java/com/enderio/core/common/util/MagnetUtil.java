package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class MagnetUtil {

  private static final @Nonnull String PREVENT_REMOTE_MOVEMENT = "PreventRemoteMovement";
  private static final @Nonnull String ALLOW_MACHINE_MOVEMENT = "AllowMachineRemoteMovement";
  public static final @Nonnull String EC_PULLER_TAG = "ECpuller";

  @Deprecated
  public static boolean shouldAttract(@Nullable BlockPos pullerPos, @Nullable Entity entity) {
    return shouldAttract(pullerPos, entity, false);
  }

  public static boolean shouldAttract(@Nullable BlockPos pullerPos, @Nullable Entity entity, boolean isMachine) {

    if (entity == null || entity.isDead) {
      return false;
    }
    if (entity instanceof IProjectile && entity.motionY > 0.01) {
      return false;
    }

    @Nonnull
    NBTTagCompound data = entity.getEntityData();

    if (isReservedByOthers(data, isMachine)) {
      return false;
    }

    if (!isReservedByUs(data)) {
      // if it is not being pulled already, pull it
      reserve(data, pullerPos);
      return true;
    }

    if (pullerPos == null) {
      // it is already being pulled, so with no further info we are done
      return false;
    }

    long posL = data.getLong(EC_PULLER_TAG);
    if (posL == pullerPos.toLong()) {
      // item already pulled from pullerPos so done
      return true;
    }

    // it is being pulled by something else, so check to see if we are closer
    BlockPos curOwner = BlockPos.fromLong(posL);
    double distToCur = curOwner.distanceSqToCenter(entity.posX, entity.posY, entity.posZ);
    double distToMe = pullerPos.distanceSqToCenter(entity.posX, entity.posY, entity.posZ);
    if (distToMe + 1 < distToCur) {
      reserve(data, pullerPos);
      return true;
    }
    return false;
  }

  public static void reserve(@Nullable Entity entity, @Nullable BlockPos pullerPos) {
    if (entity != null && !entity.isDead) {
      NBTTagCompound data = entity.getEntityData();
      reserve(data, pullerPos);
    }
  }

  public static void reserve(@Nonnull NBTTagCompound data, @Nullable BlockPos pullerPos) {
    if (pullerPos != null) {
      data.setLong(EC_PULLER_TAG, pullerPos.toLong());
    }
  }

  public static void release(@Nullable Entity entity) {
    if (entity != null && !entity.isDead) {
      NBTTagCompound data = entity.getEntityData();
      release(data);
    }
  }

  public static void release(@Nonnull NBTTagCompound data) {
    data.removeTag(EC_PULLER_TAG);
  }

  @Deprecated
  public static boolean isReserved(@Nonnull Entity entity) {
    return isReserved(entity, false);
  }

  public static boolean isReserved(@Nonnull Entity entity, boolean isMachine) {
    return isReservedByUs(entity.getEntityData()) || isReservedByOthers(entity.getEntityData(), isMachine);
  }

  public static boolean isReservedByUs(@Nonnull NBTTagCompound data) {
    return data.hasKey(EC_PULLER_TAG);
  }

  @Deprecated
  public static boolean isReservedByOthers(@Nonnull NBTTagCompound data) {
    return isReservedByOthers(data, false);
  }

  public static boolean isReservedByOthers(@Nonnull NBTTagCompound data, boolean isMachine) {
    return data.hasKey(PREVENT_REMOTE_MOVEMENT) && (!isMachine || !data.hasKey(ALLOW_MACHINE_MOVEMENT));
  }

}
