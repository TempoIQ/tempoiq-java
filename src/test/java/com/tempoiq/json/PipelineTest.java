package com.tempoiq.json;

import java.io.IOException;

import org.junit.*;
import static org.junit.Assert.*;

import com.tempoiq.Pipeline;

public class PipelineTest {

  @Test
  public void testSerializeEmptyPipeline() throws IOException {
    Pipeline pipeline = new Pipeline();
    
    String expected = "{\"functions\":[]}";
    assertEquals(expected, Json.dumps(pipeline));
  }
}
