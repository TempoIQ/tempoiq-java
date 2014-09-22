package com.tempoiq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.*;
import static org.junit.Assert.*;


public class SensorSegmentTest {

  private static final List<Sensor> data1 = Arrays.asList(new Sensor("key", "name", new HashMap<String, String>()));
  private static final List<Sensor> data2 = Arrays.asList(new Sensor("key", "name", new HashMap<String, String>()));
  private static final List<Sensor> data3 = Arrays.asList(new Sensor("key3", "name3", new HashMap<String, String>()));

  @Test
  public void testEquals() {
    SensorSegment segment1 = new SensorSegment(data1, null);
    SensorSegment segment2 = new SensorSegment(data2, null);
    assertEquals(segment1, segment2);
  }

  @Test
  public void testNotEquals_Data() {
    SensorSegment segment1 = new SensorSegment(data1, null);
    SensorSegment segment2 = new SensorSegment(data3, null);
    assertFalse(segment1.equals(segment2));
  }

  @Test
  public void testNotEquals_Next() {
    SensorSegment segment1 = new SensorSegment(data1, "next1");
    SensorSegment segment2 = new SensorSegment(data2, "next2");
    assertFalse(segment1.equals(segment2));
  }

  @Test
  public void testNotEquals_Null() {
    SensorSegment segment1 = new SensorSegment(data1, "next1");
    assertFalse(segment1.equals(null));
  }
}
