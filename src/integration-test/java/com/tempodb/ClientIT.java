package com.tempoiq;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;
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

  // Tag test devices with a unique-ish attribute and key prefix so they're 
  // unlikely to conflict with existing devices in the backend
  private static final String DEVICE_PREFIX = "b90467087145fd06";

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
    System.out.println("CREDENTIALS PATH: " + credentials.getAbsolutePath());
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
    // /* Delete devices that are tagged from this integration test */
    Selection sel = new Selection().
      addSelector(Selector.Type.DEVICES, Selector.attributes(DEVICE_PREFIX, DEVICE_PREFIX));

    Result<DeleteSummary> result = client.deleteDevices(sel);
    System.out.println("CLEANUP RESULT: " + result.getMessage());
    assertEquals(State.SUCCESS, result.getState());
  }

  static public Device createDevice() {
    List<Sensor> sensors = new ArrayList<Sensor>();
    HashMap<String, String> attributes = new HashMap<String, String>();
    attributes.put(DEVICE_PREFIX, DEVICE_PREFIX);
    sensors.add(new Sensor("sensor1"));
    sensors.add(new Sensor("sensor2"));
    Device device = new Device(DEVICE_PREFIX+"-1", "name", attributes, sensors);
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
    HashMap<String, String> attributes = new HashMap<String, String>();
    attributes.put(DEVICE_PREFIX, DEVICE_PREFIX);

    Device device = new Device(DEVICE_PREFIX+"create", "name", attributes, new ArrayList<Sensor>());

    Result<Device> result = client.createDevice(device);
    Result<Device> expected = new Result<Device>(device, 200, "OK");

    assertEquals(expected, result);
  }

  @Test
  public void testWriteDataPointBySensor() {
    Device device = createDevice();

    Map<String, Number> points = new HashMap<String, Number>();
    points.put("sensor1", 1.23);
    points.put("sensor2", 1.67);
    MultiDataPoint mp = new MultiDataPoint(new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone), points);
    Result<Void> result = client.writeDataPoints(device, mp);
    assertEquals(State.SUCCESS, result.getState());
  }

  @Test
  public void testReadDataPoints() {
    Device device = createDevice();
    DateTime start = new DateTime(2011, 1, 1, 0, 0, 0, 0, timezone);
    DateTime stop = new DateTime(2013, 1, 2, 0, 0, 0, 0, timezone);

    for (int i=1; i<11; i++) {
      Map<String, Number> points = new HashMap<String, Number>();
      points.put("sensor1", i+0.1);
      points.put("sensor2", i+10.1);
      MultiDataPoint mp = new MultiDataPoint(new DateTime(2012, i, 1, 1, 0, 0, 0, timezone), points);
      Result<Void> result = client.writeDataPoints(device, mp);
      assertEquals(State.SUCCESS, result.getState());
    }
    Selection sel = new Selection().addSelector(Selector.Type.DEVICES, Selector.key(device.getKey()));
    Cursor<Row> rows = client.read(sel, start, stop, 6);
    System.out.println("ROWS");
    int numRows = 0;
    for(Row r : rows) {
      numRows ++;
    }
    assert(10 == numRows);
  }

  @Test
  public void testReadWithPipeline() {
    Device device = createDevice();
    DateTime start = new DateTime(2012, 1, 1, 0, 0, 0, 0, timezone);
    DateTime stop = new DateTime(2012, 1, 2, 0, 0, 0, 0, timezone);

    Map<String, Number> points = new HashMap<String, Number>();
    points.put("sensor1", 4.0);
    points.put("sensor2", 2.0);
    MultiDataPoint mp = new MultiDataPoint(new DateTime(2012, 1, 1, 1, 0, 0, 0, timezone), points);
    MultiDataPoint mp2 = new MultiDataPoint(new DateTime(2012, 1, 1, 2, 0, 0, 0, timezone), points);

    List<MultiDataPoint> allPoints = new ArrayList<MultiDataPoint>();
    allPoints.add(mp);
    allPoints.add(mp2);

    Result<Void> result = client.writeDataPoints(device, allPoints);
    assertEquals(State.SUCCESS, result.getState());

    Selection sel = new Selection().
      addSelector(Selector.Type.DEVICES, Selector.key(device.getKey()));

    Pipeline pipeline = new Pipeline()
      .rollup(Period.days(1), Fold.SUM, start)
      .aggregate(Fold.MEAN);
    Cursor<Row> cursor = client.read(sel, pipeline, start, stop);
    assert(cursor.iterator().hasNext());
    for (Row row : cursor) {
      assertEquals(6.0, row.getValue(device.getKey(), "mean"));
    }
  }

  @Test
  public void testPagingReadDataPoints() {
    Device device = createDevice();
    DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, timezone);
    DateTime stop = new DateTime(2015, 1, 2, 0, 0, 0, 0, timezone);
    List<MultiDataPoint> mps = new ArrayList<MultiDataPoint>();

    for(int i=0; i<10; i++) {
      Map<String, Number> points = new HashMap<String, Number>();
      points.put("sensor1", i+1.5);
      points.put("sensor2", i*10+1.5);
      MultiDataPoint mp = new MultiDataPoint(new DateTime(2012, i+1, 1, 1, 0, 0, 0, timezone), points);
      mps.add(mp);
    }

    for(MultiDataPoint mp : mps) {
      Result<Void> result = client.writeDataPoints(device, mp);
      assertEquals(State.SUCCESS, result.getState());
    }

    Selection sel = new Selection().
      addSelector(Selector.Type.DEVICES, Selector.key(device.getKey()));

    DataPointRowCursor cursor = client.read(sel, start, stop, 6);
    Iterator<Row> iterator = cursor.iterator();
    for (int i=0; i<10; i++) {
      assert(iterator.hasNext());
      iterator.next();
    }
  }

  @Test
  public void testLatest() {
    Device device = createDevice();

    Map<String, Number> points = new HashMap<String, Number>();
    points.put("sensor1", 4.0);
    points.put("sensor2", 2.0);
    MultiDataPoint mp = new MultiDataPoint(new DateTime(2012, 1, 1, 1, 0, 0, 0, timezone), points);
    MultiDataPoint mp2 = new MultiDataPoint(new DateTime(2012, 1, 1, 2, 0, 0, 0, timezone), points);

    List<MultiDataPoint> allPoints = new ArrayList<MultiDataPoint>();
    allPoints.add(mp);
    allPoints.add(mp2);

    Result<Void> result = client.writeDataPoints(device, allPoints);
    assertEquals(State.SUCCESS, result.getState());

    Selection sel = new Selection().addSelector(Selector.Type.DEVICES, Selector.key(device.getKey()));

    Cursor<Row> cursor = client.latest(sel);
    assert(cursor.iterator().hasNext());
    for (Row row : cursor) {
      assertEquals(4.0, row.getValue(device.getKey(), "sensor1"));
    }
  }

  @Test
  public void testSingle() {
    Device device = createDevice();

    Map<String, Number> points = new HashMap<String, Number>();
    points.put("sensor1", 4.0);
    points.put("sensor2", 2.0);
    MultiDataPoint mp = new MultiDataPoint(new DateTime(2012, 1, 1, 1, 0, 0, 0, timezone), points);
    MultiDataPoint mp2 = new MultiDataPoint(new DateTime(2012, 1, 1, 2, 0, 0, 0, timezone), points);

    List<MultiDataPoint> allPoints = new ArrayList<MultiDataPoint>();
    allPoints.add(mp);
    allPoints.add(mp2);

    Result<Void> result = client.writeDataPoints(device, allPoints);
    assertEquals(State.SUCCESS, result.getState());

    Selection sel = new Selection().addSelector(Selector.Type.DEVICES, Selector.key(device.getKey()));

    Cursor<Row> cursor = client.sin(sel);
    assert(cursor.iterator().hasNext());
    for (Row row : cursor) {
      assertEquals(4.0, row.getValue(device.getKey(), "sensor1"));
    }
  }

  @Test
  public void testDeletePoints() {
    Device device = createDevice();

    Sensor sensor1 = new Sensor("sensor1");
    Sensor sensor2 = new Sensor("sensor2");

    Map<String, Number> points1 = new HashMap<String, Number>();
    points1.put("sensor1", 1.0);
    points1.put("sensor2", 10.0);

    Map<String, Number> points2 = new HashMap<String, Number>();
    points2.put("sensor1", 2.0);
    points2.put("sensor2", 20.0);

    Map<String, Number> points3 = new HashMap<String, Number>();
    points3.put("sensor1", 3.0);
    points3.put("sensor2", 30.0);

    MultiDataPoint mp = new MultiDataPoint(new DateTime(2012, 1, 1, 1, 0, 0, 0, timezone), points1);
    MultiDataPoint mp2 = new MultiDataPoint(new DateTime(2012, 1, 1, 2, 0, 0, 0, timezone), points2);
    MultiDataPoint mp3 = new MultiDataPoint(new DateTime(2012, 1, 1, 3, 0, 0, 0, timezone), points3);

    List<MultiDataPoint> allPoints = new ArrayList<MultiDataPoint>();
    allPoints.add(mp);
    allPoints.add(mp2);
    allPoints.add(mp3);

    Result<Void> result = client.writeDataPoints(device, allPoints);
    assertEquals(State.SUCCESS, result.getState());

    DateTime start = new DateTime(2012, 1, 1, 2, 0, 0, 0, timezone);
    DateTime stop = new DateTime(2012, 1, 4, 0, 0, 0, 0, timezone);

    Result<DeleteSummary> delResult = client.deleteDataPoints(device, sensor1, start, stop);
    assertEquals(State.SUCCESS, result.getState());
    assertEquals(delResult.getValue().getDeleted(), 2);

    Cursor<Row> cursor1 = client.read(new Selection().addSelector(Selector.Type.SENSORS, Selector.key("sensor1")),
      new DateTime(2011, 1, 1, 0, 0, 0, 0, timezone),
      new DateTime(2013, 1, 1, 1, 0, 0, 0, timezone));

    Cursor<Row> cursor2 = client.latest(new Selection().addSelector(Selector.Type.SENSORS, Selector.key("sensor2")));

    assert(cursor1.iterator().hasNext());
    assertEquals(1.0, cursor1.iterator().next().getValue(device.getKey(), "sensor1"));
    assert(cursor2.iterator().hasNext());
    assertEquals(30.0, cursor2.iterator().next().getValue(device.getKey(), "sensor2"));
    }
  }
