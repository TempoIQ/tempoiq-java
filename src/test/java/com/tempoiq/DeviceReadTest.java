package com.tempoiq;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.junit.Test;

public class DeviceReadTest {
  private static final Device device = new Device("device1");
  private static final String json1 = "{" +
    "\"items\":[" +
       "{\"key\":\"device1\","
       + "\"name\":\"\"," +
         "\"attributes\":{}," +
         "\"sensors\":[]}]}";

  @Test
  public void testListDevices() throws IOException {
    HttpResponse response = Util.getResponse(200, json1);
    Client client = Util.getClient(response);

    Selection sel = new Selection().
      addSelector(Selector.Type.DEVICES, Selector.key(device.getKey()));
    Cursor<Device> cursor = client.listDevices(sel);
    assert(cursor.iterator().hasNext());
    
    Device returnedDev = cursor.iterator().next();
    
    assertEquals(device.getKey(), returnedDev.getKey());
    
  }

  /*
  @Test
  public void testSensorStreamReads() throws IOException {
    HttpResponse response = Util.getResponse(200, json1);
    Client client = Util.getClient(response);

    Selection sel = new Selection().
      addSelector(Selector.Type.DEVICES, Selector.key(device.getKey()));
    DataPointRowCursor cursor = client.read(sel, start, stop);
    assert(cursor.iterator().hasNext());
    DataPointCursor sensor1 = cursor.getSensorCursor(device.getKey(), "sensor1");
    assert(sensor1.iterator().hasNext());
    for (DataPoint dp : sensor1) {
      assertEquals(1.23, dp.getValue());
    }
  }
  */
}
