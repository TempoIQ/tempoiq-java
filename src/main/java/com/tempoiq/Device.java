package com.tempoiq;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;

public class Device implements Serializable {
  private String key;
  private String name;
  private Map<String, String> attributes;
  private List<Sensor> sensors;

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  public Device() {
    this("", "", new HashMap<String, String>(), new ArrayList<Sensor>());
  }

  public Device(String key, String name, Map<String, String> attributes, List<Sensor> sensors) {
    this.key = checkNotNull(key);
    this.name = checkNotNull(name);
    this.attributes = checkNotNull(attributes);
    this.sensors = checkNotNull(sensors);
  }

  /**
   *  Key-only constructor.
   *  <ul>
   *    <li><tt>name</tt> default to ""</li>
   *    <li><tt>attributes</tt> default to empty map</li>
   *  </ul>
   *  @param key Device key
   *  @since 1.1.0
   */
  public Device(String key) {
    this(key, "", new HashMap<String, String>(), new ArrayList<Sensor>());
  }

  public String getKey() { return key; }
  public void setKey(String key) { this.key = checkNotNull(key); }

  public String getName() { return name; }
  public void setName(String name) { this.name = checkNotNull(name); }

  public Map<String, String> getAttributes() { return attributes; }
  public void setAttributes(Map<String, String> attributes) { this.attributes = checkNotNull(attributes); }

  public List<Sensor> getSensors() { return sensors; }
  public void setSensors(List<Sensor> sensors) { this.sensors = sensors; }

  static Device make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    Device device = Json.loads(body, Device.class);
    return device;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(183, 147)
      .append(key)
      .append(name)
      .append(attributes)
      .append(sensors)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof Device)) return false;

    Device rhs = (Device)obj;
    return new EqualsBuilder()
      .append(key, rhs.key)
      .append(name, rhs.name)
      .append(attributes, rhs.attributes)
      .append(sensors, rhs.sensors)
      .isEquals();
  }
}
