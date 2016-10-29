package com.github.marschall.sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 * <p>This set supports {@code null}.</p>
 *
 * <p>This set silently corrupts if you add more than
 * {@value Integer#MAX_VALUE} elements.</p>
 *
 * @param <E> the type of elements maintained by this set
 */
public class ClosedHashSet<E> implements Set<E> {

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static final int DEFAULT_INITIAL_CAPACITY = 8;

  /**
   * Adds serialization support and keeps identity under serialization.
   */
  enum Null {
    INSTANCE;
  }

  public ClosedHashSet() {
    this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    this.maxSize = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
  }

  private int size;

  private float loadFactor;

  private int maxSize;

  private Object[] elements;

  @Override
  public int size() {
    return this.size;
  }

  @Override
  public boolean isEmpty() {
    return this.size == 0;
  }

  private final int index(Object key) {
      // This function ensures that hashCodes that differ only by
      // constant multiples at each bit position have a bounded
      // number of collisions (approximately 8 at default load factor).
      int h = key == null ? 0 : key.hashCode();
      h ^= h >>> 20 ^ h >>> 12;
      h ^= h >>> 7 ^ h >>> 4;
      return (h & (this.elements.length >> 1) - 1) << 1;
  }

  private static Object toSentinelIfNull(Object key) {
      if (key == null) {
          return NULL;
      }
      return key;
  }

  static Object nonSentinel(Object key) {
      return key == NULL ? null : key;
  }

  @Override
  public boolean contains(Object o) {
    Object key = toSentinelIfNull(o);

    Object element = this.elements[this.index(key)];
    if (element == null) {
      return false;
    }

    if (element instanceof Collision) {
      Collision collision = (Collision) element;
      return collision.contains(key);
    } else {
      return element.equals(key);
    }
  }

  @Override
  public boolean add(E e) {
    Object key = toSentinelIfNull(e);

    int index = this.index(key);
    Object element = this.elements[index];
    if (element == null) {
      this.elements[index] = key;
      this.size += 1;
      return true;
    }

    if (element instanceof Collision) {
      Collision collision = (Collision) element;
      boolean changed = collision.add(key);
      if (changed) {
        this.size += 1;
      }
      return changed;
    } else if (element.equals(key)) {
      return false;
    } {
      Collision collision = new Collision(element, key);
      this.elements[index] = collision;
      this.size += 1;
      return true;
    }
  }

  @Override
  public boolean remove(Object o) {
    Object key = toSentinelIfNull(o);

    int index = this.index(key);
    Object element = this.elements[index];
    if (element == null) {
      return false;
    }

    if (element instanceof Collision) {
      Collision collision = (Collision) element;
      boolean changed = collision.remove(key);
      if (changed) {
        this.size -= 1;
      }
      return changed;
    } else if (element.equals(key)) {
      this.elements[index] = null;
      this.size -= 1;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Iterator<E> iterator() {
    return new ClosedSetIterator();
  }

  final class ClosedSetIterator implements Iterator<E> {

    private int tablePosition;
    private int overflowPosition;
    int visited;

    @Override
    public boolean hasNext() {
      return this.visited < ClosedHashSet.this.size;
    }

    @Override
    public E next() {
      if (!this.hasNext()) {
        throw new NoSuchElementException();
      }
      while (ClosedHashSet.this.elements[this.tablePosition] == null) {
          this.tablePosition += 1;
      }
      Object element = ClosedHashSet.this.elements[this.tablePosition];
      if (element instanceof Collision) {
        Collision collision = (Collision) element;
        // TODO

      }
      element = nonSentinel(element);
      this.visited += 1;
      return (E) element;
    }

    @Override
    public void remove() {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("remove");
    }

  }

  @Override
  public Object[] toArray() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void clear() {
    this.size = 0;
    Arrays.fill(this.elements, null);
  }

  private static final Object NULL = Null.INSTANCE;

  static final class Collision {

    static final int INITIAL_SIZE = 4;

    private Object[] elements;

    Collision(Object first, Object second) {
      this.elements = new Object[INITIAL_SIZE];
      this.elements[0] = first;
      this.elements[1] = second;
    }

    boolean contains(Object o) {
      for (Object each : this.elements) {
        if (each == null) {
          // early exit
          return false;
        }
        if (o.equals(each)) {
          return true;
        }
      }
      return false;
    }

    boolean add(Object o) {
      int length = elements.length;
      int i = 0;
      while (i < length) {
        Object each = this.elements[i];
        if (each == null) {
          this.elements[i] = each;
          return true;
        }
        if (o.equals(each)) {
          return false;
        }
        i += 1;
      }
      if (i == length) {
        // we reached the end of the array
        Object[] newElements = new Object[length * 2];
        System.arraycopy(this.elements, 0, newElements, 0, length);
        this.elements = newElements;
      }
      this.elements[i] = o;
      return true;
    }

    boolean remove(Object o) {
      int length = elements.length;
      for (int i = 0; i < length; i++) {
        Object each = this.elements[i];
        if (each == null) {
          // early exit
          return false;
        }
        if (o.equals(each)) {
          if (i == length - 1) {
            // last element just null out
            this.elements[i] = null;
          } else {
            System.arraycopy(this.elements, i + 1, this.elements, i, length - i - 1);
          }
          return true;
        }
      }
      return false;
    }

  }

}
