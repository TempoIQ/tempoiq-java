package com.tempoiq;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.tempoiq.util.Preconditions.*;


/**
 *  The representation of an aggregation between multiple Sensor.
 *
 *  An Aggregation allows you to specify a new Sensor that is a mathematical
 *  operation across multiple Sensor. For instance, the following Aggregation
 *  specifies the sum of multiple Sensor:
 *
 *  <p><pre>
 *  Aggregation aggregation = new Aggregation(Fold.SUM);
 *  </pre>
 *  @see Fold
 *  @since 1.0.0
 */
public class Aggregation implements Serializable, PipelineFunction {
  private Fold fold;

  /** Serialization lock */
  private static final long serialVersionUID = 1L;

  public Aggregation() {
    this(Fold.SUM);
  }

  /**
   *  Base constructor
   *  @param fold The aggregation's folding function.
   *  @since 1.0.0
   */
  public Aggregation(Fold fold) {
    this.fold = checkNotNull(fold);
  }

  /**
   *  Returns the {@link Fold}.
   *  @return The aggregation's folding function.
   *  @since 1.0.0
   */
  @JsonIgnore
  public Fold getFold() { return fold; }

  /**
   *  Sets the {@link Fold}.
   *  @param fold The folding function to use.
   *  @since 1.0.0
   */
  public void setFold(Fold fold) { this.fold = checkNotNull(fold); }

  public String getName() {
    return "aggregation";
  }

  public List<String> getArguments() {
    return Arrays.asList(new String[] { fold.name().toLowerCase() });
  }

  @Override
  public String toString() {
    return String.format("Aggregation(fold=%s)", fold.toString().toLowerCase());
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(107, 109)
      .append(fold)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof Aggregation)) return false;

    Aggregation rhs = (Aggregation)obj;
    return new EqualsBuilder()
      .append(fold, rhs.fold)
      .isEquals();
  }
}
