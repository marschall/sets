package com.github.marschall.sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SmallIntegerSetTest {

  private Set<Integer> set;

  @Before
  public void setUp() {
    this.set = new SmallIntegerSet();
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
  public void emptySet() {
    Set<Integer> emptySet = Collections.emptySet();
    assertNotNull(emptySet);
  }

}
