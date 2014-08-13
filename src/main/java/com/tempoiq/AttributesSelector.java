package com.tempoiq;

import org.apache.commons.lang3.tuple.ImmutablePair;


public class AttributesSelector extends Selector {
  private final ImmutablePair<String, String> attr;

  public AttributesSelector(String key, String value) {
    this.attr = ImmutablePair.of(key, value);
  }

  public final ImmutablePair<String, String> getAttributes() {
    return this.attr;
  }
}
