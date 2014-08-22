package com.tempoiq.json;

import java.io.IOException;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import com.tempoiq.Selection;
import com.tempoiq.Selector;


public class SelectionModule extends SimpleModule {
  public SelectionModule() {
    addSerializer(Selection.class, new SelectionSerializer());
  }

  private static class SelectionSerializer extends StdScalarSerializer<Selection> {
    public SelectionSerializer() { super(Selection.class); }

    public void serialize(Selection selection, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectFieldStart("filters");
      for (ImmutablePair<Selector.Type, Selector> pair : selection.getSelectors()) {
	String selectorTypeName;
	switch (pair.getLeft()) {
	case DEVICES:
	  selectorTypeName = "devices";
	  break;
	case SENSORS:
	  selectorTypeName = "sensors";
	  break;
	default:
	  throw new JsonGenerationException("Unknown selector type name");
	}
	
	jgen.writeObjectField(selectorTypeName, pair.getRight());
      }
      jgen.writeEndObject();
      jgen.writeEndObject();
    }
  }
}
