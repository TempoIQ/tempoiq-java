package com.tempoiq;

import java.net.URI;
import static com.tempoiq.util.Preconditions.*;

public class RowPageLoader extends PageLoader<Row> {
  private URI endpoint;
  private Executor runner;
  private String contentType;
  private String[] mediaTypeVersions;

  public RowPageLoader(RowSegment first, URI endpoint, Executor runner, String contentType, String[] mediaTypeVersions) {
    super(checkNotNull(first));
    this.endpoint = checkNotNull(endpoint);
    this.runner = checkNotNull(runner);
    this.contentType = checkNotNull(contentType);
    this.mediaTypeVersions = checkNotNull(mediaTypeVersions);
  }

  @Override
  public RowSegment fetchNext() {
    if (current != null && current.getNext() != null && !current.getNext().equals("")) {
      Result<RowSegment> result = runner.get(endpoint, current.getNext(), RowSegment.class, contentType, mediaTypeVersions);
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
