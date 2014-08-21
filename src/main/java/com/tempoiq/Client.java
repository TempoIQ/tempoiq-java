package com.tempoiq;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
 *    <li>Retrieve a filtered list of Series</li>
 *    <li>Retrieve a Series by key</li>
 *    <li>Retrieve datapoints for a single series in a specific time interval</li>
 *    <li>Write datapoints for a single series</li>
 *    <li>Retrieve datapoints aggregated across multiple Series</li>
 *    <li>Write datapoints to multiple Series</li>
 *  </ul>
 *
 *  <p>The following example initializes a Client object and retrieves datapoints for a Series referenced by the key "my-key"
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
 *    Cursor&lt;DataPoint&gt; datapoints = client.readDataPoints(new Series("my-key"), new Interval(start, end), DateTimeZone.UTC, rollup);
 *  </pre>
 *
 *  <p>The TempoIQ Rest API supports http keep-alive, and the Client object is designed to be thread-safe. It is recommended
 *  that a Client object be created and then reused for subsequent calls. This help to amoritize the cost of setting up the
 *  http client across many calls.
 */
public class Client {

  private final Credentials credentials;
  private final InetSocketAddress host;
  private final String scheme;

  private HttpClient client = null;
  private HttpHost target = null;

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
  // Timeout on milliseconds
  private static final int DEFAULT_TIMEOUT_MILLIS = 50000;  // 50 seconds
  private static final long DEFAULT_KEEPALIVE_TIMEOUT_MILLIS = 50000;  // 50 seconds
  private static final int GENERIC_ERROR_CODE = 600;
  private static final String VERSION = "1.0-SNAPSHOT";
  private static final String API_VERSION = "v1";
  private static final String API_VERSION2 = "v2";
  private final DateTimeFormatter iso8601 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private enum HttpMethod { GET, POST, PUT, DELETE }

  /**
   *  Base constructor for a Client object.
   *
   *  @param credentials Api credentials
   *  @param host Api server host address
   *  @param scheme Scheme for requests. "http" and "https" are supported.
   */
  public Client(Credentials credentials, InetSocketAddress host, String scheme) {
    checkArgument(scheme.equals("http") || scheme.equals("https"), "Scheme must be either \"http\" or \"https\".");
    this.credentials = checkNotNull(credentials, "Credentials cannot be null.");
    this.host = checkNotNull(host, "Host cannot be null.");
    this.scheme = checkNotNull(scheme, "Scheme cannot be null.");
  }

  /**
   *  Returns the client's credentials.
   *  @return Api credentials
   *  @since 1.0.0
   */
  public Credentials getCredentials() { return credentials; }

  /**
   *  Returns client's api server host.
   *  @return Api server host address.
   *  @since 1.0.0
   */
  public InetSocketAddress getHost() { return host; }

  /**
   *  Returns client's api server scheme.
   *  @return Api server scheme.
   *  @since 1.0.0
   */
  public String getScheme() { return scheme; }

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

    HttpRequest request = buildRequest(uri.toString(), HttpMethod.POST, body);
    result = execute(request, Device.class);
    return result;
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

    HttpRequest request = buildRequest(uri.toString());
    Result<Device> result = execute(request, Device.class);
    return result;
  }

  /**
   *  Updates all of a Device metadata
   *
   *  @param device The device to update
   *  @return The updated Device
   *
   *  @see Series
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

    HttpRequest request = buildRequest(uri.toString(), HttpMethod.PUT, body);
    result = execute(request, Device.class);
    return result;
  }

  /**
   *  Deletes a Device.
   *
   *  @param device The Device to delete
   *  @return {@link Void}
   *  @since 1.1.0
   */
  public Result<Void> deleteDevice(Device device) {
    checkNotNull(device);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/devices/%s/", API_VERSION2, urlencode(device.getKey())));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s", device.getKey());
      throw new IllegalArgumentException(message, e);
    }

    HttpRequest request = buildRequest(uri.toString(), HttpMethod.DELETE);
    Result<Void> result = execute(request, Void.class);
    return result;
  }

  /**
   *  Deletes a range of datapoints for a Series specified by key.
   *
   *  @param series The series
   *  @param interval The start/end datetime interval to delete.
   *  @return Void
   *
   *  @since 1.0.0
   */
  public Result<Void> deleteDataPoints(Series series, Interval interval) {
    checkNotNull(series);
    checkNotNull(interval);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/key/%s/data/", API_VERSION, urlencode(series.getKey())));
      addIntervalToURI(builder, interval);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s, interval: %s", series.getKey(), interval);
      throw new IllegalArgumentException(message, e);
    }

    HttpRequest request = buildRequest(uri.toString(), HttpMethod.DELETE);
    Result<Void> result = execute(request, Void.class);
    return result;
  }

  /**
   *  Returns a cursor of intervals/datapoints matching a predicate specified by series.
   *  <p>The system default timezone is used for the returned DateTimes.
   *
   *  @param series The series
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param predicate The predicate for the query.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @since 1.1.0
   */
  public Cursor<DataPointFound> findDataPoints(Series series, Interval interval, Predicate predicate) {
    return findDataPoints(series, interval, predicate, DateTimeZone.getDefault());
  }

  /**
   *  Returns a cursor of intervals/datapoints matching a predicate specified by series.
   *
   *  @param series The series
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param predicate The predicate for the query.
   *  @param timezone The time zone for the returned datapoints.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @since 1.1.0
   */
  public Cursor<DataPointFound> findDataPoints(Series series, Interval interval, Predicate predicate, DateTimeZone timezone) {
    checkNotNull(series);
    checkNotNull(interval);
    checkNotNull(predicate);
    checkNotNull(timezone);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/key/%s/find/", API_VERSION, urlencode(series.getKey())));
      addIntervalToURI(builder, interval);
      addPredicateToURI(builder, predicate);
      addTimeZoneToURI(builder, timezone);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s, interval: %s, predicate: %s, timezone: %s", series.getKey(), interval, predicate, timezone);
      throw new IllegalArgumentException(message, e);
    }

    Cursor<DataPointFound> cursor = new DataPointFoundCursor(uri, this);
    return cursor;
  }

  /**
   *  Returns a Series referenced by key.
   *
   *  @param key The Series key to retrieve
   *  @return The requested Series.
   *  @since 1.0.0
   */
  public Result<Series> getSeries(String key) {
    checkNotNull(key);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/key/%s/", API_VERSION, urlencode(key)));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s", key);
      throw new IllegalArgumentException(message, e);
    }

    HttpRequest request = buildRequest(uri.toString());
    Result<Series> result = execute(request, Series.class);
    return result;
  }

  /**
   *  Returns a cursor of series specified by a filter.
   *
   *  @param filter The series filter
   *  @return A Cursor of Series. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @see Filter
   *  @since 1.0.0
   */
  public Cursor<Series> getSeries(Filter filter) {
    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/", API_VERSION));
      addFilterToURI(builder, filter);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with input - filter: %s", filter);
      throw new IllegalArgumentException(message, e);
    }

    Cursor<Series> cursor = new SeriesCursor(uri, this);
    return cursor;
  }

  /**
   *  Reads a single value for a series at a specific timestamp (exact match).
   *  <p>The returned value (datapoint) can be null if there are no
   *  datapoints in the series or in the specified direction. The system default
   *  timezone is used.
   *
   *  @param series The series to read from
   *  @param timestamp The timestamp to read a value at
   *  @return The value at the specified timestamp
   *
   *  @see SingleValue
   *  @since 1.1.0
   */
  public Result<SingleValue> readSingleValue(Series series, DateTime timestamp) {
    return readSingleValue(series, timestamp, DateTimeZone.getDefault(), Direction.EXACT);
  }

  /**
   *  Reads a single value for a series at a specific timestamp (exact match).
   *  <p>The returned value (datapoint) can be null if there are no
   *  datapoints in the series or in the specified direction.
   *
   *  @param series The series to read from
   *  @param timestamp The timestamp to read a value at
   *  @param timezone The timezone of the returned datapoint
   *  @return The value at the specified timestamp
   *
   *  @see SingleValue
   *  @since 1.1.0
   */
  public Result<SingleValue> readSingleValue(Series series, DateTime timestamp, DateTimeZone timezone) {
    return readSingleValue(series, timestamp, timezone, Direction.EXACT);
  }

  /**
   *  Reads a single value for a series at a specific timestamp.
   *  <p>The returned value (datapoint) can be null if there are no
   *  datapoints in the series or in the specified direction. The system
   *  default timezone is used.
   *
   *  @param series The series to read from
   *  @param timestamp The timestamp to read a value at
   *  @param direction The direction to search if an exact timestamp match is not found
   *  @return The value at the specified timestamp
   *
   *  @see SingleValue
   *  @since 1.1.0
   */
  public Result<SingleValue> readSingleValue(Series series, DateTime timestamp, Direction direction) {
    return readSingleValue(series, timestamp, DateTimeZone.getDefault(), direction);
  }

  /**
   *  Reads a single value for a series at a specific timestamp.
   *  <p>The returned value (datapoint) can be null if there are no
   *  datapoints in the series or in the specified direction.
   *
   *  @param series The series to read from
   *  @param timestamp The timestamp to read a value at
   *  @param timezone The timezone of the returned datapoint
   *  @param direction The direction to search if an exact timestamp match is not found
   *  @return The value at the specified timestamp
   *
   *  @see SingleValue
   *  @since 1.1.0
   */
  public Result<SingleValue> readSingleValue(Series series, DateTime timestamp, DateTimeZone timezone, Direction direction) {
    checkNotNull(series);
    checkNotNull(timestamp);
    checkNotNull(timezone);
    checkNotNull(direction);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/key/%s/single/", API_VERSION, urlencode(series.getKey())));
      addTimestampToURI(builder, timestamp);
      addTimeZoneToURI(builder, timezone);
      addDirectionToURI(builder, direction);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s, timestamp: %s, timezone: %s, direction: %s", series.getKey(), timestamp.toString(), timezone.toString(), direction.toString());
      throw new IllegalArgumentException(message, e);
    }

    HttpRequest request = buildRequest(uri.toString());
    Result<SingleValue> result = execute(request, SingleValue.class);
    return result;
  }

  /**
   *  Returns a cursor of single value for a set of series
   *  <p>The returned values (datapoints) can be null if there are no
   *  datapoints in the series or in the specified direction. The
   *  system default timezone is used. The direction is set to EXACT.
   *
   *  @param filter The filter of series to read from
   *  @param timestamp The timestamp to read a value at
   *  @return A cursor over the values at the specified timestamp
   *
   *  @see Cursor
   *  @see SingleValue
   *  @since 1.1.0
   */
  public Cursor<SingleValue> readSingleValue(Filter filter, DateTime timestamp) {
    return readSingleValue(filter, timestamp, DateTimeZone.getDefault(), Direction.EXACT);
  }

  /**
   *  Returns a cursor of single value for a set of series
   *  <p>The returned values (datapoints) can be null if there are no
   *  datapoints in the series or in the specified direction.
   *  The direction is set to EXACT.
   *
   *  @param filter The filter of series to read from
   *  @param timestamp The timestamp to read a value at
   *  @param timezone The timezone of the returned datapoints
   *  @return A cursor over the values at the specified timestamp
   *
   *  @see Cursor
   *  @see SingleValue
   *  @since 1.1.0
   */
  public Cursor<SingleValue> readSingleValue(Filter filter, DateTime timestamp, DateTimeZone timezone) {
    return readSingleValue(filter, timestamp, timezone, Direction.EXACT);
  }

  /**
   *  Returns a cursor of single value for a set of series
   *  <p>The returned values (datapoints) can be null if there are no
   *  datapoints in the series or in the specified direction. The
   *  system default timezone is used.
   *
   *  @param filter The filter of series to read from
   *  @param timestamp The timestamp to read a value at
   *  @param direction The direction to search if an exact timestamp match is not found
   *  @return A cursor over the values at the specified timestamp
   *
   *  @see Cursor
   *  @see SingleValue
   *  @since 1.1.0
   */
  public Cursor<SingleValue> readSingleValue(Filter filter, DateTime timestamp, Direction direction) {
    return readSingleValue(filter, timestamp, DateTimeZone.getDefault(), direction);
  }

  /**
   *  Returns a cursor of single value for a set of series
   *  <p>The returned values (datapoints) can be null if there are no
   *  datapoints in the series or in the specified direction.
   *
   *  @param filter The filter of series to read from
   *  @param timestamp The timestamp to read a value at
   *  @param timezone The timezone of the returned datapoint
   *  @param direction The direction to search if an exact timestamp match is not found
   *  @return A cursor over the values at the specified timestamp
   *
   *  @see Cursor
   *  @see SingleValue
   *  @since 1.1.0
   */
  public Cursor<SingleValue> readSingleValue(Filter filter, DateTime timestamp, DateTimeZone timezone, Direction direction) {
    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/single/", API_VERSION));
      addFilterToURI(builder, filter);
      addTimestampToURI(builder, timestamp);
      addTimeZoneToURI(builder, timezone);
      addDirectionToURI(builder, direction);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with input - filter: %s", filter);
      throw new IllegalArgumentException(message, e);
    }

    Cursor<SingleValue> cursor = new SingleValueCursor(uri, this);
    return cursor;
  }

  /**
   *  Reads summary statistics for a series for the specified interval.
   *
   *  @param series The series to read from
   *  @param interval The interval of data to summarize
   *  @return A set of statistics for an interval of data
   *
   *  @see SingleValue
   *  @since 1.1.0
   */
  public Result<Summary> readSummary(Series series, Interval interval) {
    checkNotNull(series);
    checkNotNull(interval);
    DateTimeZone timezone = interval.getStart().getChronology().getZone();

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/key/%s/summary/", API_VERSION, urlencode(series.getKey())));
      addIntervalToURI(builder, interval);
      addTimeZoneToURI(builder, timezone);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s, interval: %s, timezone: %s", series.getKey(), interval.toString(), timezone.toString());
      throw new IllegalArgumentException(message, e);
    }

    HttpRequest request = buildRequest(uri.toString());
    Result<Summary> result = execute(request, Summary.class);
    return result;
  }

  /**
   *  Returns a cursor of datapoints specified by series.
   *  <p>The system default timezone is used for the returned DateTimes.
   *
   *  @param series The series
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @since 1.0.0
   */
  public Cursor<DataPoint> readDataPoints(Series series, Interval interval) {
    return readDataPoints(series, interval, DateTimeZone.getDefault(), null, null);
  }

  /**
   *  Returns a cursor of datapoints specified by series.
   *
   *  @param series The series
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param timezone The time zone for the returned datapoints.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @since 1.0.0
   */
  public Cursor<DataPoint> readDataPoints(Series series, Interval interval, DateTimeZone timezone) {
    return readDataPoints(series, interval, timezone, null, null);
  }

  /**
   *  Returns a cursor of datapoints specified by series.
   *  <p>The system default timezone is used for the returned DateTimes.
   *
   *  @param series The series
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param rollup The rollup for the read query. This can be null.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @since 1.0.0
   */
  public Cursor<DataPoint> readDataPoints(Series series, Interval interval, Rollup rollup) {
    return readDataPoints(series, interval, DateTimeZone.getDefault(), rollup, null);
  }

  /**
   *  Returns a cursor of datapoints specified by series.
   *
   *  @param series The series
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param timezone The time zone for the returned datapoints.
   *  @param rollup The rollup for the read query. This can be null.
   *  @param interpolation The interpolation for the read query. This can be null.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @since 1.0.0
   */
  public Cursor<DataPoint> readDataPoints(Series series, Interval interval, DateTimeZone timezone, Rollup rollup, Interpolation interpolation) {
    checkNotNull(series);
    checkNotNull(interval);
    checkNotNull(timezone);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/key/%s/segment/", API_VERSION, urlencode(series.getKey())));
      addInterpolationToURI(builder, interpolation);
      addIntervalToURI(builder, interval);
      addRollupToURI(builder, rollup);
      addTimeZoneToURI(builder, timezone);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s, interval: %s, rollup: %s, timezone: %s", series.getKey(), interval, rollup, timezone);
      throw new IllegalArgumentException(message, e);
    }

    Cursor<DataPoint> cursor = new DataPointCursor(uri, this);
    return cursor;
  }

  /**
   *  Returns a cursor of datapoints specified by series with multiple rollups.
   *  <p>The system default timezone is used for the returned DateTimes.
   *
   *  @param series The series
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param rollup The MultiRollup for the read query.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @see MultiRollup
   *  @since 1.1.0
   */
  public Cursor<MultiDataPoint> readMultiRollupDataPoints(Series series, Interval interval, MultiRollup rollup) {
    return readMultiRollupDataPoints(series, interval, DateTimeZone.getDefault(), rollup, null);
  }

  /**
   *  Returns a cursor of datapoints specified by series with multiple rollups.
   *
   *  @param series The series
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param timezone The time zone for the returned datapoints.
   *  @param rollup The MultiRollup for the read query.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @see MultiRollup
   *  @since 1.0.0
   */
  public Cursor<MultiDataPoint> readMultiRollupDataPoints(Series series, Interval interval, DateTimeZone timezone, MultiRollup rollup) {
    return readMultiRollupDataPoints(series, interval, timezone, rollup, null);
  }

  /**
   *  Returns a cursor of datapoints specified by series with multiple rollups.
   *
   *  @param series The series
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param timezone The time zone for the returned datapoints.
   *  @param rollup The MultiRollup for the read query.
   *  @param interpolation The interpolation for the read query. This can be null.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @see MultiRollup
   *  @since 1.0.0
   */
  public Cursor<MultiDataPoint> readMultiRollupDataPoints(Series series, Interval interval, DateTimeZone timezone, MultiRollup rollup, Interpolation interpolation) {
    checkNotNull(series);
    checkNotNull(interval);
    checkNotNull(timezone);
    checkNotNull(rollup);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/key/%s/data/rollups/segment/", API_VERSION, urlencode(series.getKey())));
      addInterpolationToURI(builder, interpolation);
      addIntervalToURI(builder, interval);
      addMultiRollupToURI(builder, rollup);
      addTimeZoneToURI(builder, timezone);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s, interval: %s, rollup: %s, timezone: %s", series.getKey(), interval, rollup, timezone);
      throw new IllegalArgumentException(message, e);
    }

    Cursor<MultiDataPoint> cursor = new MultiRollupDataPointCursor(uri, this);
    return cursor;
  }

  /**
   *  Returns a cursor of datapoints specified by a series filter.
   *
   *  <p>This endpoint allows one to request multiple series and apply an aggregation function.
   *  The system default timezone is used for the returned DateTimes.
   *
   *  @param filter The series filter
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param aggregation The aggregation for the read query. This is required.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Aggregation
   *  @see Cursor
   *  @see Filter
   *  @since 1.0.0
   */
  public Cursor<DataPoint> readDataPoints(Filter filter, Interval interval, Aggregation aggregation) {
    return readDataPoints(filter, interval, DateTimeZone.getDefault(), aggregation, null, null);
  }

  /**
   *  Returns a cursor of datapoints specified by a series filter.
   *
   *  <p>This endpoint allows one to request multiple series and apply an aggregation function.
   *  The system default timezone is used for the returned DateTimes.
   *
   *  @param filter The series filter
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param aggregation The aggregation for the read query. This is required.
   *  @param rollup The rollup for the read query. This can be null.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Aggregation
   *  @see Cursor
   *  @see Filter
   *  @see Rollup
   *  @since 1.0.0
   */
  public Cursor<DataPoint> readDataPoints(Filter filter, Interval interval, Aggregation aggregation, Rollup rollup) {
    return readDataPoints(filter, interval, DateTimeZone.getDefault(), aggregation, rollup, null);
  }

  /**
   *  Returns a cursor of datapoints specified by a series filter.
   *
   *  This endpoint allows one to request multiple series and apply an aggregation function.
   *
   *  @param filter The series filter
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param timezone The time zone for the returned datapoints.
   *  @param aggregation The aggregation for the read query. This is required.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Aggregation
   *  @see Cursor
   *  @see Filter
   *  @since 1.0.0
   */
  public Cursor<DataPoint> readDataPoints(Filter filter, Interval interval, DateTimeZone timezone, Aggregation aggregation) {
    return readDataPoints(filter, interval, timezone, aggregation, null, null);
  }

  /**
   *  Returns a cursor of datapoints specified by a series filter.
   *
   *  This endpoint allows one to request multiple series and apply an aggregation function.
   *
   *  @param filter The series filter
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param timezone The time zone for the returned datapoints.
   *  @param aggregation The aggregation for the read query. This is required.
   *  @param rollup The rollup for the read query. This can be null.
   *  @param interpolation The interpolation for the read query. This can be null.
   *  @return A Cursor of DataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Aggregation
   *  @see Cursor
   *  @see Filter
   *  @see Interpolation
   *  @see Rollup
   *  @since 1.0.0
   */
  public Cursor<DataPoint> readDataPoints(Filter filter, Interval interval, DateTimeZone timezone, Aggregation aggregation, Rollup rollup, Interpolation interpolation) {
    checkNotNull(filter);
    checkNotNull(interval);
    checkNotNull(aggregation);
    checkNotNull(timezone);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/segment/", API_VERSION));
      addFilterToURI(builder, filter);
      addInterpolationToURI(builder, interpolation);
      addIntervalToURI(builder, interval);
      addAggregationToURI(builder, aggregation);
      addRollupToURI(builder, rollup);
      addTimeZoneToURI(builder, timezone);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: filter: %s, interval: %s, aggregation: %s, rollup: %s, timezone: %s", filter, interval, aggregation, rollup, timezone);
      throw new IllegalArgumentException(message, e);
    }

    Cursor<DataPoint> cursor = new DataPointCursor(uri, this);
    return cursor;
  }

  /**
   *  Returns a cursor of multi-datapoints specified by a series filter.
   *
   *  <p>This endpoint allows one to request datapoints for multiple series in one call.
   *  The system default timezone is used for the returned DateTimes.
   *
   *  @param filter The series filter
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @return A Cursor of MultiDataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @see Filter
   *  @see MultiDataPoint
   *  @since 1.1.0
   */
  public Cursor<MultiDataPoint> readMultiDataPoints(Filter filter, Interval interval) {
    return readMultiDataPoints(filter, interval, DateTimeZone.getDefault(), null, null);
  }

  /**
   *  Returns a cursor of multi-datapoints specified by a series filter.
   *
   *  <p>This endpoint allows one to request datapoints for multiple series in one call.
   *  The system default timezone is used for the returned DateTimes.
   *
   *  @param filter The series filter
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param rollup The rollup for the read query. This can be null.
   *  @return A Cursor of MultiDataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @see Filter
   *  @see MultiDataPoint
   *  @see Rollup
   *  @since 1.1.0
   */
  public Cursor<MultiDataPoint> readMultiDataPoints(Filter filter, Interval interval, Rollup rollup) {
    return readMultiDataPoints(filter, interval, DateTimeZone.getDefault(), rollup, null);
  }

  /**
   *  Returns a cursor of multi-datapoints specified by a series filter.
   *
   *  This endpoint allows one to request datapoints for multiple series in one call.
   *
   *  @param filter The series filter
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param timezone The time zone for the returned datapoints.
   *  @return A Cursor of MultiDataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @see Filter
   *  @see MultiDataPoint
   *  @see Rollup
   *  @since 1.1.0
   */
  public Cursor<MultiDataPoint> readMultiDataPoints(Filter filter, Interval interval, DateTimeZone timezone) {
    return readMultiDataPoints(filter, interval, timezone, null, null);
  }

  /**
   *  Returns a cursor of multi-datapoints specified by a series filter.
   *
   *  This endpoint allows one to request datapoints for multiple series in one call.
   *
   *  @param filter The series filter
   *  @param interval An interval of time for the query (start/end datetimes)
   *  @param timezone The time zone for the returned datapoints.
   *  @param rollup The rollup for the read query. This can be null.
   *  @param interpolation The interpolation for the read query. This can be null.
   *  @return A Cursor of MultiDataPoints. The cursor.iterator().next() may throw a {@link TempoIQException} if an error occurs while making a request.
   *
   *  @see Cursor
   *  @see Filter
   *  @see Interpolation
   *  @see MultiDataPoint
   *  @see Rollup
   *  @since 1.1.0
   */
  public Cursor<MultiDataPoint> readMultiDataPoints(Filter filter, Interval interval, DateTimeZone timezone, Rollup rollup, Interpolation interpolation) {
    checkNotNull(filter);
    checkNotNull(interval);
    checkNotNull(timezone);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/multi/", API_VERSION));
      addFilterToURI(builder, filter);
      addInterpolationToURI(builder, interpolation);
      addIntervalToURI(builder, interval);
      addRollupToURI(builder, rollup);
      addTimeZoneToURI(builder, timezone);
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: filter: %s, interval: %s, rollup: %s, timezone: %s", filter, interval, rollup, timezone);
      throw new IllegalArgumentException(message, e);
    }

    Cursor<MultiDataPoint> cursor = new MultiDataPointCursor(uri, this);
    return cursor;
  }

  /**
   *  Updates all of a Series metadata
   *
   *  @param series The series to update
   *  @return The updated Series
   *
   *  @see Series
   *  @since 1.0.0
   */
  public Result<Series> updateSeries(Series series) {
    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/key/%s/", API_VERSION, urlencode(series.getKey())));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = "Could not build URI";
      throw new IllegalArgumentException(message, e);
    }

    Result<Series> result = null;
    String body = null;
    try {
      body = Json.dumps(series);
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<Series>(null, GENERIC_ERROR_CODE, message);
      return result;
    }

    HttpRequest request = buildRequest(uri.toString(), HttpMethod.PUT, body);
    result = execute(request, Series.class);
    return result;
  }

  /**
   *  Writes datapoints to single Series.
   *
   *  @param series The series to write to.
   *  @param data A list of datapoints
   *  @return {@link Void}
   *
   *  @see DataPoint
   *  @see Void
   *  @since 1.0.0
   */
  public Result<Void> writeDataPoints(Series series, List<DataPoint> data) {
    checkNotNull(series);
    checkNotNull(data);

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(String.format("/%s/series/key/%s/data/", API_VERSION, urlencode(series.getKey())));
      uri = builder.build();
    } catch (URISyntaxException e) {
      String message = String.format("Could not build URI with inputs: key: %s", series.getKey());
      throw new IllegalArgumentException(message, e);
    }

    Result<Void> result = null;
    String body = null;
    try{
      body = Json.dumps(data);
    } catch (JsonProcessingException e) {
      String message = "Error serializing the body of the request. More detail: " + e.getMessage();
      result = new Result<Void>(null, GENERIC_ERROR_CODE, message);
      return result;
    }

    HttpRequest request = buildRequest(uri.toString(), HttpMethod.POST, body);
    result = execute(request, Void.class);
    return result;
  }

  /**
   *  Writes datapoints to multiple Devices and Series.
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

    HttpRequest httpRequest = buildRequest(uri.toString(), HttpMethod.POST, body);
    result = execute(httpRequest, Void.class);
    return result;
  }

  private void addAggregationToURI(URIBuilder builder, Aggregation aggregation) {
    if(aggregation != null) {
      builder.addParameter("aggregation.fold", aggregation.getFold().toString().toLowerCase());
    }
  }

  private void addDirectionToURI(URIBuilder builder, Direction direction) {
    if(direction != null) {
      builder.addParameter("direction", direction.toString().toLowerCase());
    }
  }

  private void addFilterToURI(URIBuilder builder, Filter filter) {
    if(filter != null) {
      for(String key : filter.getKeys()) {
        builder.addParameter("key", key);
      }

      for(String tag : filter.getTags()) {
        builder.addParameter("tag", tag);
      }

      for(Map.Entry<String, String> attribute : filter.getAttributes().entrySet()) {
        builder.addParameter(String.format("attr[%s]", attribute.getKey()), attribute.getValue());
      }
    }
  }

  private void addInterpolationToURI(URIBuilder builder, Interpolation interpolation) {
    if(interpolation != null) {
      builder.addParameter("interpolation.period", interpolation.getPeriod().toString());
      builder.addParameter("interpolation.function", interpolation.getFunction().toString().toLowerCase());
    }
  }

  private void addIntervalToURI(URIBuilder builder, Interval interval) {
    if(interval != null) {
      builder.addParameter("start", interval.getStart().toString(iso8601));
      builder.addParameter("end", interval.getEnd().toString(iso8601));
    }
  }

  private void addMultiRollupToURI(URIBuilder builder, MultiRollup rollup) {
    if(rollup != null) {
      builder.addParameter("rollup.period", rollup.getPeriod().toString());
      for(Fold fold : rollup.getFolds()) {
        builder.addParameter("rollup.fold", fold.toString().toLowerCase());
      }
    }
  }

  private void addPredicateToURI(URIBuilder builder, Predicate predicate) {
    if(predicate != null) {
      builder.addParameter("predicate.period", predicate.getPeriod().toString());
      builder.addParameter("predicate.function", predicate.getFunction().toLowerCase());
    }
  }

  private void addRollupToURI(URIBuilder builder, Rollup rollup) {
    if(rollup != null) {
      builder.addParameter("rollup.period", rollup.getPeriod().toString());
      builder.addParameter("rollup.fold", rollup.getFold().toString().toLowerCase());
    }
  }

  private void addTimestampToURI(URIBuilder builder, DateTime timestamp) {
    if(timestamp != null) {
      builder.addParameter("ts", timestamp.toString(iso8601));
    }
  }

  private void addTimeZoneToURI(URIBuilder builder, DateTimeZone timezone) {
    if(timezone != null) {
      builder.addParameter("tz", timezone.toString());
    }
  }

  private String urlencode(String key) {
    String encoded;
    try {
      encoded = URLEncoder.encode(key, "utf-8").replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      encoded = key;
    }
    return encoded;
  }

  HttpRequest buildRequest(String uri) {
    return buildRequest(uri, HttpMethod.GET, null);
  }

  HttpRequest buildRequest(String uri, HttpMethod method) {
    return buildRequest(uri, method, null);
  }

  HttpRequest buildRequest(String uri, HttpMethod method, String body) {
    HttpRequest request = null;

    switch(method) {
      case POST:
        HttpPost post = new HttpPost(uri);
        if(body != null) {
          post.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        }
        request = post;
        break;
      case PUT:
        HttpPut put = new HttpPut(uri);
        if(body != null) {
          put.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        }
        request = put;
        break;
      case DELETE:
        request = new HttpDelete(uri);
        break;
      case GET:
      default:
        request = new HttpGet(uri);
        break;
    }

    return request;
  }

  HttpResponse execute(HttpRequest request) throws IOException {
    HttpClient client = getHttpClient();
    HttpContext context = getContext();
    HttpHost target = getTarget();
    HttpResponse response = client.execute(target, request, context);
    return response;
  }

  <T> Result<T> execute(HttpRequest request, Class<T> klass) {
    Result<T> result = null;
    try {
      HttpResponse response = execute(request);
      result = new Result<T>(response, klass);
    } catch (IOException e) {
      result = new Result<T>(null, GENERIC_ERROR_CODE, e.getMessage());
    }
    return result;
  }

  private synchronized HttpClient getHttpClient() {
    if(client == null) {
      HttpParams httpParams = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_TIMEOUT_MILLIS);
      HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_TIMEOUT_MILLIS);
      HttpProtocolParams.setUserAgent(httpParams, String.format("tempoiq-java/%s", getVersion()));

      DefaultHttpClient defaultClient = new DefaultHttpClient(new PoolingClientConnectionManager(), httpParams);
      defaultClient.getCredentialsProvider().setCredentials(
          new AuthScope(getTarget()),
          new UsernamePasswordCredentials(credentials.getKey(), credentials.getSecret()));

      // Add gzip header to all requests
      defaultClient.addRequestInterceptor(new HttpRequestInterceptor() {
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
          if (!request.containsHeader("Accept-Encoding")) {
            request.addHeader("Accept-Encoding", "gzip");
          }
        }
      });

      defaultClient.addResponseInterceptor(new HttpResponseInterceptor() {
        public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
          HttpEntity entity = response.getEntity();
          if (entity != null) {
            Header ceheader = entity.getContentEncoding();
            if (ceheader != null) {
              HeaderElement[] codecs = ceheader.getElements();
              for (int i = 0; i < codecs.length; i++) {
                if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                  response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                  return;
                }
              }
            }
          }
        }
      });

      defaultClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
          HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));

          while(it.hasNext()) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if(value != null && param.equalsIgnoreCase("timeout")) {
              try {
                return Long.parseLong(value) * 1000;
              } catch (NumberFormatException ignore) {
              }
            }
          }
          return DEFAULT_KEEPALIVE_TIMEOUT_MILLIS;
        }
      });

      client = defaultClient;
    }
    return client;
  }

  synchronized Client setHttpClient(HttpClient httpClient) {
    this.client = httpClient;
    return this;
  }

  private HttpContext getContext() {
    HttpHost targetHost = getTarget();

    // Create AuthCache instance
    AuthCache authCache = new BasicAuthCache();
    // Generate BASIC scheme object and add it to the local
    // auth cache
    BasicScheme basicAuth = new BasicScheme();
    authCache.put(targetHost, basicAuth);

    // Add AuthCache to the execution context
    BasicHttpContext localcontext = new BasicHttpContext();
    localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
    return localcontext;
  }

  private HttpHost getTarget() {
    if(target == null) {
      target = new HttpHost(host.getHostName(), host.getPort(), scheme);
    }
    return target;
  }

  private String getVersion() {
    return VERSION;
  }
}
