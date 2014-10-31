package com.tempoiq;

import java.util.Iterator;
import java.util.List;

import static com.tempoiq.util.Preconditions.*;


class Segment<T> implements Iterable<T> {
  protected List<T> data;
  protected PageLink nextPage;

  public Segment(List<T> data, PageLink nextPage) {
    this.data = checkNotNull(data);
    this.nextPage = nextPage;
  }

  public List<T> getData() { return  this.data; }
  public PageLink getNextPage() { return this.nextPage; }

  public Iterator<T> iterator() {
    return data.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Segment)) return false;

    Segment segment = (Segment) o;

    if (!data.equals(segment.data)) return false;
    if (nextPage != null ? !nextPage.equals(segment.nextPage) : segment.nextPage != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = data.hashCode();
    result = 31 * result + (nextPage != null ? nextPage.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Segment{" +
      "data=" + data +
      ", nextPage=" + nextPage +
      '}';
  }
}
