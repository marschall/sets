package com.github.marschall.sets;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A set for {@link Integer}s between {@value #MIN_VALUE} and
 * {@value #MAX_VALUE}.
 *
 * <p>This uses the same amount of memory as a single {@link Long} for
 * the entire {@link Set} even if the it contains 64 elements.</p>
 *
 * <p>Operations like {@link #add(Integer)} will throw an
 * {@link IllegalArgumentException} with an argument outside the supported
 * range. Operations like {@link #remove(Object)} or {@link #contains(Object)}
 * will return {@code false} with an argument outside this range.  This is in
 * accordance with the {@link Set} contract.</p>
 *
 * <p>This set does not support {@code null} elements. Operations like
 * {@link #add(Integer)}, {@link #remove(Object)} or {@link #contains(Object)}
 * will throw a {@link NullPointerException} if passed {@code null} as an
 * argument. This is in accordance with the {@link Set} contract.</p>
 *
 * <p>This set keeps the elements in their
 * <a href="https://docs.oracle.com/javase/tutorial/collections/interfaces/order.html">natural order</a>.</p>
 *
 * <p>The operations {@link #contains(Object)}, {@link #add(Integer)},
 * {@link #remove(Object)} and {@link #clear()} run in constant time.</p>
 *
 * <p>The operations {@link #addAll(Collection)},
 * {@link #removeAll(Collection)}, {@link #retainAll(Collection)}
 * and {@link #containsAll(Collection)} run in constant time when the argument
 * is a {@link SmallIntegerSet}.</p>
 *
 * <p>The operations {@link #first()} and {@link #last()} run in logarithmic time.</p>
 *
 * <p>This set is not thread safe.</p>
 *
 * <p>This set is not fail-fast.</p>
 *
 * <p>This set supports all optional {@link Set} and {@link Iterator} operations.</p>
 *
 * <p>This set will not preserve the object identity of the {@link Integer}s
 * passed in but will always return the
 * <a href="https://docs.oracle.com/javase/tutorial/java/data/autoboxing.html">autoboxed</a>
 * instances. The following code will pass:</p>
 * <pre><code>
 * Integer i = new Integer(1); // intentionally do not use instance from the Integer cache
 * this.set.add(i);
 * assertEquals(i, this.set.iterator().next());
 * assertNotSame(i, this.set.iterator().next()); // will get the instance from the Integer cache
 * </code></pre>
 *
 * <p>Takes inspiration from Eclipse Collections IntHashSet.</p>
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

  long values;

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

  boolean unset(int i) {
    if (i < MIN_VALUE) {
      return false;
    }
    if (i > MAX_VALUE) {
      return false;
    }
    long before = this.values;
    unsetNoCheck(i);
    return this.values != before;
  }

  void unsetNoCheck(int i) {
    this.values = this.values & ~(1L << i);
  }

  private boolean isSet(int i) {
    if (i < MIN_VALUE) {
      return false;
    }
    if (i > MAX_VALUE) {
      return false;
    }
    return this.isSetNoCheck(i);
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

  /**
   * Checks if instances of this set class will support containing the given
   * value.
   *
   * <p>If this set class does not support the given value operations like
   * {@link #add(Integer)} will throw an {@link IllegalArgumentException}.</p>
   *
   * <p>Even instances of this set class will not support containing the given
   * value it is still save to call operations like {@link #contains(Object)}
   * or {@link #remove(Object)}.</p>
   *
   * @param i the integer to check
   * @return {@code true} if instances of this set class will support containing
   *  the value and it is safe to call operators like {@link #add(Integer)},
   *  {@code false} if instances of this set class will not support containing
   *  the value and operators like {@link #add(Integer)} will throw an
   *  {@link IllegalArgumentException}
   */
  public static boolean isSupported(int i) {
    return i >= MIN_VALUE && i <= MAX_VALUE;
  }

  /**
   * Checks if instances of this set class will support containing the given
   * value.
   *
   * <p>If this set class does not support the given value operations like
   * {@link #add(Integer)} will throw an {@link IllegalArgumentException}
   * or a {@link NullPointerException}.</p>
   *
   * <p>Even instances of this set class will not support containing the given
   * value it is still save to call operations like {@link #contains(Object)}
   * or {@link #remove(Object)} if the argument is not {@code null}.</p>
   *
   * <p>Like {@link #isSupported(int)} but does also a {@code} null check.</p>
   *
   * @param i the integer to check
   * @return {@code true} if instances of this set class will support containing
   *  the value and it is safe to call operators like {@link #add(Integer)},
   *  {@code false} if instances of this set class will not support containing
   *  the value and operators like {@link #add(Integer)} will throw an
   *  {@link IllegalArgumentException} or a {@link NullPointerException}.
   */
  public static boolean isSupported(Integer i) {
    return i != null && isSupported(i.intValue());
  }

  static boolean isSupported(long mask, int i) {
    return isSupported(i) && (((1L << i) & mask) != 0);
  }

  private static void checkSupported(int i) {
    if (!isSupported(i)) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return size(this.values);
  }

  static int size(long bits) {
    return Long.bitCount(bits);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEmpty() {
    return isEmpty(this.values);
  }

  static boolean isEmpty(long bits) {
    return bits == 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean contains(Object o) {
    return this.isSet((Integer) o);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Integer> iterator() {
    return new SmallIntegerSetIterator();
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeIf(Predicate<? super Integer> filter) {
    // TODO also check size
    boolean modified = false;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSetNoCheck(i) && filter.test(i)) {
        this.unsetNoCheck(i);
        modified = true;
      }
    }
    return modified;
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T[] toArray(T[] a) {
    return toArray(this.values, a);
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
    int startInclusive = fromElement;
    checkSupported(startInclusive);
    int endInclusive = toElement - 1;
    if (startInclusive == endInclusive + 1) {
      return Collections.emptyNavigableSet();
    }
    checkSupported(endInclusive);
    if (fromElement == MIN_VALUE && endInclusive == MAX_VALUE) {
      return this;
    }
    if (startInclusive > endInclusive + 1) {
      throw new IllegalArgumentException();
    }
    if (endInclusive == MAX_VALUE) {
      return this.tailSet(fromElement);
    }

    // 0b1110
    long headMask = (1L << (endInclusive + 1L)) - 1L;
    // 0b0111
    long tailMask = ~((1L << fromElement) - 1L);
    // 0b0110
    long mask = headMask & tailMask;
    return new SmallIntegerSubSet(mask);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SortedSet<Integer> headSet(Integer toElement) {
    int endInclusive = toElement - 1;
    checkSupported(endInclusive);
    if (endInclusive == MAX_VALUE) {
      return this;
    }
    long mask = (1L << (endInclusive + 1L)) - 1L;
    return new SmallIntegerSubSet(mask);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SortedSet<Integer> tailSet(Integer fromElement) {
    checkSupported(fromElement);
    if (fromElement == MIN_VALUE) {
      return this;
    }
    long mask = ~((1L << fromElement) - 1L);
    return new SmallIntegerSubSet(mask);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Comparator<? super Integer> comparator() {
    // natural order
    return null;
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(Integer e) {
    return this.set(e);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean remove(Object o) {
    return this.unset((Integer) o);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsAll(Collection<?> c) {
    if (c instanceof SmallIntegerSet) {
      return this.containsAll((SmallIntegerSet) c);
    }
    if (c instanceof SmallIntegerSubSet) {
      return this.containsAll((SmallIntegerSubSet) c);
    }
    return this.containsAllGeneric(c);
  }

  private boolean containsAllGeneric(Collection<?> c) {
    // can't check for size because Collection could be a List
    // would have to check for Set and size
    for (Object each : c) {
      if (!this.contains(each)) {
        return false;
      }
    }
    return true;
  }

  private boolean containsAll(SmallIntegerSet other) {
    return containsAll(this.values, other.values);
  }

  private boolean containsAll(SmallIntegerSubSet other) {
    return containsAll(this.values, other.bits());
  }

  static boolean containsAll(long thisBits, long otherBits) {
    return (thisBits & otherBits) == otherBits;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addAll(Collection<? extends Integer> c) {
    if (c instanceof SmallIntegerSet) {
      return this.addAll((SmallIntegerSet) c);
    }
    if (c instanceof SmallIntegerSubSet) {
      return this.addAll((SmallIntegerSubSet) c);
    }
    return this.addAllGeneric(c);
  }

  private boolean addAllGeneric(Collection<? extends Integer> c) {
    boolean changed = false;
    for (Integer each : c) {
      changed |= this.add(each);
    }
    return changed;
  }

  private boolean addAll(SmallIntegerSet other) {
    return this.addAll(other.values);
  }

  private boolean addAll(SmallIntegerSubSet other) {
    return this.addAll(other.bits());
  }

  boolean addAll(long bits) {
    long before = this.values;
    this.values |= bits;
    return before != this.values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean retainAll(Collection<?> c) {
    if (c instanceof SmallIntegerSet) {
      return this.retainAll((SmallIntegerSet) c);
    }
    if (c instanceof SmallIntegerSubSet) {
      return this.retainAll((SmallIntegerSubSet) c);
    }
    return this.retainAllGeneric(c);
  }

  private boolean retainAllGeneric(Collection<?> c) {
    boolean modified = false;
    for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
      if (this.isSetNoCheck(i) && !c.contains(i)) {
        this.unsetNoCheck(i);
        modified = true;
      }
    }
    return modified;
  }

  private boolean retainAll(SmallIntegerSubSet other) {
    return this.retainAll(other.bits());
  }

  private boolean retainAll(SmallIntegerSet other) {
    return this.retainAll(other.values);
  }

  boolean retainAll(long bits) {
    long before = this.values;
    this.values &= bits;
    return before != this.values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    if (c instanceof SmallIntegerSet) {
      return this.removeAll((SmallIntegerSet) c);
    }
    if (c instanceof SmallIntegerSubSet) {
      return this.removeAll((SmallIntegerSubSet) c);
    }
    return this.removeAllGeneric(c);
  }

  private boolean removeAllGeneric(Collection<?> c) {
    boolean changed = false;
    // TODO check size, iterate over other
    for (Object each : c) {
      changed |= this.remove(each);
    }
    return changed;
  }

  private boolean removeAll(SmallIntegerSet other) {
    return this.removeAll(other.values);
  }

  private boolean removeAll(SmallIntegerSubSet other) {
    return this.removeAll(other.bits());
  }

  boolean removeAll(long bits) {
    long before = this.values;
    this.values &= ~bits;
    return before != this.values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    this.values = 0;
  }

  void clear(long bitsToClear) {
    this.values = this.values & ~bitsToClear;
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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
    if (obj instanceof SmallIntegerSubSet) {
      return this.values == ((SmallIntegerSubSet) obj).bits();
    }

    Set<?> other = (Set<?>) obj;
    if (this.size() != other.size()) {
      return false;
    }
    return this.containsAllNonThrowing(other);
  }

  /**
   * Returns a shallow copy of this {@code SmallIntegerSet} instance.
   *
   * <p>The {@link Integer} elements themselves are not cloned.</p>
   *
   * @return a shallow copy of this set
   */
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError(e);
    }
  }

  abstract static class AbstractIntegerSetIterator implements Iterator<Integer> {

    /**
     * Marks the end of the iteration has been reached.
     */
    private static final int END = -1;

    /**
     * Marks the remove index as unusable.
     */
    private static final int NO_REMOVE = -1;

    /**
     * Index of the next read, {@value #END} means end reached.
     */
    private int nextIndex;

    /**
     * Index of the next remove, {@value #NO_REMOVE} means no remove possible.
     */
    private int removeIndex;

    AbstractIntegerSetIterator() {
      this.nextIndex = this.findNextIndex(0);
    }

    abstract boolean isSetNoCheck(int i);

    abstract void unsetNoCheck(int i);

    private int findNextIndex(int initial) {
      for (int i = initial; i <= MAX_VALUE; ++i) {
        if (this.isSetNoCheck(i)) {
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
      if (!this.hasNext()) {
        throw new NoSuchElementException();
      }
      this.removeIndex = nextIndex;
      Integer next = nextIndex;
      this.nextIndex = this.findNextIndex(this.nextIndex + 1);
      return next;
    }

    @Override
    public void remove() {
      if (this.removeIndex == NO_REMOVE) {
        throw new IllegalStateException();
      }
      this.unsetNoCheck(this.removeIndex);
      this.removeIndex = NO_REMOVE;
    }

    @Override
    public void forEachRemaining(Consumer<? super Integer> action) {
      if (!this.hasNext()) {
        return;
      }
      for (int i = this.nextIndex; i <= MAX_VALUE; ++i) {
        if (this.isSetNoCheck(i)) {
          // an exception will prevent nextIndex from being updated
          action.accept(i);
        }
      }
      this.nextIndex = END;
    }

  }

  final class SmallIntegerSetIterator extends AbstractIntegerSetIterator {

    @Override
    boolean isSetNoCheck(int i) {
      return SmallIntegerSet.this.isSetNoCheck(i);
    }

    @Override
    void unsetNoCheck(int i) {
      SmallIntegerSet.this.unsetNoCheck(i);
    }

  }

  final class SmallIntegerSubSet implements SortedSet<Integer>, Cloneable {

    // TODO removeIf

    final long mask;

    SmallIntegerSubSet(long mask) {
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
      return SmallIntegerSet.first(this.bits());
    }

    @Override
    public Integer last() {
      return SmallIntegerSet.last(this.bits());
    }

    @Override
    public int size() {
      return SmallIntegerSet.size(this.bits());
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
      if ((this.mask & 1L) == 1L) {
        // we're a headSet on the main set
        return SmallIntegerSet.this.headSet(toElement);
      }
      this.checkSupported(toElement - 1);
      long lowestOneBit = Long.lowestOneBit(this.mask);
      return SmallIntegerSet.this.subSet(log2(lowestOneBit), toElement);
    }

    @Override
    public SortedSet<Integer> tailSet(Integer fromElement) {
      if ((this.mask & Long.MIN_VALUE) == Long.MIN_VALUE) {
        // we're a tailSet on the main set
        return SmallIntegerSet.this.tailSet(fromElement);
      }
      this.checkSupported(fromElement);
      long highestOneBit = Long.highestOneBit(this.mask);
      return this.subSet(fromElement, log2(highestOneBit) + 1);
    }

    @Override
    public boolean contains(Object o) {
      return SmallIntegerSet.isSet(this.bits(), (Integer) o);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new SmallIntegerSubSetIterator();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      if (c instanceof SmallIntegerSet) {
        return this.containsAll((SmallIntegerSet) c);
      }
      if (c instanceof SmallIntegerSubSet) {
        return this.containsAll((SmallIntegerSubSet) c);
      }
      return this.containsAllGeneric(c);
    }

    private boolean containsAllGeneric(Collection<?> c) {
      // can't check for size because Collection could be a List
      // would have to check for Set and size
      for (Object each : c) {
        if (!this.contains(each)) {
          return false;
        }
      }
      return true;
    }

    private boolean containsAll(SmallIntegerSet other) {
      return SmallIntegerSet.containsAll(this.bits(), other.values);
    }

    private boolean containsAll(SmallIntegerSubSet other) {
      return SmallIntegerSet.containsAll(this.bits(), other.bits());
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      if (c instanceof SmallIntegerSet) {
        return this.removeAll((SmallIntegerSet) c);
      }
      if (c instanceof SmallIntegerSubSet) {
        return this.removeAll((SmallIntegerSubSet) c);
      }
      return this.removeAllGeneric(c);
    }

    private boolean removeAllGeneric(Collection<?> c) {
      // TODO check size, iterate over other
      boolean changed = false;
      for (Object each : c) {
        changed |= this.remove(each);
      }
      return changed;
    }

    private boolean removeAll(SmallIntegerSet other) {
      return SmallIntegerSet.this.removeAll(other.values & this.mask);
    }

    private boolean removeAll(SmallIntegerSubSet other) {
      return SmallIntegerSet.this.removeAll(other.bits() & this.mask);
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
      if (c instanceof SmallIntegerSet) {
        return this.addAll((SmallIntegerSet) c);
      }
      if (c instanceof SmallIntegerSubSet) {
        return this.addAll((SmallIntegerSubSet) c);
      }
      return this.addAllGeneric(c);
    }

    private boolean addAllGeneric(Collection<? extends Integer> c) {
      boolean changed = false;
      for (Integer each : c) {
        changed |= this.add(each);
      }
      return changed;
    }

    private boolean addAll(SmallIntegerSet other) {
      long bits = other.values;
      if ((bits & this.mask) != bits) {
        throw new IllegalArgumentException();
      }
      return SmallIntegerSet.this.addAll(bits);
    }

    private boolean addAll(SmallIntegerSubSet other) {
      long bits = other.bits();
      if ((bits & this.mask) != bits) {
        throw new IllegalArgumentException();
      }
      return SmallIntegerSet.this.addAll(bits);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      if (c instanceof SmallIntegerSet) {
        return this.retainAll((SmallIntegerSet) c);
      }
      if (c instanceof SmallIntegerSubSet) {
        return this.retainAll((SmallIntegerSubSet) c);
      }
      return this.retainAllGeneric(c);
    }

    private boolean retainAllGeneric(Collection<?> c) {
      boolean modified = false;
      long bits = this.bits();
      for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
        if (SmallIntegerSet.isSetNoCheck(bits, i) && !c.contains(i)) {
          SmallIntegerSet.this.unsetNoCheck(i);
          modified = true;
        }
      }
      return modified;
    }

    private boolean retainAll(SmallIntegerSubSet other) {
      return SmallIntegerSet.this.retainAll(other.bits() | ~this.mask);
    }

    private boolean retainAll(SmallIntegerSet other) {
      return SmallIntegerSet.this.retainAll(other.values | ~this.mask);
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
      if (obj instanceof SmallIntegerSubSet) {
        return this.bits() == ((SmallIntegerSubSet) obj).bits();
      }

      Set<?> other = (Set<?>) obj;
      if (this.size() != other.size()) {
        return false;
      }
      return containsAllNonThrowing(this.bits(), other);
    }

    @Override
    public void forEach(Consumer<? super Integer> action) {
      SmallIntegerSet.forEach(this.bits(), action);
    }

    final class SmallIntegerSubSetIterator extends AbstractIntegerSetIterator {

      @Override
      boolean isSetNoCheck(int i) {
        return SmallIntegerSet.isSetNoCheck(SmallIntegerSubSet.this.bits(), i);
      }

      @Override
      void unsetNoCheck(int i) {
        SmallIntegerSet.this.unsetNoCheck(i);
      }

    }

  }

}
