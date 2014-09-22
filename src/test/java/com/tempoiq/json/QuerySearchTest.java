package com.tempoiq.json;

import java.io.IOException;

import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.QuerySearch;
import com.tempoiq.Selection;
import com.tempoiq.Selector;


public class QuerySearchTest {
  @Test
  public void testQuerySerialization() throws IOException {
    QuerySearch qs = new QuerySearch(Selector.Type.DEVICES, new Selection());
    String expected = "{\"select\":\"devices\",\"filters\":{}}";
    assertEquals(expected, Json.dumps(qs));
  }
}
