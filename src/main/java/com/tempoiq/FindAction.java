package com.tempoiq;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FindAction implements QueryAction {
  @JsonIgnore
  public final String getName() {
    return "find";
  }

  // TODO: Is this really needed in the API? Will this ever be different?
  public final String quantifier = "all";
}
