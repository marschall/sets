package com.github.marschall.sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class ClosedHashSetTest {

  private Set<String> set;

  @Before
  public void setUp() {
    this.set = new ClosedHashSet<>();
  }

  @Test
  public void justNull() {
    assertEquals(0, this.set.size());
    assertFalse(this.set.contains(null));
    assertEquals(0, this.set.size());
    assertFalse(this.set.remove(null));
    assertEquals(0, this.set.size());

    assertTrue(this.set.add(null));
    assertTrue(this.set.contains(null));
    assertEquals(1, this.set.size());
    assertFalse(this.set.add(null));
    assertTrue(this.set.contains(null));
    assertEquals(1, this.set.size());

    assertTrue(this.set.remove(null));
    assertFalse(this.set.contains(null));
    assertEquals(0, this.set.size());
    assertFalse(this.set.remove(null));
    assertFalse(this.set.contains(null));
    assertEquals(0, this.set.size());
  }

  @Test
  public void nullFirst() {
    assertTrue(this.set.add(null));
    assertTrue(this.set.add("null"));

    assertTrue(this.set.contains(null));
    assertTrue(this.set.contains("null"));
    assertEquals(2, this.set.size());

    assertTrue(this.set.remove(null));
    assertFalse(this.set.contains(null));
    assertEquals(1, this.set.size());
    assertFalse(this.set.remove(null));
  }

  @Test
  public void nullLast() {
    assertTrue(this.set.add("null"));
    assertTrue(this.set.add(null));

    assertTrue(this.set.contains(null));
    assertTrue(this.set.contains("null"));
    assertEquals(2, this.set.size());

    assertTrue(this.set.remove(null));
    assertFalse(this.set.contains(null));
    assertEquals(1, this.set.size());
    assertFalse(this.set.remove(null));
  }

}
