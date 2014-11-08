package sk.svb.ibeacon.heatmap.service;

import java.util.ArrayList;
import java.util.List;

import sk.svb.ibeacon.heatmap.activity.MainActivity;
import sk.svb.ibeacon.heatmap.logic.CanvasHelp;
import sk.svb.ibeacon.heatmap.logic.HeatPoint;
import sk.svb.ibeacon.heatmap.logic.IBeaconHeatMap;
import sk.svb.ibeacon.heatmap.logic.MyBeaconCustom2;
import sk.svb.ibeacon.heatmap.logic.MyPointF;
import sk.svb.ibeacon.heatmap.logic.TimePoint;
import sk.svb.ibeacon.heatmap.support.MySupport;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

/**
 * what does HeatMapService do:<br>
 * <br>
 * receive a list of iBeacons (each with list of values and time)<br>
 * - generateHeatPointList<br>
 * - draw it on local canvas<br>
 * - save canvas bitmap as image<br>
 * - send URL of image back to the activity<br>
 * 
 * @author mbodis
 *
 */
public class HeatMapService extends IntentService {

	public static final String TAG = "HeatMapService";

	public static final String PROCESS_RESPONSE = "sk.svb.ibeacon.service.HeatMapService.PROCESS_RESPONSE";
	private static final int HEATMAP_GEN_TIME_INTERVAL = 1000; // 1 second

	private Bitmap mBitmap;
	private Canvas mCanvas;

	private List<HeatPoint> myHeatPointList = new ArrayList<HeatPoint>();

	public HeatMapService() {
		super("HeatMapService");

	}

	public HeatMapService(String name) {
		super(name);

	}

	private void initCanvas(int w, int h) {

		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mCanvas.drawARGB(255, 255, 255, 255);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent");

		if (intent != null) {
			Log.d(TAG, "onHandleIntent != null");

			CanvasHelp ch = (CanvasHelp) intent
					.getSerializableExtra("canvasHelper");

			Object[] list = (Object[]) intent
					.getSerializableExtra("ibeaconArray");

			List<MyBeaconCustom2> myBeacons = new ArrayList<MyBeaconCustom2>();
			for (int i = 0; i < list.length; i++) {
				myBeacons.add((MyBeaconCustom2) list[i]);
			}

			int method = intent.getIntExtra("method", -1);
			long startTime = intent.getLongExtra("startTime", -1);
			long endTime = intent.getLongExtra("endTime", -1);
			int w = intent.getIntExtra("width", -1);
			int h = intent.getIntExtra("height", -1);

			if (method == -1 || startTime == -1 || endTime == -1 || w == -1
					|| h == -1 || myBeacons == null || ch == null) {
				Log.d(TAG, "ERROR some intent shit is empty");
				return;
			}

			switch (method) {

			case MainActivity.METHOD_CUSTOM2:
				Log.d(TAG, "METHOD_CUSTOM2");
				initCanvas(w, h);
				generateHeatPointList(myBeacons, startTime, endTime, w, h, ch);
				IBeaconHeatMap.drawHeatPointOnCanvas(mCanvas, myHeatPointList);

				String filePath = MySupport.saveBitmapToFile(
						getApplicationContext(), mBitmap, "custom2.png");
				Log.d(TAG, "heatmap saved: " + filePath);
				sendResultIntent(filePath);
				break;

			}
		}
	}

	/**
	 * 
	 * @param myBeacons
	 *            list of received iBeacons
	 * @param timeStart
	 * @param timeEnd
	 * @param w
	 *            canvas.width
	 * @param h
	 *            canvas.height
	 * @param ch
	 *            canvas helper (positions of iBeacons, meter in pixels)
	 * 
	 *            generate heat map points (intersection with treshold) from
	 *            saved distances of iBeacons in time
	 */
	private void generateHeatPointList(List<MyBeaconCustom2> myBeacons,
			long timeStart, long timeEnd, int w, int h, CanvasHelp ch) {
		Log.d(TAG, "generateHeatPointList");

		int secInterval = (int) ((timeEnd - timeStart) / HEATMAP_GEN_TIME_INTERVAL) + 1;
		int halfInterval = secInterval * 2;

		for (int i = 0; i < halfInterval; i++) {

			long mFrom = timeStart + i * HEATMAP_GEN_TIME_INTERVAL / 2;
			long mEnd = mFrom + HEATMAP_GEN_TIME_INTERVAL;

			for (TimePoint tpR : (myBeacons.get(0)).getPointsInterval(mFrom,
					mEnd)) {

				for (TimePoint tpG : (myBeacons.get(1)).getPointsInterval(
						mFrom, mEnd)) {

					for (TimePoint tpB : (myBeacons.get(2)).getPointsInterval(
							mFrom, mEnd)) {

						for (TimePoint tpY : (myBeacons.get(3))
								.getPointsInterval(mFrom, mEnd)) {

							IBeaconHeatMap.addBeaconAccuracy(System
									.currentTimeMillis(), w, h, ch.meter,
									new MyPointF(ch.rx, ch.ry, tpR.value
											* ch.meter), new MyPointF(ch.gx,
											ch.gy, tpG.value * ch.meter),
									new MyPointF(ch.bx, ch.by, tpB.value
											* ch.meter), new MyPointF(ch.yx,
											ch.yy, tpY.value * ch.meter),
									myHeatPointList);
						}
					}
				}

			}

		}

	}

	private void sendResultIntent(String filePath) {
		Log.d(TAG, "sendResultIntent");

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PROCESS_RESPONSE);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra("filePath", filePath);
		sendBroadcast(broadcastIntent);
	}

}