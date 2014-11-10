package com.tempoiq;

import java.net.URI;

public class DevicePageLoader extends PageLoader<Device> {
  private URI endpoint;
  private Executor runner;

  public DevicePageLoader(DeviceSegment first, URI endpoint, Executor runner) {
    this.first = first;
    this.current = first;
    this.next = null;
    this.endpoint = endpoint;
    this.runner = runner;
  }

  @Override
  public DeviceSegment fetchNext() {
    Result<DeviceSegment> result = runner.post(endpoint, current.getNext(), DeviceSegment.class);
    if (result.getState().equals(State.SUCCESS)) {
      return result.getValue();
    } else {
      return null;
    }
  }
}
