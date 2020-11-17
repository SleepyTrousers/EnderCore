package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
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

    if (entity == null || !entity.isAlive()) {
      return false;
    }
    if (entity instanceof ProjectileEntity && entity.getMotion().y > 0.01) {
      return false;
    }

    @Nonnull
    CompoundNBT data = entity.getPersistentData();

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
    double distToCur = curOwner.distanceSq(entity.getPosX(), entity.getPosY(), entity.getPosZ(), true);
    double distToMe = pullerPos.distanceSq(entity.getPosX(), entity.getPosY(), entity.getPosZ(),true);
    if (distToMe + 1 < distToCur) {
      reserve(data, pullerPos);
      return true;
    }
    return false;
  }

  public static void reserve(@Nullable Entity entity, @Nullable BlockPos pullerPos) {
    if (entity != null && entity.isAlive()) {
      CompoundNBT data = entity.getPersistentData();
      reserve(data, pullerPos);
    }
  }

  public static void reserve(@Nonnull CompoundNBT data, @Nullable BlockPos pullerPos) {
    if (pullerPos != null) {
      data.putLong(EC_PULLER_TAG, pullerPos.toLong());
    }
  }

  public static void release(@Nullable Entity entity) {
    if (entity != null && entity.isAlive()) {
      CompoundNBT data = entity.getPersistentData();
      release(data);
    }
  }

  public static void release(@Nonnull CompoundNBT data) {
    data.remove(EC_PULLER_TAG);
  }

  @Deprecated
  public static boolean isReserved(@Nonnull Entity entity) {
    return isReserved(entity, false);
  }

  public static boolean isReserved(@Nonnull Entity entity, boolean isMachine) {
    return isReservedByUs(entity.getPersistentData()) || isReservedByOthers(entity.getPersistentData(), isMachine);
  }

  public static boolean isReservedByUs(@Nonnull CompoundNBT data) {
    return data.contains(EC_PULLER_TAG);
  }

  @Deprecated
  public static boolean isReservedByOthers(@Nonnull CompoundNBT data) {
    return isReservedByOthers(data, false);
  }

  public static boolean isReservedByOthers(@Nonnull CompoundNBT data, boolean isMachine) {
    return data.contains(PREVENT_REMOTE_MOVEMENT) && (!isMachine || !data.contains(ALLOW_MACHINE_MOVEMENT));
  }

}
