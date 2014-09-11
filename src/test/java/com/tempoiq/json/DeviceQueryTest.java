package com.tempoiq.json;

import java.io.IOException;

import org.junit.*;

import static org.junit.Assert.*;

import com.tempoiq.FindAction;
import com.tempoiq.Query;
import com.tempoiq.QuerySearch;
import com.tempoiq.Selection;
import com.tempoiq.Selector;

public class DeviceQueryTest {
  
  @Test
  public void testDeviceSearch() throws IOException {
    Selection sel = new Selection();
    sel.addSelector(Selector.Type.DEVICES, Selector.all());

    Query query = new Query(
        new QuerySearch(Selector.Type.DEVICES, sel),
        null,
        new FindAction());
    
    String expected = "{\"search\":{\"select\":\"devices\",\"filters\":{\"devices\":\"all\"}},"
        + "\"find\":{\"quantifier\":\"all\"}}";
   
    assertEquals(expected, Json.dumps(query));
  }
}
