package com.tempoiq;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import com.tempoiq.json.Json;

public class WriteResponse {
  private HashMap<String, DeviceStatus> statuses;

  public WriteResponse(HashMap<String, DeviceStatus> response) {
    this.statuses = response;
  }

  public boolean wasSuccessful() {
    for (Map.Entry<String, DeviceStatus> entry : statuses.entrySet()) {
      if (entry.getValue().getSuccess() == false) {
        return false;
      }
    }
    return true;
  }

  public boolean wasPartiallySuccessful() {
    int failures = 0;
    Set<Map.Entry<String, DeviceStatus>> entries = this.statuses.entrySet();
    for (Map.Entry<String, DeviceStatus> entry : entries) { 
      if (entry.getValue().getSuccess() == false) {
        failures += 1;
      }
    }
    return failures < entries.size();
  }

  private HashMap<String, DeviceStatus> filterByState(DeviceStatus.DeviceState state) {
    HashMap<String, DeviceStatus> results = new HashMap<String, DeviceStatus>();
    for (Map.Entry<String, DeviceStatus> entry : this.statuses.entrySet()) { 
      if (entry.getValue().getState() == state) {
        results.put(entry.getKey(), entry.getValue());
      }
    }
    return results;
  }

  public HashMap<String, DeviceStatus> getExisting() {
    return filterByState(DeviceStatus.DeviceState.EXISTING);
  }

  public HashMap<String, DeviceStatus> getModified() {
    return filterByState(DeviceStatus.DeviceState.MODIFIED);
  }

  public HashMap<String, DeviceStatus> getCreated() {
    return filterByState(DeviceStatus.DeviceState.CREATED);
  }

  public HashMap<String, DeviceStatus> getFailures() {
    HashMap<String, DeviceStatus> results = new HashMap<String, DeviceStatus>();
    for (Map.Entry<String, DeviceStatus> entry : this.statuses.entrySet()) { 
      if (entry.getValue().getSuccess() == false) {
        results.put(entry.getKey(), entry.getValue());
      }
    }
    return results;
  }
  
  public static WriteResponse make(HttpResponse response) throws java.io.IOException {
    HttpEntity entity = response.getEntity();
    String responseString = EntityUtils.toString(entity, "UTF-8");
    if (responseString.isEmpty()) {
      responseString = "{}";
    }
    WriteResponse data = Json.loads(responseString, WriteResponse.class);
    return data; 
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(statuses)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof WriteResponse)) return false;

    WriteResponse rhs = (WriteResponse)obj;
    return new EqualsBuilder()
      .append(statuses, rhs.statuses)
      .isEquals();
  }
}
