package com.tempoiq.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.tempoiq.DirectionFunction;
import com.fasterxml.jackson.core.Version;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DirectionFunctionModule extends SimpleModule {
    public DirectionFunctionModule() {
        addDeserializer(DirectionFunction.class, new DirectionFunctionDeserializer());
        addSerializer(DirectionFunction.class, new DirectionFunctionSerializer());
    }

    private static class DirectionFunctionDeserializer extends StdScalarDeserializer<DirectionFunction> {
        private final DirectionFunction[] constants;
        private final List<String> acceptedValues;

        public DirectionFunctionDeserializer() {
            super(DirectionFunction.class);
            this.constants = DirectionFunction.values();
            this.acceptedValues = new ArrayList<String>();
            for (DirectionFunction constant : constants) {
                acceptedValues.add(constant.name());
            }
        }

        @Override
        public DirectionFunction deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            final String text = jp.getText().toUpperCase();
            for (DirectionFunction constant : constants) {
                if (constant.name().equals(text)) {
                    return constant;
                }
            }

            throw ctxt.mappingException(text + " was not one of " + acceptedValues);
        }
    }

    private static class DirectionFunctionSerializer extends StdScalarSerializer<DirectionFunction> {
        public DirectionFunctionSerializer() { super(DirectionFunction.class); }

        @Override
        public void serialize(DirectionFunction value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            String e = value.name().toLowerCase();
            jgen.writeString(e);
        }
    }

    @Override
    public String getModuleName() {
        return "direction-function";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }
}
