package com.tempoiq;

import org.joda.time.DateTime;

import com.tempoiq.analytics.Pipeline;
import com.tempoiq.analytics.ResultSet;

public class Client {
	
	public Client() {};

	public ResultSet read(Selector selector, DateTime start, DateTime end) {
		return null;
	}
	
	public ResultSet read(Selector selector, Pipeline pipe, DateTime start, DateTime end) {
		return null;
	}
	
	public ResultSet readSensor(Sensor sensor, DateTime start, DateTime end) {
		return null;
	}
	
	public ResultSet readSensor(Sensor sensor, Pipeline pipe, DateTime start, DateTime end) {
		return null;
	}
	
	
	public Response latest(Selector selector, Pipeline pipe) {
		return null;
	}
}

