package com.tempoiq;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;

/**
 *  A Sensor/DataPoint pair for use with the getSingleValue calls
 *  @since 1.1.0
 */
public class SingleValue implements Serializable {

  private Sensor sensor;
  private DataPoint datapoint;

  /** Serialization lock */
  private static final long serialVersionUID = 1L;

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  public SingleValue() {
    this(new Sensor(), new DataPoint());
  }

  /**
   *  Base constructor
   *  @param sensor The Sensor associated with the datapoint
   *  @param datapoint The datapoint
   *  @since 1.1.0
   */
  public SingleValue(@JsonProperty("sensor") Sensor sensor, @JsonProperty("data") DataPoint datapoint) {
    this.sensor = checkNotNull(sensor);
    this.datapoint = checkNotNull(datapoint);
  }

  /**
   *  Returns the sensor of this SingleValue.
   *  @return the sensor
   *  @since 1.1.0
   */
  @JsonProperty("sensor")
  public Sensor getSensor() { return sensor; }

  /**
   *  Sets the sensor of this SingleValue.
   *  @param sensor The sensor of this SingleValue
   *  @since 1.1.0
   */
  public void setSensor(Sensor sensor) { this.sensor = checkNotNull(sensor); }

  /**
   *  Returns the datapoint of this SingleValue.
   *  @return the datapoint
   *  @since 1.1.0
   */
  @JsonProperty("data")
  public DataPoint getDataPoint() { return datapoint; }

  /**
   *  Sets the datapoint of this SingleValue.
   *  @param datapoint The DataPoint of this SingleValue
   *  @since 1.1.0
   */
  public void setDataPoint(DataPoint datapoint) { this.datapoint = checkNotNull(datapoint); }

  static SingleValue make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    SingleValue value = Json.loads(body, SingleValue.class);
    return value;
  }

  @Override
  public String toString() {
    return String.format("SingleValue(sensor=%s, datapoint=%s)", sensor.toString(), datapoint.toString());
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(19, 31)
      .append(sensor)
      .append(datapoint)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof SingleValue)) return false;

    SingleValue rhs = (SingleValue)obj;
    return new EqualsBuilder()
      .append(sensor, rhs.sensor)
      .append(datapoint, rhs.datapoint)
      .isEquals();
  }
}
