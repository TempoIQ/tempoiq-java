package com.tempoiq;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.junit.*;
import static org.junit.Assert.*;
import com.tempoiq.json.Json;


public class ResultTest {

  @Test
  public void testConstructor() {
    Result<Long> result = new Result<Long>(1L, 200, "message");
    assertEquals(200, result.getCode());
    assertEquals("message", result.getMessage());
  }

  @Test
  public void testEquals() {
    Result<Long> result = new Result<Long>(1L, 200, "message");
    Result<Long> expected = new Result<Long>(1L, 200, "message");
    assertEquals(expected, result);
  }

  @Test
  public void testNotEquals() {
    Result<Long> result1 = new Result<Long>(1L, 200, "message");
    Result<Long> result2 = new Result<Long>(2L, 100, "message");
    Result<Long> result3 = new Result<Long>(2L, 200, "message1");
    Result<Long> expected = new Result<Long>(2L, 200, "message");
    assertFalse(expected.equals(result1));
    assertFalse(expected.equals(result2));
    assertFalse(expected.equals(result3));
  }

  @Test
  public void testSuccessfulRequest() throws IOException {
    HttpResponse response = Util.getResponse(200, "");
    Result<Void> result = new Result<Void>(response, Void.class);
    Result<Void> expected = new Result<Void>(null, 200, "OK");
    assertEquals(expected, result);
    assertTrue(result.getState() == State.SUCCESS);
  }

  @Test
  public void testFailedRequest_Body() throws IOException {
    HttpResponse response = Util.getResponse(403, "You are forbidden");
    Result<Void> result = new Result<Void>(response, Void.class);
    Result<Void> expected = new Result<Void>(null, 403, "You are forbidden");
    assertEquals(expected, result);
    assertTrue(result.getState() == State.FAILURE);
  }

  @Test
  public void testFailedRequest_NoBody() throws IOException {
    HttpResponse response = Util.getResponse(403, "");
    Result<Void> result = new Result<Void>(response, Void.class);
    Result<Void> expected = new Result<Void>(null, 403, "Forbidden");
    assertEquals(expected, result);
    assertTrue(result.getState() == State.FAILURE);
  }

  @Test
  public void testPartialFailure() throws IOException {
    String json = "{\"device-1\": {\"device_state\": \"modified\", \"message\": null, \"success\": true}}";
    WriteResponse resp = Json.loads(json, WriteResponse.class);
    HttpResponse response = Util.getResponse(207, json);
    Result<WriteResponse> result = new Result<WriteResponse>(response, WriteResponse.class);

    Result<WriteResponse> expected = new Result<WriteResponse>(resp, 207, "Multi-Status"); 
    assertEquals(expected, result);
    assertTrue(result.getState() == State.PARTIAL_SUCCESS);
  }

  @Test
  public void testUpsertNoBody() throws IOException {
    String json = "";
    HttpResponse response = Util.getResponse(200, json);
    WriteResponse resp = new WriteResponse(new HashMap<String, DeviceStatus>());
    Result<WriteResponse> result = new Result<WriteResponse>(response, WriteResponse.class);

    Result<WriteResponse> expected = new Result<WriteResponse>(resp, 200, "OK");
    assertEquals(expected, result);
    assertTrue(result.getState() == State.SUCCESS);
  }
}
