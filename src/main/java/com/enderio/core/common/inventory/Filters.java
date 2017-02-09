package com.enderio.core.common.inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.item.ItemStack;

public class Filters {

  public static final @Nonnull Callback<ItemStack> NO_CALLBACK = new Callback<ItemStack>() {
    @Override
    public final void onChange(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
    }
  };

  public static final @Nonnull Predicate<ItemStack> ALWAYS_TRUE = Predicates.<ItemStack> alwaysTrue();

  public static final @Nonnull Predicate<ItemStack> ALWAYS_FALSE = Predicates.<ItemStack> alwaysFalse();

  public static final @Nonnull Predicate<ItemStack> ONLY_STACKABLE = new PredicateItemStack() {
    @Override
    public boolean doApply(@Nonnull ItemStack input) {
      return input.isStackable();
    }
  };

  public static @Nonnull Predicate<ItemStack> and(final @Nonnull Predicate<ItemStack> a, final @Nonnull Predicate<ItemStack> b) {
    return new PredicateItemStack() {
      @Override
      public boolean doApply(@Nonnull ItemStack input) {
        return a.apply(input) && b.apply(input);
      }
    };
  }

  public static @Nonnull Predicate<ItemStack> or(final @Nonnull Predicate<ItemStack> a, final @Nonnull Predicate<ItemStack> b) {
    return new PredicateItemStack() {
      @Override
      public boolean doApply(@Nonnull ItemStack input) {
        return a.apply(input) || b.apply(input);
      }
    };
  }

  public static @Nonnull Predicate<ItemStack> not(final @Nonnull Predicate<ItemStack> a) {
    return new PredicateItemStack() {
      @Override
      public boolean doApply(@Nonnull ItemStack input) {
        return !a.apply(input);
      }
    };
  }

  // ///////////////////////////////////////////////////////////////////

  private Filters() {
  }

  public static abstract class PredicateItemStack implements Predicate<ItemStack> {

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      return super.equals(obj);
    }

    @Override
    public boolean apply(@Nullable ItemStack input) {
      Preconditions.checkNotNull(input);
      return !input.isEmpty() && doApply(input);
    }

    public abstract boolean doApply(@Nonnull ItemStack input);

  }

}
