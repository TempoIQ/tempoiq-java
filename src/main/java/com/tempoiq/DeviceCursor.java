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

  private Segment<Device> firstSegment() {
    String body;
    try {
      body = Json.dumps(query);
    } catch (JsonProcessingException e) {
      throw new TempoIQException(String.format("Error fetching first segment for iterator. More detail: %s", query), 0);
    }
    HttpRequest request = client.buildRequest(uri.toString(), body);
    Result<DeviceSegment> result = client.execute(request, DeviceSegment.class);
    if (result.getState().equals(State.SUCCESS)) {
      return result.getValue();
    } else {
      throw new TempoIQException(String.format("Error fetching first segment for iterator. More detail: Request to TempoIQ failed: %s", result.getMessage()), 0);
    }
  }

  public Iterator<Device> iterator() {
    return new SegmentInnerIterator<Device>(client, uri, firstSegment(), Device.class);
  }
}
