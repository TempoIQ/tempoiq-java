package com.tempoiq;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpDeleteWithBody;
import org.apache.http.client.methods.HttpGetWithBody;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import static com.tempoiq.util.Preconditions.*;

public class Executor {

  private final Credentials credentials;
  private final InetSocketAddress host;
  private final String scheme;
  private HttpClient client = null;
  private HttpHost target = null;

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
  private static final int DEFAULT_TIMEOUT_MILLIS = 50000;  // 50 seconds
  private static final long DEFAULT_KEEPALIVE_TIMEOUT_MILLIS = 50000;  // 50 seconds
  private static final int GENERIC_ERROR_CODE = 600;
  private static final String VERSION = "1.0-SNAPSHOT";

  private enum HttpMethod { GET, POST, PUT, DELETE }

  public Executor(Credentials credentials, InetSocketAddress host, String scheme) {
    checkArgument(scheme.equals("http") || scheme.equals("https"), "Scheme must be either \"http\" or \"https\".");
    this.credentials = credentials;
    this.host = host;
    this.scheme = scheme;
  }

  public <T> Result<T> get(URI endpoint, Class<T> klass) {
    return get(endpoint, klass, "", new String[] {});
  }

  public <T> Result<T> get(URI endpoint, Class<T> klass, String contentType, String[] mediaTypes) {
    HttpRequest request = buildRequest(endpoint, HttpMethod.GET, null, contentType, mediaTypes);
    return execute(request, klass);
  }
  
  public <T> Result<T> get(URI endpoint, String body, Class<T> klass, String contentType) {
    return get(endpoint, body, klass, contentType, new String[] {});
  }

  public <T> Result<T> get(URI endpoint, String body, Class<T> klass, String contentType, String[] mediaTypes) {
    HttpRequest request = buildRequest(endpoint, HttpMethod.GET, body, contentType, mediaTypes);
    return execute(request, klass);
  }

  public <T> Result<T> post(URI endpoint, String body, Class<T> klass, String contentType) {
    return post(endpoint, body, klass, contentType, new String[] {});
  }

  public <T> Result<T> post(URI endpoint, String body, Class<T> klass, String contentType, String[] mediaTypes) {
    HttpRequest request = buildRequest(endpoint, HttpMethod.POST, body, contentType, mediaTypes);
    return execute(request, klass);
  }

  public <T> Result<T> put(URI endpoint, String body, String contentType, Class<T> klass) {
    return put(endpoint, body, klass, contentType, new String[] {});
  }

  public <T> Result<T> put(URI endpoint, String body, Class<T> klass, String contentType, String[] mediaTypes) {
    HttpRequest request = buildRequest(endpoint, HttpMethod.PUT, body, contentType, mediaTypes);
    return execute(request, klass);
  }

  public Result<DeleteSummary> delete(URI endpoint) {
    HttpRequest request = buildRequest(endpoint, HttpMethod.DELETE, null, "", new String[] {});
    return execute(request, DeleteSummary.class);
  }

  public Result<DeleteSummary> delete(URI endpoint, String body) {
    HttpRequest request = buildRequest(endpoint, HttpMethod.DELETE, body, "", new String[] {});
    return execute(request, DeleteSummary.class);
  }

  public Result<DeleteSummary> delete(URI endpoint, String body, String contentType, String[] mediaTypes) {
    HttpRequest request = buildRequest(endpoint, HttpMethod.DELETE, body, contentType, mediaTypes);
    return execute(request, DeleteSummary.class);
  }

  <T> Result<T> execute(HttpRequest request, Class<T> klass) {
    Result<T> result = null;
    try {
      HttpResponse response = executeRequest(request);
      result = new Result<T>(response, klass);
    } catch (IOException e) {
      result = new Result<T>(null, GENERIC_ERROR_CODE, e.getMessage());
    }
    return result;
  }

  HttpResponse executeRequest(HttpRequest request) throws IOException {
    HttpClient client = getHttpClient();
    HttpContext context = getContext();
    HttpHost target = getTarget();
    HttpResponse response = client.execute(target, request, context);
    return response;
  }

  HttpRequest buildRequest(URI uri, HttpMethod method, String body, String contentType, String[] mediaTypes) {
    String endpoint = uri.toString();
    HttpRequest request = null;
    switch(method) {
      case POST:
        HttpPost post = new HttpPost(endpoint);
        if(body != null) {
          post.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        }
        request = post;
        break;
      case PUT:
        HttpPut put = new HttpPut(endpoint);
        if(body != null) {
          put.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        }
        request = put;
        break;
      case DELETE:
        HttpDeleteWithBody delete = new HttpDeleteWithBody(endpoint);
        if(body != null) {
          delete.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        }
        request = delete;
        break;
      case GET:
      default:
        HttpGetWithBody get = new HttpGetWithBody(endpoint);
        if(body != null) {
          get.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        }
        request = get;
        break;
    }
    if (contentType != null && (! contentType.equals(""))) {
      request.setHeader("Content-Type", contentType);
    }
    if (mediaTypes != null && mediaTypes.length > 0) {
      request.setHeader("Accept", StringUtils.join(mediaTypes, ","));
    }
    return request;
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

  synchronized Executor setHttpClient(HttpClient httpClient) {
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

  private String getVersion() {
    return VERSION;
  }

  HttpHost getTarget() {
    if(target == null) {
      target = new HttpHost(host.getHostName(), host.getPort(), scheme);
    }
    return target;
  }

  void setTarget(HttpHost target) {
    this.target = target;
  }

  InetSocketAddress getHost() {
    return host;
  }

  String getScheme() {
    return scheme;
  }

  synchronized HttpClient getClient() {
    return client;
  }

  synchronized void setClient(HttpClient client) {
    this.client = client;
  }

  Credentials getCredentials() {
    return credentials;
  }
}
