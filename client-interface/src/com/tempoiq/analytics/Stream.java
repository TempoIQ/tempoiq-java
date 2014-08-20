package com.tempoiq.analytics;

import com.tempoiq.DataPoint;

public class Stream extends Cursor<DataPoint> {
	
	private StreamMetadata metadata;
	
	public StreamMetadata getMetadata() {
		return null;
	}
	
	/**
	 * When might this be null?
	 * @return
	 */
	public Cursor<DataPoint> getData() {
		return null;
	}

}
