package com.tempoiq.json;

import java.io.IOException;

import java.util.Map;

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
import com.tempoiq.Query;
import com.tempoiq.QueryAction;
import com.tempoiq.QuerySearch;



public class QueryModule extends SimpleModule {
  public QueryModule() {
    addSerializer(Query.class, new QuerySerializer());
    addSerializer(QuerySearch.class, new QuerySearchSerializer());
    addSerializer(Selection.class, new SelectionSerializer());
    addSerializer(Selector.Type.class, new SelectorTypeSerializer());
  }

  private static class QuerySerializer extends StdScalarSerializer<Query> {
    public QuerySerializer() { super(Query.class); }

    public void serialize(Query query, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectField("search", query.getSearch());
      if (query.getPipeline() != null) {
	jgen.writeObjectField("fold", query.getPipeline());
      }
      QueryAction action = query.getAction();
      jgen.writeObjectField(action.getName(), action);
      jgen.writeEndObject();
    }
  }

  private static class SelectionSerializer extends StdScalarSerializer<Selection> {
    public SelectionSerializer() { super(Selection.class); }

    public void serialize(Selection selection, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      for (Map.Entry<Selector.Type, Selector> entry : selection.getSelectors().entrySet()) {
	jgen.writeObjectField(entry.getKey().name().toLowerCase(), entry.getValue());
      }
      jgen.writeEndObject();
    }
  }

  private static class QuerySearchSerializer extends StdScalarSerializer<QuerySearch> {
    public QuerySearchSerializer() { super(QuerySearch.class); }

    public void serialize(QuerySearch qs, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectField("select", qs.getType());
      jgen.writeObjectField("filters", qs.getSelection());
      jgen.writeEndObject();
    }
  }

  private static class SelectorTypeSerializer extends StdScalarSerializer<Selector.Type> {
    public SelectorTypeSerializer() { super(Selector.Type.class); }

    public void serialize(Selector.Type value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      jgen.writeString(value.name().toLowerCase());
    }
  }
}
