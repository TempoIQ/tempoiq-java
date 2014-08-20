package com.tempoiq.analytics;

import java.util.Set;

import com.tempoiq.DataPoint;
import com.tempoiq.SelectorType;

/*
 *       ResultSet
 *			 |       
 *          \|/
 * 		Cursor
 * 
 * 
 * 
 * 
 * 
 */
public class ResultSet {
	
	public Stream first() {
		return this.getAllStreams().iterator().next();
	}
	
	public Stream fromDevice(String key) {
		for (Stream s : this.getAllStreams()) {
			if (s.getMetadata().getKey(SelectorType.DEVICE) == key) {
				return s;
			}
		}
		return null;
	}
	
	public Stream fromSensor(String key) {
		for (Stream s : this.getAllStreams()) {
			if (s.getMetadata().getKey(SelectorType.SENSOR) == key) {
				return s;
			}
		}
		return null;
	}
	
	public Set<Stream> getAllStreams() {
		return null;
	}
	
	public Cursor<Row> getRows() {
		return null;
	}
}
