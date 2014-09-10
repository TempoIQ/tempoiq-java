package com.tempoiq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pipeline implements Serializable {
  private List<PipelineFunction> functions = new ArrayList<PipelineFunction>();

  private void addFunction(PipelineFunction function) {
    functions.add(function);
  }

  public Pipeline aggregate(Fold fold) {
    addFunction(new Aggregation(fold));
    return this;
  }

  @JsonProperty("functions")
  public List<PipelineFunction> getFunctions() { return functions; }
}
