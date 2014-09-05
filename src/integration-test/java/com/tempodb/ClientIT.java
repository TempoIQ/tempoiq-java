package com.tempoiq;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.junit.*;
import static org.junit.Assert.*;

import static com.tempoiq.util.Preconditions.*;


public class ClientIT {
  private static final Client client;
  private static final Client invalidClient;
  private static final DateTimeZone timezone = DateTimeZone.UTC;
  private static final DateTime start = new DateTime(1500, 1, 1, 0, 0, 0, 0, timezone);
  private static final DateTime end = new DateTime(3000, 1, 1, 0, 0, 0, 0, timezone);
  private static final Interval interval = new Interval(start, end);

  private static final int SLEEP = 5000;

  static {
    File credentials = new File("integration-credentials.properties");
    if(!credentials.exists()) {
      String message = "Missing credentials file for integration test.\n" +
        "Please supply a file 'integration-credentials.properties' with the following format:\n" +
        "  database.id=<id>\n" +
        "  credentials.key=<key>\n" +
        "  credentials.secret=<secret>\n" +
        "  hostname=<hostname>\n" +
        "  port=<port>\n" +
        "  scheme=<scheme>\n";

      System.exit(1);
    }

    client = getClient(credentials);
    invalidClient = new Client(new Credentials("key", "secret"),
                               client.getHost(), client.getScheme());
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

  @BeforeClass
  static public void onetimeSetup() {
    cleanup();
  }

  static public void cleanup() {
    // /* Delete all devices */
    Result<DeleteSummary> result = client.deleteAllDevices();
    assertEquals(State.SUCCESS, result.getState());
  }

  static public Device createDevice() {
    List<Sensor> sensors = new ArrayList<Sensor>();
    sensors.add(new Sensor("key1"));
    sensors.add(new Sensor("key2"));
    Device device = new Device("create-device", "name", new HashMap<String, String>(), sensors);
    Result<Device> result = client.createDevice(device);
    return result.getValue();
  }

  @After
  public void tearDown() { cleanup(); }

  @Test
  public void testInvalidCredentials() {
    Device device = new Device();
    Result<Device> result = invalidClient.createDevice(device);
    Result<Device> expected = new Result<Device>(null, 403, "Forbidden");
    assertEquals(expected, result);
  }

  @Test
  public void testCreateDevices() {
    Device device = new Device("create-device", "name", new HashMap<String, String>(), new ArrayList<Sensor>());

    Result<Device> result = client.createDevice(device);
    Result<Device> expected = new Result<Device>(device, 200, "OK");

    assertEquals(expected, result);
  }

  @Test
  public void testWriteDataPointBySensor() {
    Device device = createDevice();

    Map<String, Number> points = new HashMap<String, Number>();
    points.put("key1", 1.23);
    points.put("key2", 1.67);
    MultiDataPoint mp = new MultiDataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), points);
    Result<Void> result2 = client.writeDataPoints(device, mp);
    assertEquals(State.SUCCESS, result2.getState());
  }


  // @Test
  // public void testDeleteDataPointsBySensor() throws InterruptedException {
  //   // Write datapoints
  //   DataPoint dp = new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), 12.34);
  //   Result<Void> result1 = client.writeDataPoints(new Sensor("key1"), Arrays.asList(dp));
  //   assertEquals(State.SUCCESS, result1.getState());
  //   Thread.sleep(SLEEP);

  //   // Read datapoints
  //   List<DataPoint> expected1 = Arrays.asList(dp);
  //   Cursor<DataPoint> cursor1 = client.readDataPoints(new Sensor("key1"), interval, timezone);
  //   assertEquals(expected1, toList(cursor1));

  //   // Delete datapoints
  //   Result<Void> result2 = client.deleteDataPoints(new Sensor("key1"), interval);
  //   assertEquals(new Result<Void>(null, 200, "OK"), result2);

  //   // Read datapoints again
  //   List<DataPoint> expected2 = new ArrayList<DataPoint>();
  //   Cursor<DataPoint> cursor2 = client.readDataPoints(new Sensor("key1"), interval, timezone);
  //   assertEquals(expected2, toList(cursor2));
  // }

  // @Test
  // public void testFindDataPointBySensor() throws InterruptedException {
  //   DataPoint dp1 = new DataPoint(new DateTime(2012, 1, 2, 0, 0 ,0, 0, timezone), 23.45);
  //   DataPoint dp2 = new DataPoint(new DateTime(2012, 1, 2, 1, 0 ,0, 0, timezone), 34.56);

  //   Result<Void> result = client.writeDataPoints(new Sensor("key-find"), Arrays.asList(dp1, dp2));
  //   assertEquals(State.SUCCESS, result.getState());
  //   Thread.sleep(SLEEP);

  //   DateTime start = new DateTime(2012, 1, 2, 0, 0, 0, 0, timezone);
  //   DateTime end = new DateTime(2012, 1, 3, 0, 0, 0, 0, timezone);
  //   Interval interval = new Interval(start, end);
  //   Predicate predicate = new Predicate(Period.days(1), "max");
  //   DataPointFound dpf1 = new DataPointFound(interval, dp2);

  //   List<DataPointFound> expected = Arrays.asList(dpf1);
  //   Cursor<DataPointFound> cursor = client.findDataPoints(new Sensor("key-find"), new Interval(start, end), predicate, timezone);
  //   assertEquals(expected, toList(cursor));
  // }

  // @Test
  // public void testReadDataPointByKey() throws InterruptedException {
  //   DataPoint dp1 = new DataPoint(new DateTime(2012, 1, 2, 0, 0 ,0, 0, timezone), 23.45);
  //   DataPoint dp2 = new DataPoint(new DateTime(2012, 1, 2, 1, 0 ,0, 0, timezone), 34.56);

  //   Result<Void> result = client.writeDataPoints(new Sensor("key1"), Arrays.asList(dp1, dp2));
  //   assertEquals(State.SUCCESS, result.getState());
  //   Thread.sleep(SLEEP);

  //   DateTime start = new DateTime(2012, 1, 2, 0, 0, 0, 0, timezone);
  //   DateTime end = new DateTime(2012, 1, 3, 0, 0, 0, 0, timezone);

  //   List<DataPoint> expected = Arrays.asList(dp1, dp2);
  //   Cursor<DataPoint> cursor = client.readDataPoints(new Sensor("key1"), new Interval(start, end), timezone);
  //   assertEquals(expected, toList(cursor));
  // }

  // @Test
  // public void testReadSingleValue() throws InterruptedException {
  //   DataPoint dp1 = new DataPoint(new DateTime(2012, 1, 2, 0, 0 ,0, 0, timezone), 23.45);
  //   DataPoint dp2 = new DataPoint(new DateTime(2012, 1, 2, 1, 0 ,0, 0, timezone), 34.56);

  //   Result<Void> result = client.writeDataPoints(new Sensor("key1"), Arrays.asList(dp1, dp2));
  //   assertEquals(State.SUCCESS, result.getState());
  //   Thread.sleep(SLEEP);

  //   DateTime ts = new DateTime(2012, 1, 2, 0, 0, 0, 0, timezone);

  //   SingleValue expected = new SingleValue(new Sensor("key1"), new DataPoint(ts, 23.45));
  //   Result<SingleValue> value = client.readSingleValue(new Sensor("key1"), ts, timezone, Direction.EXACT);
  //   assertEquals(expected, value.getValue());
  // }

  // @Test
  // public void testReadMultiDataPoints() throws InterruptedException {
  //   WriteRequest wr = new WriteRequest()
  //     .add(new Sensor("key1"), new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), 5.0))
  //     .add(new Sensor("key2"), new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), 6.0))
  //     .add(new Sensor("key1"), new DataPoint(new DateTime(2012, 1, 1, 0, 1, 0, 0, timezone), 7.0))
  //     .add(new Sensor("key2"), new DataPoint(new DateTime(2012, 1, 1, 0, 1, 0, 0, timezone), 8.0));

  //   Result<Void> result1 = client.writeDataPoints(wr);
  //   assertEquals(new Result<Void>(null, 200, "OK"), result1);

  //   Thread.sleep(SLEEP);

  //   Filter filter = new Filter();
  //   filter.addKey("key1");
  //   filter.addKey("key2");
  //   Cursor<MultiDataPoint> cursor = client.readMultiDataPoints(filter, interval, timezone);

  //   Map<String, Number> data1 = new HashMap<String, Number>();
  //   data1.put("key1", 5.0);
  //   data1.put("key2", 6.0);

  //   Map<String, Number> data2 = new HashMap<String, Number>();
  //   data2.put("key1", 7.0);
  //   data2.put("key2", 8.0);

  //   List<MultiDataPoint> expected = Arrays.asList(
  //     new MultiDataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), data1),
  //     new MultiDataPoint(new DateTime(2012, 1, 1, 0, 1, 0, 0, timezone), data2)
  //   );
  //   assertEquals(expected, toList(cursor));
  // }

  // @Test
  // public void testReadMultiRollupDataPointByKey() throws InterruptedException {
  //   DataPoint dp1 = new DataPoint(new DateTime(2012, 1, 2, 0, 0 ,0, 0, timezone), 23.45);
  //   DataPoint dp2 = new DataPoint(new DateTime(2012, 1, 2, 1, 0 ,0, 0, timezone), 34.56);

  //   Result<Void> result = client.writeDataPoints(new Sensor("key1"), Arrays.asList(dp1, dp2));
  //   assertEquals(State.SUCCESS, result.getState());
  //   Thread.sleep(SLEEP);

  //   DateTime start = new DateTime(2012, 1, 2, 0, 0, 0, 0, timezone);
  //   DateTime end = new DateTime(2012, 1, 3, 0, 0, 0, 0, timezone);
  //   MultiRollup rollup = new MultiRollup(Period.days(1), new Fold[] { Fold.MAX, Fold.MIN });

  //   Cursor<MultiDataPoint> cursor = client.readMultiRollupDataPoints(new Sensor("key1"), new Interval(start, end), timezone, rollup);

  //   Map<String, Number> data1 = new HashMap<String, Number>();
  //   data1.put("max", 34.56);
  //   data1.put("min", 23.45);

  //   MultiDataPoint mdp1 = new MultiDataPoint(new DateTime(2012, 1, 2, 0, 0, 0, 0, timezone), data1);
  //   List<MultiDataPoint> expected = Arrays.asList(mdp1);

  //   assertEquals(expected, toList(cursor));
  // }

  // @Test
  // public void testWriteDataPoints() throws InterruptedException {
  //   WriteRequest wr = new WriteRequest()
  //     .add(new Sensor("key1"), new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), 5.0))
  //     .add(new Sensor("key2"), new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), 6.0))
  //     .add(new Sensor("key1"), new DataPoint(new DateTime(2012, 1, 1, 0, 1, 0, 0, timezone), 7.0))
  //     .add(new Sensor("key2"), new DataPoint(new DateTime(2012, 1, 1, 0, 2, 0, 0, timezone), 8.0));

  //   Thread.sleep(SLEEP);

  //   Result<Void> result = client.writeDataPoints(wr);
  //   assertEquals(new Result<Void>(null, 200, "OK"), result);
  // }

  // @Test
  // public void testReadDataPoints() throws InterruptedException {
  //   WriteRequest wr = new WriteRequest()
  //     .add(new Sensor("key1"), new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), 5.0))
  //     .add(new Sensor("key2"), new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), 6.0))
  //     .add(new Sensor("key1"), new DataPoint(new DateTime(2012, 1, 1, 0, 1, 0, 0, timezone), 7.0))
  //     .add(new Sensor("key2"), new DataPoint(new DateTime(2012, 1, 1, 0, 1, 0, 0, timezone), 8.0));

  //   Result<Void> result1 = client.writeDataPoints(wr);
  //   assertEquals(new Result<Void>(null, 200, "OK"), result1);

  //   Thread.sleep(SLEEP);

  //   Filter filter = new Filter();
  //   filter.addKey("key1");
  //   filter.addKey("key2");
  //   Aggregation aggregation = new Aggregation(Fold.SUM);
  //   Cursor<DataPoint> cursor = client.readDataPoints(filter, interval, timezone, aggregation);

  //   DataPoint dp1 = new DataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), 11.0);
  //   DataPoint dp2 = new DataPoint(new DateTime(2012, 1, 1, 0, 1, 0, 0, timezone), 15.0);
  //   List<DataPoint> expected = Arrays.asList(dp1, dp2);
  //   assertEquals(expected, toList(cursor));
  // }

  // @Test
  // public void testGetSensorByKey() {
  //   // Create a sensor
  //   HashMap<String, String> attributes = new HashMap<String, String>();
  //   attributes.put("appidÜ", "1234");
  //   attributes.put("txn", "/def ault");

  //   Sensor sensor = new Sensor("appidÜ:1234.txn:/def ault.cou+()+={}nt", "name", new HashSet<String>(), attributes);
  //   Result<Sensor> result1 = client.createSensor(sensor);

  //   // Get the sensor
  //   Result<Sensor> result2 = client.getSensor("appidÜ:1234.txn:/def ault.cou+()+={}nt");
  //   Result<Sensor> expected = new Result<Sensor>(sensor, 200, "OK");
  //   assertEquals(expected, result2);
  // }

  // @Test
  // public void testGetSensorByFilter() {
  //   // Create a sensor
  //   HashSet<String> tags = new HashSet<String>();
  //   tags.add("get-filter");
  //   Sensor sensor = new Sensor("create-sensor", "name", tags, new HashMap<String, String>());
  //   Result<Sensor> result1 = client.createSensor(sensor);

  //   // Get the sensor by filter
  //   Filter filter = new Filter();
  //   filter.addTag("get-filter");
  //   Cursor<Sensor> cursor = client.getSensor(filter);
  //   List<Sensor> expected = Arrays.asList(sensor);
  //   assertEquals(expected, toList(cursor));
  // }

  // @Test
  // public void testUpdateSensor() {
  //   // Create a sensor
  //   HashSet<String> tags = new HashSet<String>();
  //   tags.add("update");
  //   Sensor sensor = new Sensor("update-sensor", "name", tags, new HashMap<String, String>());
  //   Result<Sensor> result1 = client.createSensor(sensor);

  //   // Update the sensor
  //   sensor.getTags().add("update2");
  //   Result<Sensor> result2 = client.updateSensor(sensor);
  //   assertEquals(new Result<Sensor>(sensor, 200, "OK"), result2);

  //   // Get the sensor
  //   Result<Sensor> result3 = client.getSensor("update-sensor");
  //   Result<Sensor> expected = new Result<Sensor>(sensor, 200, "OK");
  //   assertEquals(expected, result3);
  // }

  // @Test
  // public void testDeleteSensor() {
  //   // Create a sensor
  //   HashSet<String> tags = new HashSet<String>();
  //   tags.add("delete");
  //   Sensor sensor = new Sensor("delete-sensor", "name", tags, new HashMap<String, String>());
  //   Result<Sensor> result1 = client.createSensor(sensor);

  //   // Delete the sensor
  //   Result<Void> result2 = client.deleteSensor(sensor);
  //   assertEquals(new Result<Void>(null, 200, "OK"), result2);

  //   // Get the sensor
  //   Result<Sensor> result3 = client.getSensor("delete-sensor");
  //   Result<Sensor> expected = new Result<Sensor>(null, 403, "Forbidden");
  //   assertEquals(expected, result3);
  // }

  // @Test
  // public void testDeleteSensorByFilter() {
  //   // Create a sensor
  //   HashSet<String> tags = new HashSet<String>();
  //   tags.add("delete-filter");
  //   Sensor sensor1 = new Sensor("delete-sensor", "name", tags, new HashMap<String, String>());
  //   Sensor sensor2 = new Sensor("delete-sensor2", "name", new HashSet<String>(), new HashMap<String, String>());
  //   Result<Sensor> result1 = client.createSensor(sensor1);
  //   Result<Sensor> result2 = client.createSensor(sensor2);

  //   // Get the sensor by filter
  //   Filter filter = new Filter();
  //   filter.addTag("delete-filter");
  //   Cursor<Sensor> cursor = client.getSensor(filter);
  //   List<Sensor> expected1 = Arrays.asList(sensor1);
  //   assertEquals(expected1, toList(cursor));

  //   // Delete the sensor by filter
  //   Result<DeleteSummary> result3 = client.deleteSensor(filter);
  //   assertEquals(new Result<DeleteSummary>(new DeleteSummary(1), 200, "OK"), result3);

  //   // Get the sensor by filter again
  //   Cursor<Sensor> cursor2 = client.getSensor(filter);
  //   List<Sensor> expected2 = Arrays.asList();
  //   assertEquals(expected2, toList(cursor2));
  // }

  // private <T> List<T> toList(Cursor<T> cursor) {
  //   List<T> output = new ArrayList<T>();
  //   for(T dp : cursor) {
  //     output.add(dp);
  //   }
  //   return output;
  // }
}
