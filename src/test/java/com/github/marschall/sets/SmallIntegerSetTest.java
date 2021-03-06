package com.github.marschall.sets;

import static com.github.marschall.sets.Lists.toList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SmallIntegerSetTest {

  private SortedSet<Integer> set;

  @BeforeEach
  public void setUp() {
    this.set = new SmallIntegerSet();
  }

  @Test
  public void isSupported() {
    assertFalse(SmallIntegerSet.isSupported(null));
    assertFalse(SmallIntegerSet.isSupported(SmallIntegerSet.MIN_VALUE - 1));
    assertFalse(SmallIntegerSet.isSupported(SmallIntegerSet.MAX_VALUE + 1));
    assertFalse(SmallIntegerSet.isSupported((Integer) (SmallIntegerSet.MIN_VALUE - 1)));
    assertFalse(SmallIntegerSet.isSupported((Integer) (SmallIntegerSet.MAX_VALUE + 1)));

    assertTrue(SmallIntegerSet.isSupported(SmallIntegerSet.MIN_VALUE));
    assertTrue(SmallIntegerSet.isSupported(SmallIntegerSet.MAX_VALUE));
    assertTrue(SmallIntegerSet.isSupported((Integer) (SmallIntegerSet.MIN_VALUE)));
    assertTrue(SmallIntegerSet.isSupported((Integer) (SmallIntegerSet.MAX_VALUE)));
  }

  @Test
  public void isEmpty() {
    assertTrue(this.set.isEmpty());
    this.set.add(SmallIntegerSet.MIN_VALUE);
    assertFalse(this.set.isEmpty());
  }

  @Test
  public void addAndContains() {
    assertFalse(this.set.contains(SmallIntegerSet.MIN_VALUE - 1));
    assertFalse(this.set.contains(SmallIntegerSet.MAX_VALUE + 1));

    for (int i = SmallIntegerSet.MIN_VALUE; i <= SmallIntegerSet.MAX_VALUE; i++) {
      int expectedSize = i - SmallIntegerSet.MIN_VALUE;
      assertEquals(expectedSize, this.set.size());

      assertFalse(this.set.contains(i));
      assertTrue(this.set.add(i));
      assertFalse(this.set.add(i));
      assertTrue(this.set.contains(i));

      assertEquals(expectedSize + 1, this.set.size());
    }
  }

  @Test
  public void clear() {
    this.set.add(SmallIntegerSet.MIN_VALUE);

    this.set.clear();
    assertTrue(this.set.isEmpty());
    assertEquals(0, this.set.size());
  }

  @Test
  public void addNull() {
    assertThrows(NullPointerException.class, () -> this.set.add(null));
  }

  @Test
  public void addTooSmall() {
    assertThrows(IllegalArgumentException.class, () -> this.set.add(SmallIntegerSet.MIN_VALUE - 1));
  }

  @Test
  public void addTooLarge() {
    assertThrows(IllegalArgumentException.class, () -> this.set.add(SmallIntegerSet.MAX_VALUE + 1));
  }

  @Test
  public void containsAll() {
    this.set.add(9);
    this.set.add(12);
    this.set.add(23);

    assertTrue(this.set.containsAll(Arrays.asList(9, 12)));
    assertFalse(this.set.containsAll(Arrays.asList(9, 13)));
    assertFalse(this.set.containsAll(Arrays.asList(9, 12, 64)));
  }

  @Test
  public void containsAllSmallIntegerSet() {
    this.set.add(9);
    this.set.add(12);
    this.set.add(23);

    SmallIntegerSet other = new SmallIntegerSet();
    other.addAll(Arrays.asList(9, 12));
    assertTrue(this.set.containsAll(other));

    other.clear();
    other.addAll(Arrays.asList(9, 13));
    assertFalse(this.set.containsAll(other));

    other.clear();
    other.addAll(Arrays.asList(9, 12, 63));
    assertFalse(this.set.containsAll(other));
  }

  @Test
  public void containsNull() {
    assertThrows(NullPointerException.class, () -> this.set.contains(null));
  }

  @Test
  public void containsString() {
    assertThrows(ClassCastException.class, () -> this.set.contains("String"));
  }

  @Test
  public void addAllGeneric() {
    List<Integer> toAdd = Arrays.asList(1, 2);
    assertTrue(this.set.addAll(toAdd));

    assertTrue(this.set.contains(1));
    assertTrue(this.set.contains(2));
  }

  @Test
  public void addAllNoChange() {
    this.set.add(1);
    this.set.add(2);
    List<Integer> toAdd = Arrays.asList(1, 2);
    assertFalse(this.set.addAll(toAdd));
  }

  @Test
  public void addAllSmallIntegerSet() {
    this.set.addAll(Arrays.asList(1, 2, 10));

    SmallIntegerSet c = new SmallIntegerSet();
    c.addAll(Arrays.asList(10, 20, 21));

    assertTrue(this.set.addAll(c));
    assertEquals(5, this.set.size());
    assertTrue(this.set.contains(1));
    assertTrue(this.set.contains(2));
    assertTrue(this.set.contains(10));
    assertTrue(this.set.contains(20));
    assertTrue(this.set.contains(21));

    assertFalse(this.set.addAll(c));
  }

  @Test
  public void remove() {
    IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
      .boxed()
      .forEach(this.set::add);



    for (int i = SmallIntegerSet.MIN_VALUE; i <= SmallIntegerSet.MAX_VALUE; i++) {
      int expectedSize = (SmallIntegerSet.MAX_VALUE - SmallIntegerSet.MIN_VALUE) - i + 1;
      assertEquals(expectedSize, this.set.size());

      assertTrue(this.set.contains(i));
      assertTrue(this.set.remove(i));
      assertFalse(this.set.remove(i));
      assertFalse(this.set.contains(i));

      assertEquals(expectedSize - 1, this.set.size());
    }

    assertTrue(this.set.isEmpty());
  }

  @Test
  public void removeOutOfRange() {
    assertFalse(this.set.remove(SmallIntegerSet.MIN_VALUE - 1));
    assertFalse(this.set.remove(SmallIntegerSet.MAX_VALUE + 1));
  }

  @Test
  public void removeNull() {
    assertThrows(NullPointerException.class, () -> this.set.remove(null));
  }

  @Test
  public void removeString() {
    assertThrows(ClassCastException.class, () -> this.set.remove("String"));
  }

  @Test
  public void removeAll() {
    List<Integer> toRemove = Arrays.asList(9, 12, 13);

    assertFalse(this.set.removeAll(toRemove));

    this.set.addAll(Arrays.asList(9, 12, 18, 24));

    assertTrue(this.set.removeAll(toRemove));

    assertFalse(this.set.contains(9));
    assertFalse(this.set.contains(12));
    assertFalse(this.set.contains(13));
    assertTrue(this.set.contains(18));
    assertTrue(this.set.contains(24));

    assertFalse(this.set.removeAll(toRemove));
  }

  @Test
  public void removeAllSmallIntegerSet() {
    this.set.addAll(Arrays.asList(1, 9, 12));
    SmallIntegerSet toRemove = new SmallIntegerSet();
    toRemove.addAll(Arrays.asList(9, 12, 13));

    assertTrue(this.set.removeAll(toRemove));

    assertEquals(1, this.set.size());
    assertTrue(this.set.contains(1));
    assertFalse(this.set.contains(9));
    assertFalse(this.set.contains(12));
    assertFalse(this.set.contains(13));

    assertFalse(this.set.removeAll(toRemove));
  }

  @Test
  public void removeAllNull() {
    assertThrows(NullPointerException.class, () -> this.set.removeAll(Arrays.asList(9, null)));
  }

  @Test
  public void removeAllString() {
    assertThrows(ClassCastException.class, () -> this.set.removeAll(Arrays.asList(9, "String")));
  }

  @Test
  public void removeIf() {
    this.set.addAll(Arrays.asList(0, 1, 2, 3, 4, 5));

    this.set.removeIf(i -> i % 2 == 1);
    assertArrayEquals(new Object[] {0, 2, 4}, this.set.toArray());
  }

  @Test
  public void retainAll() {
    this.set.addAll(Arrays.asList(9, 12));

    assertTrue(this.set.retainAll(Arrays.asList(12, 24)));
    assertEquals(1, this.set.size());
    assertFalse(this.set.contains(9));
    assertTrue(this.set.contains(12));
    assertFalse(this.set.contains(24));

    assertFalse(this.set.retainAll(Arrays.asList(12, 24)));
  }

  @Test
  public void retainAllSmalIntegerSet() {
    this.set.addAll(Arrays.asList(9, 12));

    SmallIntegerSet toRetain = new SmallIntegerSet();
    toRetain.addAll(Arrays.asList(12, 24));

    assertTrue(this.set.retainAll(toRetain));
    assertEquals(1, this.set.size());
    assertFalse(this.set.contains(9));
    assertTrue(this.set.contains(12));
    assertFalse(this.set.contains(24));

    assertFalse(this.set.retainAll(toRetain));
  }

  @Test
  public void forEach() {
    this.set.add(9);
    this.set.add(12);

    List<Integer> seen = new ArrayList<>(2);
    this.set.forEach(seen::add);

    assertEquals(Arrays.asList(9, 12), seen);
  }

  @Test
  public void toArrayArrayArgument() {
    this.set.add(9);
    this.set.add(12);

    Object[] result = this.set.toArray(new Integer[0]);
    assertArrayEquals(new Object[] {9, 12}, result);

    Integer[] array = new Integer[2];
    result = this.set.toArray(array);
    assertArrayEquals(new Object[] {9, 12}, result);
    assertSame(array, result);
  }

  @Test
  public void toArrayArrayArgumentSetNull() {
    Object[] array = new Object[4];
    Arrays.fill(array, 1);

    this.set.add(9);
    this.set.add(12);

    Object[] result = this.set.toArray(array);
    assertSame(array, result);

    assertArrayEquals(new Object[] {9, 12, null, 1}, result);
  }

  @Test
  public void toArrayArrayArgumentKeepComponentType() {
    this.set.add(9);

    Object[] result = this.set.toArray(new Object[0]);
    assertEquals(Object.class, result.getClass().getComponentType());

    result = this.set.toArray(new Integer[0]);
    assertEquals(Integer.class, result.getClass().getComponentType());
  }

  @Test
  public void toArrayNoArgument() {
    this.set.add(9);
    this.set.add(12);

    assertArrayEquals(new Object[] {9, 12}, this.set.toArray());
  }

  @Test
  public void testClone() {
    this.set.add(1);

    @SuppressWarnings("unchecked")
    Set<Integer> clone = (Set<Integer>) ((SmallIntegerSet) this.set).clone();
    assertTrue(clone.contains(1));

    clone.add(2);

    assertTrue(clone.contains(2));
    assertEquals(2, clone.size());
    assertFalse(this.set.contains(2));
    assertEquals(1, this.set.size());
  }

  @Test
  public void testHashCode() {
    this.set.add(9);
    this.set.add(12);

    Set<Integer> equalSet1 = new HashSet<>(4);
    equalSet1.add(9);
    equalSet1.add(12);

    Set<Integer> equalSet2 = new TreeSet<>();
    equalSet2.add(9);
    equalSet2.add(12);

    assertEquals(this.set.hashCode(), equalSet1.hashCode());
    assertEquals(this.set.hashCode(), equalSet2.hashCode());
  }

  @Test
  public void testEquals() {
    assertEquals(this.set, Collections.emptySet());
    assertEquals(Collections.emptySet(), this.set);

    this.set.add(9);
    this.set.add(12);

    Set<Integer> equalSet1 = new HashSet<>(4);
    equalSet1.add(9);
    equalSet1.add(12);

    Set<Integer> equalSet2 = new TreeSet<>();
    equalSet2.add(9);
    equalSet2.add(12);

    Set<Integer> equalSet3 = new SmallIntegerSet();
    equalSet3.add(9);
    equalSet3.add(12);

    assertEquals(this.set, this.set);

    assertEquals(this.set, equalSet1);
    assertEquals(this.set, equalSet2);

    assertEquals(equalSet1, this.set);
    assertEquals(equalSet2, this.set);

    assertEquals(equalSet3, this.set);
    assertEquals(this.set, equalSet3);
  }

  @Test
  public void testNotEquals() {
    this.set.add(9);
    this.set.add(12);

    assertNotEquals(this.set, Arrays.asList(9, 12));
    assertNotEquals(this.set, Arrays.asList(9, 12, 13));
    assertNotEquals(this.set, Arrays.asList(9));

    assertNotEquals(Arrays.asList(9, 12), this.set);
    assertNotEquals(Arrays.asList(9, 12, 13), this.set);
    assertNotEquals(Arrays.asList(9), this.set);

    Set<Object> notEqualSet1 = new HashSet<>(4);
    notEqualSet1.add(9);
    notEqualSet1.add("12");

    assertNotEquals(this.set, notEqualSet1);
    assertNotEquals(notEqualSet1, this.set);

    Set<Object> notEqualSet2 = new HashSet<>(4);
    notEqualSet2.add(9);
    notEqualSet2.add(null);

    assertNotEquals(this.set, notEqualSet2);
    assertNotEquals(notEqualSet2, this.set);

    Set<Object> notEqualSet3 = new HashSet<>(4);
    notEqualSet3.add(9);
    notEqualSet3.add(13);

    assertNotEquals(this.set, notEqualSet3);
    assertNotEquals(notEqualSet3, this.set);

    Set<Object> notEqualSet4 = new HashSet<>(4);
    notEqualSet4.add(9);
    notEqualSet4.add(12);
    notEqualSet4.add(13);

    assertNotEquals(this.set, notEqualSet4);
    assertNotEquals(notEqualSet4, this.set);

    Set<Integer> notEqualSet5 = new SmallIntegerSet();
    notEqualSet5.add(9);
    notEqualSet5.add(12);
    notEqualSet5.add(13);

    assertNotEquals(this.set, notEqualSet5);

    assertNotEquals(this.set, null);
    assertNotEquals(this.set, "aString");
  }

  @Test
  public void testToString() {
    assertEquals(Collections.emptySet().toString(), this.set.toString());

    this.set.add(9);
    this.set.add(12);

    Set<Integer> equalSet = new TreeSet<>();
    equalSet.add(9);
    equalSet.add(12);

    assertEquals(equalSet.toString(), this.set.toString());
  }

  @Test
  public void identity() {
    Integer i = new Integer(1);
    this.set.add(i);
    assertEquals(i, this.set.iterator().next());
    assertNotSame(i, this.set.iterator().next());
  }

  @Test
  public void iteratorEdgeCases() {
    assertEquals(Collections.emptyList(), toList(this.set.iterator()));

    this.set.add(SmallIntegerSet.MIN_VALUE);
    assertEquals(Arrays.asList(SmallIntegerSet.MIN_VALUE), toList(this.set.iterator()));

    this.set.clear();

    this.set.add(SmallIntegerSet.MAX_VALUE);
    assertEquals(Arrays.asList(SmallIntegerSet.MAX_VALUE), toList(this.set.iterator()));

    this.set.add(SmallIntegerSet.MIN_VALUE);
    assertEquals(Arrays.asList(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE), toList(this.set.iterator()));
  }



  @Test
  public void emptyIteratorSemantics() {
    assertFalse(this.set.iterator().hasNext());

    try {
      this.set.iterator().next();
      fail("iterator should not have next");
    } catch (NoSuchElementException e) {
      // should reach here
    }
  }


  @Test
  public void iteratorRemove() {
    this.set.add(1);
    Iterator<Integer> iterator = this.set.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(Integer.valueOf(1), iterator.next());
    iterator.remove();
    assertFalse(iterator.hasNext());
    try {
      iterator.remove();
      fail("iterator should no longer allow remove");
    } catch (IllegalStateException e) {
      // should reach here
    }
    assertTrue(this.set.isEmpty());
  }

  @Test
  public void oneElementIteratorSemantics() {
    this.set.add(1);
    Iterator<Integer> iterator = this.set.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(Integer.valueOf(1), iterator.next());
    assertFalse(iterator.hasNext());
    try {
      iterator.next();
      fail("iterator should not have next");
    } catch (NoSuchElementException e) {
      // should reach here
    }
  }

  @Test
  public void forEachRemainingFromStart() {
    this.set.addAll(Arrays.asList(11, 22));
    Iterator<Integer> iterator = this.set.iterator();

    List<Integer> acc = new ArrayList<>(2);
    iterator.forEachRemaining(acc::add);

    assertEquals(Arrays.asList(11, 22), acc);
    assertFalse(iterator.hasNext());
  }

  @Test
  public void forEachRemainingSkipOne() {
    this.set.addAll(Arrays.asList(11, 22));
    Iterator<Integer> iterator = this.set.iterator();
    iterator.next();

    List<Integer> acc = new ArrayList<>(1);
    iterator.forEachRemaining(acc::add);

    assertEquals(Collections.singletonList(22), acc);
    assertFalse(iterator.hasNext());
  }

  @Test
  public void forEachRemainingEmpty() {
    this.set.iterator().forEachRemaining(e -> fail("should not have any more elements"));
  }

  @Test
  public void noFirst() {
    assertThrows(NoSuchElementException.class, () -> this.set.first());
  }

  @Test
  public void first() {
    this.set.add(SmallIntegerSet.MAX_VALUE);
    assertEquals(Integer.valueOf(SmallIntegerSet.MAX_VALUE), this.set.first());

    this.set.add(SmallIntegerSet.MIN_VALUE);
    assertEquals(Integer.valueOf(SmallIntegerSet.MIN_VALUE), this.set.first());
  }

  @Test
  public void noLast() {
    assertThrows(NoSuchElementException.class, () -> this.set.last());
  }

  @Test
  public void last() {
    this.set.add(SmallIntegerSet.MIN_VALUE);
    assertEquals(Integer.valueOf(SmallIntegerSet.MIN_VALUE), this.set.last());

    this.set.add(SmallIntegerSet.MAX_VALUE);
    assertEquals(Integer.valueOf(SmallIntegerSet.MAX_VALUE), this.set.last());
  }

  @Test
  public void emptySet() {
    Set<Integer> emptySet = Collections.emptySet();
    assertNotNull(emptySet);
  }

}
