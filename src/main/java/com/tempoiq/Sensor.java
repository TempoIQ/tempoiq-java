package com.tempoiq;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;


/**
 *  A data sensor and it's metadata.
 *
 *  @since 1.0.0
 */
public class Sensor implements Serializable {
  private String key;
  private String name;
  private Map<String, String> attributes;

  /** Serialization lock */
  private static final long serialVersionUID = 1L;

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  public Sensor() {
    this("", "", new HashMap<String, String>());
  }

  /**
   *  Key-only constructor.
   *  <ul>
   *    <li><tt>name</tt> default to ""</li>
   *    <li><tt>attributes</tt> default to empty map</li>
   *  </ul>
   *  @param key Sensor key
   *  @since 1.0.0
   */
  public Sensor(String key) {
    this(key, "", new HashMap<String, String>());
  }

  /**
   *  Base constructor
   *
   *  @param key Sensor key
   *  @param name Human readable name for the Sensor
   *  @param attributes Map of key/value pair metadata
   *  @since 1.0.0
   */
  public Sensor(String key, String name, Map<String, String> attributes) {
    this.key = checkNotNull(key);
    this.name = checkNotNull(name);
    this.attributes = checkNotNull(attributes);
  }

  /**
   *  Returns the key of this Sensor.
   *  @return Sensor key.
   *  @since 1.0.0
   */
  public String getKey() { return key; }

  /**
   *  Sets the key of this Sensor.
   *  @param key Sensor key.
   *  @since 1.0.0
   */
  public void setKey(String key) { this.key = checkNotNull(key); }

  /**
   *  Returns the name of this Sensor.
   *  @return Sensor name.
   *  @since 1.0.0
   */
  public String getName() { return name; }

  /**
   *  Sets the name of this Sensor.
   *  @param name Sensor name.
   *  @since 1.0.0
   */
  public void setName(String name) { this.name = checkNotNull(name); }

  /**
   *  Returns the attributes of this Sensor.
   *  @return Sensor attributes.
   *  @since 1.0.0
   */
  public Map<String, String> getAttributes() { return attributes; }

  /**
   *  Sets the attributes of this Sensor.
   *  @param attributes Sensor attributes.
   *  @since 1.0.0
   */
  public void setAttributes(Map<String, String> attributes) { this.attributes = checkNotNull(attributes); }

  static Sensor make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    Sensor sensor = Json.loads(body, Sensor.class);
    return sensor;
  }

  @Override
  public String toString() {
    return String.format("Sensor(key=%s, name=%s, attributes=%s)", key, name, attributes);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(119, 123)
      .append(key)
      .append(name)
      .append(attributes)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof Sensor)) return false;

    Sensor rhs = (Sensor)obj;
    return new EqualsBuilder()
      .append(key, rhs.key)
      .append(name, rhs.name)
      .append(attributes, rhs.attributes)
      .isEquals();
  }
}
