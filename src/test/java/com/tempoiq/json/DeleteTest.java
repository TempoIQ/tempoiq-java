package com.tempoiq.json;

        import com.tempoiq.Delete;
        import com.tempoiq.json.Json;
        import org.joda.time.DateTime;
        import org.joda.time.DateTimeZone;
        import org.junit.*;

        import java.io.IOException;

        import static org.junit.Assert.*;


public class DeleteTest {

  @Test
  public void testDeserializeUTC() throws IOException {
    DateTimeZone zone = DateTimeZone.UTC;
    Delete delete = Json.loads("{\"start\":\"2012-01-01T00:00:00.000Z\",\"stop\":\"2015-01-01T00:00:00.000Z\"}", Delete.class, zone);
    Delete expected = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0, zone), new DateTime(2015, 1, 1, 0, 0, 0, 0, zone));
    assertEquals(expected, delete);
  }

  @Test
  public void testDeserializeTZ() throws IOException {
    DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    Delete delete = Json.loads("{\"start\":\"2012-01-01T00:00:00.000-06:00\",\"stop\":\"2015-01-01T00:00:00.000-06:00\"}", Delete.class, zone);
    Delete expected = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0, zone), new DateTime(2015, 1, 1, 0, 0, 0, 0, zone));
    assertEquals(expected, delete);
  }
  @Test
  public void testSerializeUTC() throws IOException {
    DateTimeZone zone = DateTimeZone.UTC;
    Delete delete = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0, zone), new DateTime(2015, 1, 1, 0, 0, 0, 0, zone));
    String expected = "{\"start\":\"2012-01-01T00:00:00.000Z\",\"stop\":\"2015-01-01T00:00:00.000Z\"}";
    assertEquals(expected, Json.dumps(delete));
  }

  @Test
  public void testSerializeTZ() throws IOException {
    DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    Delete delete = new Delete(new DateTime(2012, 1, 1, 0, 0, 0, 0, zone), new DateTime(2015, 1, 1, 0, 0, 0, 0, zone));
    String expected = "{\"start\":\"2012-01-01T00:00:00.000-06:00\",\"stop\":\"2015-01-01T00:00:00.000-06:00\"}";

    assertEquals(expected, Json.dumps(delete));
  }
}
