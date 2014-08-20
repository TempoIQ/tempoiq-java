package com.tempoiq.analytics;

import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

public class Row {
	public DateTime getTimestamp() {
		return null;
	}
	
	public Set<StreamMetadata> getStreams() {
		return null;
	}
	
	public Map<StreamMetadata, Number> getData() {
		return null;
	}
	
	public Number get(StreamMetadata stream) {
		return null;
	}
}
