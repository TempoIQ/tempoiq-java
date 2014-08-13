package com.tempoiq;

public class TempoIQException extends RuntimeException {
  private int code = 0;

  public TempoIQException() { super(); }

  public TempoIQException(String message, int code) {
    super(message);
    this.code = code;
  }

  public TempoIQException(String message, Throwable cause, int code) {
    super(message, cause);
    this.code = code;
  }
}
