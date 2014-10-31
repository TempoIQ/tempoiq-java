package com.tempoiq;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.tempoiq.http.PageLinks;
import com.tempoiq.json.Json;
import static com.tempoiq.util.Preconditions.*;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorSegment extends Segment<Sensor> {

  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  public SensorSegment() {
    this(new ArrayList<Sensor>(), null);
  }

  public SensorSegment(List<Sensor> data) {
    super(data, null);
  }

  @JsonCreator
  public SensorSegment(@JsonProperty("data") List<Sensor> data,
                       @JsonProperty("next_page") PageLink next) {
    super(data, next);
  }

  static SensorSegment make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    return Json.loads(body, SensorSegment.class);
  }


}
