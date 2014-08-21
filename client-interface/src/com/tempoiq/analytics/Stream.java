package com.tempoiq.analytics;

import com.tempoiq.DataPoint;

/**
 * Just a wrapper around Cursor<DataPoint> that provides
 * StreamMetadata so you know what the cursor contains.
 * 
 * Used when iterating through a resultSet by stream (as opposed to by row)
 * 
 *
 */
public class Stream extends Cursor<DataPoint> {
	
	private StreamMetadata metadata;
	
	public StreamMetadata getMetadata() {
		return null;
	}
	

}
