package com.tempoiq;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;

import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;

/**
 *  The main object used to make calls to the TempoIQ api.
 *
 *  It is a thin wrapper around the <a target="_blank" href="http://tempo-db.com/docs/api/">TempoIQ Rest API</a>
 *
 *  <p>A client object holds the session information required to authenticate and connect to the Rest api. An api key and secret
 *  are required. These can be obtained by signing up at <a href="http://tempo-db.com">http://tempo-db.com</a>. The client
 *  also lets you specify a different hostname (for instance, if you are on a dedicated cluster) and port, and whether to
 *  use SSL encryption on the connection.
 *
 *  <p>Using the client, you can:
 *  <ul>
 *    <li>Retrieve a filtered list of Sensor</li>
 *    <li>Retrieve a Sensor by key</li>
 *    <li>Retrieve datapoints for a single sensor in a specific time interval</li>
 *    <li>Write datapoints for a single sensor</li>
 *    <li>Retrieve datapoints aggregated across multiple Sensor</li>
 *    <li>Write datapoints to multiple Sensor</li>
 *  </ul>
 *
 *  <p>The following example initializes a Client object and retrieves datapoints for a Sensor referenced by the key "my-key"
 *  for the time period <tt>2012-01-01</tt> to <tt>2010-01-02</tt>, returning the hourly average. This calls returns a <tt>Cursor&lt;DataPoint&gt;</tt>
 *  which provides a lazily loaded iterable of DataPoints.
 *
 *  <p><pre>
 *    import java.net.InetSocketAddress;
 *    import org.joda.time.DateTime;
 *    import org.joda.time.DateTimeZone;
 *    import org.joda.time.Interval;
 *    import org.joda.time.Period;
 *
 *    Database database = new Database("database-id");
 *    Credentials credentials = new Credentials("api-key", "api-secret");
 *    InetSocketAddress host = new InetSocketAddress("api.tempo-db.com", 443);
 *    Client client = new Client(database, credentials, host, "https");
 *
 *    DateTime start = new DateTime(2012, 1, 1, 0, 0, 0, 0);
 *    DateTime end = new DateTime(2012, 1, 2, 0, 0, 0, 0);
 *    Rollup rollup = new Rollup(Period.hours(1), Fold.MEAN);
 *
 *    Cursor&lt;DataPoint&gt; datapoints = client.readDataPoints(new Sensor("my-key"), new Interval(start, end), DateTimeZone.UTC, rollup);
 *  </pre>
 *
 *  <p>The TempoIQ Rest API supports http keep-alive, and the Client object is designed to be thread-safe. It is recommended
 *  that a Client object be created and then reused for subsequent calls. This help to amoritize the cost of setting up the
 *  http client across many calls.
 */
public class Client {

  private Executor runner;
  private static final String API_VERSION2 = "v2";
  private static final String SIMPLE_READ_MEDIATYPE = "application/prs.tempoiq.datapoint-collection.v1";
  private static final String PAGINATED_READ_MEDIATYPE = "application/prs.tempoiq.datapoint-collection.v2";
  private static final String SIMPLE_SEARCH_MEDIATYPE = "application/prs.tempoiq.device-collection.v1";
  private static final String PAGINATED_SEARCH_MEDIATYPE = "application/prs.tempoiq.device-collection.v2";
  private static final int GENERIC_ERROR_CODE = 600;

  /**
   *  Base constructor for a Client object.
   *
   *  @param credentials Api credentials
   *  @param host Api server host address
   *  @param scheme Scheme for requests. "http" and "https" are supported.
   */
  public Client(Credentials credentials, InetSocketAddress host, String scheme) {
    checkArgument(scheme.equals("http") || scheme.equals("https"), "Scheme must be either \"http\" or \"https\".");
    this.runner = new Executor(credentials, host, scheme);
  }

  /**
   *  Returns the client's credentials.
   *  @return Api credentials
   *  @since 1.0.0
   */
  public Credentials getCredentials() { return runner.getCredentials(); }

  /**
   *  Returns client's api server host.
   *  @return Api server host address.
   *  @since 1.0.0
   */
  public InetSocketAddress getHost() { return runner.getHost(); }

  /**
   *  Returns client's api server scheme.
   *  @return Api server scheme.
   *  @since 1.0.0
   */
  public String getScheme() { return runner.getScheme(); }

  /**
   *  Create a Device
   *  @param device The Device to create
   *  @return The created Device
   *
   *  @since 1.1.0
   *  @throws NullPointerException if the input Device is null.
   */
  public Result<Device> createDevice(Device device) {
    checkNotNull(device);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/devices/", API_VERSION2));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = "Could not build URI";
      throw new IllegalArgumentException(message, e);
    }

    Result<Device> result = null;
    String body = null;
    try {
      body = Json.dumps(device);
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<Device>(null, GENERIC_ERROR_CODE, message);
      return result;
    }

    return runner.post(uri, body, Device.class);
  }

  /**
   *  Returns a Device referenced by key.
   *
   *  @param key The Device key to retrieve
   *  @return The requested Device.
   *  @since 1.1.0
   */
  public Result<Device> getDevice(String key) {
    checkNotNull(key);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/devices/%s/", API_VERSION2, urlencode(key)));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s", key);
      throw new IllegalArgumentException(message, e);
    }

    return runner.get(uri, Device.class);
  }

  /**
   *  Updates all of a Device metadata
   *
   *  @param device The device to update
   *  @return The updated Device
   *
   *  @see Sensor
   *  @since 1.1.0
   */
  public Result<Device> updateDevice(Device device) {
    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/devices/%s/", API_VERSION2, urlencode(device.getKey())));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = "Could not build URI";
      throw new IllegalArgumentException(message, e);
    }

    Result<Device> result = null;
    String body = null;
    try {
      body = Json.dumps(device);
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<Device>(null, GENERIC_ERROR_CODE, message);
      return result;
    }

    return runner.put(uri, body, Device.class);
  }

  /**
   *  Deletes a Device.
   *
   *  @param device The Device to deleteDataPoints
   *  @return {@link Void}
   *  @since 1.1.0
   */
  public Result<DeleteSummary> deleteDevice(Device device) {
    checkNotNull(device);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/devices/%s/", API_VERSION2, urlencode(device.getKey())));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s", device.getKey());
      throw new IllegalArgumentException(message, e);
    }

    return runner.delete(uri);
  }

  /**
   *  Deletes set of devices by a selection.
   *
   *  @param selection The device selection @see Selection
   *  @return A DeleteSummary providing information about the series deleted.
   *
   *  @see DeleteSummary
   *  @see Selection
   *  @since 1.1.0
   */
  public Result<DeleteSummary> deleteDevices(Selection selection) {
    checkNotNull(selection);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/devices/", API_VERSION2));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with input - selection: %s", selection);
      throw new IllegalArgumentException(message, e);
    }

    Result<DeleteSummary> result = null;
    String body = null;
    try {
      Query query = new Query(
        new QuerySearch(Selector.Type.DEVICES, selection),
        null,
        new FindAction());
      body = Json.dumps(query);
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<DeleteSummary>(null, GENERIC_ERROR_CODE, message);
      return result;
    }

    return runner.delete(uri, body);
  }

  public Result<Void> writeDataPoints(Device device, MultiDataPoint data) {
    checkNotNull(device);
    checkNotNull(data);

    WriteRequest wr = new WriteRequest();
    for(Map.Entry<String, Number> entry : data.getData().entrySet()) {
      wr.add(device, new Sensor(entry.getKey()), new DataPoint(data.getTimestamp(), entry.getValue()));
    }

    return writeDataPoints(wr);
  }

  public Result<Void> writeDataPoints(Device device, List<MultiDataPoint> data) {
    checkNotNull(device);
    checkNotNull(data);
    WriteRequest wr = new WriteRequest();
    for (MultiDataPoint point : data) {
      for(Map.Entry<String, Number> entry : point.getData().entrySet()) {
        wr.add(device, new Sensor(entry.getKey()), new DataPoint(point.getTimestamp(), entry.getValue()));
      }
    }
    return writeDataPoints(wr);
  }

  /**
   *  Writes datapoints to multiple Devices and Sensor.
   *
   *  <p>This request can partially succeed. You should check the {@link Result#getState()} to check if the request was
   *  successful. If the request was partially successful, the result's {@link MultiStatus} can be inspected to determine
   *  what failed.
   *
   *  @param request A WriteRequest for the DataPoints to write.
   *  @return {@link Void}
   *
   *  @see MultiDataPoint
   *  @see MultiStatus
   *  @since 1.0.0
   */
  public Result<Void> writeDataPoints(WriteRequest request) {
    checkNotNull(request);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/write/", API_VERSION2));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = "Could not build URI.";
      throw new IllegalArgumentException(message, e);
    }

    Result<Void> result = null;
    String body = null;
    try {
      body = Json.dumps(request.asMap());
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<Void>(null, GENERIC_ERROR_CODE, message);
      return result;
    }

    return runner.post(uri, body, Void.class);
  }

  public DeviceCursor listDevices(Selection selection) {
    return listDevices(selection, null);
  }

  public DeviceCursor listDevices(Selection selection, Integer limit) {
    checkNotNull(selection);
    String mediaType = mediaType("datapoint-collection", "v2");

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/devices/", API_VERSION2));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = "Could not build URI.";
      throw new IllegalArgumentException(message, e);
    }

    Query query = new Query(
      new QuerySearch(Selector.Type.DEVICES, selection),
      null,
      new FindAction());

    Result<DeviceSegment> result = null;
    String body = null;
    try {
      body = Json.dumps(query);
      result = runner.get(uri, body, DeviceSegment.class, mediaType);
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<DeviceSegment>(null, GENERIC_ERROR_CODE, message);
    }
    return new DeviceCursor(result, this.runner, uri, mediaType);
  }

  public DataPointRowCursor read(Selection selection, Pipeline pipeline, DateTime start, DateTime stop, Integer limit) {
    checkNotNull(selection);
    checkNotNull(start);
    checkNotNull(stop);
    String mediaType = mediaType("datapoint-collection", "v2");

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/read/", API_VERSION2));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = "Could not build URI.";
      throw new IllegalArgumentException(message, e);
    }

    Query query = new Query(
      new QuerySearch(Selector.Type.DEVICES, selection),
      pipeline,
      new ReadAction(start, stop, limit));
    Result<RowSegment> result = null;
    String body = null;
    try {
      body = Json.dumps(query);
      result = runner.get(uri, body, RowSegment.class, mediaType);
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<RowSegment>(null, GENERIC_ERROR_CODE, message);
    }
    return new DataPointRowCursor(result, this.runner, uri, mediaType);
  }

  public DataPointRowCursor read(Selection selection, DateTime start, DateTime stop) {
    return read(selection, new Pipeline(), start, stop, null);
  }
  
  public DataPointRowCursor read(Selection selection, Pipeline pipeline, DateTime start, DateTime stop) {
    return read(selection, pipeline, start, stop, null);
  }

  public DataPointRowCursor read(Selection selection, DateTime start, DateTime stop, Integer limit) {
    return read(selection, new Pipeline(), start, stop, limit);
  }

  public DataPointRowCursor latest(Selection selection, Pipeline pipeline) {
    checkNotNull(selection);
    checkNotNull(pipeline);
    String mediaType = mediaType("datapoint-collection", "v2");

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/single/", API_VERSION2));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = "Could not build URI.";
      throw new IllegalArgumentException(message, e);
    }

    Query query = new Query(
      new QuerySearch(Selector.Type.DEVICES, selection),
      pipeline,
      new SingleValueAction());

    Result<RowSegment> result = null;
    String body = null;
    try {
      body = Json.dumps(query);
      result = runner.get(uri, body, RowSegment.class, mediaType);
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<RowSegment>(null, GENERIC_ERROR_CODE, message);
    }
    return new DataPointRowCursor(result, this.runner, uri, mediaType);
  }

  public DataPointRowCursor latest(Selection selection) {
    return latest(selection, new Pipeline());
  }

  public Result<DeleteSummary> deleteDataPoints(Device device, Sensor sensor, DateTime start, DateTime stop) {
    checkNotNull(device);
    checkNotNull(sensor);
    checkNotNull(start);
    checkNotNull(stop);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/devices/%s/sensors/%s/datapoints", API_VERSION2, device.getKey(), sensor.getKey()));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = "Could not build URI.";
      throw new IllegalArgumentException(message, e);
    }

    Delete delete = new Delete(start, stop);
    Result<DeleteSummary> result = null;
    String body;

    try {
      body = Json.dumps(delete);
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<DeleteSummary>(null, GENERIC_ERROR_CODE, message);
      return result;
    }
    return runner.delete(uri, body);
  }

  public void setHttpClient(HttpClient client) { this.runner.setHttpClient(client); }

  private String urlencode(String key) {
    String encoded;
    try {
      encoded = URLEncoder.encode(key, "utf-8").replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      encoded = key;
    }
    return encoded;
  }

  private String mediaType(String entity, String version) {
    return String.format("application/prs.tempoiq.%s.%s+json", entity, version);
  }
}
