package com.github.marschall.sets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
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

  // TODO modifications reflected

  private SortedSet<Integer> set;
  private Supplier<SortedSet<Integer>> setFactory;

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
    set.add(1);
    SortedSet<Integer> subSet = set.subSet(1, 1);
    assertEquals(0, subSet.size());
    assertFalse(subSet.contains(1));

    subSet = set.subSet(1, 2);
    assertEquals(1, subSet.size());
    assertTrue(subSet.contains(1));
  }

  @Test
  public void headSet() {
    set.add(1);
    set.add(3);
    set.add(5);
    SortedSet<Integer> headSet = set.headSet(6);

    assertArrayEquals(new Integer[] {1, 3, 5}, headSet.toArray());

    headSet = set.headSet(5);

    assertArrayEquals(new Integer[] {1, 3}, headSet.toArray());

    headSet = set.headSet(2);

    assertArrayEquals(new Integer[] {1}, headSet.toArray());
  }

  @Test
  public void headSetEdgetCases() {
    set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));

    SortedSet<Integer> headSet = set.headSet(1);
    assertEquals(1, headSet.size());

    headSet = set.headSet(SmallIntegerSet.MAX_VALUE);
    assertEquals(63, headSet.size());

    headSet = set.headSet(SmallIntegerSet.MAX_VALUE + 1);
    assertEquals(64, headSet.size());
  }

  @Test
  public void tailSet() {
    set.add(1);
    set.add(3);
    set.add(5);

    SortedSet<Integer> tailSet = set.tailSet(1);
    assertArrayEquals(new Integer[] {1, 3, 5}, tailSet.toArray());

    tailSet = set.tailSet(2);
    assertArrayEquals(new Integer[] {3, 5}, tailSet.toArray());

    tailSet = set.tailSet(5);
    assertArrayEquals(new Integer[] {5}, tailSet.toArray());
  }

  @Test
  public void tailSetEdgeCase() {
    set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));

    SortedSet<Integer> tailSet = set.tailSet(1);
    assertEquals(63, tailSet.size());

    tailSet = set.tailSet(SmallIntegerSet.MAX_VALUE);
    assertEquals(1, tailSet.size());

    tailSet = set.tailSet(SmallIntegerSet.MIN_VALUE);
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
  public void subSetEquals() {
    this.set.addAll(Arrays.asList(10, 11, 12, 13));

    SortedSet<Integer> subSet = this.set.subSet(11, 13);

    SortedSet<Integer> equalSet = this.setFactory.get();
    equalSet.addAll(Arrays.asList(11, 12));

    assertEquals(equalSet, subSet);
    assertEquals(subSet, equalSet);
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
    set.add(1);
    set.add(2);
    set.add(3);
    SortedSet<Integer> subSet = set.subSet(1, 3);

    SortedSet<Integer> subSet2 = subSet.subSet(2, 3);
    assertNotNull(subSet2);

    try {
      subSet2.add(3);
      fail("3 should not be allowed");
    } catch (IllegalArgumentException e) {
      // should reach here
    }
  }



  @Test
  public void subSetEdgeCases() {
    set.addAll(IntStream.rangeClosed(SmallIntegerSet.MIN_VALUE, SmallIntegerSet.MAX_VALUE)
            .boxed()
            .collect(Collectors.toList()));
    SortedSet<Integer> subSet = set.subSet(1, SmallIntegerSet.MAX_VALUE);

    assertEquals(Integer.valueOf(1), subSet.first());
    assertEquals(Integer.valueOf(SmallIntegerSet.MAX_VALUE - 1), subSet.last());

    assertEquals(62, subSet.size());
  }

}
