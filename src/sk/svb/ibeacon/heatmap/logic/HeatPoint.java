package sk.svb.ibeacon.heatmap.logic;

import android.graphics.Point;

/**
 * heat point position on map
 * @author mbodis
 *
 */
public class HeatPoint extends Point {

	public static final int MIN_HEAT = 0;
	public static final int MAX_HEAT = 100;
	public static final int HEAT_UP = 5;

	private int heatLevel = MIN_HEAT;

	public HeatPoint(int x, int y) {
		super(x, y);		
	}

	public int getHeatLevel() {
		return heatLevel;
	}

	public void setHeatLevel(int heatLevel) {
		this.heatLevel = heatLevel;
	}
	
	public void increaseHeatLevel(){
		if (this.heatLevel<MAX_HEAT)
			this.heatLevel += HEAT_UP;	
	}

}
