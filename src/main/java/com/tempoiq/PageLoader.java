package com.tempoiq;

import java.util.Iterator;

public abstract class PageLoader<T> implements Iterator<Segment<T>> {
  protected Segment<T> first;
  protected Segment<T> current;
  protected Segment<T> onDeck;
  private boolean needsToFetch;
  private boolean isExhausted;

  public PageLoader() {
    this.isExhausted = false;
    this.needsToFetch = true;
  }

  @Override
  public boolean hasNext() {
    if (this.isExhausted) {
      return false;
    }

    if (onDeck != null) {
      return true;
    }

    boolean locallyExhausted = (current == null || current.getNext() == null || current.getNext().equals(""));
    if (locallyExhausted) {
      this.isExhausted = true;
      return false;
    }

    refreshNext();
    if (onDeck != null) {
      return true;
    } else {
      this.isExhausted = true;
      return false;
    }
  }

  public abstract Segment<T> fetchNext();

  private void refreshNext() {
    if (needsToFetch || onDeck == null) {
      onDeck = fetchNext();
      needsToFetch = false;
    }
  }

  @Override
  public Segment<T> next() {
    refreshNext();
    Segment<T> outbound = current;
    current = onDeck;
    onDeck = null;
    needsToFetch = true;
    return outbound;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void reset() {
    current = first;
    onDeck = null;
  }
}
