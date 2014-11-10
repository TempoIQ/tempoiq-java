package com.tempoiq;

import java.net.URI;
import java.util.Iterator;

public class RowPageLoader extends PageLoader<Row> {
  private URI endpoint;
  private Executor runner;

  public RowPageLoader(RowSegment first, URI endpoint, Executor runner) {
    this.first = first;
    this.current = first;
    this.next = null;
    this.endpoint = endpoint;
    this.runner = runner;
  }

  @Override
  public RowSegment fetchNext() {
    Result<RowSegment> result = runner.post(endpoint, current.getNext(), RowSegment.class);
    if (result.getState().equals(State.SUCCESS)) {
      return result.getValue();
    } else {
      return null;
    }
  }
}
