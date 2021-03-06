package com.tempoiq.json;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.DateTimeZone;


public class Json {
  private Json() { /* singleton */ }

  private static ObjectMapper mapper;

  protected static ObjectMapper getObjectMapper() {
    if(mapper == null) {
      final ObjectMapper _mapper = new ObjectMapper();
      _mapper.registerModule(new JodaModule());
      _mapper.registerModule(new DateTimeZoneModule());
      _mapper.registerModule(new FoldModule());
      _mapper.registerModule(new DeviceSegmentModule());
      _mapper.registerModule(new IntervalModule());
      _mapper.registerModule(new QueryModule());
      _mapper.registerModule(new RowSegmentModule());
      _mapper.registerModule(new SelectorModule());
      _mapper.registerModule(new SingleActionModule());
      _mapper.registerModule(new SummaryModule());
      _mapper.registerModule(new WritableDataPointModule());
      _mapper.registerModule(new WriteRequestModule());
      _mapper.registerModule(new DirectionFunctionModule());
      _mapper.registerModule(new DeviceStateModule()); 
      _mapper.registerModule(new WriteResponseModule()); 
      _mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      _mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper = _mapper;
    }
    return mapper;
  }

  public static <T> T loads(String src, Class<?> valueType) throws IOException {
    return reader().withType(valueType).readValue(src);
  }

  public static <T> T loads(String src, TypeReference<?> valueType) throws IOException {
    return reader().withType(valueType).readValue(src);
  }

  public static <T> T loads(String src, Class<?> valueType, DateTimeZone timezone) throws IOException {
    return reader(timezone).withType(valueType).readValue(src);
  }

  public static <T> T loads(String src, TypeReference<?> valueType, DateTimeZone timezone) throws IOException {
    return reader(timezone).withType(valueType).readValue(src);
  }

  public static ObjectReader reader() {
    return getObjectMapper().reader();
  }

  public static ObjectReader reader(DateTimeZone timezone) {
    return reader().with(timezone.toTimeZone());
  }

  public static ObjectWriter writer() {
    return getObjectMapper().writer();
  }

  public static ObjectWriter writer(DateTimeZone timezone) {
    return writer().with(timezone.toTimeZone());
  }

  public static String dumps(Object value) throws JsonProcessingException {
    return writer().writeValueAsString(value);
  }

  public static String dumps(Object value, DateTimeZone timezone) throws JsonProcessingException {
    return writer(timezone).writeValueAsString(value);
  }
}
