package com.tempoiq;

import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.http.HttpRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;

public class DeviceCursor implements Cursor<Device> {
  private DeviceSegment first;
  private final Executor runner;
  private final URI endpoint;
  private final String mediaTypeVersion;

  public DeviceCursor(Result<DeviceSegment> result, Executor runner, URI endpoint) {
    if (result.getState().equals(State.SUCCESS)) {
      this.first = checkNotNull(result.getValue());
      this.runner = checkNotNull(runner);
      this.endpoint = checkNotNull(endpoint);
      this.mediaTypeVersion = null;
    } else {
      throw new TempoIQException(result.getMessage(), result.getCode());
    }
  }

  public DeviceCursor(Result<DeviceSegment> result, Executor runner, URI endpoint, String mediaTypeVersion) {
    if (result.getState().equals(State.SUCCESS)) {
      this.first = checkNotNull(result.getValue());
      this.runner = checkNotNull(runner);
      this.endpoint = checkNotNull(endpoint);
      this.mediaTypeVersion = mediaTypeVersion;
    } else {
      throw new TempoIQException(result.getMessage(), result.getCode());
    }
  }

  public Iterator<Device> iterator() {
    final DevicePageLoader pages =  new DevicePageLoader(first, endpoint, runner, mediaTypeVersion);
    return new PagingIterator<Device>(pages);
  }
}
