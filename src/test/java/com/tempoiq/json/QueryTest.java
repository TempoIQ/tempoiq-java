package com.tempoiq.json;

import java.io.IOException;

import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.Query;
import com.tempoiq.QuerySearch;
import com.tempoiq.FindAction;
import com.tempoiq.Selection;
import com.tempoiq.Selector;


public class QueryTest {

  @Test
  public void testFullQuerySerializion() throws IOException {
    Selection selection = new Selection().
      addSelector(Selector.Type.DEVICES, Selector.or(
		    Selector.key("building-1234"), Selector.key("building-4321"))).
      addSelector(Selector.Type.SENSORS, Selector.key("temp-1"));
    Query query = new Query(
      new QuerySearch(Selector.Type.DEVICES, selection),
      null,
      new FindAction());

    String expected = "{\"search\":{\"select\":\"devices\",\"filters\":{\"devices\":{\"or\":[{\"key\":\"building-1234\"},{\"key\":\"building-4321\"}]},\"sensors\":{\"key\":\"temp-1\"}}},\"find\":{\"quantifier\":\"all\"}}";
    assertEquals(expected, Json.dumps(query));
  }
}
