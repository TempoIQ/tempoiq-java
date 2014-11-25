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
import com.tempoiq.Row;
import com.tempoiq.RowSegment;

public class RowSegmentModule extends SimpleModule {

  public RowSegmentModule() {
    addDeserializer(RowSegment.class, new RowSegmentDeserializer());
  }

  private static class RowSegmentDeserializer extends StdScalarDeserializer<RowSegment> {
    public RowSegmentDeserializer() { super(RowSegment.class); }

    @Override
    public RowSegment deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
      JsonNode root = parser.readValueAsTree();
      JsonNode rowsNode = root.get("data");
      JsonNode pageNode = root.get("next_page");

      if(rowsNode == null) {
        throw context.mappingException("Missing 'data' field in RowSegment.");
      }

      List<Row> data = Json.getObjectMapper()
              .reader()
              .withType(new TypeReference<List<Row>>() {})
              .readValue(rowsNode);


      if (pageNode != null) {
        JsonNode queryNode = pageNode.get("next_query");
        if (queryNode == null) {
          throw context.mappingException("Missing 'next_query' field in RowSegment.");
        } else {
          String nextPage = Json.getObjectMapper().writeValueAsString(queryNode);
          return new RowSegment(data, nextPage);
        }
      } else {
        return new RowSegment(data);
      }
    }
  }

  @Override
  public String getModuleName() {
    return "row-segment";
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }
}
