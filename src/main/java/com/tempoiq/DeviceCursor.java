package com.tempoiq;

import java.net.URI;
import java.util.Iterator;

import org.apache.http.HttpRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;


public class DeviceCursor implements Cursor<Device> {
  private final URI uri;
  private final Client client;
  private Query query;

  public DeviceCursor(URI uri, Client client, Query query) {
    this.uri = checkNotNull(uri);
    this.client = checkNotNull(client);
    this.query = checkNotNull(query);
  }

  public Iterator<Device> iterator() {
    String body = null;
    try {
      body = Json.dumps(query);
    } catch (JsonProcessingException e) {
      throw new TempoIQException("Error serializing the body of the request. More detail: " + e.getMessage(), 0);
    }
    HttpRequest request = client.buildRequest(uri.toString(), body);
    Result<DeviceSegment> result = client.execute(request, DeviceSegment.class);

    Iterator<Device> iterator = null;
    if(result.getState() == State.SUCCESS) {
      @SuppressWarnings("unchecked") // This cast is always ok
      SegmentIterator<Segment<Device>> segments = new SegmentIterator(client, result.getValue(), DeviceSegment.class);
      iterator = new SegmentInnerIterator<Device>(segments);
    } else {
      throw new TempoIQException(result.getMessage(), result.getCode());
    }
    return iterator;
  }
}
