package com.enderio.core.common.autosave.handlers.minecraft;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.autosave.Registry;
import com.enderio.core.common.autosave.annotations.Store.StoreFor;
import com.enderio.core.common.autosave.exceptions.NoHandlerFoundException;
import com.enderio.core.common.autosave.handlers.IHandler;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class HandleBlockPos implements IHandler<BlockPos> {

  public HandleBlockPos() {
  }

  @Override
  public boolean canHandle(Class<?> clazz) {
    return BlockPos.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean store(@Nonnull Registry registry, @Nonnull Set<StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name, @Nonnull BlockPos object)
      throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoHandlerFoundException {
    nbt.setLong(name, object.toLong());
    return true;
  }

  @Override
  public BlockPos read(@Nonnull Registry registry, @Nonnull Set<StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name, @Nullable BlockPos object)
      throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoHandlerFoundException {
    if (nbt.hasKey(name)) {
      return BlockPos.fromLong(nbt.getLong(name));
    }
    return object;
  }

}
