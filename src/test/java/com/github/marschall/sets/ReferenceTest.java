package com.github.marschall.sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

public class ReferenceTest {

  @Test
  public void comparator() {
    NavigableSet<Integer> set = new TreeSet<>();

    assertNull(set.comparator());
    assertSame(Collections.reverseOrder(), set.descendingSet().comparator());
  }

  @Test
  public void subSet() {
    NavigableSet<Integer> set = new TreeSet<>();
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
    NavigableSet<Integer> set = new TreeSet<>();
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
