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

import com.tempoiq.json.Json;


public class WriteDataPointsTest {

  private static final String json = "{" +
    "\"key1\":{\"key1\":[{\"t\":\"2012-03-27T05:00:00.000Z\",\"v\":12.34}]}" +
    "}";
  private static final String multistatus_json = "{\"device-1\": {\"device_state\": \"modified\", \"message\": null, \"success\": true}}";

  private static final DateTimeZone timezone = DateTimeZone.UTC;
  private static final WriteRequest wr = new WriteRequest()
    .add(new Device("key1"), new Sensor("key1"), new DataPoint(new DateTime(2012, 3, 27, 5, 0, 0, 0, timezone), 12.34));

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  @Test
  public void smokeTest() throws IOException {
    UpsertResponse resp = Json.loads(multistatus_json, UpsertResponse.class);
    HttpResponse response = Util.getResponse(200, multistatus_json);
    Client client = Util.getClient(response);
    Result<UpsertResponse> result = client.writeDataPoints(wr);
    
    Result<UpsertResponse> expected = new Result<UpsertResponse>(resp, 200, "OK");
    assertEquals(expected, result);
    assertEquals("OK", result.getMessage());
  }

  @Test
  public void testMethod() throws IOException {
    HttpResponse response = Util.getResponse(200, multistatus_json);
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    Result<UpsertResponse> result = client.writeDataPoints(wr);

    HttpRequest request = Util.captureRequest(mockClient);
    assertEquals("POST", request.getRequestLine().getMethod());
  }

  @Test
  public void testUri() throws IOException, URISyntaxException {
    HttpResponse response = Util.getResponse(200, multistatus_json);
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    Result<UpsertResponse> result = client.writeDataPoints(wr);

    HttpRequest request = Util.captureRequest(mockClient);
    URI uri = new URI(request.getRequestLine().getUri());
    assertEquals("/v2/write/", uri.getPath());
  }

  @Test
  public void testBody() throws IOException {
    HttpResponse response = Util.getResponse(200, multistatus_json);
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    Result<UpsertResponse> result = client.writeDataPoints(wr);

    ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
    verify(mockClient).execute(any(HttpHost.class), argument.capture(), any(HttpContext.class));
    assertEquals(json, EntityUtils.toString(argument.getValue().getEntity(), DEFAULT_CHARSET));
  }

  @Test
  public void testMultiStatus() throws IOException {
    UpsertResponse resp = Json.loads(multistatus_json, UpsertResponse.class);
    HttpResponse response = Util.getResponse(207, multistatus_json);
    Client client = Util.getClient(response);

    Result<UpsertResponse> result = client.writeDataPoints(wr);

    Result<UpsertResponse> expected = new Result<UpsertResponse>(null, 207, "Multi-Status", resp);
    assertEquals(expected, result);
  }
}
