package com.tempoiq;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import static com.tempoiq.util.Preconditions.*;


/**
 *  DataPoint for a Sensor, used for bulk writing of DataPoints.
 *  <p>Allows you to specify a timestamp/value pair, as well as the {@link Sensor}
 *  that it is associated with.
 *  @since 1.0.0
 */
public class WritableDataPoint implements Serializable {
  private Device device;
  private Sensor sensor;
  private DateTime timestamp;
  private Number value;

  /** Serialization lock */
  private static final long serialVersionUID = 1L;

  public WritableDataPoint() {
    this(new Device(""), new Sensor(""), new DateTime(), 0.0);
  }

  /**
   *  Base constructor
   *  @param sensor {@link Sensor} for the DataPoint
   *  @param timestamp Timestamp for the DataPoint
   *  @param value Value for the DataPoint
   *  @since 1.0.0
   */
  public WritableDataPoint(Device device, Sensor sensor, DateTime timestamp, Number value) {
    this.device = checkNotNull(device);
    this.sensor = checkNotNull(sensor);
    this.timestamp = checkNotNull(timestamp);
    this.value = checkNotNull(value);
  }

  /**
   *  Returns the {@link Device} of this WritableDataPoint.
   *  @return the {@link Device}
   *  @since 1.1.0
   */
  public Device getDevice() { return device; }

  /**
   *  Sets the {@link Device} of this WritableDataPoint.
   *  @param device The {@link Device}
   *  @since 1.1.0
   */
  public void setDevice(Device device) { this.device = checkNotNull(device); }

  /**
   *  Returns the {@link Sensor} of this WritableDataPoint.
   *  @return the {@link Sensor}
   *  @since 1.0.0
   */
  public Sensor getSensor() { return sensor; }

  /**
   *  Sets the {@link Sensor} of this WritableDataPoint.
   *  @param sensor The {@link Sensor}
   *  @since 1.0.0
   */
  public void setSensor(Sensor sensor) { this.sensor = checkNotNull(sensor); }

  /**
   *  Returns the timestamp of this WritableDataPoint.
   *  @return the timestamp
   *  @since 1.0.0
   */
  public DateTime getTimestamp() { return timestamp; }

  /**
   *  Sets the timestamp of this WritableDataPoint.
   *  @param timestamp The timestamp of this WritableDataPoint
   *  @since 1.0.0
   */
  public void setTimestamp(DateTime timestamp) { this.timestamp = checkNotNull(timestamp); }

  /**
   *  Returns the value of this WritableDataPoint.
   *  @return the value
   *  @since 1.0.0
   */
  public Number getValue() { return value; }

  /**
   *  Sets the value of this WritableDataPoint.
   *  @param value The value of this WritableDataPoint
   *  @since 1.0.0
   */
  public void setValue(Number value) { this.value = checkNotNull(value); }

  @Override
  public String toString() {
    return String.format("WritableDataPoint(sensor=%s, timestamp=%s, value=%s", sensor, timestamp, value);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(137, 139)
      .append(sensor)
      .append(timestamp)
      .append(value)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof WritableDataPoint)) return false;

    WritableDataPoint rhs = (WritableDataPoint)obj;
    return new EqualsBuilder()
      .append(sensor, rhs.sensor)
      .append(timestamp, rhs.timestamp)
      .append(value, rhs.value)
      .isEquals();
  }
}
