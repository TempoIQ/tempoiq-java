package com.tempoiq;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;

public class Selection {
  private final List<ImmutablePair<Selector.Type, Selector>> selectors;

  public Selection() {
    this.selectors = new ArrayList<ImmutablePair<Selector.Type, Selector>>();
  }

  public Selection addSelector(Selector.Type type, Selector selector) {
    this.selectors.add(ImmutablePair.of(type, selector));
    return this;
  }

  public List<ImmutablePair<Selector.Type, Selector>> getSelectors() {
    return selectors;
  }
}
