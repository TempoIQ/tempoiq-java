package com.tempoiq;

import java.net.InetSocketAddress;

import org.junit.*;
import static org.junit.Assert.*;


public class ClientBuilderTest {

  private static final Database database = new Database("id");
  private static final Credentials credentials = new Credentials("key", "secret");

  @Test
  public void testDefaults() {
    Client client = new ClientBuilder()
                      .credentials(credentials)
                      .build();
    assertEquals("api.tempo-db.com", client.getHost().getHostName());
    assertEquals(443, client.getHost().getPort());
    assertEquals("https", client.getScheme());
  }
}
