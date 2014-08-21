package com.tempoiq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.tempoiq.util.Preconditions.*;


/**
 *  A request for writing multiple DataPoints to multiple Sensor.
 *  <p>The request is created and datapoints are added for a Sensor.
 *  @since 1.0.0
 */
public class WriteRequest implements Iterable<WritableDataPoint> {

  private final List<WritableDataPoint> data;

  /**
   *  Base constructor
   *  @since 1.0.0
   */
  public WriteRequest() {
    this.data = new ArrayList<WritableDataPoint>();
  }

  /**
   *  Adds a DataPoint to the request for a Device and Sensor.
   *  @param sensor The Sensor to write to.
   *  @param datapoint The DataPoint to write.
   *  @return The updated request.
   *  @since 1.0.0
   */
  public WriteRequest add(Device device, Sensor sensor, DataPoint datapoint) {
    WritableDataPoint mdp = new WritableDataPoint(device, sensor, datapoint.getTimestamp(), datapoint.getValue());
    data.add(mdp);
    return this;
  }

  /**
   *  Adds a list of DataPoints to the request for a Sensor.
   *  @param sensor The Sensor to write to.
   *  @param datapoints The list of DataPoints to write.
   *  @return The updated request.
   *  @since 1.0.0
   */
  public WriteRequest add(Device device, Sensor sensor, List<DataPoint> datapoints) {
    for(DataPoint datapoint : datapoints) {
      WritableDataPoint mdp = new WritableDataPoint(device, sensor, datapoint.getTimestamp(), datapoint.getValue());
      data.add(mdp);
    }
    return this;
  }

  public Map<String, Map<String, List<DataPoint>>> asMap() {
    Map<String, Map<String, List<DataPoint>>> devices = new HashMap<String, Map<String, List<DataPoint>>>();
    for(WritableDataPoint dp : data) {
      Map<String, List<DataPoint>> sensorMap = devices.get(dp.getDevice().getKey());
      if (sensorMap == null) {
	sensorMap = new HashMap<String, List<DataPoint>>();
	devices.put(dp.getDevice().getKey(), sensorMap);
      }
      List<DataPoint> points = sensorMap.get(dp.getSensor().getKey());
      if (points == null) {
	points = new ArrayList<DataPoint>();
	sensorMap.put(dp.getSensor().getKey(), points);
      }
      points.add(new DataPoint(dp.getTimestamp(), dp.getValue()));
    }

    return devices;
  }

  @Override
  public Iterator<WritableDataPoint> iterator() {
    return data.iterator();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(257, 263)
      .append(data)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof WriteRequest)) return false;

    WriteRequest rhs = (WriteRequest)obj;
    return new EqualsBuilder()
      .append(data, rhs.data)
      .isEquals();
  }
}
