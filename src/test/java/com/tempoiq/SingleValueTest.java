package com.tempoiq;

import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;


public class SingleValueTest {

  private static final Sensor sensor1 = new Sensor("key1");
  private static final Sensor sensor2 = new Sensor("key1");
  private static final Sensor sensor3 = new Sensor("key2");

  private static final DataPoint dp1 = new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0), 12.34);
  private static final DataPoint dp2 = new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0), 12.34);
  private static final DataPoint dp3 = new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0), 10.34);

  @Test
  public void testEquals() {
    SingleValue sv1 = new SingleValue(sensor1, dp1);
    SingleValue sv2 = new SingleValue(sensor2, dp2);
    assertEquals(sv1, sv2);
  }

  @Test
  public void testNotEquals_Sensor() {
    SingleValue sv1 = new SingleValue(sensor1, dp1);
    SingleValue sv2 = new SingleValue(sensor3, dp2);
    assertFalse(sv1.equals(sv2));
  }

  @Test
  public void testNotEquals_DataPoint() {
    SingleValue sv1 = new SingleValue(sensor1, dp1);
    SingleValue sv2 = new SingleValue(sensor2, dp3);
    assertFalse(sv1.equals(sv2));
  }

  @Test
  public void testNotEquals_Null() {
    SingleValue sv1 = new SingleValue(sensor1, dp1);
    assertFalse(sv1.equals(null));
  }
}

