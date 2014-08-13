package com.tempoiq;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;


public class AttributesSelector implements Selector<ImmutablePair<String, String>> {
  private ImmutablePair<String, String> attr;

  public AttributesSelector(String key, String value) {
    setData(ImmutablePair.of(key, value));
  }

  public boolean isCompound() {
    return false;
  }

  public List<Selector> getChildren() {
    return Collections.<Selector>emptyList();
  }

  public void setData(ImmutablePair<String, String> attr) {
    this.attr = attr;
  }

  public ImmutablePair<String, String> getData() {
    return this.attr;
  }
}
