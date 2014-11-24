package com.tempoiq;

import java.net.URI;
import static com.tempoiq.util.Preconditions.*;

public class DevicePageLoader extends PageLoader<Device> {
  private URI endpoint;
  private Executor runner;
  private String mediaTypeVersion;

  public DevicePageLoader(DeviceSegment first, URI endpoint, Executor runner, String mediaTypeVersion) {
    super(first);
    this.endpoint = checkNotNull(endpoint);
    this.runner = checkNotNull(runner);
    this.mediaTypeVersion = checkNotNull(mediaTypeVersion);
  }

  @Override
  public DeviceSegment fetchNext() {
    if (current != null && current.getNext() != null && !current.getNext().equals("")) {
      Result<DeviceSegment> result = runner.get(endpoint, current.getNext(), DeviceSegment.class, this.mediaTypeVersion);
      if (result.getState().equals(State.SUCCESS)) {
        return result.getValue();
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
}