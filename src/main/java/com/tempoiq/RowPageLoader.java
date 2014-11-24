package com.tempoiq;

import java.net.URI;

public class RowPageLoader extends PageLoader<Row> {
  private URI endpoint;
  private Executor runner;
  private String mediaTypeVersion;

  public RowPageLoader(RowSegment first, URI endpoint, Executor runner, String mediaTypeVersion) {
    super(first);
    this.endpoint = endpoint;
    this.runner = runner;
    this.mediaTypeVersion = mediaTypeVersion;
  }

  @Override
  public RowSegment fetchNext() {
    if (current != null && current.getNext() != null && !current.getNext().equals("")) {
      Result<RowSegment> result = runner.get(endpoint, current.getNext(), RowSegment.class, mediaTypeVersion);
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
