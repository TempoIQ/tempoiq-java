package com.tempoiq.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.tempoiq.DeviceStatus;

public class DeviceStatusModule extends SimpleModule {
  public DeviceStatusModule() {
    addDeserializer(DeviceStatus.class, new DeviceStatusDeserializer());
  }

  private static class DeviceStatusDeserializer extends StdScalarDeserializer<DeviceStatus> {

    public DeviceStatusDeserializer() {
      super(DeviceStatus.class);
    }

    @Override
    public DeviceStatus deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      return jp.readValueAs(new TypeReference<DeviceStatus>() {}); 
      /*DeviceStatus data = Json.getObjectMapper()
        .reader()
        .withType(new TypeReference<DeviceStatus>() {})
        .readValue(node);
      return data;*/
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
