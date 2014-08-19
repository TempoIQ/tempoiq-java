import org.joda.time.DateTime;
import org.joda.time.Period;

import com.tempoiq.Client;
import com.tempoiq.Response;
import com.tempoiq.Selector;
import com.tempoiq.SelectorType;
import com.tempoiq.analytics.Grouping;
import com.tempoiq.analytics.Pipeline;
import com.tempoiq.analytics.PipelineCombine;
import com.tempoiq.analytics.operations.AggregateOperation;


public class Sandbox {

	Client clie = new Client();
	
	DateTime start = DateTime.now();
	DateTime end = DateTime.now();
	
	Selector sel = Selector.and(
			Selector.matchKey(SelectorType.SENSOR, "temperature"),
			Selector.matchAttribute(SelectorType.DEVICE, "user", "123"),
			Selector.matchAttribute(SelectorType.DEVICE, "type", "thermostat"));
	
	
	Response resp = clie.read(sel, start, end);
	
	
	/*
	 * Example 1: systemic anomalies from unreliable sensors
	 */
	
	Selector sel1 = Selector.matchAttribute(SelectorType.DEVICE, "type", "uinvV1");
	
	Pipeline pipe1 = Pipeline.selectSensor("frequency")
			.inRange(58, 62)
			.not()
			.aggregate(AggregateOperation.SUM, Grouping.deviceAttribute("installation"))
			.gt(2);
			
	
	
	/*
	 * Example 2: 
	 */

	Pipeline pipe2 = PipelineCombine.and(
			Pipeline.selectSensor("dcPower").gt(10),
			PipelineCombine.divide(Pipeline.selectSensor("acPower"), 
					Pipeline.selectSensor("dcPower"))
					.lt(0.8)
			)
			.holdTrue(Period.minutes(30));
	
	
	/*******
	- Underperformance relative to other panels in installation:

	If a panel's AC power is 20% below the installation's average for 30 minutes, send an alert.
	*/
	
	Pipeline pipe3 = PipelineCombine.relativeDifference(Pipeline.selectSensor("acPower"),
			Pipeline.selectSensor("acPower")
					.aggregate(AggregateOperation.MEAN, Grouping.deviceAttribute("installation"))
			)
			.holdTrue(Period.minutes(30));
	
}
