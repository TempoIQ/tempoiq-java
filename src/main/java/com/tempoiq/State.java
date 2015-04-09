package com.tempoiq;


/**
 *  The success state of a {@link Result}.
 *
 *  {@link Result}'s can complete successfully, completely fail, or partially succeed.
 *  @since 1.0.0
 */
public enum State {
  /**
   *  Request failed completely.
   *  @since 1.0.0
   */
  FAILURE,

  /**
   *  Request completed successfully.
   *  @since 1.0.0
   */
  SUCCESS,

  /** Request partially succeeded.
   *  Check the response type for more information.
   *  @since 1.0.0
   */
  PARTIAL_SUCCESS
}
