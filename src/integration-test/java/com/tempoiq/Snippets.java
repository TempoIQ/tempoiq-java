package com.tempoiq;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;

import static com.tempoiq.util.Preconditions.*;


public class Snippets {

  private static final Client client;
  private static final Client invalidClient;
  private static final DateTimeZone timezone = DateTimeZone.UTC;
  private static final DateTime start = new DateTime(1500, 1, 1, 0, 0, 0, 0, timezone);
  private static final DateTime end = new DateTime(3000, 1, 1, 0, 0, 0, 0, timezone);
  private static final Interval interval = new Interval(start, end);

  static {
    File credentials = new File("integration-credentials.properties");
    if(!credentials.exists()) {
      String message = "Missing credentials file for integration test.\n" +
        "Please supply a file 'integration-credentials.properties' with the following format:\n" +
        "  credentials.key=<key>\n" +
        "  credentials.secret=<secret>\n" +
        "  hostname=<hostname>\n" +
        "  port=<port>\n" +
        "  scheme=<scheme>\n";

      System.err.print(message);
    }
    client = getClient(credentials);
    invalidClient = new Client(new Credentials("key", "secret"),
                               client.getHost(), client.getScheme());
  }

  static Client getcClient() {

    // snippet-begin create-client
    // import com.tempoiq.*;

    InetSocketAddress host = new InetSocketAddress("my-company.backend.tempoiq.com", 443);
    Credentials credentials = new Credentials("my-key", "my-secret");
    Client client = new Client(credentials, host, "https");
    // snippet-end
    return client;
  }

  static Client getClient(File propertiesFile) {
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(propertiesFile));
    } catch (Exception e) {
      throw new IllegalArgumentException("No credentials file", e);
    }

    String key = checkNotNull(properties.getProperty("credentials.key"));
    String secret = checkNotNull(properties.getProperty("credentials.secret"));
    String hostname = checkNotNull(properties.getProperty("hostname"));
    int port = Integer.parseInt(checkNotNull(properties.getProperty("port")));
    String scheme = checkNotNull(properties.getProperty("scheme"));
    Credentials credentials = new Credentials(key, secret);
    InetSocketAddress host = new InetSocketAddress(hostname, port);
    return new Client(credentials, host, scheme);
  }

  public void testCreateDevice() {
    // snippet-begin create-device
    // import java.util.*;
    // import com.tempoiq.*;
    // import org.joda.time.*;

    // create device attributes
    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put("model", "v1");

    // create sensors
    Sensor sensor1 = new Sensor("temperature");
    Sensor sensor2 = new Sensor("humidity");
    List<Sensor> sensors = new ArrayList<Sensor>();
    sensors.addAll(Arrays.asList(sensor1, sensor2));

    // create device with key "thermostat.0" with attributes and sensors
    Device device = new Device("thermostat.0", "", attributes, sensors);

    // store in TempoIQ
    Result<Device> result = client.createDevice(device);

    // Check that the request was successful
    if(result.getState() != State.SUCCESS) {
      System.out.format("Error creating device! %s", result.getMessage()).println();
    }
    // snippet-end
  }

  public void testWriteDataPoints() {
    // snippet-begin write-data
    // import java.util.*;
    // import com.tempoiq.*;
    // import org.joda.time.*;

    // create datapoint at 2015-01-01T00:00:00.000Z for sensors temperature and humidity
    DateTime dt1 = new DateTime(2015, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
    Map<String, Number> points1 = new HashMap<String, Number>();
    points1.put("temperature", 68);
    points1.put("humidity", 71.5);
    MultiDataPoint mp1 = new MultiDataPoint(dt1, points1);

    // create another datapoint, five minutes later, at 2015-01-01T00:05:00.000Z for sensors temperature and humidity
    DateTime dt2 = dt1.plus(Period.minutes(5));
    Map<String, Number> points2 = new HashMap<String, Number>();
    points2.put("temperature", 67.5);
    points2.put("humidity", 70.0);
    MultiDataPoint mp2 = new MultiDataPoint(dt2, points2);

    // Store datapoints in TempoIQ
    Device device = new Device("thermostat.0");
    Result<Void> result = client.writeDataPoints(device, Arrays.asList(mp1, mp2));

    // Check that the request was successful
    if(result.getState() != State.SUCCESS) {
      System.out.format("Error writing data! %s", result.getMessage()).println();
    }
    // snippet-end
  }

  public void testReadRawDataPoints() {
    // snippet-begin read-data-one-device
    // import java.util.*;
    // import com.tempoiq.*;
    // import org.joda.time.*;

    // Set up the time range to read [2015-01-01, 2015-01-02)
    DateTime start = new DateTime(2015, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
    DateTime end = new DateTime(2015, 1, 2, 0, 0, 0, 0, DateTimeZone.UTC);

    Device device = new Device("thermostat.0");
    Selection selection = new Selection()
      .addSelector(Selector.Type.DEVICES, Selector.key(device.getKey()));

    Cursor<Row> cursor = client.read(selection, start, end);
    for(Row row : cursor) {
      System.out.format("timestamp %s, temperature: %f, humidity: %f",
            row.getTimestamp(),
            row.getValue("thermostat.0", "temperature"),
            row.getValue("thermostat.0", "humidity")).println();
    }
    // snippet-end
  }

  public void testGetDevice() {
    // snippet-begin get-device
    // import com.tempoiq.*;

    // get the device with key "thermostat.1"
    Result<Device> result = client.getDevice("thermostat.1");

    // Check that the request was successful
    if(result.getState() != State.SUCCESS) {
      System.out.format("Error getting device! %s", result.getMessage()).println();
    }
    // snippet-end
  }

  public void testGetDevices() {
    // snippet-begin get-devices
    // imoprt com.tempoiq.*;

    // create selection for all devices with attribute region in "south" or "east"
    Selection selection = new Selection()
      .addSelector(Selector.Type.DEVICES,
          Selector.or(
            Selector.attributes("region", "south"),
            Selector.attributes("region", "east")
          )
      );

    Cursor<Device> cursor = client.listDevices(selection);

    for(Device device : cursor) {
      System.out.format("device: %s", device.getKey()).println();
      for(Sensor sensor : device.getSensors()) {
        System.out.format("\tsensor: %s", sensor.getKey()).println();
      }
    }
    // snippet-end
  }

  public void testUpdateDevice() {
    // snippet-begin update-device
    // import com.tempoiq.*;

    Result<Device> result = client.getDevice("thermostat.4");

    // Check that the request was successful
    if(result.getState() != State.SUCCESS) {
      System.out.format("Error getting device! %s", result.getMessage()).println();
    }

    // mutate the device
    Device device = result.getValue();
    device.getAttributes().put("customer", "internal-test");
    device.getAttributes().put("region", "east");

    // update in TempoIQ
    result = client.updateDevice(device);

    if(result.getState() != State.SUCCESS) {
      System.out.format("Error updating device! %s", result.getMessage()).println();
    }
    // snippet-end
  }

  public void testDeleteDevices() {
    Device create = new Device("thermostat.5");
    Result<Device> create_result = client.createDevice(create);

    // snippet-begin delete-devices
    Selection selection = new Selection()
      .addSelector(Selector.Type.DEVICES, Selector.key("thermostat.5"));

    Result<DeleteSummary> result = client.deleteDevices(selection);

    if(result.getState() == State.SUCCESS) {
      System.out.format("Deleted %d devices.", result.getValue().getDeleted()).println();
    } else {
      System.out.format("Error deleting devices! %s", result.getMessage()).println();
    }
    // snippet-end
  }
}
