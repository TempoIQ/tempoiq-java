package com.tempoiq;

import java.util.List;

public interface Selector<T> {
  /**
   * Returns whether this is a compound selector or
   * not. Basically: Only OR / AND selectors.
   * @return A boolean
   * @since 1.1.0
   */
  boolean isCompound();

  /**
   * Enumerate the list of children, if this is a
   * compount selector.
   * @return A list of child selectors
   * @since 1.1.0
   */
  List<Selector> getChildren();
  
  void setData(T data);

  T getData();
}
