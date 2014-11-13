package com.tempoiq;

import java.net.URI;

public class RowPageLoader extends PageLoader<Row> {
  private URI endpoint;
  private Executor runner;

  public RowPageLoader(RowSegment first, URI endpoint, Executor runner) {
    super();
    this.first = first;
    this.current = first;
    this.onDeck = null;
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
