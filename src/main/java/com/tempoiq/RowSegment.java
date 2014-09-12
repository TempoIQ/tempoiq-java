package com.tempoiq;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.tempoiq.json.Json;

class RowSegment extends Segment<Row> {
  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  @JsonCreator
  public RowSegment(@JsonProperty("data") List<Row> rows) {
    super(rows, "");
  }

  static RowSegment make(HttpResponse response) throws IOException {
    String body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
    RowSegment segment = Json.loads(body, RowSegment.class);
    return segment;
  }
}
