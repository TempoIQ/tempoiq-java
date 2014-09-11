package com.tempoiq;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.junit.*;
import static org.junit.Assert.*;


public class RollupTest {
  private static final DateTimeZone timezone = DateTimeZone.UTC;
  private static final DateTime start = new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone);
  private static final DateTime stop = new DateTime(2012, 1, 2, 0, 0, 0, 0, timezone);

  @Test
  public void testEquals() {
    Rollup r1 = new Rollup(Period.minutes(1), Fold.SUM, start, stop);
    Rollup r2 = new Rollup(Period.minutes(1), Fold.SUM, start, stop);
    assertEquals(r1, r2);
  }

  @Test
  public void testNotEquals_Period() {
    Rollup r1 = new Rollup(Period.minutes(1), Fold.SUM, start, stop);
    Rollup r2 = new Rollup(Period.minutes(2), Fold.SUM, start, stop);
    assertFalse(r1.equals(r2));
  }

  @Test
  public void testNotEquals_Fold() {
    Rollup r1 = new Rollup(Period.minutes(1), Fold.SUM, start, stop);
    Rollup r2 = new Rollup(Period.minutes(1), Fold.MEAN, start, stop);
    assertFalse(r1.equals(r2));
  }

  @Test
  public void testNotEquals_Null() {
    Rollup r1 = new Rollup(Period.minutes(1), Fold.SUM, start, stop);
    assertFalse(r1.equals(null));
  }
}
