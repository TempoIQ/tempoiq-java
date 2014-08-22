package com.tempoiq.json;

import java.io.IOException;

import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.Selection;
import com.tempoiq.Selector;

public class SelectionTest {
  
  @Test
  public void testDeviceAndSensorSelection() throws IOException {
    Selection sel = new Selection().
      addSelector(Selector.Type.DEVICES, Selector.or(
		    Selector.key("building-1234"), Selector.key("building-4321"))).
      addSelector(Selector.Type.SENSORS, Selector.key("temp-1"));
    String expected = "{\"filters\":{\"devices\":{\"or\":[{\"key\":\"building-1234\"},{\"key\":\"building-4321\"}]},\"sensors\":{\"key\":\"temp-1\"}}}";
    assertEquals(expected, Json.dumps(sel));
  }
}
