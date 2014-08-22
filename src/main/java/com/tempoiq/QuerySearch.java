package com.tempoiq;

class QuerySearch {
  private Selector.Type type;
  private Selection selection;

  public QuerySearch(Selector.Type type, Selection selection) {
    this.type = type;
    this.selection = selection;
  }
}
