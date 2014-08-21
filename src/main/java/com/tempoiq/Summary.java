package com.tempoiq;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;


/**
 *  A set of summary statistics for a sensor.
 *
 *  @since 1.1.0
 */
public class Summary implements Map<String, Number>, Serializable {

  private Sensor sensor;
  private Interval interval;
  private Map<String, Number> summary;

  /** Serialization lock */
  private static final long serialVersionUID = 1L;

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  public Summary() {
    this(new Sensor(""), new Interval(new DateTime(), new DateTime()), new HashMap<String, Number>());
  }

  /**
   *  Base constructor
   *
   *  @param sensor Sensor
   *  @param interval Interval of data to calculate the statistics
   *  @param summary Map of statistic string to value
   *  @since 1.1.0
   */
  public Summary(Sensor sensor, Interval interval, Map<String, Number> summary) {
    this.sensor = checkNotNull(sensor);
    this.interval = checkNotNull(interval);
    this.summary = checkNotNull(summary);
  }

  /**
   *  Returns the sensor of this Summary
   *  @return Summary sensor.
   *  @since 1.1.0
   */
  public Sensor getSensor() { return sensor; }

  /**
   *  Sets the sensor of this Summary.
   *  @param sensor Summary sensor.
   *  @since 1.1.0
   */
  public void setSensor(Sensor sensor) { this.sensor = checkNotNull(sensor); }

  /**
   *  Returns the interval of this Summary.
   *  @return Summary interval.
   *  @since 1.1.0
   */
  public Interval getInterval() { return interval; }

  /**
   *  Sets the interval of this Summary.
   *  @param interval Summary interval.
   *  @since 1.1.0
   */
  public void setInterval(Interval interval) { this.interval = checkNotNull(interval); }

  public void clear() { summary.clear(); }

  public boolean containsKey(Object key) { return summary.containsKey(key); }

  public boolean containsValue(Object value) { return summary.containsValue(value); }

  public Set<Map.Entry<String, Number>> entrySet() { return summary.entrySet(); }

  public Number get(Object key) { return summary.get(key); }

  public boolean isEmpty() { return summary.isEmpty(); }

  public Set<String> keySet() { return summary.keySet(); }

  public Number put(String key, Number value) { return summary.put(key, value); }

  public void putAll(Map<? extends String,? extends Number> m) { summary.putAll(m); }

  public Number remove(Object key) { return summary.remove(key); }

  public int size() { return summary.size(); }

  public Collection<Number> values() { return summary.values(); }

  static Summary make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    Summary summary = Json.loads(body, Summary.class);
    return summary;
  }

  @Override
  public String toString() {
    return String.format("Summary(sensor=%s, interval=%s, summary=%s)", sensor, interval, summary);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(119, 123)
      .append(sensor)
      .append(interval)
      .append(summary)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof Summary)) return false;

    Summary rhs = (Summary)obj;
    return new EqualsBuilder()
      .append(sensor, rhs.sensor)
      .append(interval, rhs.interval)
      .append(summary, rhs.summary)
      .isEquals();
  }
}
