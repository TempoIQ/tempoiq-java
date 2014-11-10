package com.tempoiq;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.junit.*;
import static org.junit.Assert.*;


public class DeleteDeviceTest {
  private static final Device device = new Device("key1");
  private static final String json1 = "{\"deleted\":1}";

  @Test
  public void smokeTest() throws IOException {
    HttpResponse response = Util.getResponse(200, json1);
    Client client = Util.getClient(response);

    Result<DeleteSummary> expected = new Result<DeleteSummary>(new DeleteSummary(1), 200, "OK");
    Result<DeleteSummary> result = client.deleteDevice(device);
    assertEquals(expected, result);
  }

  @Test
  public void testMethod() throws IOException {
    HttpResponse response = Util.getResponse(200, json1);
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    client.deleteDevice(device);

    HttpRequest request = Util.captureRequest(mockClient);
    assertEquals("DELETE", request.getRequestLine().getMethod());
  }

  @Test
  public void testUri() throws IOException, URISyntaxException {
    HttpResponse response = Util.getResponse(200, json1);
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    client.deleteDevice(device);

    HttpRequest request = Util.captureRequest(mockClient);
    URI uri = new URI(request.getRequestLine().getUri());
    assertEquals("/v2/devices/key1/", uri.getPath());
  }
}
