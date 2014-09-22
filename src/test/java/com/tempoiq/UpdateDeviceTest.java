package com.tempoiq;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;


public class UpdateDeviceTest {

  private static final String json = "{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{},\"sensors\":[]}";
  private static final String body = "{\"key\":\"key1\",\"name\":\"name1\",\"attributes\":{},\"sensors\":[]}";
  private static final Device device = new Device("key1", "name1", new HashMap<String, String>(), new ArrayList<Sensor>());
  private static final Device device1 = new Device("key1", "name1", new HashMap<String, String>(), new ArrayList<Sensor>());

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  @Test
  public void smokeTest() throws IOException {
    HttpResponse response = Util.getResponse(200, json);
    Client client = Util.getClient(response);

    Result<Device> result = client.updateDevice(device);

    assertEquals(device1, result.getValue());
  }

  @Test
  public void testMethod() throws IOException {
    HttpResponse response = Util.getResponse(200, json);
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    Result<Device> result = client.updateDevice(device);

    HttpRequest request = Util.captureRequest(mockClient);
    assertEquals("PUT", request.getRequestLine().getMethod());
  }

  @Test
  public void testUri() throws IOException, URISyntaxException {
    HttpResponse response = Util.getResponse(200, json);
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    Result<Device> result = client.updateDevice(device);

    HttpRequest request = Util.captureRequest(mockClient);
    URI uri = new URI(request.getRequestLine().getUri());
    assertEquals("/v2/devices/key1/", uri.getPath());
  }

  @Test
  public void testBody() throws IOException {
    HttpResponse response = Util.getResponse(200, json);
    HttpClient mockClient = Util.getMockHttpClient(response);
    Client client = Util.getClient(mockClient);

    Result<Device> result = client.updateDevice(device);

    ArgumentCaptor<HttpPut> argument = ArgumentCaptor.forClass(HttpPut.class);
    verify(mockClient).execute(any(HttpHost.class), argument.capture(), any(HttpContext.class));
    assertEquals(body, EntityUtils.toString(argument.getValue().getEntity(), DEFAULT_CHARSET));
  }
}
