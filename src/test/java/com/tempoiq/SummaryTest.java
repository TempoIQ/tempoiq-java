package com.tempoiq;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.*;
import static org.junit.Assert.*;


public class SummaryTest {

  private static final Sensor sensor1 = new Sensor("key1");
  private static final Sensor sensor2 = new Sensor("key1");
  private static final Sensor sensor3 = new Sensor("key2");

  private static final DateTime dt1 = new DateTime(2012, 1, 1, 0, 0, 0, 0);
  private static final DateTime dt2 = new DateTime(2012, 1, 1, 0, 0, 0, 0);
  private static final DateTime dt3 = new DateTime(2012, 1, 2, 0, 0, 0, 0);

  private static final Map<String, Number> data1;
  private static final Map<String, Number> data2;
  private static final Map<String, Number> data3;
  static {
    data1 = new HashMap<String, Number>();
    data1.put("max", 12.34);
    data1.put("min", 23.45);

    data2 = new HashMap<String, Number>();
    data2.put("max", 12.34);
    data2.put("min", 23.45);

    data3 = new HashMap<String, Number>();
    data3.put("max", 12.34);
    data3.put("min", 45.67);
  }

  @Test
  public void testEquals() {
    Summary s1 = new Summary(sensor1, new Interval(dt1, dt1), data1);
    Summary s2 = new Summary(sensor2, new Interval(dt2, dt2), data2);
    assertEquals(s1, s2);
  }

  @Test
  public void testNotEquals_Sensor() {
    Summary s1 = new Summary(sensor1, new Interval(dt1, dt1), data1);
    Summary s2 = new Summary(sensor3, new Interval(dt2, dt2), data2);
    assertFalse(s1.equals(s2));
  }

  @Test
  public void testNotEquals_Interval() {
    Summary s1 = new Summary(sensor1, new Interval(dt1, dt1), data1);
    Summary s2 = new Summary(sensor2, new Interval(dt2, dt3), data2);
    assertFalse(s1.equals(s2));
  }

  @Test
  public void testNotEquals_Data() {
    Summary s1 = new Summary(sensor1, new Interval(dt1, dt1), data1);
    Summary s2 = new Summary(sensor2, new Interval(dt2, dt2), data3);
    assertFalse(s1.equals(s2));
  }

  @Test
  public void testNotEquals_Null() {
    Summary s1 = new Summary(sensor1, new Interval(dt1, dt1), data1);
    assertFalse(s1.equals(null));
  }
}
