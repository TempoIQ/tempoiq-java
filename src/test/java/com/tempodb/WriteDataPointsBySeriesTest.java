package com.tempoiq;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;


public class WriteDataPointsBySeriesTest {

  private static final String json = "[" +
    "{\"t\":\"2012-03-27T05:00:00.000Z\",\"v\":12.34}," +
    "{\"t\":\"2012-03-27T05:01:00.000Z\",\"v\":23.45}" +
  "]";

  private static final DateTimeZone timezone = DateTimeZone.UTC;
  private static final List<DataPoint> data = Arrays.asList(new DataPoint(new DateTime(2012, 3, 27, 5, 0, 0, 0, timezone), 12.34),
                                                            new DataPoint(new DateTime(2012, 3, 27, 5, 1, 0, 0, timezone), 23.45));
  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
  private static final Series series = new Series("key1");

  @Test
  public void smokeTest() throws IOException {
    HttpResponse response = Util.getResponse(200, "");
    Client client = Util.getClient(response);
    Result<Void> result = client.writeDataPoints(series, data);

    Result<Void> expected = new Result<Void>(null, 200, "OK");
    assertEquals(expected, result);
  }

  @Test
  public void testMethod() throws IOException {
    HttpResponse response = Util.getResponse(200, "");
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    Result<Void> result = client.writeDataPoints(series, data);

    HttpRequest request = Util.captureRequest(mockClient);
    assertEquals("POST", request.getRequestLine().getMethod());
  }

  @Test
  public void testUri() throws IOException, URISyntaxException {
    HttpResponse response = Util.getResponse(200, "");
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    Result<Void> result = client.writeDataPoints(series, data);

    HttpRequest request = Util.captureRequest(mockClient);
    URI uri = new URI(request.getRequestLine().getUri());
    assertEquals("/v1/series/key/key1/data/", uri.getPath());
  }

  @Test
  public void testBody() throws IOException {
    HttpResponse response = Util.getResponse(200, "");
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    Result<Void> result = client.writeDataPoints(series, data);

    ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
    verify(mockClient).execute(any(HttpHost.class), argument.capture(), any(HttpContext.class));
    assertEquals(json, EntityUtils.toString(argument.getValue().getEntity(), DEFAULT_CHARSET));
  }
}
