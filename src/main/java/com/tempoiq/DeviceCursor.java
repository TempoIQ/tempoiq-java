package com.tempoiq;

import java.net.URI;
import java.util.Iterator;

import org.apache.http.HttpRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;


public class DeviceCursor implements Cursor<Device> {
  private DeviceSegment first;
  private final Executor runner;
  private final URI endpoint;

  public DeviceCursor(DeviceSegment first, Executor runner, URI endpoint) {
    this.endpoint = checkNotNull(endpoint);
    this.runner = checkNotNull(runner);
    this.first = checkNotNull(first);
  }

  public DeviceCursor(DeviceSegment first) {
    this.first = checkNotNull(first);
    this.runner = null;
    this.endpoint = null;
  }

  public DeviceCursor(Result<DeviceSegment> result, Executor runner, URI endpoint) {
    if (result.getState().equals(State.SUCCESS)) {
      this.first = checkNotNull(result.getValue());
      this.runner = runner;
      this.endpoint = endpoint;
    } else {
      throw new TempoIQException(result.getMessage(), result.getCode());
    }
  }

  public Iterator<Device> iterator() {
    return first.iterator();
  }
}
