package com.tempoiq.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.Sensor;
import com.tempoiq.SensorSegment;


public class SensorSegmentTest {

  private static final Map<String, String> attributes;
  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put("key1", "value1");
    attributes = Collections.unmodifiableMap(map);
  }

  @Test
  public void testDeserialize() throws IOException {
    String json = "[" +
      "{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{\"key1\":\"value1\"}}" +
    "]";

    SensorSegment segment = Json.loads(json, SensorSegment.class);
    List<Sensor> data = Arrays.asList(new Sensor("key1", "name1", attributes));
    SensorSegment expected = new SensorSegment(data, null);
    assertEquals(expected, segment);
  }
}
