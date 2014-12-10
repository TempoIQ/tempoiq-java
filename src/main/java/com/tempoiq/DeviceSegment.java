package com.tempoiq;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tempoiq.json.Json;

public class DeviceSegment extends Segment<Device> {
  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  @JsonCreator
  public DeviceSegment(@JsonProperty("data") List<Device> devices) {
    super(devices, "");
  }

  public DeviceSegment(List<Device> devices, String nextPage) {
    super(devices, nextPage);
  }

  static DeviceSegment make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    DeviceSegment segment = Json.loads(body, DeviceSegment.class);
    return segment;
  }
}
