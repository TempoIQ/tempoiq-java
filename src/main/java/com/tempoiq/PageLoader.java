package com.tempoiq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import static com.tempoiq.util.Preconditions.*;

public abstract class PageLoader<T> implements Iterator<Segment<T>> {
  protected Segment<T> current;

  public PageLoader(Segment<T> first) {
    this.current = checkNotNull(first);
  }

  @Override
  public boolean hasNext() {
    if (current != null) {
      return true;
    } else {
      return false;
    }
  }

  public abstract Segment<T> fetchNext();

  @Override
  public Segment<T> next() {
    if (current != null) {
      Segment<T> tmp = current;
      current = fetchNext();
      return tmp;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
