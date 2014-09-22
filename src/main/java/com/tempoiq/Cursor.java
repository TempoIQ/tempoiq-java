package com.tempoiq;

/**
 *  An interface representing an {@link Iterable} of some other model object.
 *
 *  This is usually used for returning a sequence of {@link DataPoint}'s or {@link Sensor}.
 *  Note that the returned iterator is not cached, so iterating multiple times requires a
 *  network call.
 *
 *  <p>This iterator will throw a {@link TempoIQException} if an error occurs while retrieving the
 *  data.
 *  @since 1.0.0
 */
public interface Cursor<T> extends Iterable<T> { }
