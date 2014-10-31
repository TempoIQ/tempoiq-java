package com.tempoiq.json;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tempoiq.Device;
import com.tempoiq.DeviceSegment;
import com.tempoiq.PageLink;


public class DeviceSegmentModule extends SimpleModule {

  public DeviceSegmentModule() {
    addDeserializer(DeviceSegment.class, new DeviceSegmentDeserializer());
  }

  private static class DeviceSegmentDeserializer extends StdScalarDeserializer<DeviceSegment> {
    public DeviceSegmentDeserializer() { super(DeviceSegment.class); }

    @Override
    public DeviceSegment deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
      JsonNode root = parser.readValueAsTree();
      JsonNode devicesNode = root.get("data");
      JsonNode nextPageNode = root.get("next_page");

      if(devicesNode == null) {
        throw context.mappingException("Missing 'data' field in DeviceSegment.");
      }

      List<Device> data = Json.getObjectMapper()
        .reader()
        .withType(new TypeReference<List<Device>>() {})
        .readValue(devicesNode);

      PageLink nextPage;
      if(nextPageNode == null) {
        nextPage = null;
      } else{
        nextPage = Json.getObjectMapper()
          .reader()
          .withType(new TypeReference<PageLink>(){})
          .readValue(nextPageNode);
      }

      return new DeviceSegment(data, nextPage);
    }
  }

  @Override
  public String getModuleName() {
    return "device-segment";
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }
}
