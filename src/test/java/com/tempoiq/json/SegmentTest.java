package com.tempoiq.json;

import java.io.IOException;
import org.apache.http.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.*;
import com.tempoiq.Segment;
import com.tempoiq.RowSegment;
import static org.junit.Assert.*;

public class SegmentTest {
  private static final String json1 = "{" +
    "\"data\":[" +
       "{\"t\":\"2012-01-01T01:00:00.000Z\",\"data\":{" +
         "\"device1\":{" +
           "\"sensor1\":1.23," +
           "\"sensor2\":1.677}}}]}";

  private static final String json2 = "{" +
    "\"data\":[" +
      "{\"t\":\"2012-01-01T01:00:00.000Z\",\"data\":{" +
        "\"device1\":{" +
          "\"sensor1\":1.23," +
          "\"sensor2\":1.677}}}],"+
    "\"next_page\":{\"next_query\": {" +
        "\"search\":{" +
        "\"select\":\"sensors\"," +
        "\"filters\":{" +
          "\"devices\":{" +
            "\"and\":[" +
              "{\"key\":\"key1\"}" +
            "]}}}," +
          "\"read\":{\"start\":\"2014-01-01T01:00:00.001Z\"," +
          "\"stop\":\"2014-03-01T00:00:00.000Z\"," +
          "\"limit\":1," +
          "\"include_selection\":false}" +
        "}}}";

  @Test
  public void testDeserialize() throws IOException {
    final String expected = "{\"search\":{" +
        "\"select\":\"sensors\"," +
        "\"filters\":{" +
          "\"devices\":{" +
            "\"and\":[" +
              "{\"key\":\"key1\"}" +
            "]}}}," +
          "\"read\":{\"start\":\"2014-01-01T01:00:00.001Z\"," +
          "\"stop\":\"2014-03-01T00:00:00.000Z\"," +
          "\"limit\":1," +
          "\"include_selection\":false}" +
        "}";
    RowSegment deserialized = Json.loads(json2, RowSegment.class);
    assertEquals(expected, deserialized.getNext());
  }
}