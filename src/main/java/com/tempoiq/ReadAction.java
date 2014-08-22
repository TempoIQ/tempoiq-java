package com.tempoiq;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

class ReadAction implements QueryAction {
  private DateTime start;
  private DateTime stop;

  public ReadAction(DateTime start, DateTime stop) {
    this.start = start;
    this.stop = stop;
  }

  public final String getName() {
    return "read";
  }

  @JsonProperty("start")
  public DateTime getStart() {
    return start;
  }

  @JsonProperty("stop")
  public DateTime getStop() {
    return stop;
  }
}
