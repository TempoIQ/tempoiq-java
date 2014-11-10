package com.tempoiq;

import java.util.Iterator;

public abstract class PageLoader<T> implements Iterator<Segment<T>> {
  protected Segment<T> first;
  protected Segment<T> current;
  protected Segment<T> next;

  @Override
  public boolean hasNext() {
    //if we already loaded the next segment, we have a next segment
    if (next != null) {
      return true;
    }
    //if the current segment has no pointer, or there is no current segment, we cannot fetch the next segment
    boolean locallyExhausted = (current == null || current.getNext() == null || current.getNext().equals(""));
    if (locallyExhausted) {
      return false;
    }
    //if we fetch a segment, then there's a next segment, otherwise something has gone wrong
    next = fetchNext();
    if (next == null) {
      return false;
    } else {
      return true;
    }
  }

  public abstract Segment<T> fetchNext();

  @Override
  public Segment<T> next() {
    Segment<T> tmp = current;
    if (next == null) {
      next = fetchNext();
    }
    current = next;
    return tmp;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void reset() {
    current = first;
    next = null;
  }
}
