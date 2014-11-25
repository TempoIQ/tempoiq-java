package com.tempoiq;

import java.net.URI;
import java.util.Iterator;

import static com.tempoiq.util.Preconditions.*;

public class DeviceCursor implements Cursor<Device> {
  private DeviceSegment first;
  private final Executor runner;
  private final URI endpoint;
  private final String contentType;
  private final String[] mediaTypeVersions;

  public DeviceCursor(Result<DeviceSegment> result,
                      Executor runner,
                      URI endpoint,
                      String contentType,
                      String[] mediaTypeVersions) {
    if (result.getState().equals(State.SUCCESS)) {
      this.first = checkNotNull(result.getValue());
      this.runner = checkNotNull(runner);
      this.endpoint = checkNotNull(endpoint);
      this.contentType = checkNotNull(contentType);
      this.mediaTypeVersions = checkNotNull(mediaTypeVersions);
    } else {
      throw new TempoIQException(result.getMessage(), result.getCode());
    }
  }

  public Segment<Device> getFirst() {
    return this.first;
  }

  public Iterator<Device> iterator() {
    final DevicePageLoader pages =  new DevicePageLoader(first, endpoint, runner, contentType, mediaTypeVersions);
    return new PagingIterator<Device>(pages);
  }
}
