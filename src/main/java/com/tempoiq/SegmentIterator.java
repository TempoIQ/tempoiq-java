package com.tempoiq;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.tempoiq.util.Preconditions.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tempoiq.json.Json;
import org.apache.http.HttpRequest;


class SegmentIterator<T extends Segment<?>> implements Iterator<T> {

  private final Client client;
  private T segment;
  private Class<T> klass;

  public SegmentIterator(Client client, T initial, Class<T> klass) {
    this.client = checkNotNull(client);
    this.segment = checkNotNull(initial);
    this.klass =  klass;
  }

  @Override
  public final T next() {
    if(!hasNext()) {
      throw new NoSuchElementException();
    }
    T rv = this.segment;

    if(this.segment.getNext() != null) {
      String uri = Client.nextPageUriByQueryType(this.segment.getNext().getNextQuery());
      String body;
      try {
        body = Json.dumps(this.segment.getNext().getNextQuery());
      } catch (JsonProcessingException e) {
        throw new TempoIQException("Error serializing the body of the request. More detail: " + e.getMessage(), 0);
      }
      HttpRequest request = client.buildRequest(uri, body);
      Result<T> result = client.execute(request, klass);
      if(result.getState() == State.SUCCESS) {
        this.segment = result.getValue();
      } else {
        throw new TempoIQException(result.getMessage(), result.getCode());
      }
    } else {
      this.segment = null;
    }
    return rv;
  }

  @Override
  public final boolean hasNext() {
    return segment != null;
  }

  @Override public final void remove() {
    throw new UnsupportedOperationException();
  }
}
