package com.tempoiq.json;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.tempoiq.SingleValueAction;

public class SingleValueActionModule extends SimpleModule {

    private static class SingleValueActionDeserializer extends StdScalarSerializer<SingleValueAction> {
        public SingleValueActionDeserializer() { super(SingleValueAction.class); }

    @Override
    public void serialize(SingleValueAction action, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeEndObject();
    }
  }

    @Override
    public String getModuleName() {
        return "singlevalue";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }
}

