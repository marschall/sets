package com.github.marschall.sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SortedSetTest {

  private SortedSet<Integer> set;

  public SortedSetTest(SortedSet<Integer> set) {
    this.set = set;
  }

  @Parameters
  public static Collection<Object[]> sets() {
    return Arrays.asList(
            new Object[] {new TreeSet<Integer>()},
            new Object[] {new SmallIntegerSet()});
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

}
