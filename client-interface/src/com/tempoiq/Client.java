package com.tempoiq;

import org.joda.time.DateTime;

import com.tempoiq.analytics.Pipeline;

public class Client {
	
	public Client() {};

	public Response read(Selector selector, DateTime start, DateTime end) {
		return null;
	};
	
	public Response read(Selector selector, Pipeline pipe, DateTime start, DateTime end) {
		return null;
	};
	
	public Response readSensor(Sensor sensor, DateTime start, DateTime end) {
		return null;
	};
	
	public Response readSensor(Sensor sensor, Pipeline pipe, DateTime start, DateTime end) {
		return null;
	};
	
	
	public Response latest(Selector selector, Pipeline pipe) {
		return null;
	};
}

