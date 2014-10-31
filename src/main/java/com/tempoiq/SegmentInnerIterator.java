package com.tempoiq;

import org.apache.http.HttpRequest;

import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;
import static com.tempoiq.util.Preconditions.*;

class SegmentInnerIterator<T> implements Iterator<T> {
  private final Client client;
  private final URI endpoint;
  private final Class<T> klass;
  private Segment<T> currentSegment;
  private Iterator<T> currentIterator;

  public SegmentInnerIterator(Client client, URI endpoint, Segment<T> segment, Class<T> klass) {
    this.client = checkNotNull(client);
    this.endpoint = checkNotNull(endpoint);
    this.klass = checkNotNull(klass);
    this.currentSegment = segment;
    this.currentIterator = segment.iterator();
  }

  public boolean hasNext() {
    if (currentIterator.hasNext()) {
      return true;
    } else if (currentSegment.getNextPage() != null) {
      this.currentSegment = nextSegment(currentSegment.getNextPage().getNextQuery());
      this.loadNextIterator();
      return currentIterator.hasNext();
    } else {
      return false;
    }
  }

  public T next() {
    if(!hasNext()) {
      throw new NoSuchElementException();
    }
    return currentIterator.next();
  }

  public final void remove() {
    throw new UnsupportedOperationException();
  }

  private Segment<T> nextSegment(String body) {
    HttpRequest request = client.buildRequest(endpoint.toString(), body);
    Result<Segment<T>> result = (Result<Segment<T>>)(client.execute(request, currentSegment.getClass()));
    return result.getValue();
  }

  private void loadNextIterator() {
    this.currentIterator = this.currentSegment.iterator();
  }
}
