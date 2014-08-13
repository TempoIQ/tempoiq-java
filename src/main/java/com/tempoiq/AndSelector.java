package com.tempoiq;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


public class AndSelector extends Selector {
  private List<Selector> children = new ArrayList<Selector>();
  
  public AndSelector(Selector... children) {
    this.children = Arrays.asList(children);
  }

  public List<Selector> getChildren() {
    return this.children;
  }
}
