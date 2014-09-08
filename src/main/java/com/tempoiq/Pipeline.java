package com.tempoiq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pipeline implements Serializable {
  private List<PipelineFunction> functions = new ArrayList<PipelineFunction>();

  public void addFunction(PipelineFunction function) {
    functions.add(function);
  }

  @JsonProperty("functions")
  public List<PipelineFunction> getFunctions() { return functions; }
}
