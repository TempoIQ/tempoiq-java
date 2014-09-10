package com.tempoiq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.*;
import static org.junit.Assert.*;

public class DataPointReadTest {
  private static final DateTimeZone timezone = DateTimeZone.UTC;
  private static final Device device = new Device("device1");
  private static final String json1 = "{" +
    "\"data\":[" +
       "{\"t\":\"2012-01-01T01:00:00.000Z\",\"data\":{" +
         "\"device1\":{" +
           "\"sensor1\":1.23," +
           "\"sensor2\":1.677}}}]}";

  @Test
  public void smokeTest() throws IOException {
    HttpResponse response = Util.getResponse(200, json1);
    Client client = Util.getClient(response);
    DateTime start = new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone);
    DateTime stop = new DateTime(2012, 1, 2, 0, 0, 0, 0, timezone);

    Map<String, Number> points = new HashMap<String, Number>();
    points.put("sensor1", 1.23);
    points.put("sensor2", 1.677);
    MultiDataPoint mp = new MultiDataPoint(new DateTime(2012, 1, 1, 1, 0, 0, 0, timezone), points);
    Result<Void> result = client.writeDataPoints(device, mp);
    assertEquals(State.SUCCESS, result.getState());

    Selection sel = new Selection().
      addSelector(Selector.Type.DEVICES, Selector.key(device.getKey()));
    Cursor<Row> cursor = client.read(sel, start, stop);
    assert(cursor.iterator().hasNext());
    for (Row row : cursor) {
      assertEquals(1.23, row.getValue(device.getKey(), "sensor1"));
      assertEquals(1.677, row.getValue(device.getKey(), "sensor2"));
    }
  }
}
