package com.tempoiq.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import org.junit.*;
import static org.junit.Assert.*;
import com.tempoiq.Device;
import com.tempoiq.Sensor;

public class DeviceTest {
  private static final Map<String, String> attributes;

  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put("key1", "value1");
    attributes = Collections.unmodifiableMap(map);
  }

  private static final Sensor sensor = new Sensor("key1", "name1", attributes);
  private static final Device device = new Device("key1", "name1", attributes, new ArrayList<Sensor>());

  @Test
  public void testDeserialize() throws IOException {
    final Device expected = new Device("key1", "name1", attributes, Arrays.asList(sensor));    
    String json = "{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{\"key1\":\"value1\"},\"sensors\":[{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{\"key1\":\"value1\"}}]}";
    Device deserialized = Json.loads(json, Device.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void testSerializeEmptySensors() throws IOException {
    String expected = "{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{\"key1\":\"value1\"},\"sensors\":[]}";
    assertEquals(expected, Json.dumps(device));
  }

  @Test
  public void testSerializeWithSensors() throws IOException {
    final Device device = new Device("key1", "name1", attributes, Arrays.asList(sensor));    
    String expected = "{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{\"key1\":\"value1\"},\"sensors\":[{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{\"key1\":\"value1\"}}]}";
    assertEquals(expected, Json.dumps(device));
  }
}
