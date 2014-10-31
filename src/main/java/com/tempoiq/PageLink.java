package com.tempoiq;

import com.fasterxml.jackson.annotation.JsonProperty;
import static com.tempoiq.util.Preconditions.*;

public class PageLink {
  private Query nextQuery;

  public Query getNextQuery() {
    return nextQuery;
  }

  public void setNextQuery(Query nextQuery) {
    this.nextQuery = checkNotNull(nextQuery);
  }

  public PageLink(@JsonProperty("next_query") Query nextQuery) {
    this.nextQuery = checkNotNull(nextQuery);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PageLink pageLink = (PageLink) o;

    if (!nextQuery.equals(pageLink.nextQuery)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return nextQuery.hashCode();
  }
}
