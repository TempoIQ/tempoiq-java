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

import com.tempoiq.AttributesSelector;
import com.tempoiq.AttributeKeySelector;
import com.tempoiq.KeySelector;


public class SelectorModule extends SimpleModule {
  public SelectorModule() {
    addSerializer(AttributesSelector.class, new AttributesSelectorSerializer());
    addSerializer(AttributeKeySelector.class, new AttributeKeySelectorSerializer());
    addSerializer(KeySelector.class, new KeySelectorSerializer());
  }

  private static class KeySelectorSerializer extends StdScalarSerializer<KeySelector> {
    private static final String selectorField = "key";

    public KeySelectorSerializer() { super(KeySelector.class); }

    public void serialize(KeySelector selector, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectField(selectorField, selector.getData());
      jgen.writeEndObject();
    }
  }

  private static class AttributesSelectorSerializer extends StdScalarSerializer<AttributesSelector> {
    private static final String selectorField = "attributes";

    public AttributesSelectorSerializer() { super(AttributesSelector.class); }

    public void serialize(AttributesSelector selector, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectFieldStart(selectorField);
      jgen.writeObjectField(selector.getData().getLeft(), selector.getData().getRight());
      jgen.writeEndObject();
      jgen.writeEndObject();
    }
  }

  private static class AttributeKeySelectorSerializer extends StdScalarSerializer<AttributeKeySelector> {
    private static final String selectorField = "attribute";

    public AttributeKeySelectorSerializer() { super(AttributeKeySelector.class); }

    public void serialize(AttributeKeySelector selector, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectField(selectorField, selector.getData());
      jgen.writeEndObject();
    }
  }
}
