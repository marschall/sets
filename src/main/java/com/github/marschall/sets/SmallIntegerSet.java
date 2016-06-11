package com.github.marschall.sets;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A set for small integers.
 *
 * <p>Only supports values from {@value #MIN_VALUE} to {@value #MIN_VALUE}
 * but only used the same amount of memory as a sing {@link Long} which
 * is the same as an {@link Integer} on HotSpot even if the map contains
 * 64 elements.</p>
 *
 * <p>Does not support {@code null} elements.</p>
 *
 * <p>Takes inspiration from Eclipse Collections IntHashSet.</p>
 */
public final class SmallIntegerSet implements Set<Integer>, Serializable {

  private static final long serialVersionUID = 1L;

  public static final int MIN_VALUE = 0;

  public static final int MAX_VALUE = 63;

  private long values;

  /**
   * Creates a new empty {@link SmallIntegerSet}.
   */
  public SmallIntegerSet() {
    this.values = 0L;
  }

  private boolean set(int i) {
    checkSupported(i);
    long before = this.values;
    long after = this.values | (1L << i);
    this.values = after;
    return before != after;
  }

  private boolean isSet(int i) {
    if (i < MIN_VALUE) {
      return false;
    }
    if (i > MAX_VALUE) {
      return false;
    }
    return (this.values & (1L << i)) != 0;
  }


  public static boolean isSupported(int i) {
    return i >= MIN_VALUE && i <= MAX_VALUE;
  }

  private void checkSupported(int i) {
    if (!isSupported(i)) {
      throw new IllegalArgumentException();
    }
  }


  @Override
  public int size() {
    return Long.bitCount(this.values);
  }

  @Override
  public boolean isEmpty() {
    return this.values == 0;
  }

  @Override
  public boolean contains(Object o) {
    return isSet((Integer) o);
  }

  @Override
  public Iterator<Integer> iterator() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void forEach(Consumer<? super Integer> action) {
    // TODO Auto-generated method stub
  }

  @Override
  public Object[] toArray() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean add(Integer e) {
    return this.set(e);
  }

  @Override
  public boolean remove(Object o) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    // TODO fast patch for small integer set
    for (Object each : c) {
      if (!this.contains(each)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends Integer> c) {
    // TODO fast patch for small integer set
    boolean changed = false;
    for (Integer each : c) {
      changed |= this.add(each);
    }
    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    // TODO fast patch for small integer set
    boolean changed = false;
    for (Object each : c) {
      changed |= this.remove(each);
    }
    return changed;
  }

  @Override
  public void clear() {
    this.values = 0;
  }

}
