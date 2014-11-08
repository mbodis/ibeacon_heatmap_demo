package sk.svb.ibeacon.heatmap.logic;

import java.io.Serializable;

/**
 * helper to save position of 4 iBeacons and meter 
 * @author mbodis
 *
 */
public class CanvasHelp implements Serializable {

	private static final long serialVersionUID = -5631408158219360819L;
	
	public float rx, ry, gx, gy, bx, by, yx, yy, meter;

	public CanvasHelp(float rx, float ry, float gx, float gy, float bx,
			float by, float yx, float yy, float meter) {
		this.rx = rx;
		this.ry = ry;

		this.gx = gx;
		this.gy = gy;

		this.bx = bx;
		this.by = by;

		this.yx = yx;
		this.yy = yy;

		this.meter = meter;
	}
}
