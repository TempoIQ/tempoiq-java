package com.tempoiq.json;

import java.io.IOException;

import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.AndSelector;
import com.tempoiq.AttributesSelector;
import com.tempoiq.AttributeKeySelector;
import com.tempoiq.OrSelector;
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

  @Test
  public void testEmptyOrSelector() throws IOException {
    Selector orSelector = new OrSelector();
    String expected = "{\"or\":[]}";
    assertEquals(expected, Json.dumps(orSelector));
  }

  @Test
  public void testOrSelector() throws IOException {
    Selector orSelector = new OrSelector(new KeySelector("some-key"));
    String expected = "{\"or\":[{\"key\":\"some-key\"}]}";
    assertEquals(expected, Json.dumps(orSelector));
  }

  @Test
  public void testEmptyAndSelector() throws IOException {
    Selector andSelector = new AndSelector();
    String expected = "{\"and\":[]}";
    assertEquals(expected, Json.dumps(andSelector));
  }

  @Test
  public void testAndSelector() throws IOException {
    Selector andSelector = new AndSelector(new KeySelector("some-key"));
    String expected = "{\"and\":[{\"key\":\"some-key\"}]}";
    assertEquals(expected, Json.dumps(andSelector));
  }

  @Test
  public void testNestedCompoundSelector() throws IOException {
    Selector compoundSelector = new OrSelector(
      new AndSelector(
	new AttributeKeySelector("building"),
	new AttributesSelector("region", "northwest")),
      new KeySelector("building-123"));
    String expected = "{\"or\":[{\"and\":[{\"attribute\":\"building\"},{\"attributes\":{\"region\":\"northwest\"}}]},{\"key\":\"building-123\"}]}";
    assertEquals(expected, Json.dumps(compoundSelector));
  }

  @Test
  public void testSelectorInterface() throws IOException {
    Selector compoundSelector = Selector.or(
      Selector.and(
	Selector.attributeKey("building"),
	Selector.attributes("region", "northwest")),
      Selector.key("building-123"));
    String expected = "{\"or\":[{\"and\":[{\"attribute\":\"building\"},{\"attributes\":{\"region\":\"northwest\"}}]},{\"key\":\"building-123\"}]}";
    assertEquals(expected, Json.dumps(compoundSelector));
  }
}
