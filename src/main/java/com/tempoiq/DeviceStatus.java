package com.tempoiq;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DeviceStatus {
  private DeviceState state;
  private boolean success;
  private String message;

  public enum DeviceState {
    EXISTING,
    MODIFIED,
    CREATED
  }

  public DeviceStatus(
      @JsonProperty("device_state") DeviceState state, 
      @JsonProperty("success") boolean success, 
      @JsonProperty("message") String message) {
    this.state = state;
    this.success = success;
    this.message = message;
  }

  public DeviceState getState() {
    return this.state;
  }

  public boolean getSuccess() {
    return this.success;
  }

  public String getMessage() {
    return this.message;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(state)
      .append(success)
      .append(message)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof DeviceStatus)) return false;

    DeviceStatus rhs = (DeviceStatus)obj;
    return new EqualsBuilder()
      .append(state, rhs.state)
      .append(success, rhs.success)
      .append(message, rhs.message)
      .isEquals();
  }
}
