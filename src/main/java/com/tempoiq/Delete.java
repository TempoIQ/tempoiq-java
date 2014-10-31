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
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof Delete)) return false;

    Delete rhs = (Delete)obj;
    return new EqualsBuilder()
      .append(start, rhs.start)
      .append(stop, rhs.stop)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(19, 31)
      .append(start)
      .append(stop)
      .toHashCode();
  }
}
