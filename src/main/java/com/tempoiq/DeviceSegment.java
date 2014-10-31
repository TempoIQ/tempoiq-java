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

  public DeviceSegment(@JsonProperty("data") List<Device> devices,
                      @JsonProperty("next_page")  PageLink nextPage) {
    super(devices, nextPage);
  }

  static DeviceSegment make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    return Json.loads(body, DeviceSegment.class);
  }
}
