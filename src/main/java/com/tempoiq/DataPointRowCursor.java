package com.tempoiq;

import java.net.URI;
import java.util.*;

import static com.tempoiq.util.Preconditions.*;

public class DataPointRowCursor implements Cursor<Row> {
  private final RowSegment first;
  private final Executor runner;
  private final URI endpoint;
  private final String contentType;
  private final String[] mediaTypeVersions;

  public DataPointRowCursor(Result<RowSegment> result,
                            Executor runner,
                            URI endpoint,
                            String contentType,
                            String[] mediaTypeVersion) {
    if (result.getState().equals(State.SUCCESS)) {
      this.first = checkNotNull(checkNotNull(result.getValue()));
      this.runner = runner;
      this.endpoint = endpoint;
      this.contentType = contentType;
      this.mediaTypeVersions = mediaTypeVersion;
    } else {
      throw new TempoIQException(result.getMessage(), result.getCode());
    }
  }

  public Map<String, Map<String, DataPointCursor>> pointsByStream() {
    Map<String, Map<String, DataPointCursor>> streams = new HashMap<String, Map<String, DataPointCursor>>();
    for (String deviceKey : devicesForCursor()) {
        streams.put(deviceKey, pointsForDevice(deviceKey));
    }
    return streams;
  }

  public Map<String, DataPointCursor> pointsForDevice(String deviceKey) {
    Set<String> sensors = streamsForDevice(deviceKey);
    Map<String, DataPointCursor> result = new HashMap<String, DataPointCursor>();
    for (String sensorKey : sensors) {
      result.put(sensorKey, pointsForStream(deviceKey, sensorKey));
    }
    return result;
  }

  public DataPointCursor pointsForStream(String deviceKey, String sensorKey) {
    return new DataPointCursor(this, deviceKey, sensorKey);
  }

  public Iterator<Row> iterator() {
      RowPageLoader pages = new RowPageLoader(first, endpoint, runner, contentType, mediaTypeVersions);
      return new PagingIterator<Row>(pages);
  }

  public Segment<Row> getFirst() {
    return this.first;
  }

  public Set<String> devicesForCursor() {
    Set<String> seen = new HashSet<String>();
    for (Row row : this) {
      seen.addAll(row.getValues().keySet());
    }
    return seen;
  }

  public Set<String> streamsForDevice(String deviceKey) {
    Set<String> seen = new HashSet<String>();
    for (Row row : this) {
      if (row.getValues().containsKey(deviceKey)) {
        seen.addAll(row.getValues().get(deviceKey).keySet());
      }
    }
    return seen;
  }
}
