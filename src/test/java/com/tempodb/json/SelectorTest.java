package com.tempoiq.json;

import java.io.IOException;

import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.AttributesSelector;
import com.tempoiq.AttributeKeySelector;
import com.tempoiq.Selector;
import com.tempoiq.KeySelector;

public class SelectorTest {
  
  @Test
  public void testSerializeKeySelector() throws IOException {
    Selector keySelector = new KeySelector("key-1234");
    String expected = "{\"key\":\"key-1234\"}";
    assertEquals(expected, Json.dumps(keySelector));
  }

  @Test
  public void testAttributesSelector() throws IOException {
    Selector attributesSelector = new AttributesSelector("building", "445 W Erie");
    String expected = "{\"attributes\":{\"building\":\"445 W Erie\"}}";
    assertEquals(expected, Json.dumps(attributesSelector));
  }

  @Test
  public void testAttributeKeySelector() throws IOException {
    Selector attributeKeySelector = new AttributeKeySelector("building");
    String expected = "{\"attribute\":\"building\"}";
    assertEquals(expected, Json.dumps(attributeKeySelector));
  }
}
