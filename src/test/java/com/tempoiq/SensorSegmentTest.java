package com.tempoiq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;


public class SensorSegmentTest {

  private static final List<Sensor> data1 = Arrays.asList(new Sensor("key", "name", new HashMap<String, String>()));
  private static final List<Sensor> data2 = Arrays.asList(new Sensor("key", "name", new HashMap<String, String>()));
  private static final List<Sensor> data3 = Arrays.asList(new Sensor("key3", "name3", new HashMap<String, String>()));
  private static final Query query1 = new Query(
    new QuerySearch(Selector.Type.DEVICES,
      new Selection().addSelector(Selector.Type.SENSORS, Selector.all())),
    new Pipeline(),
    new ReadAction(DateTime.now(), DateTime.now().minusDays(1)));

  private static final Query query2 = new Query(
    new QuerySearch(Selector.Type.SENSORS,
      new Selection().addSelector(Selector.Type.SENSORS, Selector.key("key3"))),
    new Pipeline(),
    new ReadAction(DateTime.now(), DateTime.now().minusDays(1)));

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
    SensorSegment segment1 = new SensorSegment(data1, new PageLink(query1));
    SensorSegment segment2 = new SensorSegment(data2, new PageLink(query2));
    assertFalse(segment1.equals(segment2));
  }

  @Test
  public void testNotEquals_Null() {
    SensorSegment segment1 = new SensorSegment(data1, null);
    assertFalse(segment1.equals(null));
  }
}
