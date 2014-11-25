package com.tempoiq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
class ReadAction implements QueryAction {
  private DateTime start;
  private DateTime stop;
  private Integer limit;

  public ReadAction(DateTime start, DateTime stop) {
    this.start = start;
    this.stop = stop;
    this.limit = null;
  }

  public ReadAction(DateTime start, DateTime stop, Integer limit) {
    this.start = start;
    this.stop = stop;
    this.limit = limit;
  }

  @JsonIgnore
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

  @JsonProperty("limit")
  public Integer getLimit() {
    return limit;
  }
}
