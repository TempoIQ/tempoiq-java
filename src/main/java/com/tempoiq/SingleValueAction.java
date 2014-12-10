package com.tempoiq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SingleValueAction implements QueryAction {
  private Integer limit;

  public SingleValueAction(int limit) {
    this.limit = limit;
  }

  public SingleValueAction() {
    this.limit = null;
  }

  @JsonIgnore
  public final String getName() {
    return "single";
  }

  @JsonProperty("include_selection")
  public final boolean includeSelection = false;

  @JsonProperty("limit")
  public Integer getLimit() {
    return this.limit;
  }
}
