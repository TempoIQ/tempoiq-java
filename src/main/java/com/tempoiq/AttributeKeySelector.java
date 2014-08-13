package com.tempoiq;

import java.util.Collections;
import java.util.List;


public class AttributeKeySelector implements Selector<String> {
  private String key;

  public AttributeKeySelector(String key) {
    setData(key);
  }

  public boolean isCompound() {
    return false;
  }

  public List<Selector> getChildren() {
    return Collections.<Selector>emptyList();
  }

  public void setData(String key) {
    this.key = key;
  }

  public String getData() {
    return this.key;
  }
}
