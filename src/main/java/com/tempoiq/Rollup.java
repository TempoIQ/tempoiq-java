package com.tempoiq;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import static com.tempoiq.util.Preconditions.*;


/**
 *  The representation of a rollup of a single {@link Sensor}.
 *
 *  A Rollup allows you to specify a time period and folding function for a
 *  {@link DataPoint} query. A Rollup represents a reduction in the amount of data
 *  returned to you. For instance, if you want to know the hourly average of a range
 *  of data, the Rollup is specified as:
 *
 *  <p><pre>
 *  import org.joda.time.Period;
 *
 *  Rollup rollup = new Rollup(Period.hours(1), Fold.SUM);
 *  </pre>
 *  @see Fold
 *  @since 1.0.0
 */
public class Rollup implements Serializable, PipelineFunction {
  private Period period;
  private Fold fold;
  private DateTime start;
  private DateTime stop;
  private final static PeriodFormatter periodFormat = ISOPeriodFormat.standard();
  private final static DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime().withZoneUTC();

  /** Serialization lock */
  private static final long serialVersionUID = 1L;

  public Rollup() {
    this(Period.minutes(1), Fold.SUM, DateTime.now(), DateTime.now().plusHours(1));
  }

  /**
   *  Base constructor
   *
   *  @param period The rollup period.
   *  @param fold The rollup folding function.
   *  @param start The rollup start time
   *  @param stop The rollup stop time
   *  @since 1.1.0
   */
  public Rollup(Period period, Fold fold, DateTime start, DateTime stop) {
    this.period = period;
    this.fold = fold;
    this.start = start;
    this.stop = stop;
  }

  public String getName() {
    return "rollup";
  }

  public List<String> getArguments() {
    String[] args = new String[] {
      fold.name().toLowerCase(),
      periodFormat.print(period),
      dateFormat.print(start),
      dateFormat.print(stop)
    };

    return Arrays.asList(args);
  }

  /**
   *  Returns the rollup period.
   *  @return The rollup period.
   *  @since 1.0.0
   */
  @JsonIgnore
  public Period getPeriod() { return period; }

  /**
   *  Sets the rollup period.
   *  @param period The period.
   *  @since 1.0.0
   */
  public void setPeriod(Period period) { this.period = checkNotNull(period); }

  /**
   * Returns the rollup folding function.
   * @return The rollup folding function.
   * @since 1.0.0
   */
  @JsonIgnore
  public Fold getFold() { return fold; }

  /** Sets the rollup folding function.
   *  @param fold The rollup folding function.
   *  @since 1.0.0
   */
  public void setFold(Fold fold) { this.fold = checkNotNull(fold); }

  /**
   *  Returns the rollup start time.
   *  @return The rollup start datetime.
   *  @since 1.1.0
   */
  @JsonIgnore
  public DateTime getStart() { return start; }

  /**
   *  Sets the rollup start time.
   *  @param start The start datetime.
   *  @since 1.1.0
   */
  public void setStart(DateTime start) { this.start = checkNotNull(start); }

  /**
   *  Returns the rollup stop time.
   *  @return The rollup stop datetime.
   *  @since 1.1.0
   */
  @JsonIgnore
  public DateTime getStop() { return stop; }

  /**
   *  Sets the rollup start time.
   *  @param start The start datetime.
   *  @since 1.1.0
   */
  public void setStop(DateTime stop) { this.stop = checkNotNull(stop); }


  @Override
  public String toString() {
    return String.format("Rollup(period=%s,fold=%s,start=%s,stop=%s)", period.toString(), fold.toString().toLowerCase(), start.toString(), stop.toString());
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(13, 41)
      .append(period)
      .append(fold)
      .append(start)
      .append(stop)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof Rollup)) return false;

    Rollup rhs = (Rollup)obj;
    return new EqualsBuilder()
      .append(period, rhs.period)
      .append(fold, rhs.fold)
      .append(start, rhs.start)
      .append(stop, rhs.stop)
      .isEquals();
  }
}
