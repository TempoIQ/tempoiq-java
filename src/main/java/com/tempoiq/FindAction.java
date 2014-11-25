package com.tempoiq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FindAction implements QueryAction {
  private Integer limit;
  
  public FindAction() {
    this.limit = null;
  }

  public FindAction(int limit) {
    this.limit = limit;
  }

  @JsonIgnore
  public final String getName() {
    return "find";
  }

  @JsonProperty("limit")
  public Integer getLimit() {
    return limit;
  }

  @JsonProperty("quantifier")
  public final String quantifier = "all";
}
