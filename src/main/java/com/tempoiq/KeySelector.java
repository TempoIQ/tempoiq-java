package com.tempoiq;


public class KeySelector extends Selector {
  private final String key;

  public KeySelector(String key) {
    this.key = key;
  }

  public final String getKey() {
    return this.key;
  }
}
