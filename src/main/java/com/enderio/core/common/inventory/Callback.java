package com.enderio.core.common.inventory;

import javax.annotation.Nonnull;

public interface Callback<T> {

  void onChange(@Nonnull T oldStack, @Nonnull T newStack);

}
