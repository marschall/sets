package com.github.marschall.sets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static com.github.marschall.sets.Lists.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SortedSetTest {

  private SortedSet<Integer> set;
  private final Supplier<SortedSet<Integer>> setFactory;

  public SortedSetTest(Supplier<SortedSet<Integer>> setFactory) {
    this.setFactory = setFactory;
  }

  @Parameters
  public static Collection<Object[]> sets() {
    return Arrays.asList(
            new Supplier[] {TreeSet::new},
            new Supplier[] {SmallIntegerSet::new});
  }

  @Before
  public void setUp() throws ReflectiveOperationException {
    this.set = this.setFactory.get();
  }

  @Test
  public void comparator() {
    assertNull(set.comparator());
//    assertSame(Collections.reverseOrder(), set.descendingSet().comparator());
    assertNull(set.subSet(0, 11).comparator());
  }

  @Test
  public void subSet() {
    this.set.add(1);
    SortedSet<Integer> subSet = this.set.subSet(1, 1);
    assertEquals(0, subSet.size());
    assertFalse(subSet.contains(1));

    subSet = this.set.subSet(1, 2);
    assertEquals(1, subSet.size());
    assertTrue(subSet.contains(1));
  }

  @Test
  public void headSet() {
    this.set.add(1);
    this.set.add(3);
    this.set.add(5);
    SortedSet<Integer> headSet = this.set.headSet(6);

    assertArrayEquals(new Integer[] {1, 3, 5}, headSet.toArray());

    headSet = this.set.headSet(5);

    assertArrayEquals(new Integer[] {1, 3}, headSet.toArray());

    headSet = this.set.headSet(2);

    assertArrayEquals(new Integer[] {1}, headSet.toArray());
  }

  @Test
  public void headSetEdgetCases() {
    this.set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));

    SortedSet<Integer> headSet = this.set.headSet(1);
    assertEquals(1, headSet.size());

    headSet = this.set.headSet(SmallIntegerSet.MAX_VALUE);
    assertEquals(63, headSet.size());

    headSet = this.set.headSet(SmallIntegerSet.MAX_VALUE + 1);
    assertEquals(64, headSet.size());
  }

  @Test
  public void tailSet() {
    this.set.add(1);
    this.set.add(3);
    this.set.add(5);

    SortedSet<Integer> tailSet = this.set.tailSet(1);
    assertArrayEquals(new Integer[] {1, 3, 5}, tailSet.toArray());

    tailSet = this.set.tailSet(2);
    assertArrayEquals(new Integer[] {3, 5}, tailSet.toArray());

    tailSet = this.set.tailSet(5);
    assertArrayEquals(new Integer[] {5}, tailSet.toArray());
  }

  @Test
  public void tailSetEdgeCase() {
    this.set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));

    SortedSet<Integer> tailSet = this.set.tailSet(1);
    assertEquals(63, tailSet.size());

    tailSet = this.set.tailSet(SmallIntegerSet.MAX_VALUE);
    assertEquals(1, tailSet.size());

    tailSet = this.set.tailSet(SmallIntegerSet.MIN_VALUE);
    assertEquals(64, tailSet.size());
  }

  @Test
  public void clearSubSet() {
    this.set.addAll(Arrays.asList(10, 11, 12, 13));

    SortedSet<Integer> subSet = this.set.subSet(11, 13);
    subSet.clear();


    assertArrayEquals(new Integer[] {10, 13}, this.set.toArray());
  }

  @Test
  public void subSetEqualsSameType() {
    this.set.addAll(Arrays.asList(10, 11, 12, 13));

    SortedSet<Integer> subSet = this.set.subSet(11, 13);

    SortedSet<Integer> equalSet = this.setFactory.get();
    equalSet.addAll(Arrays.asList(11, 12));

    assertEquals(equalSet, subSet);
    assertEquals(subSet, equalSet);
    assertEquals(subSet, subSet);
  }

  @Test
  public void subSetEqualsSubset() {
    this.set.addAll(Arrays.asList(10, 11, 12));

    SortedSet<Integer> secondSet = this.setFactory.get();
    secondSet.addAll(Arrays.asList(11, 12, 13));

    assertEquals(this.set.subSet(11, 13), secondSet.subSet(11, 13));
  }

  @Test
  public void subSetNotEqualsSubset() {
    this.set.addAll(Arrays.asList(10, 11, 12));

    SortedSet<Integer> secondSet = this.setFactory.get();
    secondSet.addAll(Arrays.asList(11, 12, 13, 14));

    assertNotEquals(this.set.subSet(11, 13), secondSet.subSet(11, 14));
  }

  @Test
  public void subSetEqualsOtherSet() {
    this.set.addAll(Arrays.asList(10, 11, 12));

    Set<Integer> other = new HashSet<>();
    other.addAll(Arrays.asList(11, 12));

    assertEquals(this.set.subSet(11, 13), other);
  }

  @Test
  public void subSetNotEqualsOtherSet() {
    this.set.addAll(Arrays.asList(10, 11, 12));

    Set<Integer> other = new HashSet<>();
    other.addAll(Arrays.asList(10, 11, 12));

    assertNotEquals(this.set.subSet(11, 13), other);
  }

  @Test
  public void subSetNotEqualsDifferentType() {
    this.set.addAll(Arrays.asList(10, 11, 12, 13));

    SortedSet<Integer> subSet = this.set.subSet(11, 13);
    assertNotEquals(subSet, null);
    assertNotEquals(subSet, "aString");
  }

  @Test
  public void subSetNotEquals() {
    this.set.addAll(Arrays.asList(10, 11, 12, 13));

    SortedSet<Integer> subSet = this.set.subSet(11, 14);

    SortedSet<Integer> notEqualSet = this.setFactory.get();
    notEqualSet.addAll(Arrays.asList(11, 12));

    assertNotEquals(notEqualSet, subSet);
    assertNotEquals(subSet, notEqualSet);
  }

  @Test
  public void subSetRange() {
    this.set.add(1);
    this.set.add(2);
    this.set.add(3);
    SortedSet<Integer> subSet = this.set.subSet(1, 3);

    SortedSet<Integer> subSet2 = subSet.subSet(2, 3);
    assertNotNull(subSet2);

    try {
      subSet2.add(3);
      fail("3 should not be allowed");
    } catch (IllegalArgumentException e) {
      // should reach here
    }

    try {
      subSet2.add(0);
      fail("0 should not be allowed");
    } catch (IllegalArgumentException e) {
      // should reach here
    }
  }

  @Test
  public void subSetContains() {
    this.set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));
    SortedSet<Integer> subSet = set.subSet(1, SmallIntegerSet.MAX_VALUE);

    assertTrue(this.set.contains(SmallIntegerSet.MIN_VALUE));
    assertFalse(subSet.contains(SmallIntegerSet.MIN_VALUE));

    assertTrue(this.set.contains(SmallIntegerSet.MAX_VALUE));
    assertFalse(subSet.contains(SmallIntegerSet.MAX_VALUE));

    assertFalse(subSet.contains(SmallIntegerSet.MAX_VALUE + 1));
    assertFalse(subSet.contains(SmallIntegerSet.MIN_VALUE - 1));
  }

  @Test
  public void subSetRemove() {
    this.set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));
    SortedSet<Integer> subSet = this.set.subSet(1, SmallIntegerSet.MAX_VALUE);

    assertFalse(subSet.remove(SmallIntegerSet.MIN_VALUE));
    assertTrue(this.set.contains(SmallIntegerSet.MIN_VALUE));

    assertTrue(this.set.contains(2));
    assertTrue(subSet.remove(2));
    assertFalse(this.set.contains(2));

    assertTrue(subSet.contains(3));
    assertTrue(this.set.remove(3));
    assertFalse(subSet.contains(2));
  }

  @Test
  public void subSetRemoveAll() {
    this.set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));
    SortedSet<Integer> subSet = this.set.subSet(1, SmallIntegerSet.MAX_VALUE);

    if (subSet instanceof TreeSet) {
      assertTrue(subSet.removeAll((TreeSet<?>) ((TreeSet<?>) subSet.subSet(2, SmallIntegerSet.MAX_VALUE - 1)).clone()));
    } else {
      assertTrue(subSet.removeAll(subSet.subSet(2, SmallIntegerSet.MAX_VALUE - 1)));
    }

    assertArrayEquals(new Integer[] {0, 1, SmallIntegerSet.MAX_VALUE - 1, SmallIntegerSet.MAX_VALUE}, this.set.toArray());
    assertArrayEquals(new Integer[] {1, SmallIntegerSet.MAX_VALUE - 1}, subSet.toArray());
  }

  @Test
  public void subSetRemoveAllLarger() {
    this.set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));

    SortedSet<Integer> subSet = this.set.subSet(1, SmallIntegerSet.MAX_VALUE);
    assertTrue(subSet.removeAll(this.set));

    assertArrayEquals(new Integer[] {0, SmallIntegerSet.MAX_VALUE}, this.set.toArray());
    assertTrue(subSet.isEmpty());
  }

  @Test
  public void subSetRemoveAllOtherType() {
    this.set.addAll(Arrays.asList(10, 30, 50));

    Set<Integer> toRemove = new HashSet<Integer>(4);
    toRemove.addAll(Arrays.asList(10, 30, 50));

    SortedSet<Integer> subSet = this.set.subSet(20, 40);
    assertTrue(subSet.removeAll(toRemove));

    assertTrue(subSet.isEmpty());
    assertArrayEquals(new Integer[] {10, 50}, this.set.toArray());
  }

  @Test
  public void subSetRemoveIf() {
    this.set.addAll(Arrays.asList(0, 1, 2, 3, 4, 5));

    SortedSet<Integer> subSet = this.set.subSet(1, 5);

    subSet.removeIf(i -> i % 2 == 0);
    assertArrayEquals(new Object[] {1, 3}, subSet.toArray());
  }

  @Test
  public void subSetContainsAllOuterSet() {
    this.set.addAll(Arrays.asList(10, 20, 30, 40));

    SortedSet<Integer> subSet = this.set.subSet(20, 31);
    SortedSet<Integer> otherSet = this.setFactory.get();
    otherSet.addAll(Arrays.asList(20, 30));

    assertTrue(subSet.containsAll(otherSet));

    otherSet = this.setFactory.get();
    otherSet.addAll(Arrays.asList(10, 20, 30, 40));
    assertTrue(this.set.containsAll(otherSet));
    assertFalse(subSet.containsAll(otherSet));
  }

  @Test
  public void subSetContainsAllSubSet() {
    this.set.addAll(Arrays.asList(10, 20, 30, 40, 50, 60));

    SortedSet<Integer> largerSubSet = this.set.subSet(20, 51);
    SortedSet<Integer> smallerSubSet = largerSubSet.subSet(20, 41);

    assertTrue(largerSubSet.containsAll(smallerSubSet));
    assertFalse(smallerSubSet.containsAll(largerSubSet));

    assertFalse(smallerSubSet.containsAll(this.set));
    assertFalse(largerSubSet.containsAll(this.set));
  }

  @Test
  public void subSetContainsAllOtherType() {
    this.set.addAll(Arrays.asList(30, 40, 50, 60));

    SortedSet<Integer> subSet = this.set.subSet(40, 51);

    Set<Integer> other = new HashSet<Integer>(4);
    other.addAll(Arrays.asList(40, 50));

    assertTrue(subSet.containsAll(other));

    other = new HashSet<Integer>(this.set);
    assertTrue(this.set.containsAll(other));
    assertFalse(subSet.containsAll(other));
  }

  @Test
  public void subSetAdd() {
    SortedSet<Integer> subSet = this.set.subSet(1, SmallIntegerSet.MAX_VALUE);

    assertTrue(subSet.add(1));
    assertTrue(this.set.contains(1));

    assertTrue(this.set.add(2));
    assertTrue(subSet.contains(2));
  }

  @Test
  public void subSetAddAllOuterType() {
    SortedSet<Integer> subSet = this.set.subSet(0, 11);

    SortedSet<Integer> outerType = this.setFactory.get();
    outerType.addAll(Arrays.asList(1, 2, 3));

    assertTrue(subSet.addAll(outerType));

    assertArrayEquals(new Integer[] {1, 2, 3}, subSet.toArray());
    assertArrayEquals(new Integer[] {1, 2, 3}, this.set.toArray());
  }

  @Test
  public void subSetAddSubSet() {
    SortedSet<Integer> subSet = this.set.subSet(0, 11);

    SortedSet<Integer> outerType = this.setFactory.get();
    outerType.addAll(Arrays.asList(1, 2, 3, 11));

    assertTrue(subSet.addAll(outerType.subSet(1, 5)));

    assertArrayEquals(new Integer[] {1, 2, 3}, subSet.toArray());
    assertArrayEquals(new Integer[] {1, 2, 3}, this.set.toArray());

  }

  @Test
  public void subSetAddAllOutOfRange() {
    SortedSet<Integer> subSet = this.set.subSet(0, 11);

    SortedSet<Integer> toAdd = this.set.subSet(5, 16);
    toAdd.addAll(Arrays.asList(10, 15));

    try {
      subSet.addAll(toAdd);
      fail("should be out of range");
    } catch (IllegalArgumentException e) {
      // should reach here
    }
  }

  @Test
  public void subSetAddAllOutOfRangeOuter() {
    SortedSet<Integer> subSet = this.set.subSet(0, 11);

    SortedSet<Integer> toAdd = this.setFactory.get();
    toAdd.addAll(Arrays.asList(10, 15));

    try {
      subSet.addAll(toAdd);
      fail("should be out of range");
    } catch (IllegalArgumentException e) {
      // should reach here
    }
  }

  @Test
  public void subSetAddAllOtherType() {
    SortedSet<Integer> subSet = this.set.subSet(0, 11);

    assertTrue(subSet.addAll(Arrays.asList(6, 4, 2)));

    assertArrayEquals(new Integer[] {2, 4, 6}, this.set.toArray());
  }

  @Test
  public void subSetRetainAllOuterType() {
    this.set.addAll(Arrays.asList(1, 10, 15, 20, 30));

    SortedSet<Integer> subSet = this.set.subSet(10, 21);

    SortedSet<Integer> toRetain = this.setFactory.get();
    toRetain.addAll(Arrays.asList(10, 20));

    assertTrue(subSet.retainAll(toRetain));

    assertArrayEquals(new Integer[] {1, 10, 20, 30}, this.set.toArray());
    assertArrayEquals(new Integer[] {10, 20}, subSet.toArray());
  }

  @Test
  public void subSetRetainAllOtherType() {
    this.set.addAll(Arrays.asList(1, 10, 15, 20, 30));

    SortedSet<Integer> subSet = this.set.subSet(10, 21);

    Set<Integer> toRetain = new HashSet<>(4);
    toRetain.addAll(Arrays.asList(10, 20));

    assertTrue(subSet.retainAll(toRetain));

    assertArrayEquals(new Integer[] {1, 10, 20, 30}, this.set.toArray());
    assertArrayEquals(new Integer[] {10, 20}, subSet.toArray());
  }

  @Test
  public void subSetRetainAllSubSetType() {
    this.set.addAll(Arrays.asList(1, 10, 15, 20, 30));

    SortedSet<Integer> subSet = this.set.subSet(10, 21);

    SortedSet<Integer> toRetain = this.setFactory.get();
    toRetain.addAll(Arrays.asList(10, 15, 20));

    assertTrue(subSet.retainAll(toRetain.subSet(10, 16)));

    assertArrayEquals(new Integer[] {1, 10, 15, 30}, this.set.toArray());
    assertArrayEquals(new Integer[] {10, 15}, subSet.toArray());
  }

  @Test
  public void outerSetContainsAll() {
    this.set.addAll(Arrays.asList(1, 2, 3, 4));

    SortedSet<Integer> subSet = this.set.subSet(2, 4);
    assertTrue(this.set.containsAll(subSet));
  }

  @Test
  public void outerSetRemoveAll() {
    this.set.addAll(Arrays.asList(1, 2, 3, 4));

    SortedSet<Integer> toRemoveOuter = this.setFactory.get();
    toRemoveOuter.addAll(Arrays.asList(2, 3, 4, 5, 6));

    assertTrue(this.set.removeAll(toRemoveOuter.subSet(3, 6)));
    assertArrayEquals(new Integer[] {1, 2}, this.set.toArray());
  }

  @Test
  public void outerSetAddAll() {
    this.set.addAll(Arrays.asList(1, 2));

    SortedSet<Integer> toAddOuter = this.setFactory.get();
    toAddOuter.addAll(Arrays.asList(3, 4, 5, 6));

    assertTrue(this.set.addAll(toAddOuter.subSet(4, 6)));
    assertArrayEquals(new Integer[] {1, 2, 4, 5}, this.set.toArray());
  }

  @Test
  public void outerSetRetainAll() {
    this.set.addAll(Arrays.asList(1, 2, 3, 4));

    this.set.retainAll(this.set.subSet(2, 4));
    assertArrayEquals(new Integer[] {2, 3}, this.set.toArray());
  }

  @Test
  public void subSetEdgeCases() {
    this.set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));
    SortedSet<Integer> subSet = this.set.subSet(1, SmallIntegerSet.MAX_VALUE);

    assertEquals(Integer.valueOf(1), subSet.first());
    assertEquals(Integer.valueOf(SmallIntegerSet.MAX_VALUE - 1), subSet.last());

    assertEquals(62, subSet.size());
  }

  @Test
  public void subSetForEach() {
    this.set.addAll(Arrays.asList(1, 9, 12, 25));


    SortedSet<Integer> subSet = this.set.subSet(5, 15);
    List<Integer> seen = new ArrayList<>(2);
    subSet.forEach(seen::add);

    assertEquals(Arrays.asList(9, 12), seen);
  }

  @Test
  public void subSetToArrayArrayArgument() {
    this.set.addAll(Arrays.asList(3, 9, 12, 15));

    SortedSet<Integer> subSet = this.set.subSet(9, 13);
    Object[] result = subSet.toArray(new Integer[0]);
    assertArrayEquals(new Object[] {9, 12}, result);

    Integer[] array = new Integer[2];
    result = subSet.toArray(array);
    assertArrayEquals(new Object[] {9, 12}, result);
    assertSame(array, result);
  }

  @Test
  public void subSetToArrayArrayArgumentSetNull() {
    this.set.addAll(Arrays.asList(3, 9, 12, 15));

    SortedSet<Integer> subSet = this.set.subSet(9, 13);

    Object[] array = new Object[4];
    Arrays.fill(array, 1);

    Object[] result = subSet.toArray(array);
    assertSame(array, result);

    assertArrayEquals(new Object[] {9, 12, null, 1}, result);
  }

  @Test
  public void subSetToString() {
    SortedSet<Integer> subSet = this.set.subSet(10, 21);

    assertEquals(this.set.toString(), subSet.toString());

    this.set.add(11);
    assertEquals(this.set.toString(), subSet.toString());

    this.set.add(12);
    assertEquals(this.set.toString(), subSet.toString());

    this.set.add(9);
    Set<Integer> equalSet = this.setFactory.get();
    equalSet.addAll(Arrays.asList(11, 12));

    assertEquals(equalSet.toString(), subSet.toString());
    assertNotEquals(this.set.toString(), subSet.toString());
  }

  @Test
  public void subSetHashCode() {
    this.set.addAll(Arrays.asList(3, 9, 12, 15));

    SortedSet<Integer> subSet = this.set.subSet(9, 13);

    Set<Integer> equalSet = new HashSet<>(Arrays.asList(9, 12));

    assertEquals(equalSet.hashCode(), subSet.hashCode());
  }

  @Test
  public void iteratorEdgeCases() {
    this.set.add(SmallIntegerSet.MIN_VALUE);
    this.set.add(1);
    this.set.add(SmallIntegerSet.MAX_VALUE);

    assertEquals(Collections.emptyList(),
            toList(this.set.subSet(2, SmallIntegerSet.MAX_VALUE).iterator()));

    assertEquals(Arrays.asList(SmallIntegerSet.MIN_VALUE),
            toList(this.set.headSet(1).iterator()));

    assertEquals(Arrays.asList(SmallIntegerSet.MAX_VALUE),
            toList(this.set.tailSet(SmallIntegerSet.MAX_VALUE).iterator()));

    assertEquals(Arrays.asList(SmallIntegerSet.MIN_VALUE, 1),
            toList(this.set.headSet(SmallIntegerSet.MAX_VALUE).iterator()));
  }



  @Test
  public void emptyIteratorSemantics() {
    assertFalse(this.set.subSet(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE + 1).iterator().hasNext());

    try {
      this.set.subSet(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE + 1).iterator().next();
      fail("iterator should not have next");
    } catch (NoSuchElementException e) {
      // should reach here
    }
  }


  @Test
  public void iteratorRemove() {
    this.set.add(1);
    this.set.add(SmallIntegerSet.MAX_VALUE - 1);
    SortedSet<Integer> headSet = this.set.headSet(SmallIntegerSet.MAX_VALUE - 1);
    Iterator<Integer> iterator = headSet.iterator();
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
    assertTrue(headSet.isEmpty());
  }

  @Test
  public void oneElementIteratorSemantics() {
    this.set.add(1);
    this.set.add(SmallIntegerSet.MAX_VALUE - 1);
    Iterator<Integer> iterator = this.set.headSet(SmallIntegerSet.MAX_VALUE - 1).iterator();
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
    this.set.addAll(Arrays.asList(SmallIntegerSet.MIN_VALUE, 11, 22, SmallIntegerSet.MAX_VALUE));
    Iterator<Integer> iterator = this.set.subSet(SmallIntegerSet.MIN_VALUE + 1, SmallIntegerSet.MAX_VALUE).iterator();

    List<Integer> acc = new ArrayList<>(2);
    iterator.forEachRemaining(acc::add);

    assertEquals(Arrays.asList(11, 22), acc);
    assertFalse(iterator.hasNext());
  }

  @Test
  public void forEachRemainingSkipOne() {
    this.set.addAll(Arrays.asList(SmallIntegerSet.MIN_VALUE, 11, 22, SmallIntegerSet.MAX_VALUE));
    Iterator<Integer> iterator = this.set.subSet(SmallIntegerSet.MIN_VALUE + 1, SmallIntegerSet.MAX_VALUE).iterator();
    iterator.next();

    List<Integer> acc = new ArrayList<>(1);
    iterator.forEachRemaining(acc::add);

    assertEquals(Collections.singletonList(22), acc);
    assertFalse(iterator.hasNext());
  }

  @Test
  public void forEachRemainingEmpty() {
    this.set.addAll(Arrays.asList(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE));
    SortedSet<Integer> subSet = this.set.subSet(SmallIntegerSet.MIN_VALUE + 1, SmallIntegerSet.MAX_VALUE);
    subSet.iterator().forEachRemaining(e -> fail("should not have any more elements"));
  }

}
