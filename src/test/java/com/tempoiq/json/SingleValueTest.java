package com.tempoiq.json;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.DataPoint;
import com.tempoiq.Sensor;
import com.tempoiq.SingleValue;


public class SingleValueTest {

  private static final Sensor sensor = new Sensor("key1");

  @Test
  public void testDeserializeUTC() throws IOException {
    String json = "{" +
      "\"sensor\":{" +
        "\"id\":\"id1\"," +
        "\"key\":\"key1\"," +
        "\"name\":\"\"," +
        "\"tags\":[]," +
        "\"attributes\":{}" +
      "}," +
      "\"data\":{" +
        "\"t\":\"2012-01-01T00:00:01.000Z\"," +
        "\"v\":12.34" +
      "}," +
      "\"tz\":\"UTC\"" +
    "}";

    DateTimeZone zone = DateTimeZone.UTC;
    SingleValue value = Json.loads(json, SingleValue.class);

    DataPoint datapoint = new DataPoint(new DateTime(2012, 1, 1, 0, 0, 1, 0, zone), 12.34);
    SingleValue expected = new SingleValue(sensor, datapoint);
    assertEquals(expected, value);
  }

  @Test
  public void testDeserializeTZ() throws IOException {
    String json = "{" +
      "\"sensor\":{" +
        "\"id\":\"id1\"," +
        "\"key\":\"key1\"," +
        "\"name\":\"\"," +
        "\"tags\":[]," +
        "\"attributes\":{}" +
      "}," +
      "\"data\":{" +
        "\"t\":\"2012-01-01T00:00:01.000-06:00\"," +
        "\"v\":12.34" +
      "}," +
      "\"tz\":\"America/Chicago\"" +
    "}";

    DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    SingleValue value = Json.loads(json, SingleValue.class);

    DataPoint datapoint = new DataPoint(new DateTime(2012, 1, 1, 0, 0, 1, 0, zone), 12.34);
    SingleValue expected = new SingleValue(sensor, datapoint);
    assertEquals(expected, value);
  }
}
