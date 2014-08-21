package com.tempoiq.http;

import org.apache.http.HttpResponse;
import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.Util;


public class PageLinksTest {

  @Test
  public void testLinkHeader() {
    String header = "</v1/sensor/id/id1/data/segment/?start=2012-03-27T00:02:00.000-05:00&end=2012-03-28>; rel=\"next\"";
    HttpResponse response = Util.getResponse(200, "");
    response.setHeader("Link", header);

    PageLinks pageLinks = new PageLinks(response);
    String expected = "/v1/sensor/id/id1/data/segment/?start=2012-03-27T00:02:00.000-05:00&end=2012-03-28";
    assertEquals(expected, pageLinks.getNext());
  }

  @Test
  public void testMissingLinkHeader() {
    HttpResponse response = Util.getResponse(200, "");

    PageLinks pageLinks = new PageLinks(response);
    String expected = "";
    assertEquals(expected, pageLinks.getNext());
  }

  @Test
  public void testLinkNext() {
    String header = "</v1/sensor/id/id1/data/segment/?start=2012-03-27T00:02:00.000-05:00&end=2012-03-28>; rel=\"last\"";
    HttpResponse response = Util.getResponse(200, "");
    response.setHeader("Link", header);

    PageLinks pageLinks = new PageLinks(response);
    String expected = "";
    assertEquals(expected, pageLinks.getNext());
  }
}
