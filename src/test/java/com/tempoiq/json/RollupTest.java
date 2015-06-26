package com.tempoiq.json;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.Fold;
import com.tempoiq.Rollup;


public class RollupTest {
  private static final DateTimeZone timezone = DateTimeZone.UTC;
  private static final DateTime start = new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone);

  @Test
  public void testSerialize() throws IOException {
    Rollup rollup = new Rollup(Period.minutes(1), Fold.SUM, start);
    String expected1 = "{\"name\":\"rollup\",\"arguments\":[\"sum\",\"PT1M\",\"2012-01-01T00:00:00.000Z\"]}";
    String expected2 = "{\"arguments\":[\"sum\",\"PT1M\",\"2012-01-01T00:00:00.000Z\",\"name\":\"rollup\",}";
    try {
      assertEquals(expected1, Json.dumps(rollup));
    } catch (Throwable e) {
      assertEquals(expected2, Json.dumps(rollup));
    }
  }
}
