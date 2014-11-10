package com.tempoiq;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FindAction implements QueryAction {
  @JsonIgnore
  public final String getName() {
    return "find";
  }

  public final String quantifier = "all";
}
