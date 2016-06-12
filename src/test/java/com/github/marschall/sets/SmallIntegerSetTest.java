package com.github.marschall.sets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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
  public void clear() {
    this.set.add(SmallIntegerSet.MIN_VALUE);

    this.set.clear();
    assertTrue(this.set.isEmpty());
    assertEquals(0, this.set.size());
  }

  @Test(expected = NullPointerException.class)
  public void addNull() {
    this.set.add(null);
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

  @Test(expected = NullPointerException.class)
  public void containsNull() {
    this.set.contains(null);
  }

  @Test(expected = ClassCastException.class)
  public void containsString() {
    this.set.contains("String");
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
    // TODO
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

  @Test(expected = NullPointerException.class)
  public void removeNull() {
    this.set.remove(null);
  }

  @Test(expected = ClassCastException.class)
  public void removeString() {
    this.set.remove("String");
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
  public void emptySet() {
    Set<Integer> emptySet = Collections.emptySet();
    assertNotNull(emptySet);
  }

}
