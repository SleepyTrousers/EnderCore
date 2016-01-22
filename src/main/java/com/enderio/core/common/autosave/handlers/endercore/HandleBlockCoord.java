package com.enderio.core.common.autosave.handlers.endercore;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

import com.enderio.core.common.autosave.Registry;
import com.enderio.core.common.autosave.annotations.Store.StoreFor;
import com.enderio.core.common.autosave.exceptions.NoHandlerFoundException;
import com.enderio.core.common.autosave.handlers.IHandler;
import com.enderio.core.common.util.BlockCoord;

public class HandleBlockCoord implements IHandler<BlockCoord> {

  public HandleBlockCoord() {
  }

  @Override
  public boolean canHandle(Class<?> clazz) {
    return BlockCoord.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean store(@Nonnull Registry registry, @Nonnull Set<StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name, @Nonnull BlockCoord object)
      throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoHandlerFoundException {
    nbt.setIntArray(name, new int[] { object.x, object.y, object.z });
    return true;
  }

  @Override
  public BlockCoord read(@Nonnull Registry registry, @Nonnull Set<StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name,
      @Nullable BlockCoord object) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoHandlerFoundException {
    if (nbt.hasKey(name)) {
      int[] intArray = nbt.getIntArray(name);
      return new BlockCoord(intArray[0], intArray[1], intArray[2]);
    }
    return object;
  }

}
