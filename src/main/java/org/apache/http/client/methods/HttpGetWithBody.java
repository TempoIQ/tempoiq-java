package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class HttpGetWithBody extends HttpEntityEnclosingRequestBase {
  public static final String METHOD_NAME = "GET";
  public String getMethod() { return METHOD_NAME; }

  public HttpGetWithBody(final String uri) {
    super();
    setURI(URI.create(uri));
  }
  public HttpGetWithBody(final URI uri) {
    super();
    setURI(uri);
  }
  public HttpGetWithBody() { super(); }
}
