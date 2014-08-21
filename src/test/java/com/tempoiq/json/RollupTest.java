package com.tempoiq.json;

import java.io.IOException;

import org.joda.time.Period;
import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.Fold;
import com.tempoiq.Rollup;


public class RollupTest {

  @Test
  public void testDeserialize() throws IOException {
    Rollup rollup = Json.loads("{\"period\":\"PT1M\",\"fold\":\"sum\"}", Rollup.class);
    Rollup expected = new Rollup(Period.minutes(1), Fold.SUM);
    assertEquals(expected, rollup);
  }

  @Test
  public void testSerialize() throws IOException {
    Rollup rollup = new Rollup(Period.minutes(1), Fold.SUM);
    String expected = "{\"period\":\"PT1M\",\"fold\":\"sum\"}";
    assertEquals(expected, Json.dumps(rollup));
  }
}
