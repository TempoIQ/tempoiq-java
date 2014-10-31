package com.tempoiq;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import static com.tempoiq.util.Preconditions.*;

/**
 * this represents the range of time to deleteDataPoints for a sensor
 */
public class Delete implements Serializable {
  
  private DateTime start;
  private DateTime stop;

  public Delete(@JsonProperty("start") DateTime start, @JsonProperty("stop") DateTime stop) {
    this.start = checkNotNull(start);
    this.stop = checkNotNull(stop);
  }

  public DateTime getStart() {
    return start;
  }

  public void setStart(DateTime start) {
    this.start = checkNotNull(start);
  }

  public DateTime getStop() {
    return stop;
  }

  public void setStop(DateTime stop) {
    this.stop = checkNotNull(stop);
  }

  @Override
  public String toString() {
    return "Delete{" +
            "start=" + start +
            ", stop=" + stop +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Delete delete = (Delete) o;

    if (!start.equals(delete.start)) return false;
    if (!stop.equals(delete.stop)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = start.hashCode();
    result = 31 * result + stop.hashCode();
    return result;
  }
}
