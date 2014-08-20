import org.joda.time.DateTime;
import org.joda.time.Period;

import com.tempoiq.Client;
import com.tempoiq.DataPoint;
import com.tempoiq.Response;
import com.tempoiq.Selector;
import com.tempoiq.SelectorType;
import com.tempoiq.analytics.Grouping;
import com.tempoiq.analytics.Pipeline;
import com.tempoiq.analytics.PipelineCombine;
import com.tempoiq.analytics.ResultSet;
import com.tempoiq.analytics.Row;
import com.tempoiq.analytics.Stream;
import com.tempoiq.analytics.StreamMetadata;
import com.tempoiq.analytics.operations.AggregateOperations;
import com.tempoiq.analytics.operations.RollupFold;


public class Sandbox {

	Client clie = new Client();
	
	DateTime start = DateTime.now();
	DateTime end = DateTime.now();
	
	Selector sel = Selector.and(
			Selector.matchKey(SelectorType.SENSOR, "temperature"),
			Selector.matchAttribute(SelectorType.DEVICE, "user", "123"),
			Selector.matchAttribute(SelectorType.DEVICE, "type", "thermostat"));
	
	
	ResultSet resp = clie.read(sel, start, end);
	
	
	/*
	 * Example 1: systemic anomalies from unreliable sensors
	 */
	
	Selector sel1 = Selector.matchAttribute(SelectorType.DEVICE, "type", "uinvV1");
	
	Pipeline pipe1 = Pipeline.selectSensor("frequency")
			.inRange(58, 62)
			.not()
			.aggregate(AggregateOperations.SUM, Grouping.deviceAttribute("installation"))
			.gt(2);
			
	
	
	/*
	 * Example 2: 
	 */

	Pipeline pipe2 = PipelineCombine.and(Grouping.DEVICE,
			Pipeline.selectSensor("dcPower").gt(10),
			PipelineCombine.divide(Grouping.DEVICE,
					Pipeline.selectSensor("acPower"), 
					Pipeline.selectSensor("dcPower"))
					.lt(0.8)
			)
			.holdTrue(Period.minutes(30));
	
	
	/*******
	- Underperformance relative to other panels in installation:

	If a panel's AC power is 20% below the installation's average for 30 minutes, send an alert.
	*/
	
	Pipeline pipe3 = PipelineCombine.relativeDifference(Grouping.DEVICE,
			Pipeline.selectSensor("acPower"),
			Pipeline.selectSensor("acPower")
					.aggregate(AggregateOperations.MEAN, Grouping.deviceAttribute("installation"))
			)
			.holdTrue(Period.minutes(30));
	
	/* example 4: multi rollup for a sensor 
	 * 
	 */
	
	Pipeline pipe4 = Pipeline.selectSensor("temperature")
			.fanout(Pipeline.all().rollup(Period.days(1), RollupFold.MAX),	// { device: { key: "asdf", attributes: { "k": "v" } }, 
																			//   sensor: { key: "asdf", attributes: { "k": "v" } },
																			//   
					Pipeline.all().rollup(Period.days(1), RollupFold.MIN),
					Pipeline.all().rollup(Period.days(1), RollupFold.MEAN));
	
	/*
	 * Accessing returned data
	 */
	
	/*
	 * { device: { key: "dev1",
	 * 			   attributes: { "key1": "val1" } }
	 *   sensor: { key: "temp" }
	 *   extraContext: { rollup: "mean" } }
	 * 
	 */
	
	ResultSet res = clie.read(selection, start, end); // Stream of values from 2 sensors on a device

	// Iterating stream-first
	for (Stream str : res.getAllStreams()) {
		System.out.printf("Stream from device %s, sensor %s\n", 
				str.getMetadata().getKey(SelectorType.DEVICE), str.getMetadata().getKey(SelectorType.SENSOR));
		for (DataPoint dp : str) {
			System.out.printf("t: %s, v: %d\n", dp.getTimestamp(), dp.getValue());
		}
	}
	
	// Retreiving one sensor
	Stream voltage = res.fromSensor("voltage");
	for (DataPoint dp : voltage) {
		// blah blah
	}
	
	// Iterating row-first
	for (Row row : res.getRows()) {
		System.out.printf("t: %s\n");
		for (StreamMetadata stream : row.getStreams()) {
			System.out.printf("sensor: %s, value: %d\n", 
					stream.getKey(SelectorType.SENSOR), row.get(stream));
		}
	}

	
	
	Response res2 = null;	// Hourly power for all houses in a neighborhood (~10)
	

	
	
	
}
