package com.github.marschall.sets;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * A {@link Set} implementation based on an array implementation.
 *
 * <p>Since many operations like {@link #contains(Object)}, {@link #add(Object)}
 * and {@link #remove(Object)} have linear complexity this class should only
 * be used for small sets containing only a handful of elements.</p>
 *
 * <p>Operations like {@link #toArray()} are very efficient and only a single
 * array copy.</p>
 *
 * <p>This class as a custom spliterator that splits well.</p>
 *
 * @param <E> the type of elements maintained by this set
 */
public final class ArraySet<E> implements Set<E>, Serializable, Cloneable {

  // TODO decide on supporting null

  private static final long serialVersionUID = 1L;

  private Object[] elements;

  private int size;

  /**
   * Default constructor.
   */
  public ArraySet() {
    this(4);
  }

  public ArraySet(int initialSize) {
    this.elements = new Object[initialSize];
    this.size = 0;
  }

  /**
   * Array constructor.
   *
   * @param elements the initial set elements,
   *  will not be copied, should not be modified by caller
   */
  public ArraySet(E... elements) {
    this.size = elements.length;
    this.elements = elements;
  }

  @Override
  public int size() {
    return this.size;
  }

  @Override
  public boolean isEmpty() {
    return this.size == 0;
  }

  @Override
  public boolean contains(Object o) {
    return indexOf(o) != -1;
  }

  private int indexOf(Object o) {
    for (int i = 0; i < this.size; i++) {
      if (Objects.equals(this.elements[i], o)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public Iterator<E> iterator() {
    return new ArraySetIterator();
  }

  final class ArraySetIterator implements Iterator<E> {

    /**
     * Index of the next read.
     */
    private int next;

    private boolean nextCalled;

    ArraySetIterator() {
      this.next = 0;
      this.nextCalled = false;
    }

    @Override
    public boolean hasNext() {
      return this.next < size;
    }

    @Override
    public E next() {
      if (!this.hasNext()) {
        throw new NoSuchElementException();
      }
      this.nextCalled = true;
      return (E) elements[next++];
    }

    @Override
    public void remove() {
      if (!this.nextCalled) {
        throw new IllegalStateException();
      }
      ArraySet.this.remove(this.next--);
      this.nextCalled = false;
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
      for (int i = next; i < elements.length; ++i) {
        action.accept((E) elements[i]);
      }
    }

  }

  @Override
  public Object[] toArray() {
    Object[] array = new Object[this.size];
    System.arraycopy(this.elements, 0, array, 0, this.size);
    return array;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    T[] result;
    if (a.length < this.size) {
      result = (T[]) Array.newInstance(a.getClass().getComponentType(), this.size);
    } else {
      result = a;
      if (result.length > size) {
        result[size] = null;
        if (result.length > this.size) {
          result[size] = null;
        }
      }
    }
    System.arraycopy(this.elements, 0, result, 0, this.size);
    return result;
  }

  @Override
  public boolean add(E e) {
    if (this.contains(e)) {
      return false;
    }
    if (this.size == this.elements.length) {
      // overflow will cause negative array size which will cause exception
      Object[] newElements = new Object[this.elements.length * 2];
      System.arraycopy(this.elements, 0, newElements, 0, this.size);
      this.elements = newElements;
    }
    this.elements[this.size++] = e;
    return true;
  }

  @Override
  public boolean remove(Object o) {
    int index = this.indexOf(o);
    if (index == -1) {
      return false;
    }
    this.remove(index);
    return true;
  }

  private void remove(int index) {
    System.arraycopy(this.elements, index + 1, this.elements, index, this.size - index);
    this.elements[this.size--] = null;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object e : c) {
      if (!this.contains(e)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    boolean changed = false;
    for (E e : c) {
      changed |= this.add(e);
    }
    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    boolean changed = false;
    Iterator<E> iterator = this.iterator();
    while (iterator.hasNext()) {
        E next = iterator.next();
        if (!c.contains(next)) {
            iterator.remove();
            changed = true;
        }
    }
    return changed;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    // TODO could be made more efficient by coalescing removes
    boolean changed = false;
    for (Object e : c) {
      changed |= this.remove(e);
    }
    return changed;
  }

  @Override
  public void clear() {
    this.size = 0;
    Arrays.fill(elements, null);
  }

  @Override
  public Spliterator<E> spliterator() {
    return Spliterators.spliterator(this.elements, 0, this.size,
            Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.DISTINCT);
  }

  /**
   * Returns a shallow copy of this {@code ArraySet} instance
   *
   * <p>The elements themselves are not cloned.</p>
   *
   * @return a shallow copy of this set
   */
  public Object clone() {
      try {
        ArraySet<?> newSet = (ArraySet<?>) super.clone();
          newSet.elements = this.elements.clone();
          return newSet;
      } catch (CloneNotSupportedException e) {
          throw new InternalError(e);
      }
  }

}
