package com.github.marschall.sets;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A set for small integers.
 *
 * <p>Only supports values from {@value #MIN_VALUE} to {@value #MAX_VALUE}.
 * Uses the same amount of memory as a single {@link Long} for the entire
 * {@link Set} even if the it contains 64 elements. On HotSpot a {@link Long}
 * uses the same amount of memory as an {@link Integer}.</p>
 *
 * <p>Does not support {@code null} elements.</p>
 *
 * <p>Takes inspiration from Eclipse Collections IntHashSet.</p>
 */
public final class SmallIntegerSet implements Set<Integer>, Serializable, Cloneable {
  // TODO implement SortedSet
  // TODO implement NavigableSet

  private static final long serialVersionUID = 1L;

  /**
   * Smallest value supported by this {@link Set}.
   *
   * <p>Any attempt at inserting a smaller value will throw a
   * {@link IllegalArgumentException}.</p>
   */
  public static final int MIN_VALUE = 0;

  /**
   * Largest value supported by this {@link Set}.
   *
   * <p>Any attempt at inserting a larger value will throw a
   * {@link IllegalArgumentException}.</p>
   */
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

  private boolean unset(int i) {
    if (i < MIN_VALUE) {
      return false;
    }
    if (i > MAX_VALUE) {
      return false;
    }
    long before = this.values;
    long after = this.values & ~(1L << i);
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
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSet(i)) {
        action.accept(i);
      }
    }
  }

  @Override
  public Object[] toArray() {
    // REVIEW discussable if it should be an Integer[]
    Object[] result = new Object[this.size()];
    int current = 0;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSet(i)) {
        result[current++] = i;
      }
    }
    return result;
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
    return this.unset((Integer) o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    // TODO fast path for small integer set
    for (Object each : c) {
      if (!this.contains(each)) {
        return false;
      }
    }
    return true;
  }

  private boolean containsAllNonThrowing(Collection<?> c) {
    // avoids exceptions in the case of null or anything but Integer
    for (Object each : c) {
      if (!(each instanceof Integer)) {
        return false;
      }
      if (!this.contains(each)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends Integer> c) {
    // TODO fast path for small integer set
    boolean changed = false;
    for (Integer each : c) {
      changed |= this.add(each);
    }
    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    // TODO fast path for small integer set
    boolean modified = false;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSet(i) && !c.contains(i)) {
        this.unset(i);
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    // TODO fast path for small integer set
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

  @Override
  public String toString() {
    if (this.isEmpty()) {
      return "[]";
    }
    StringBuilder builder = new StringBuilder(this.estimateToStringSize());
    builder.append('[');
    boolean first = true;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSet(i)) {
        if (!first) {
          builder.append(',').append(' ');
        } else {
          first = false;
        }
        builder.append(i);
      }
    }
    builder.append(']');
    return builder.toString();
  }

  private int estimateToStringSize() {
    int toStringSize = 2; // []
    boolean first = true;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSet(i)) {
        if (!first) {
          toStringSize += 2; // ", "
        } else {
          first = false;
        }
        if (i < 10) {
          toStringSize += 1;
        } else {
          toStringSize += 2;
        }
      }
    }
    return toStringSize;
  }

  @Override
  public int hashCode() {
    // took contract form AbstractSet, has to produce the same results
    // as unordered sets
    int hashCode = 0;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSet(i)) {
        hashCode += i;
      }
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Set)) {
      return false;
    }
    if (obj instanceof SmallIntegerSet) {
      return this.values == ((SmallIntegerSet) obj).values;
    }

    Set<?> other = (Set<?>) obj;
    if (this.size() != other.size()) {
      return false;
    }
    return containsAllNonThrowing(other);
  }

  /**
   * Returns a shallow copy of this {@code SmallIntegerSet} instance.
   *
   * @return a clone of this {@code SmallIntegerSet} instance
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError(e);
    }
  }

}
