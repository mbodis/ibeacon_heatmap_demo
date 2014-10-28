package sk.svb.ibeacon.heatmap.logic;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.PointF;

/**
 * calculate intersection of ibeacon accuraties based on visual intersection of circles
 * saving heatPoints<br>
 * incerasing "heat" value of points<br>
 * calculate intersection of ibeacon accuraties<br>
 * 
 * tow circle intersection from<br>
 * http://mathworld.wolfram.com/Circle-CircleIntersection.html
 * 
 * @author mbodis
 *
 */
public class IBeaconHeatMap {

	private static final String TAG = "HeatingBacon";

	private static final double DISTANCE_TRASHOLD = 0.1; // 10 centimeters
	private static final long UPDATE_MIN_INTERVAL = 200; // miliseconds
	private static final int HEAT_AREA_CONST = 5; // pixels

	private List<HeatPoint> heatPointList;
	private long lastUpdate = 0;

	public IBeaconHeatMap() {
		heatPointList = new ArrayList<HeatPoint>();		
	}

	private void test() {
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < i; j++) {
				addHeatPoint(300, 500 + i * 10, 10000, 10000);
			}
		}
	}

	public List<HeatPoint> getHeatPointList() {
		return heatPointList;
	}

	public void setHeatPointList(List<HeatPoint> heatPointList) {
		this.heatPointList = heatPointList;
	}

	/**
	 * test if three accuracy distances are close enought, if yes add center of
	 * penetrations of these circles to heat map, else do nothing, wait for
	 * better accuracy from iBeacons
	 *
	 * @param now
	 *            time in milis timestamp of adding new accuracy
	 * @param canvas
	 *            canvas from activity, using his sizes
	 * @param meter
	 *            one meter in pixels
	 * @param redP
	 *            center of red iBeacon
	 * @param greenP
	 *            center of green iBeacon
	 * @param blueP
	 *            center of blue iBeacon
	 * @param radiusR
	 *            radius of red iBeacon (pixels)
	 * @param radiusG
	 *            radius of green iBeacon (pixels)
	 * @param radiusB
	 *            radius of blue iBeacon (pixels)
	 */
	public void addBeaconAccuracy(long now, Canvas canvas, float meter,
			PointF redP, PointF greenP, PointF blueP, double radiusR,
			double radiusG, double radiusB) {

		// don't use data from the same time
		if (lastUpdate == 0 || now - lastUpdate > UPDATE_MIN_INTERVAL) {

			// get penetration of circles
			List<PointF> penetrList = getPenetrationOfThreeCircles(redP,
					radiusR, greenP, radiusG, blueP, radiusB);
			if (penetrList.size() == 0)
				return;

			// are penetration points close enought?
			List<PointF> closePoints = threePointCloseEnought(penetrList, meter);
			if (closePoints.size() == 0)
				return;

			// find center
			PointF centerPoint = getCenterOfThreePoints(closePoints);
			if (centerPoint != null) {
				// add point to heatMap
				addHeatPoint((int) centerPoint.x, (int) centerPoint.y,
						canvas.getWidth(), canvas.getHeight());
			}

		}

	}

	private void addHeatPoint(int x, int y, int width, int height) {

		// find for heatpoint in some close area
		for (int xx = x - HEAT_AREA_CONST; xx < x + HEAT_AREA_CONST; xx++) {
			for (int yy = y - HEAT_AREA_CONST; yy < y + HEAT_AREA_CONST; yy++) {

				if (xx < 0 || xx > width || yy < 0 || yy > height)
					continue;

				boolean find = false;
				for (HeatPoint hp : heatPointList) {
					if (hp.x == xx && hp.y == yy) {
						hp.increaseHeatLevel();
						find = true;
					}
				}

				if (!find) {
					heatPointList.add(new HeatPoint(xx, yy));
				}
			}
		}

	}

	/**
	 * input 3 points and 3 assign radius<br>
	 * return 6 penetratiopn points these circles<br>
	 * else return null
	 * 
	 * @return 6 points
	 */
	public static List<PointF> getPenetrationOfThreeCircles(PointF red,
			double radiusR, PointF green, double radiusG, PointF blue,
			double radiusB) {

		List<PointF> result = new ArrayList<PointF>();

		result.addAll(intersectionOfTwoCircles(red, radiusR, green, radiusG));
		result.addAll(intersectionOfTwoCircles(blue, radiusB, red, radiusR));
		result.addAll(intersectionOfTwoCircles(green, radiusG, blue, radiusB));
//		result.addAll(intersectionOfTwoCircles(blue, radiusB, green, radiusG));

		return result;
	}

	/**
	 * input 6 points return 3 points if there are 3 close enought TEST TODO
	 */
	private List<PointF> threePointCloseEnought(List<PointF> points,
			double meter) {

		List<PointF> result = new ArrayList<PointF>();

		for (int i = 0; i < points.size(); i++) {
			for (int j = 0; j < points.size(); j++) {
				for (int k = 0; k < points.size(); k++) {
					if (i != j
							&& i != k
							&& j != k
							&& ((dist2Points(points.get(i), points.get(j)) / meter) <= DISTANCE_TRASHOLD)
							&& ((dist2Points(points.get(i), points.get(k)) / meter) <= DISTANCE_TRASHOLD)
							&& ((dist2Points(points.get(j), points.get(k)) / meter) <= DISTANCE_TRASHOLD)) {
						result.add(points.get(i));
						result.add(points.get(j));
						result.add(points.get(k));
						return result;
					}
				}
			}
		}
		return result;
	}

	/**
	 * input: 3 points return centre of triangle (made of these 3 points)
	 */
	private PointF getCenterOfThreePoints(List<PointF> points) {
		double x = 0;
		double y = 0;
		for (PointF point : points) {
			x += point.x;
			y += point.y;
		}
		x = x / points.size();
		y = y / points.size();
		return new PointF((int) x, (int) y);
	}

	public static double dist2Points(PointF p1, PointF p2) {
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
				* (p1.y - p2.y));
	}

	/**
	 * thanks to: http://mathworld.wolfram.com/Circle-CircleIntersection.html
	 */
	public static List<PointF> intersectionOfTwoCircles(PointF p1, double r1,
			PointF p2, double r2) {

		List<PointF> result = new ArrayList<PointF>();

		double dist = dist2Points(p1, p2);
		// Log.d(TAG, "dist2points: " + dist);

		// no penetration
		if (r1 + r2 < dist) {
			// Log.d(TAG, "1 no intersect");

			// no penetration
		} else if (Math.abs(r2 - r1) > dist) {
			// Log.d(TAG, "2 no intersect");

			// one point penetration
		} else if (r1 + r2 == dist) {
			// Log.d(TAG, "3 one point intersect");
			double vx = (p2.x - p1.x) * r1;
			double vy = (p2.y - p1.y) * r1;
			PointF pp = new PointF((float) (vx + p1.x), (float) (vy + p1.y));
			result.add(pp);
			result.add(pp);// because i'm expecting two point in next method

			// one point intersection
		} else if (Math.abs(r2 - r1) == dist) {
			// Log.d(TAG, "4 one point intersect");
			double vx = (p2.x - p1.x) * r1;
			double vy = (p2.y - p1.y) * r1;
			PointF pp = new PointF((float) (vx + p1.x), (float) (vy + p1.y));
			result.add(pp);
			result.add(pp);// because i'm expecting two point in next method

			// two points intersection
		} else {
			// Log.d(TAG, "5 tow points intersect");

			// unit vector (vector between two points divide distance)
			double jx = (double)(p2.x - p1.x) / dist;
			double jy = (double)(p2.y - p1.y) / dist;
						
			// math wolfram alpha
			// double d = dist;
			// double R = r1;
			// double r = r2;
			// double a = 1
			// / d
			// * Math.sqrt((-d + r - R) * (-d - r + R) * (-d + r + R)
			// * (d + r + R));
			// double a_half = a / 2;
			
			double a = (1 / dist * Math.sqrt((-dist + r2 - r1)
					* (-dist - r2 + r1) * (-dist + r2 + r1) * (dist + r2 + r1))) / 2;
			double vxe = -1 * (double)(p2.x - p1.x) / dist * a;
			double vye = (double)(p2.y - p1.y) / dist * a;
						
			double d1 = dist - (double)(dist*dist - r2*r2 + r1*r1  )/ (2*dist);					
			double ppx = (float)(p2.x - jx*d1);
			double ppy = (float)(p2.y - jy*d1);
			
			// debug axe of intersection (the intersection is perpendiculat to this)
			//result.add(new PointF((float)(p2.x - jx*d1), (float)(p2.y - jy*d1)));
						
			if (p1.x > p2.x) {
				result.add(new PointF((float) (ppx + vxe), (float) (ppy + vye)));
				result.add(new PointF((float) (ppx - vxe), (float) (ppy - vye)));				
				
			} else {
				result.add(new PointF((float) ppx, (float) (ppy - vxe)));
				result.add(new PointF((float) ppx, (float) (ppy + vxe)));
			}

		}

		return result;
	}

}
