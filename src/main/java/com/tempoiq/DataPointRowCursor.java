package com.tempoiq;

import java.net.URI;
import java.util.Iterator;

import org.apache.http.HttpRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;


public class DataPointRowCursor implements Cursor<Row> {
  private final URI uri;
  private final Client client;
  private Query query;

  public DataPointRowCursor(URI uri, Client client, Query query) {
    this.uri = checkNotNull(uri);
    this.client = checkNotNull(client);
    this.query = checkNotNull(query);
  }

  public DataPointCursor getSensorCursor(String deviceKey, String sensorKey) {
    return new DataPointCursor(this, deviceKey, sensorKey);
  }

  private Segment<Row> firstSegment() {
    String body;
    try {
      body = Json.dumps(query);
    } catch (JsonProcessingException e) {
      throw new TempoIQException(String.format("Error fetching first segment for iterator. More detail: %s", query), 0);
    }
    HttpRequest request = client.buildRequest(uri.toString(), body);
    Result<RowSegment> result = client.execute(request, RowSegment.class);
    if (result.getState().equals(State.SUCCESS)) {
      return result.getValue();
    } else {
      throw new TempoIQException(String.format("Error fetching first segment for iterator. More detail: Request to TempoIQ failed: %s", result.getMessage()), 0);
    }
  }

  public Iterator<Row> iterator() {
    return new SegmentInnerIterator<Row>(client, uri, firstSegment(), Row.class);
  }
}