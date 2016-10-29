package com.github.marschall.sets;

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
    assertFalse(this.set.contains(null));
    assertFalse(this.set.remove(null));
    assertTrue(this.set.add(null));
    assertFalse(this.set.add(null));
    assertTrue(this.set.remove(null));
    assertFalse(this.set.remove(null));
  }

  @Test
  public void nullFirst() {
  }

  @Test
  public void nullLast() {
  }

}
