package com.tempoiq.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.tempoiq.DeviceStatus;

public class DeviceStateModule extends SimpleModule {
  public DeviceStateModule() {
    addDeserializer(DeviceStatus.DeviceState.class, new DeviceStateDeserializer());
  }

  private static class DeviceStateDeserializer extends StdScalarDeserializer<DeviceStatus.DeviceState> {

    public DeviceStateDeserializer() {
      super(DeviceStatus.DeviceState.class);
    }

    @Override
    public DeviceStatus.DeviceState deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      final String text = jp.getText();
      if (text.equals("existing")) {
          return DeviceStatus.DeviceState.EXISTING;
      } else if (text.equals("modified")) {
          return DeviceStatus.DeviceState.MODIFIED;
      } else if (text.equals("created")) {
          return DeviceStatus.DeviceState.CREATED;
      } else {
        throw ctxt.mappingException("Got unknown device state: " + text); 
      }
    }
  }

  @Override
  public String getModuleName() {
    return "deviceState";
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }
}
