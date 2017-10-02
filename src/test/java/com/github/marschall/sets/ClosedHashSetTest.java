package com.github.marschall.sets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClosedHashSetTest {

  private Set<String> set;

  @BeforeEach
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
