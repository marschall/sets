package com.github.marschall.sets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
    assertArrayEquals(new Integer[] {10, 50}, subSet.toArray());
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

}
