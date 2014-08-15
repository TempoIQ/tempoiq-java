package com.tempoiq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tempoiq.util.Preconditions.*;

public class Device implements Serializable {
  private String key;
  private String name;
  private Map<String, String> attributes;
  private List<Sensor> sensors;

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
}
