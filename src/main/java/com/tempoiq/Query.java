package com.tempoiq;

public class Query {
  private QuerySearch search;
  private Pipeline pipeline;
  private QueryAction action;

  public Query(QuerySearch search, Pipeline pipeline, QueryAction action) {
    this.search = search;
    this.action = action;
    this.pipeline = pipeline;
  }

  public QuerySearch getSearch() {
    return search;
  }

  public QueryAction getAction() {
    return action;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }
}
