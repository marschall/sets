package com.github.marschall.sets;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.marschall.sets.SmallIntegerSet.SmallIntegerSubSet;

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
 * <p>{@link #contains(Object)}, {@link #add(Integer)}, {@link #remove(Object)}
 * and {@link #clear()} run in constant time.</p>
 *
 * <p>The operations {@link #addAll(Collection)},
 * {@link #removeAll(Collection)}, {@link #retainAll(Collection)}
 * and {@link #containsAll(Collection)} run in constant time when the argument
 * is a {@link SmallIntegerSet}.</p>
 *
 * <p>{@link #first()} and {@link #last()} run in logarithmic time.</p>
 *
 * <p>Takes inspiration from Eclipse Collections IntHashSet.</p>
 *
 * <p>This set is not thread safe.</p>
 *
 * <p>This set is not fail-fast.</p>
 *
 * <h2>Footprint</h2>
 *
 * <a href="http://openjdk.java.net/projects/code-tools/jol/">Java Object Layout</a>
 * reports the following sizes for HotSpot:
 *
 * <pre><code>
 * ***** 32-bit VM: **********************************************************
 * com.github.marschall.sets.SmallIntegerSet object internals:
 *  OFFSET  SIZE  TYPE DESCRIPTION                    VALUE
 *       0     8       (object header)                N/A
 *       8     8  long SmallIntegerSet.values         N/A
 * Instance size: 16 bytes
 * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
 *
 * ***** 64-bit VM: **********************************************************
 * com.github.marschall.sets.SmallIntegerSet object internals:
 *  OFFSET  SIZE  TYPE DESCRIPTION                    VALUE
 *       0    16       (object header)                N/A
 *      16     8  long SmallIntegerSet.values         N/A
 * Instance size: 24 bytes
 * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
 *
 * ***** 64-bit VM, compressed references enabled: ***************************
 * com.github.marschall.sets.SmallIntegerSet object internals:
 *  OFFSET  SIZE  TYPE DESCRIPTION                    VALUE
 *       0    12       (object header)                N/A
 *      12     4       (alignment/padding gap)        N/A
 *      16     8  long SmallIntegerSet.values         N/A
 * Instance size: 24 bytes
 * Space losses: 4 bytes internal + 0 bytes external = 4 bytes total
 *
 * ***** 64-bit VM, compressed references enabled, 16-byte align: ************
 * com.github.marschall.sets.SmallIntegerSet object internals:
 *  OFFSET  SIZE  TYPE DESCRIPTION                    VALUE
 *       0    12       (object header)                N/A
 *      12     4       (alignment/padding gap)        N/A
 *      16     8  long SmallIntegerSet.values         N/A
 *      24     8       (loss due to the next object alignment)
 * Instance size: 32 bytes
 * Space losses: 4 bytes internal + 8 bytes external = 12 bytes total
 * </code></pre>
 */
public final class SmallIntegerSet implements SortedSet<Integer>, Serializable, Cloneable {
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

  /**
   * Indices of the one bits. Uses the trick from
   * {@link Long#compareUnsigned(long, long)} so that
   * {@link Arrays#binarySearch(long[], long) can be used.
   */
  private static final long[] ONE_BIT_INDICES = {
      0b1L + Long.MIN_VALUE,
      0b10L + Long.MIN_VALUE,
      0b100L + Long.MIN_VALUE,
      0b1000L + Long.MIN_VALUE,
      0b10000L + Long.MIN_VALUE,
      0b100000L + Long.MIN_VALUE,
      0b1000000L + Long.MIN_VALUE,
      0b10000000L + Long.MIN_VALUE,
      0b100000000L + Long.MIN_VALUE,
      0b1000000000L + Long.MIN_VALUE,
      0b10000000000L + Long.MIN_VALUE,
      0b100000000000L + Long.MIN_VALUE,
      0b1000000000000L + Long.MIN_VALUE,
      0b10000000000000L + Long.MIN_VALUE,
      0b100000000000000L + Long.MIN_VALUE,
      0b1000000000000000L + Long.MIN_VALUE,
      0b10000000000000000L + Long.MIN_VALUE,
      0b100000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b10000000000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b100000000000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
      0b1000000000000000000000000000000000000000000000000000000000000000L + Long.MIN_VALUE,
  };

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
    return isSetNoCheck(this.values, i);
  }

  static boolean isSet(long bits, int i) {
    if (i < MIN_VALUE) {
      return false;
    }
    if (i > MAX_VALUE) {
      return false;
    }
    return isSetNoCheck(bits, i);
  }

  private static boolean isSetNoCheck(long bits, int i) {
    return (bits & (1L << i)) != 0;
  }

  public static boolean isSupported(int i) {
    return i >= MIN_VALUE && i <= MAX_VALUE;
  }

  static boolean isSupported(long mask, int i) {
    return isSupported(i) && (((1L << i) & mask) != 0);
  }

  private static void checkSupported(int i) {
    if (!isSupported(i)) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public int size() {
    return size(this.values);
  }

  static int size(long bits) {
    return Long.bitCount(bits);
  }

  @Override
  public boolean isEmpty() {
    return isEmpty(this.values);
  }

  static boolean isEmpty(long bits) {
    return bits == 0;
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
    forEach(this.values, action);
  }

  static void forEach(long bits, Consumer<? super Integer> action) {
    // TODO also check size
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (isSetNoCheck(bits, i)) {
        action.accept(i);
      }
    }
  }

  @Override
  public boolean removeIf(Predicate<? super Integer> filter) {
    // TODO also check size
    boolean modified = false;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (isSetNoCheck(i) && filter.test(i)) {
        this.unset(i);
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public Object[] toArray() {
    return toArray(this.values);
  }

  static Object[] toArray(long bits) {
    // REVIEW discussable if it should be an Integer[]
    Object[] result = new Object[size(bits)];
    int current = 0;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (isSetNoCheck(bits, i)) {
        result[current++] = i;
      }
    }
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    return toArray(this.values, a);
  }

  @SuppressWarnings("unchecked")
  static <T> T[] toArray(long bits, T[] a) {
    int size = size(bits);
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
      if (isSetNoCheck(bits, i)) {
        result[current++] = (T) (Integer) i;
      }
    }
    return result;
  }

  @Override
  public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
    int startInclusive = fromElement;
    checkSupported(startInclusive);
    int endInclusive = toElement - 1;
    checkSupported(endInclusive);
    if (fromElement == MIN_VALUE && endInclusive == MAX_VALUE) {
      return this;
    }
    if (startInclusive > endInclusive + 1) {
      throw new IllegalArgumentException();
    }
    if (startInclusive == MIN_VALUE) {
      return headSet(toElement);
    }
    if (endInclusive == MAX_VALUE) {
      return tailSet(fromElement);
    }
    if (startInclusive == endInclusive + 1) {
      return Collections.emptyNavigableSet();
    }

    // 0b1110
    long headMask = (1L << (endInclusive + 1L)) - 1L;
    // 0b0111
    long tailMask = ~((1L << fromElement) - 1L);
    long mask = headMask & tailMask;
    return new SmallIntegerSubSet(mask);
  }

  @Override
  public SortedSet<Integer> headSet(Integer toElement) {
    int endInclusive = toElement - 1;
    checkSupported(endInclusive);
    if (endInclusive == MAX_VALUE) {
      return this;
    }
    long mask = (1L << (endInclusive + 1L)) - 1L;
    return new SmallIntegerHeadSet(mask);
  }

  @Override
  public SortedSet<Integer> tailSet(Integer fromElement) {
    checkSupported(fromElement);
    if (fromElement == MIN_VALUE) {
      return this;
    }
    long mask = ~((1L << fromElement) - 1L);
    return new SmallIntegerTailSet(mask);
  }

  @Override
  public Comparator<? super Integer> comparator() {
    // natural order
    return null;
  }

  @Override
  public Integer first() {
    return first(this.values);
  }

  static Integer first(long bits) {
    if (bits == 0) {
      throw new NoSuchElementException();
    }
    long lowestOneBit = Long.lowestOneBit(bits);
    return log2(lowestOneBit);
  }

  static int log2(long lowestOneBit) {
    return Arrays.binarySearch(ONE_BIT_INDICES, lowestOneBit + Long.MIN_VALUE);
  }

  @Override
  public Integer last() {
    return last(this.values);
  }

  static Integer last(long bits) {
    if (bits == 0) {
      throw new NoSuchElementException();
    }
    long highestOneBit = Long.highestOneBit(bits);
    return log2(highestOneBit);
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
    if (c instanceof SmallIntegerSet) {
      return containsAll((SmallIntegerSet) c);
    }
    return containsAllGeneric(c);
  }

  private boolean containsAllGeneric(Collection<?> c) {
    for (Object each : c) {
      if (!this.contains(each)) {
        return false;
      }
    }
    return true;
  }

  private boolean containsAll(SmallIntegerSet other) {
    long otherValues = other.values;
    return (this.values & otherValues) == otherValues;
  }

  private boolean containsAllNonThrowing(Collection<?> c) {
    return containsAllNonThrowing(this.values, c);
  }

  static boolean containsAllNonThrowing(long bits, Collection<?> c) {
    // avoids exceptions in the case of null or anything but Integer
    for (Object each : c) {
      if (!(each instanceof Integer)) {
        return false;
      }
      if (!isSet(bits, (Integer) each)) {
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

  void clear(long bitsToClear) {
    this.values = this.values & ~bitsToClear;
  }

  @Override
  public String toString() {
    if (this.isEmpty()) {
      return "[]";
    }
    return toStringNotEmpty(this.values);
  }

  static String toStringNotEmpty(long bits) {
    StringBuilder builder = new StringBuilder(estimateToStringSize(bits));
    builder.append('[');
    boolean first = true;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (isSetNoCheck(bits, i)) {
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

  private static int estimateToStringSize(long bits) {
    int toStringSize = 2; // []
    boolean first = true;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (isSetNoCheck(bits, i)) {
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
    return hashCode(this.values);
  }

  static int hashCode(long bits) {
    // took contract form AbstractSet, has to produce the same results
    // as unordered sets
    int hashCode = 0;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (isSetNoCheck(bits, i)) {
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
    if (obj instanceof AbstractSmallIntegerSubSet) {
      return this.values == ((AbstractSmallIntegerSubSet) obj).bits();
    }

    Set<?> other = (Set<?>) obj;
    if (this.size() != other.size()) {
      return false;
    }
    return containsAllNonThrowing(other);
  }

  /**
   * Returns a copy of this {@code SmallIntegerSet} instance.
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

  abstract class AbstractSmallIntegerSubSet implements SortedSet<Integer>, Cloneable {

    final long mask;

    AbstractSmallIntegerSubSet(long mask) {
      this.mask = mask;
    }

    @Override
    public Comparator<? super Integer> comparator() {
      return null;
    }

    long bits() {
      return values & this.mask;
    }

    private boolean isSupported(int i) {
      return SmallIntegerSet.isSupported(this.mask, i);
    }

    @Override
    public Integer first() {
      return SmallIntegerSet.first(bits());
    }

    @Override
    public Integer last() {
      return SmallIntegerSet.last(bits());
    }

    @Override
    public int size() {
      return SmallIntegerSet.size(bits());
    }

    @Override
    public boolean add(Integer e) {
      this.checkSupported(e);
      return SmallIntegerSet.this.add(e);
    }

    void checkSupported(Integer e) {
      if (!this.isSupported(e)) {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public boolean remove(Object o) {
      if (!this.isSupported((Integer) o)) {
        return false;
      }
      return SmallIntegerSet.this.remove(o);
    }

    @Override
    public boolean isEmpty() {
      return SmallIntegerSet.isEmpty(this.bits());
    }

    @Override
    public void clear() {
      SmallIntegerSet.this.clear(this.mask);
    }

    @Override
    public String toString() {
      long bits = this.bits();
      if (SmallIntegerSet.isEmpty(bits)) {
        return "[]";
      }
      return SmallIntegerSet.toStringNotEmpty(this.bits());
    }

    @Override
    public Object[] toArray() {
      return SmallIntegerSet.toArray(this.bits());
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return SmallIntegerSet.toArray(this.bits(), a);
    }

    @Override
    public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
      this.checkSupported(fromElement);
      this.checkSupported(toElement - 1);
      return SmallIntegerSet.this.subSet(fromElement, toElement);
    }

    @Override
    public SortedSet<Integer> headSet(Integer toElement) {
      this.checkSupported(toElement - 1);
      long lowestOneBit = Long.lowestOneBit(this.mask);
      return SmallIntegerSet.this.subSet(log2(lowestOneBit), toElement);
    }

    @Override
    public SortedSet<Integer> tailSet(Integer fromElement) {
      this.checkSupported(fromElement);
      long highestOneBit = Long.highestOneBit(this.mask);
      return this.subSet(fromElement, log2(highestOneBit) + 1);
    }

    @Override
    public boolean contains(Object o) {
      return SmallIntegerSet.isSet(this.bits(), (Integer) o);
    }

    @Override
    public int hashCode() {
      return SmallIntegerSet.hashCode(this.bits());
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
        return this.bits() == ((SmallIntegerSet) obj).values;
      }
      if (obj instanceof AbstractSmallIntegerSubSet) {
        return this.bits() == ((AbstractSmallIntegerSubSet) obj).bits();
      }

      Set<?> other = (Set<?>) obj;
      if (this.size() != other.size()) {
        return false;
      }
      return containsAllNonThrowing(this.bits(), other);
    }

    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        // this shouldn't happen, since we are Cloneable
        throw new InternalError(e);
      }
    }

    @Override
    public void forEach(Consumer<? super Integer> action) {
      SmallIntegerSet.forEach(this.bits(), action);
    }

  }

  final class SmallIntegerHeadSet extends AbstractSmallIntegerSubSet {

    SmallIntegerHeadSet(long mask) {
      super(mask);
    }

    @Override
    public SortedSet<Integer> headSet(Integer toElement) {
      this.checkSupported(toElement - 1);
      return SmallIntegerSet.this.headSet(toElement);
    }

  }

  final class SmallIntegerTailSet extends AbstractSmallIntegerSubSet {

    SmallIntegerTailSet(long mask) {
      super(mask);
    }

    @Override
    public SortedSet<Integer> tailSet(Integer fromElement) {
      this.checkSupported(fromElement);
      return SmallIntegerSet.this.tailSet(fromElement);
    }

  }

  final class SmallIntegerSubSet extends AbstractSmallIntegerSubSet {

    SmallIntegerSubSet(long mask) {
      super(mask);
    }

  }

}
