package com.tempoiq;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import static com.tempoiq.util.Preconditions.*;

public class Row {
  private DateTime timezone;
  private Map<String, Map<String, DataPoint>> values;

  public Row(@JsonProperty("t") DateTime timestamp,
	     @JsonProperty("data") Map<String, Map<String, DataPoint>> values) {
    this.timezone = checkNotNull(timezone);
    this.values = checkNotNull(values);
  }

  public DataPoint getKey(String deviceKey, String sensorKey) {
    Map<String, DataPoint> sensors = values.get(deviceKey);
    if (sensors == null) {
      return null;
    }
    return sensors.get(sensorKey);
  }
}
