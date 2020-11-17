package com.enderio.core.common.util;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import org.apache.commons.lang3.Validate;

import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

public class NNList<E> extends NonNullList<E> {

  public static final @Nonnull NNList<Direction> FACING = NNList.of(Direction.class);

  public static final @Nonnull NNList<Direction> FACING_HORIZONTAL = new NNList<Direction>(Direction.Plane.HORIZONTAL.iterator());

  public static final @Nonnull NNList<BlockPos> SHELL = new NNList<>();
  static {
    for (int y = -1; y <= 1; y++) {
      for (int z = -1; z <= 1; z++) {
        for (int x = -1; x <= 1; x++) {
          if (x != 0 || y != 0 || z != 0) {
            SHELL.add(new BlockPos(x, y, z));
          }
        }
      }
    }
    Collections.shuffle(SHELL);
  }

  public NNList() {
    this(new ArrayList<E>(), null);
  }

  public NNList(Collection<E> fillWith) {
    this();
    addAll(fillWith);
  }

  public NNList(Iterator<E> fillWith) {
    this();
    while (fillWith.hasNext()) {
      add(fillWith.next());
    }
  }

  public NNList(int size, @Nonnull E fillWith) {
    this();
    for (int i = 0; i < size; i++) {
      add(fillWith);
    }
  }

  @SafeVarargs
  public NNList(E... fillWith) {
    this();
    Collections.addAll(this, fillWith);
  }

  protected NNList(List<E> list, E defaultElement) {
    super(list, defaultElement);
    this.delegate = list;
    this.defaultElement = defaultElement;
  }

  public @Nonnull NNList<E> copy() {
    return new NNList<E>(this);
  }

  public static @Nonnull <X> NNList<X> wrap(List<X> list) {
    return list instanceof NNList ? (NNList<X>) list : new NNList<X>(list, null);
  }

  public static @Nonnull <X extends Enum<?>> NNList<X> of(Class<X> e) {
    NNList<X> list = new NNList<X>(e.getEnumConstants());
    return list;
  }

  public static <X extends Enum<?>> void addAllEnum(NNList<? super X> list, Class<X> e) {
    list.addAll(e.getEnumConstants());
  }

  public static @Nonnull <T> Collector<T, ?, NNList<T>> collector() {
    return NullHelper.notnullJ(Collectors.toCollection(NNList::new), "Collectors.toCollection");
  }

  /**
   * Finds the element after the given element.
   * <p>
   * Please note that this does do identity, not equality, checks and cannot handle multiple occurrences of the same element in the list.
   *
   * @throws InvalidParameterException
   *           if the given element is not part of the list.
   */
  public @Nonnull E next(E current) {
    for (int i = 0; i < delegate.size(); i++) {
      if (get(i) == current) {
        if (i + 1 < delegate.size()) {
          return get(i + 1);
        } else {
          return get(0);
        }
      }
    }
    throw new InvalidParameterException();
  }

  /**
   * Finds the element before the given element.
   * <p>
   * Please note that this does do identity, not equality, checks and cannot handle multiple occurrences of the same element in the list.
   *
   * @throws InvalidParameterException
   *           if the given element is not part of the list.
   */
  public @Nonnull E prev(E current) {
    for (int i = 0; i < delegate.size(); i++) {
      if (get(i) == current) {
        if (i > 0) {
          return get(i - 1);
        } else {
          return get(delegate.size() - 1);
        }
      }
    }
    throw new InvalidParameterException();
  }

  public NNList<E> apply(@Nonnull Callback<E> callback) {
    for (E e : delegate) {
      if (e == null) {
        throw new NullPointerException();
      }
      callback.apply(e);
    }
    return this;
  }

  @FunctionalInterface
  public static interface Callback<E> {
    void apply(@Nonnull E e);
  }

  public boolean applyShort(@Nonnull ShortCallback<E> callback) {
    for (E e : delegate) {
      if (e == null) {
        throw new NullPointerException();
      }
      if (callback.apply(e)) {
        return true;
      }
    }
    return callback.finish();
  }

  @FunctionalInterface
  public static interface ShortCallback<E> {
    boolean apply(@Nonnull E e);

    /**
     * This is called if the callback did not signal <code>true</code> for any element to determine the final result of the run.
     */
    default boolean finish() {
      return false;
    }
  }

  @Override
  public @Nonnull NNIterator<E> iterator() {
    return new ItrImpl<E>(delegate.iterator());
  }

  /**
   * Creates a fast iterator for read-only lists. Do not use on lists that may be changed.
   */
  public @Nonnull NNIterator<E> fastIterator() {
    return new FastItrImpl();
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

  private class FastItrImpl implements NNIterator<E> {
    int cursor = 0;

    @Override
    public boolean hasNext() {
      return cursor != delegate.size();
    }

    @Override
    public @Nonnull E next() {
      try {
        return get(cursor++);
      } catch (IndexOutOfBoundsException e) {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  private static final @Nonnull NNList<Object> EMPTY = new NNList<Object>(Collections.emptyList(), null);

  @SuppressWarnings("unchecked")
  public static @Nonnull <X> NNList<X> emptyList() {
    return (NNList<X>) EMPTY;
  }

  @SafeVarargs
  public final NNList<E> addAll(E... el) {
    for (E e : el) {
      add(e);
    }
    return this;
  }

  public static <E, L extends List<E>> L addIf(@Nonnull L list, @Nullable E e) {
    if (e != null) {
      list.add(e);
    }
    return list;
  }

  public NNList<E> addIf(@Nullable E e) {
    if (e != null) {
      add(e);
    }
    return this;
  }

  @SuppressWarnings("null")
  @Override
  public @Nonnull <T> T[] toArray(T[] a) {
    return delegate.toArray(a);
  }

  public NNList<E> removeAllByClass(Class<? extends E> clazz) {
    for (Iterator<E> iterator = delegate.iterator(); iterator.hasNext();) {
      if (clazz.isAssignableFrom(iterator.next().getClass())) {
        iterator.remove();
      }
    }
    return this;
  }

  // The following replaces all super methods to use our own storage

  private final List<E> delegate;
  private final E defaultElement;

  @Override
  @Nonnull
  public E get(int p_get_1_) {
    return NullHelper.notnull(delegate.get(p_get_1_), "Unexpect 'null' object in NNList");
  }

  @Override
  public @Nonnull E set(int p_set_1_, E p_set_2_) {
    return NullHelper.notnull(delegate.set(p_set_1_, Validate.notNull(p_set_2_)), "Unexpect 'null' object in NNList");
  }

  @Override
  public void add(int p_add_1_, E p_add_2_) {
    delegate.add(p_add_1_, Validate.notNull(p_add_2_));
  }

  @Override
  public @Nonnull E remove(int p_remove_1_) {
    return NullHelper.notnull(delegate.remove(p_remove_1_), "Unexpect 'null' object in NNList");
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public void clear() {
    if (defaultElement == null) {
      removeRange(0, delegate.size());
    } else {
      for (int i = 0; i < delegate.size(); ++i) {
        delegate.set(i, defaultElement);
      }
    }
  }

}
