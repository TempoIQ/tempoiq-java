package com.tempoiq;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.*;

import static org.junit.Assert.*;

public class PageLoaderTest {

  private class IntegerPageLoader extends PageLoader<Integer> {
    private int innerIndex;
    private List<Segment<Integer>> segments;

    public IntegerPageLoader(List<Segment<Integer>> segments) {
      super();
      this.segments = segments;
      if (!segments.isEmpty()) {
        this.first = segments.get(0);
        this.current = this.first;
      }
      this.innerIndex = 0;
    }

    @Override
    public Segment<Integer> fetchNext() {
      if (innerIndex < segments.size()) {
        this.onDeck = segments.get(innerIndex);
        innerIndex ++;
      } else {
        onDeck = null;
      }
      return onDeck;
    }
  }

  private static Executor runner = new Executor(new Credentials("key", "secret"),
    new InetSocketAddress("some.backend.tempo-iq.com", 443), "https");

  @Test
  public void testNextLogic() {
    List<Segment<Integer>> segments = new ArrayList<Segment<Integer>>();
    for (Integer i = 0; i < 10; i++) {
      List<Integer> data = new ArrayList<Integer>();
      for (Integer j = 0; j < 10; j++) {
        data.add(j);
      }
      segments.add(new Segment<Integer>(data, String.format("%s", i)));
    }
    PageLoader<Integer> integerPageLoader = new IntegerPageLoader(segments);

    for (int k = 0; k < 10; k++) {
      assertNotNull(integerPageLoader.next());
    }
  }

  @Test
  public void testEmptiesGracefully() {
    List<Segment<Integer>> segments = new ArrayList<Segment<Integer>>();
    for (Integer i = 0; i < 10; i++) {
      List<Integer> data = new ArrayList<Integer>();
      for (Integer j = 0; j < 10; j++) {
        data.add(j);
      }
      segments.add(new Segment<Integer>(data, String.format("%s", i)));
    }
    PageLoader<Integer> integerPageLoader = new IntegerPageLoader(segments);

    for (int k = 0; k < 10; k++) {
      integerPageLoader.next();
    }
    assertFalse(integerPageLoader.hasNext());
  }

  @Test
  public void testHasNextLogic() {
    List<Segment<Integer>> segments = new ArrayList<Segment<Integer>>();
    for (Integer i = 0; i < 10; i++) {
      List<Integer> data = new ArrayList<Integer>();
      for (Integer j = 0; j < 10; j++) {
        data.add(j);
      }
      segments.add(new Segment<Integer>(data, String.format("%s", i)));
    }
    PageLoader<Integer> integerPageLoader = new IntegerPageLoader(segments);

    for (int k = 0; k < 10; k++) {
      assertTrue(integerPageLoader.hasNext());
      integerPageLoader.next();
    }
  }
}
