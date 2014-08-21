package com.tempoiq.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.Sensor;


public class SensorTest {

  private static final Map<String, String> attributes;
  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put("key1", "value1");
    attributes = Collections.unmodifiableMap(map);
  }

  private static final Sensor sensor = new Sensor("key1", "name1", attributes);

  @Test
  public void testDeserialize() throws IOException {
    String json = "{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{\"key1\":\"value1\"}}";
    Sensor deserialized = Json.loads(json, Sensor.class);
    assertEquals(sensor, deserialized);
  }

  @Test
  public void testSerialize() throws IOException {
    String expected = "{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{\"key1\":\"value1\"}}";
    String json = Json.dumps(sensor);
    assertEquals(expected, json);
  }
}
