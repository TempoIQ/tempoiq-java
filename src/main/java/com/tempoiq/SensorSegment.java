package com.tempoiq;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.tempoiq.http.PageLinks;
import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;


public class SensorSegment extends Segment<Sensor> {

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  public SensorSegment() {
    this(new ArrayList<Sensor>(), "");
  }

  @JsonCreator
  public SensorSegment(@JsonProperty("data") List<Sensor> data) {
    super(data, null);
  }

  public SensorSegment(List<Sensor> data, String next) {
    super(data, next);
  }

  static SensorSegment make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    SensorSegment segment = Json.loads(body, SensorSegment.class);
    PageLinks links = new PageLinks(response);
    segment.next = links.getNext();
    return segment;
  }

  @Override
  public String toString() {
    return String.format("SensorSegment(data=%s, next=%s)", data, next);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(127, 129)
      .append(data)
      .append(next)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof SensorSegment)) return false;

    SensorSegment rhs = (SensorSegment)obj;
    return new EqualsBuilder()
      .append(data, rhs.data)
      .append(next, rhs.next)
      .isEquals();
  }
}
