package com.tempoiq.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import com.tempoiq.AllSelector;
import com.tempoiq.AndSelector;
import com.tempoiq.AttributesSelector;
import com.tempoiq.AttributeKeySelector;
import com.tempoiq.KeySelector;
import com.tempoiq.OrSelector;
import com.tempoiq.Selector;


public class SelectorModule extends SimpleModule {
  public SelectorModule() {
    addSerializer(AllSelector.class, new AllSelectorSerializer());
    addSerializer(AndSelector.class, new AndSelectorSerializer());
    addSerializer(AttributesSelector.class, new AttributesSelectorSerializer());
    addSerializer(AttributeKeySelector.class, new AttributeKeySelectorSerializer());
    addSerializer(KeySelector.class, new KeySelectorSerializer());
    addSerializer(OrSelector.class, new OrSelectorSerializer());
  }

  private static class KeySelectorSerializer extends StdScalarSerializer<KeySelector> {
    private static final String selectorField = "key";

    public KeySelectorSerializer() { super(KeySelector.class); }

    public void serialize(KeySelector selector, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectField(selectorField, selector.getKey());
      jgen.writeEndObject();
    }
  }

  private static class AttributesSelectorSerializer extends StdScalarSerializer<AttributesSelector> {
    private static final String selectorField = "attributes";

    public AttributesSelectorSerializer() { super(AttributesSelector.class); }

    public void serialize(AttributesSelector selector, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectFieldStart(selectorField);
      jgen.writeObjectField(selector.getAttributes().getLeft(), selector.getAttributes().getRight());
      jgen.writeEndObject();
      jgen.writeEndObject();
    }
  }

  private static class AttributeKeySelectorSerializer extends StdScalarSerializer<AttributeKeySelector> {
    private static final String selectorField = "attribute";

    public AttributeKeySelectorSerializer() { super(AttributeKeySelector.class); }

    public void serialize(AttributeKeySelector selector, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectField(selectorField, selector.getKey());
      jgen.writeEndObject();
    }
  }

  private static class OrSelectorSerializer extends StdScalarSerializer<OrSelector> {
    private static final String selectorField = "or";

    public OrSelectorSerializer() { super(OrSelector.class); }

    public void serialize(OrSelector selector, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeArrayFieldStart(selectorField);
      for (Selector child : selector.getChildren()) {
	jgen.writeRawValue(Json.dumps(child));
      }
      jgen.writeEndArray();
      jgen.writeEndObject();
    }
  }

  private static class AndSelectorSerializer extends StdScalarSerializer<AndSelector> {
    private static final String selectorField = "and";

    public AndSelectorSerializer() { super(AndSelector.class); }

    public void serialize(AndSelector selector, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeArrayFieldStart(selectorField);
      for (Selector child : selector.getChildren()) {
	jgen.writeRawValue(Json.dumps(child));
      }
      jgen.writeEndArray();
      jgen.writeEndObject();
    }
  }

  private static class AllSelectorSerializer extends StdScalarSerializer<AllSelector> {
    public AllSelectorSerializer() { super(AllSelector.class); }

    public void serialize(AllSelector selector, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeString("all");
    }
  }
}
