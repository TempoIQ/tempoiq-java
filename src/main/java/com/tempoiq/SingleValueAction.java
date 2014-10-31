package com.tempoiq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

public class SingleValueAction implements QueryAction {
  @JsonIgnore
  public final String getName() {
    return "single";
  }

  @JsonProperty("include_selection")
  public final boolean includeSelection = false;
}
