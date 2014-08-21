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
