package com.tempoiq.json;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import com.tempoiq.Sensor;
import com.tempoiq.SensorSegment;


public class SensorSegmentModule extends SimpleModule {

  public SensorSegmentModule() {
    addDeserializer(SensorSegment.class, new SensorSegmentDeserializer());
  }

  private static class SensorSegmentDeserializer extends StdScalarDeserializer<SensorSegment> {
    public SensorSegmentDeserializer() { super(SensorSegment.class); }

    @Override
    public SensorSegment deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
      if(parser.getCurrentToken() == JsonToken.START_ARRAY) {
        List<Sensor> sensor = parser.readValueAs(new TypeReference<List<Sensor>>() {});
        return new SensorSegment(sensor);
      }
      throw context.mappingException("Expected JSON array");
    }
  }

  @Override
  public String getModuleName() {
    return "sensor-segment";
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }
}
