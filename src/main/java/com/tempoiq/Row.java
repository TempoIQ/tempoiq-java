package com.tempoiq;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import static com.tempoiq.util.Preconditions.*;

public class Row {
  private DateTime timestamp;
  private Map<String, Map<String, Number>> values;

  public Row(@JsonProperty("t") DateTime timestamp,
	     @JsonProperty("data") Map<String, Map<String, Number>> values) {
    this.timestamp = checkNotNull(timestamp);
    this.values = checkNotNull(values);
  }

  public DateTime getTimestamp() {
    return timestamp;
  }

  public Number getValue(String deviceKey, String sensorKey) {
    Map<String, Number> sensors = values.get(deviceKey);
    if (sensors == null) {
      return null;
    }

    return sensors.get(sensorKey);
  }

  public boolean hasSensor(String deviceKey, String sensorKey) {
    Map<String, Number> sensors = values.get(deviceKey);
    if (sensors == null) {
      return false;
    }

    return sensors.containsKey(sensorKey);
  }
  
  public Map<String, Map<String, Number>> getValues() {
	  return values;
  }
}
