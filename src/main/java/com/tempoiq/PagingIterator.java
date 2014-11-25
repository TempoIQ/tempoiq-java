package com.tempoiq;

import java.util.*;
import static com.tempoiq.util.Preconditions.*;

public class PagingIterator<T> implements Iterator<T> {
  private final Iterator<Segment<T>> pages;
  private Iterator<T> current;

  public PagingIterator(PageLoader<T> pages) {
    this.pages = checkNotNull(pages);
    if (pages.hasNext()) {
      this.current = pages.next().iterator();
    } else {
      this.current = new Iterator<T>() {
        @Override
        public boolean hasNext() {
          return false;
        }
        @Override
        public T next() {
          throw new NoSuchElementException();
        }
        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  @Override
  public T next() {
    if (current.hasNext()) {
      return current.next();
    } else if (pages.hasNext()) {
      current = pages.next().iterator();
      if (current.hasNext()) {
        return current.next();
      } else {
        throw new NoSuchElementException();
      }
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public boolean hasNext() {
    if (current.hasNext()) {
      return true;
    } else if (pages.hasNext()) {
      current = pages.next().iterator();
      return current.hasNext();
    } else {
      return false;
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
