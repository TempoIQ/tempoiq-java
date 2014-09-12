package com.tempoiq;

public class QuerySearch {
  private Selector.Type type;
  private Selection selection;

  public QuerySearch(Selector.Type type, Selection selection) {
    this.type = type;
    this.selection = selection;
  }

  public Selection getSelection() {
    return selection;
  }

  public Selector.Type getType() {
    return type;
  }
}
