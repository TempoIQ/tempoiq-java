package com.tempoiq;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import static org.junit.Assert.*;


public class SensorTest {

  private static final Map<String, String> attributes;
  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put("key1", "value1");
    map.put("key2", "value2");
    attributes = Collections.unmodifiableMap(map);
  }

  @Test
  public void testEquals() {
    Sensor s1 = new Sensor("key", "name", attributes);
    Sensor s2 = new Sensor("key", "name", attributes);
    assertEquals(s1, s2);
  }

  @Test
  public void testNotEquals_Key() {
    Sensor s1 = new Sensor("key1", "name", attributes);
    Sensor s2 = new Sensor("key2", "name", attributes);
    assertFalse(s1.equals(s2));
  }

  @Test
  public void testNotEquals_Name() {
    Sensor s1 = new Sensor("key", "name1", attributes);
    Sensor s2 = new Sensor("key", "name2", attributes);
    assertFalse(s1.equals(s2));
  }

  @Test
  public void testNotEquals_Attributes() {
    Map<String, String> attributes2 = new HashMap<String, String>();
    attributes2.put("key1", "value1");

    Sensor s1 = new Sensor("key", "name", attributes);
    Sensor s2 = new Sensor("key", "name", attributes2);
    assertFalse(s1.equals(s2));
  }

  @Test
  public void testNotEquals_Null() {
    Sensor s1 = new Sensor("key", "name1", attributes);
    assertFalse(s1.equals(null));
  }
}
