package com.tempoiq.json;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tempoiq.*;

public class PageLinkModule extends SimpleModule {
  public PageLinkModule() {
    addDeserializer(PageLink.class, new PageLinkDeserializer());
  }

  private static class PageLinkDeserializer extends StdScalarDeserializer<PageLink> {
    public PageLinkDeserializer() {
      super(PageLink.class);
    }

    @Override
    public PageLink deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
      JsonNode root = Json.getObjectMapper().readTree(parser);
      JsonNode nextQueryNode = root.get("next_query");
      if (nextQueryNode == null) {
        throw new TempoIQException("Error parsing next_page: no value given for next_query", 0);
      } else {
        return new PageLink(nextQueryNode.toString());
      }
    }
  }

  @Override
  public String getModuleName() {
    return "page-link";
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }
}