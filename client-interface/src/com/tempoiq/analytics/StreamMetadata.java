package com.tempoiq.analytics;

import com.tempoiq.SelectorType;

/*
 * Information that identifies a stream in a query or monitoring webhook. 
 * 
 * How to convey information other than sensor/device? 
 * Like attributes if a stream is from aggregating devices by an attribute?
 * Or rollup function if we've asked for multiple rollups on a sensor?
 */
public class StreamMetadata { 
	
	// Returns null if stream is the result of aggregating multiple sensors
	public Sensor getSensor() {
		return null;
	}
	
	// Returns null if stream is the result of aggregating multiple devices
	public Device getDevice() {
		return null;
	}
	
	/*
	public String getKey(SelectorType type) {
		return "";
	}
	
	public String getAttribute(SelectorType type, String key) {
		return "";
	}
	*/
	public String getExtraContext(String key) {
		return "";
	}
	
}
