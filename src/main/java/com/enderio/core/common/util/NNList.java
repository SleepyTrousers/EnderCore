package com.enderio.core.common.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;

public class NNList<E> extends NonNullList<E> {

  public static final @Nonnull NNList<EnumFacing> FACING = NNList.of(EnumFacing.class);

  public static final @Nonnull NNList<EnumFacing> FACING_HORIZONTAL = new NNList<EnumFacing>(EnumFacing.HORIZONTALS);

  public static final @Nonnull NNList<BlockRenderLayer> RENDER_LAYER = NNList.of(BlockRenderLayer.class);

  public NNList() {
    super();
  }

  public NNList(List<E> fillWith) {
    super();
    addAll(fillWith);
  }

  public NNList(E... fillWith) {
    super();
    Collections.addAll(this, fillWith);
  }

  protected NNList(List<E> list, E defaultElement) {
    super(list, defaultElement);
  }

  public @Nonnull NNList<E> copy() {
    return new NNList<E>(this);
  }

  public static @Nonnull <X> NNList<X> wrap(List<X> list) {
    return new NNList<X>(list, null);
  }

  public static @Nonnull <X extends Enum<?>> NNList<X> of(Class<X> e) {
    NNList<X> list = new NNList<X>(e.getEnumConstants());
    return list;
  }

  public void apply(@Nonnull Callback<E> callback) {
    for (E e : this) {
      if (e == null) {
        throw new NullPointerException();
      }
      callback.apply(e);
    }
  }

  public static interface Callback<E> {
    void apply(@Nonnull E e);
  }

  @Override
  public @Nonnull NNIterator<E> iterator() {
    return new ItrImpl<E>(super.iterator());
  }

  public static interface NNIterator<E> extends Iterator<E> {

    @Override
    @Nonnull
    E next();

  }

  private static class ItrImpl<E> implements NNIterator<E> {

    private final Iterator<E> parent;

    public ItrImpl(Iterator<E> iterator) {
      parent = iterator;
    }

    @Override
    public boolean hasNext() {
      return parent.hasNext();
    }

    @Override
    public @Nonnull E next() {
      final E next = parent.next();
      if (next == null) {
        throw new NullPointerException();
      }
      return next;
    }

    @Override
    public void remove() {
      parent.remove();
    }

  }

}
