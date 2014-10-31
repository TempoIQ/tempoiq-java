package com.tempoiq.json;

import com.tempoiq.PageLink;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class PageLinkTest {

  @Test
  public void TestDeserializesPageLink() throws IOException {
    String json = "{\"next_query\":{\"sensors\":\"all\",\"devices\":\"all\"}}";
    PageLink fromJson = Json.loads(json, PageLink.class);
    PageLink expected = new PageLink("{\"sensors\":\"all\",\"devices\":\"all\"}");
    assertEquals(fromJson, expected);
  }
}
