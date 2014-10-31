package com.tempoiq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.tempoiq.util.Preconditions.*;


class Segment<T> implements Iterable<T> {
  protected List<T> data;
  protected PageLink next;

  public Segment(List<T> data, PageLink next) {
    this.data = checkNotNull(data);
    this.next = next;
  }

  public List<T> getData() { return  this.data; }
  public PageLink getNext() { return this.next; }

  public Iterator<T> iterator() {
    return data.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Segment)) return false;

    Segment segment = (Segment) o;

    if (!data.equals(segment.data)) return false;
    if (next != null ? !next.equals(segment.next) : segment.next != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = data.hashCode();
    result = 31 * result + (next != null ? next.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Segment{" +
      "data=" + data +
      ", next=" + next +
      '}';
  }
}
