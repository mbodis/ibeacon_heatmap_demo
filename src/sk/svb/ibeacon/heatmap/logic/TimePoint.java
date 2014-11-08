package sk.svb.ibeacon.heatmap.logic;

import java.io.Serializable;

/**
 * point in time
 * 
 * @author mbodis
 *
 */
public class TimePoint implements Serializable {
	
	private static final long serialVersionUID = -3817960479911028772L;

	public double value = -1;
	public long time = -1;

	TimePoint(double v, long t) {
		this.value = v;
		this.time = t;
	}
}
