package com.tempoiq;

import java.net.URI;
import java.util.Iterator;

import org.apache.http.HttpRequest;

import static com.tempoiq.util.Preconditions.*;


class SensorCursor implements Cursor<Sensor> {
  private final URI uri;
  private final Client client;

  public SensorCursor(URI uri, Client client) {
    this.uri = checkNotNull(uri);
    this.client = checkNotNull(client);
  }

  public Iterator<Sensor> iterator() {
    HttpRequest request = client.buildRequest(uri.toString());
    Result<SensorSegment> result = client.execute(request, SensorSegment.class);

    Iterator<Sensor> iterator = null;
    if(result.getState() == State.SUCCESS) {
      @SuppressWarnings("unchecked") // This cast is always ok
      SegmentIterator<Segment<Sensor>> segments = new SegmentIterator(client, result.getValue(), SensorSegment.class);
      iterator = new SegmentInnerIterator<Sensor>(segments);
    } else {
      throw new TempoIQException(result.getMessage(), result.getCode());
    }
    return iterator;
  }
}
