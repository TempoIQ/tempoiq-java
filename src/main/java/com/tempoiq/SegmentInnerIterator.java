package com.tempoiq;

import java.util.Iterator;
import java.util.NoSuchElementException;


class SegmentInnerIterator<T> implements Iterator<T> {
  private Iterator<Segment<T>> segments;
  private Iterator<T> currentSegment;

  public SegmentInnerIterator(SegmentIterator<Segment<T>> segments) {
    this.segments = segments;
    currentSegment = null;
  }

  public boolean hasNext() {
    boolean hasNext = true;
    if(currentSegment == null) {
      if(segments.hasNext()) {
        currentSegment = segments.next().iterator();
      } else {
        return false;
      }
    }

    while(!currentSegment.hasNext() && segments.hasNext()) {
      currentSegment = segments.next().iterator();
    }
    return currentSegment.hasNext();
  }

  public T next() {
    if(!hasNext()) {
      throw new NoSuchElementException();
    }
    return currentSegment.next();
  }

  public final void remove() {
    throw new UnsupportedOperationException();
  }
}
