package com.tempoiq;


public class AttributeKeySelector extends Selector {
  private final String key;

  public AttributeKeySelector(String key) {
    this.key = key;
  }

  public String getKey() {
    return this.key;
  }
}
