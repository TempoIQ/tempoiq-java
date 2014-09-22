package com.tempoiq;

import java.util.TreeMap;
import java.util.Map;

public class Selection {
  private final Map<Selector.Type, Selector> selectors;

  public Selection() {
    this.selectors = new TreeMap<Selector.Type, Selector>();
  }

  public Selection addSelector(Selector.Type type, Selector selector) {
    this.selectors.put(type, selector);
    return this;
  }

  public Map<Selector.Type, Selector> getSelectors() {
    return selectors;
  }
}
