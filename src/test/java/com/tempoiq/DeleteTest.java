package com.tempoiq;

import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;


public class DeleteTest {

  @Test
  public void testEquals() {
    Delete d1 = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0), new DateTime(2015, 1, 1, 0, 0, 0, 0));
    Delete d2 = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0), new DateTime(2015, 1, 1, 0, 0, 0, 0));
    assertEquals(d1, d2);
  }

  @Test
  public void testNotEquals_Start() {
    Delete d1 = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0), new DateTime(2015, 1, 1, 0, 0, 0, 0));
    Delete d2 = new Delete(new DateTime(2014, 1, 1, 0, 0, 0, 0), new DateTime(2015, 1, 1, 0, 0, 0, 0));
    assertFalse(d1.equals(d2));
  }

  @Test
  public void testNotEquals_Stop() {
    Delete d1 = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0), new DateTime(2015, 1, 1, 0, 0, 0, 0));
    Delete d2 = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0), new DateTime(2019, 1, 1, 0, 0, 0, 0));
    assertFalse(d1.equals(d2));
  }

  @Test
  public void testNotEquals_Null() {
    Delete d1 = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0), new DateTime(2015, 1, 1, 0, 0, 0, 0));
    assertFalse(d1.equals(null));
  }
}

