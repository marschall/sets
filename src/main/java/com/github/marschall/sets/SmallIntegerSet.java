package com.github.marschall.sets;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A set for small integers.
 *
 * <p>Only supports values from {@value #MIN_VALUE} to {@value #MAX_VALUE}.
 * Uses the same amount of memory as a single {@link Long} for the entire
 * {@link Set} even if the it contains 64 elements.</p>
 *
 * <p>This set does not support {@code null} elements.</p>
 *
 * <p>This set keeps the elements in their natural order.</p>
 *
 * <p>All the set operations {@link #addAll(Collection)},
 * {@link #removeAll(Collection)} and {@link #retainAll(Collection)}
 * run in constant time when the argument is a SmallIntegerSet.</p>
 *
 * <p>Takes inspiration from Eclipse Collections IntHashSet.</p>
 *
 * <p>This set is not thread safe.</p>
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
    this.values = this.values | (1L << i);
    return before != this.values;
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
    return isSetNoCheck(i);
  }

  private boolean isSetNoCheck(int i) {
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
    return new SmallIntegerSetIterator();
  }

  @Override
  public void forEach(Consumer<? super Integer> action) {
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSetNoCheck(i)) {
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
      if (this.isSetNoCheck(i)) {
        result[current++] = i;
      }
    }
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    int size = this.size();
    T[] result;
    if (a.length < size) {
      result = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
    } else {
      result = a;
      if (a.length > size) {
        a[size] = null;
      }
    }
    int current = 0;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSetNoCheck(i)) {
        result[current++] = (T) (Integer) i;
      }
    }
    return result;
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
      if (!this.isSet((Integer) each)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends Integer> c) {
    if (c instanceof SmallIntegerSet) {
      return addAll((SmallIntegerSet) c);
    }
    return addAllGeneric(c);
  }

  private boolean addAllGeneric(Collection<? extends Integer> c) {
    boolean changed = false;
    for (Integer each : c) {
      changed |= this.add(each);
    }
    return changed;
  }

  private boolean addAll(SmallIntegerSet other) {
    long before = this.values;
    this.values |= other.values;
    return before != this.values;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    if (c instanceof SmallIntegerSet) {
      return retainAll((SmallIntegerSet) c);
    }
    return retainAllGeneric(c);
  }

  private boolean retainAllGeneric(Collection<?> c) {
    boolean modified = false;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSetNoCheck(i) && !c.contains(i)) {
        this.unset(i);
        modified = true;
      }
    }
    return modified;
  }

  private boolean retainAll(SmallIntegerSet other) {
    long before = this.values;
    this.values &= other.values;
    return before != this.values;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    if (c instanceof SmallIntegerSet) {
      return removeAll((SmallIntegerSet) c);
    }
    return removeAllGeneric(c);
  }

  private boolean removeAllGeneric(Collection<?> c) {
    boolean changed = false;
    for (Object each : c) {
      changed |= this.remove(each);
    }
    return changed;
  }

  private boolean removeAll(SmallIntegerSet other) {
    long before = this.values;
    this.values &= ~other.values;
    return before != this.values;
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
      if (this.isSetNoCheck(i)) {
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
      if (this.isSetNoCheck(i)) {
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
      if (this.isSetNoCheck(i)) {
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

  final class SmallIntegerSetIterator implements Iterator<Integer> {

    /**
     * Marks the end of the iteration has been reached.
     */
    private static final int END = -1;

    /**
     * Index of the next read, -1 means end reached.
     */
    private int nextIndex;

    SmallIntegerSetIterator() {
      this.nextIndex = findNextIndex(0);
    }

    private int findNextIndex(int initial) {
      for (int i = initial; i <= MAX_VALUE; ++i) {
        if (isSetNoCheck(i)) {
          return i;
        }
      }
      return END;
    }

    @Override
    public boolean hasNext() {
      return this.nextIndex != END;
    }

    @Override
    public Integer next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Integer next = nextIndex;
      this.nextIndex = findNextIndex(this.nextIndex + 1);
      return next;
    }

    @Override
    public void remove() {
      // TODO Auto-generated method stub
      Iterator.super.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super Integer> action) {
      if (!this.hasNext()) {
        return;
      }
      for (int i = this.nextIndex; i <= MAX_VALUE; ++i) {
        if (isSetNoCheck(i)) {
          // an exception will prevent nextIndex from being updated
          action.accept(i);
        }
      }
      this.nextIndex = END;
    }

  }

}
