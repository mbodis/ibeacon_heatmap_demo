package sk.svb.ibeacon.heatmap.logic;

import java.util.ArrayList;
import java.util.List;

import sk.svb.ibeacon.heatmap.activity.ShowBeaconsActivity;
import sk.svb.ibeacon.heatmap.service.HeatMapService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

/**
 * calculate intersection of iBeacon accuracies based on visual intersection of
 * circles saving heatPoints<br>
 * increasing "heat" value of points<br>
 * calculate intersection of iBeacon accuracies<br>
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
	private static final int HEAT_AREA_CONST = 5; // pixels
	private static final int REDRAW_INTERVAL = 1500;
	private static final int FILTER_INTERVAL = 200;

	private Bitmap mBitmap;
	private Canvas mCanvas;
	long lastRedraw = 0;
	long lastUpdate = 0;

	private List<HeatPoint> heatPointList;

	public IBeaconHeatMap() {
		heatPointList = new ArrayList<HeatPoint>();
	}

	public void setCanvas(Canvas c) {
		mBitmap = Bitmap.createBitmap(c.getWidth(), c.getHeight(),
				Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mCanvas.drawARGB(255, 255, 255, 255);
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public List<HeatPoint> getHeatPointList() {
		return heatPointList;
	}

	public void setHeatPointList(List<HeatPoint> heatPointList) {
		this.heatPointList = heatPointList;
	}

	public void resetHeatMap() {
		heatPointList = new ArrayList<HeatPoint>();

	}

	public void doHeatmapRedraw() {
		if (System.currentTimeMillis() - lastRedraw > REDRAW_INTERVAL) {

			lastRedraw = System.currentTimeMillis();
			drawHeatPointOnCanvas(mCanvas, getHeatPointList());
		}
	}

	/**
	 * filtered addBeaconAccuracy method 
	 */
	public void addBeaconAccuracyFilter(long now, int w, int h, float meter,
			MyPointF redP, MyPointF greenP, MyPointF blueP, MyPointF yellowP,
			List<HeatPoint> heatPointList) {

		if (System.currentTimeMillis() - lastUpdate > FILTER_INTERVAL) {
			lastUpdate = System.currentTimeMillis();

			IBeaconHeatMap.addBeaconAccuracy(now, w, h, meter, redP, greenP,
					blueP, yellowP, heatPointList);
		}
	}

	/**
	 * 
	 * send saved values to intent to generate heat-map
	 */
	@SuppressWarnings("unchecked")
	public void custom2GenerateHeatmap(Context ctx, List<?> myBeacons,
			int method, long startTime, int width, int height, CanvasHelp ch) {
		Log.d(TAG, "custom2GenerateHeatmap intent");
		
		Intent intent = new Intent(ctx, HeatMapService.class);
		intent.putExtra("method", method);
		intent.putExtra("startTime", startTime);
		intent.putExtra("endTime", System.currentTimeMillis());
		intent.putExtra("width", width);
		intent.putExtra("height", height);
		intent.putExtra("ibeaconArray",
				((List<MyBeaconCustom2>) myBeacons).toArray());
		intent.putExtra("canvasHelper", ch);

		ctx.startService(intent);
		
	}

	/******************** STATIC METHODS *****************/

	/**
	 * test if three accuracy distances are close enough, if yes add center of
	 * penetrations of these circles to heat map, else do nothing, wait for
	 * better accuracy from iBeacons
	 *
	 * @param now
	 *            time in miliseconds time stamp of adding new accuracy
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
	public static void addBeaconAccuracy(long now, int w, int h, float meter,
			MyPointF redP, MyPointF greenP, MyPointF blueP, MyPointF yellowP,
			List<HeatPoint> heatPointList) {

		// get penetration of circles
		List<MyPointF> list = new ArrayList<MyPointF>();
		list.add(redP);
		list.add(greenP);
		list.add(blueP);
		list.add(yellowP);
		List<PointF> penetrList = getIntersectionOfThreeCircles(list);
		if (penetrList.size() == 0)
			return;

		// are penetration points close enough?
		List<PointF> closePoints = threePointCloseEnought(penetrList, meter);
		if (closePoints.size() == 0)
			return;

		// find center
		PointF centerPoint = getCenterOfThreePoints(closePoints);
		if (centerPoint != null) {
			// add point to heatMap
			addHeatPoint((int) centerPoint.x, (int) centerPoint.y, w, h,
					heatPointList);

		}

	}

	private static void addHeatPoint(int x, int y, int width, int height,
			List<HeatPoint> heatPointList) {

		// find for heat point in some close area
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

		// Log.d(TAG, "heatPointList SIZE: " + heatPointList.size());

	}

	/**
	 * input 3 points and 3 assign radius<br>
	 * return 6 penetration points these circles<br>
	 * else return null
	 * 
	 * @return 6 points
	 */
	public static List<PointF> getIntersectionOfThreeCircles(List<MyPointF> list) {

		List<PointF> result = new ArrayList<PointF>();

		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < list.size(); j++) {
				if (i != j) {
					result.addAll(intersectionOfTwoCircles(list.get(i),
							list.get(i).radius, list.get(j), list.get(j).radius));
				}
			}
		}

		return result;
	}

	/**
	 * input 6 points return 3 points if there are 3 close enough
	 */
	private static List<PointF> threePointCloseEnought(List<PointF> points,
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
	 * input: 3 points return center of triangle (made of these 3 points)
	 */
	private static PointF getCenterOfThreePoints(List<PointF> points) {
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
	public static List<PointF> intersectionOfTwoCircles(MyPointF p1, double r1,
			MyPointF p2, double r2) {

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
			// Log.d(TAG, "5 two points intersect");

			// roomH / roomW
			double ratio = Math.abs(p1.y - p2.y) / Math.abs(p1.x - p2.x);

			// unit vector (vector between two points divide distance)
			double jx = (double) (p2.x - p1.x) / dist;
			double jy = (double) (p2.y - p1.y) / dist;

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
			double d1 = dist - (double) (dist * dist - r2 * r2 + r1 * r1)
					/ (2 * dist);

			// debug axe of intersection (the intersection is perpendicular to
			// this)
			double ppx = (float) (p2.x - jx * d1);
			double ppy = (float) (p2.y - jy * d1);
			// result.add(new PointF((float)(p2.x - jx*d1), (float)(p2.y -
			// jy*d1)));

			if (p1.y > p2.y) {
				// perpendicular to jx, jy
				double X = (double) (p2.x - p1.x) / dist * a;
				double Y = -1 * (double) (p2.y - p1.y) / dist * a;

				result.add(new PointF((float) (ppx + X * ratio),
						(float) (ppy + Y / ratio)));
				result.add(new PointF((float) (ppx - X * ratio),
						(float) (ppy - Y / ratio)));

			} else if (p1.y < p2.y) {
				// perpendicular to jx, jy
				double X = (double) (p2.x - p1.x) / dist * a;
				double Y = -1 * (double) (p2.y - p1.y) / dist * a;

				result.add(new PointF((float) (ppx + X * ratio),
						(float) (ppy + Y / ratio)));
				result.add(new PointF((float) (ppx - X * ratio),
						(float) (ppy - Y / ratio)));

			} else {
				// perpendicular to jx, jy
				double X = -1 * (double) (p2.x - p1.x) / dist * a;

				result.add(new PointF((float) ppx, (float) (ppy - X)));
				result.add(new PointF((float) ppx, (float) (ppy + X)));
			}

		}

		return result;
	}

	public static void drawHeatPointOnCanvas(Canvas mCanvas,
			List<HeatPoint> heatPointList) {

		for (HeatPoint hp : heatPointList) {
			Paint p = new Paint();
			p.setARGB(ShowBeaconsActivity.NO_ALPHA, 255 / HeatPoint.MAX_HEAT
					* hp.getHeatLevel(), 0, 255 / HeatPoint.MAX_HEAT
					* HeatPoint.MAX_HEAT - hp.getHeatLevel());
			mCanvas.drawPoint(hp.x, hp.y, p);
		}

	}

}
