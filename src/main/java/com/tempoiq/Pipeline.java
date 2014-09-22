package com.tempoiq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;
import org.joda.time.Period;

public class Pipeline implements Serializable {
  private List<PipelineFunction> functions = new ArrayList<PipelineFunction>();

  private void addFunction(PipelineFunction function) {
    functions.add(function);
  }

  public Pipeline aggregate(Fold fold) {
    addFunction(new Aggregation(fold));
    return this;
  }

  public Pipeline rollup(Period period, Fold fold, DateTime start) {
    addFunction(new Rollup(period, fold, start));
    return this;
  }

  @JsonProperty("functions")
  public List<PipelineFunction> getFunctions() { return functions; }
}
