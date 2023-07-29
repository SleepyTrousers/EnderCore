package com.enderio.core.common.autosave.handlers.java;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.autosave.Registry;
import com.enderio.core.common.autosave.annotations.Store;
import com.enderio.core.common.autosave.handlers.IHandler;

import net.minecraft.nbt.NBTTagCompound;

public class HandleBoolean implements IHandler<Boolean> {

  public HandleBoolean() {
  }

  @Override
  public boolean canHandle(Class<?> clazz) {
    return Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean store(@Nonnull Registry registry, @Nonnull Set<Store.StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name,
      @Nonnull Boolean object) throws IllegalArgumentException, IllegalAccessException {
    nbt.setBoolean(name, object);
    return true;
  }

  @Override
  public Boolean read(@Nonnull Registry registry, @Nonnull Set<Store.StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name,
      @Nullable Boolean object) {
    return nbt.hasKey(name) ? nbt.getBoolean(name) : object != null ? object : false;
  }

}
