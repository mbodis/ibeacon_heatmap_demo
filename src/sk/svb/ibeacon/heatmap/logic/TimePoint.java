package sk.svb.ibeacon.heatmap.logic;

/**
 * point in time
 * @author mbodis
 *
 */
public class TimePoint {
	public double value = -1;
	public long time = -1;
	
	TimePoint(double v, long t){
		this.value = v;
		this.time = t;		
	}
}
