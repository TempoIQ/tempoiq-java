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
import org.joda.time.DateTimeZone;

import com.tempoiq.http.PageLinks;
import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;


public class MultiDataPointSegment extends Segment<MultiDataPoint> {

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private DateTimeZone timezone;
  private Rollup rollup;

  public MultiDataPointSegment() {
    this(new ArrayList<MultiDataPoint>(), "", DateTimeZone.UTC, null);
  }

  @JsonCreator
  public MultiDataPointSegment(@JsonProperty("data") List<MultiDataPoint> data, @JsonProperty("tz") DateTimeZone timezone, @JsonProperty("rollup") Rollup rollup) {
    this(data, null, timezone, rollup);
  }

  public MultiDataPointSegment(List<MultiDataPoint> data, String next, DateTimeZone timezone, Rollup rollup) {
    super(data, next);
    this.timezone = checkNotNull(timezone);
    this.rollup = rollup;
  }

  @JsonProperty("tz")
  public DateTimeZone getTimeZone() { return this.timezone; }
  public void setTimeZone(DateTimeZone timezone) { this.timezone = checkNotNull(timezone); }

  public Rollup getRollup() { return this.rollup; }
  public void setRollup(Rollup rollup) { this.rollup = rollup; }

  static MultiDataPointSegment make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    MultiDataPointSegment segment = Json.loads(body, MultiDataPointSegment.class);
    PageLinks links = new PageLinks(response);
    segment.next = links.getNext();
    return segment;
  }

  @Override
  public String toString() {
    return String.format("MultiDataPointSegment(data=%s, next=%s, timezone=%s, rollup=%s", data, next, timezone, rollup);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(11, 43)
      .append(data)
      .append(next)
      .append(timezone)
      .append(rollup)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(obj instanceof MultiDataPointSegment)) return false;

    MultiDataPointSegment rhs = (MultiDataPointSegment)obj;
    return new EqualsBuilder()
      .append(data, rhs.data)
      .append(next, rhs.next)
      .append(timezone, rhs.timezone)
      .append(rollup, rhs.rollup)
      .isEquals();
  }
}
