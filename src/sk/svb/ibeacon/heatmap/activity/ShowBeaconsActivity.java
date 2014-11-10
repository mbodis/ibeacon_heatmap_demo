package sk.svb.ibeacon.heatmap.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sk.svb.ibeacon.heatmap.R;
import sk.svb.ibeacon.heatmap.db.DatabaseHelper;
import sk.svb.ibeacon.heatmap.logic.CanvasHelp;
import sk.svb.ibeacon.heatmap.logic.IBeaconHeatMap;
import sk.svb.ibeacon.heatmap.logic.MyBeaconRaw;
import sk.svb.ibeacon.heatmap.logic.MyIBeaconDevice;
import sk.svb.ibeacon.heatmap.logic.MyPointF;
import sk.svb.ibeacon.heatmap.service.HeatMapService;
import sk.svb.ibeacon.heatmap.support.Logger;
import sk.svb.ibeacon.heatmap.support.MySupport;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.util.IBeaconUtils;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

/**
 * the position of iBeacons is fixed: place your beacon as you see on screen (by
 * colors) draw beacon accuracies<br>
 * draw heat0map, based on your location to iBeacons<br>
 * if accuracies are close enough, a heat point is created - or heated up<br>
 * 
 * @author mbodis
 *
 */
@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ShowBeaconsActivity extends Activity {

	private static final String TAG = "ShowBeaconsActivity";

	public static final int ALPHA = 20;
	public static final int NO_ALPHA = 255;

	// layout, common
	private MySurfaceView mySurfaceView;
	float dn;
	private int method = -1;
	private boolean toggleLog = true;
	private boolean toggleIBeacons = true;
	private boolean toggleHeatMap = false;
	private boolean logIntoFile = false;
	private PowerManager.WakeLock wl;

	// iBeacon scan
	List<?> myBeacons;
	private BluetoothAdapter mBluetoothAdapter;
	private LeScanCallback mLeScanCallback;

	// canvas
	private boolean initCanvasSizes = false;
	private float margin; // canvas.height/20
	private float meter; // meter in pixels
	private int roomW, roomH;

	// canvas iBeacons
	private float radiusR = 0f;
	private float radiusG = 0f;
	private float radiusB = 0f;
	private float radiusY = 0f;
	float top, v, rx, ry, gx, gy, bx, by, yx, yy;

	// canvas colors
	private Paint pR, pG, pB, pY, pBL, psqBL;
	private Paint pRa, pGa, pBa, pYa, pBLa;

	// heatMap
	private boolean useHeatMap = false;
	private IBeaconHeatMap hmb;
	private Bitmap gradient;
	private long startTime = System.currentTimeMillis();
	private String movingIBeacon; // moving iBeacons on canvas
	private boolean updateNewValues = true;

	// custom generation heatMap
	private boolean customGenerationHeatMapStarted = false;
	private Bitmap generatedHeatMapFromService;
	private boolean generatingHeatmap = false;
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		hmb = new IBeaconHeatMap();

		// wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);

		// remove all previous logs
		if (Logger.removeAllLogs(getApplicationContext())) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.toast_remove_logs), Toast.LENGTH_SHORT)
					.show();
		}

		if (getIntent() != null) {
			method = getIntent().getIntExtra("method", -1);
			roomW = getIntent().getIntExtra("r_width", -1);
			roomH = getIntent().getIntExtra("r_height", -1);
			Log.d(TAG, "selected method: " + method);
		}
		if (method == -1 || roomW == -1 || roomH == -1) {
			Toast.makeText(getApplicationContext(), "No method selected ERR",
					Toast.LENGTH_SHORT).show();
			finish();
		}
		initBtLe();
		initColors();
		mySurfaceView = new MySurfaceView(this);

		// drag & drop iBeacons
		mySurfaceView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (movingIBeacon == null) {
						if (Math.abs(event.getX() - rx) < 30
								&& Math.abs(event.getY() - ry) < 30) {
							vibrate();
							movingIBeacon = "red";

						} else if (Math.abs(event.getX() - gx) < 30
								&& Math.abs(event.getY() - gy) < 30) {
							movingIBeacon = "green";
							vibrate();

						} else if (Math.abs(event.getX() - bx) < 30
								&& Math.abs(event.getY() - by) < 30) {
							movingIBeacon = "blue";
							vibrate();
						} else if (Math.abs(event.getX() - yx) < 30
								&& Math.abs(event.getY() - yy) < 30) {
							movingIBeacon = "yellow";
							vibrate();
						}
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (movingIBeacon != null && movingIBeacon.equals("red")) {
						rx = event.getX();
						ry = event.getY();
					} else if (movingIBeacon != null
							&& movingIBeacon.equals("green")) {
						gx = event.getX();
						gy = event.getY();
					} else if (movingIBeacon != null
							&& movingIBeacon.equals("blue")) {
						bx = event.getX();
						by = event.getY();
					} else if (movingIBeacon != null
							&& movingIBeacon.equals("yellow")) {
						yx = event.getX();
						yy = event.getY();
					}
					break;
				case MotionEvent.ACTION_UP:
					if (movingIBeacon != null && movingIBeacon.equals("red")) {
						rx = event.getX();
						ry = event.getY();
						movingIBeacon = null;
						vibrate();
					} else if (movingIBeacon != null
							&& movingIBeacon.equals("green")) {
						gx = event.getX();
						gy = event.getY();
						movingIBeacon = null;
						vibrate();
					} else if (movingIBeacon != null
							&& movingIBeacon.equals("blue")) {
						bx = event.getX();
						by = event.getY();
						movingIBeacon = null;
						vibrate();
					} else if (movingIBeacon != null
							&& movingIBeacon.equals("yellow")) {
						yx = event.getX();
						yy = event.getY();
						movingIBeacon = null;
						vibrate();
					}
					break;
				}
				return true;
			}
		});
		setContentView(mySurfaceView);

		gradient = BitmapFactory.decodeResource(getResources(),
				R.drawable.heat_map);

		gradient = MySupport.getResizedBitmap(gradient, (int) (20 * dn),
				(int) (180 * dn));

		initReceiver();
	}

	@Override
	protected void onPause() {
		wl.release();
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
		super.onPause();
		mySurfaceView.onPause();

	}

	@Override
	protected void onResume() {
		wl.acquire();
		mBluetoothAdapter.startLeScan(mLeScanCallback);
		super.onResume();
		mySurfaceView.onResume();

	}

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void initReceiver() {
		IntentFilter filter = new IntentFilter(HeatMapService.PROCESS_RESPONSE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "onReceive - message from intentService");
				
				if (intent != null) {
					String path = intent.getStringExtra("filePath");
					if (path == null) {
						Log.d(TAG, "empty content");
						return;

					} else {
						generatingHeatmap = false;

						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inPreferredConfig = Bitmap.Config.ARGB_8888;
						generatedHeatMapFromService = BitmapFactory.decodeFile(
								path, options);						
						setContentView(mySurfaceView);
						Log.d(TAG, "intent updated bitmap");
					}

				}

			}

		};
		registerReceiver(receiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.ibeacon_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_toggle_log) {
			toggleLog = !toggleLog;
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.toast_debug_state)
							+ (toggleLog ? getString(R.string.on)
									: getString(R.string.off)),
					Toast.LENGTH_SHORT).show();
			return true;
		} else if (id == R.id.action_toggle_heatmap) {
			hmb.toggleHeatMap();
			toggleHeatMap = !toggleHeatMap;
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.toast_heatmap_state)
							+ (toggleHeatMap ? getString(R.string.on)
									: getString(R.string.off)),
					Toast.LENGTH_SHORT).show();
			return true;
		} else if (id == R.id.action_toggle_ibeacons) {
			toggleIBeacons = !toggleIBeacons;
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.toast_ibeacons_state)
							+ (toggleIBeacons ? getString(R.string.on)
									: getString(R.string.off)),
					Toast.LENGTH_SHORT).show();
			return true;
		} else if (id == R.id.action_reset_heatmap) {
			hmb.resetHeatMap();
			Toast.makeText(getApplicationContext(),
					getString(R.string.toast_reset_heatmap), Toast.LENGTH_SHORT)
					.show();
			return true;
		} else if (id == R.id.action_log_to_file) {
			logIntoFile = true;
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.toast_log_to_file)
							+ Logger.getFolderName(getApplicationContext()),
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * btLe scan runs all the time activity runs
	 */
	private void initBtLe() {
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		myBeacons = DatabaseHelper.getSavedBeacons(getApplicationContext(),
				method);

		useHeatMap = (myBeacons.size() >= 3);

		mLeScanCallback = new LeScanCallback() {

			@Override
			public void onLeScan(final BluetoothDevice device, final int rssi,
					final byte[] scanRecord) {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (updateNewValues) {

							final BluetoothLeDevice deviceLe = new BluetoothLeDevice(
									device, rssi, scanRecord,
									System.currentTimeMillis());

							if (IBeaconUtils.isThisAnIBeacon(deviceLe)) {
								MyIBeaconDevice ibeacon = new MyIBeaconDevice(
										deviceLe);

								for (int i = 0; i < myBeacons.size(); i++) {

									// match beacon
									if (((MyBeaconRaw) myBeacons.get(i))
											.getDeviceAddress().equals(
													ibeacon.getAddress())) {

										// update accuracy
										((MyBeaconRaw) myBeacons.get(i))
												.setAccuracy(ibeacon
														.getAccuracy(), System
														.currentTimeMillis());
									}
								}

							}
						}

					}
				});

			}
		};
	}

	/**
	 * logging beacon accuraties to file, for better visualization
	 */
	private void log_beacon(double acc, String filename) {
		String secNow = new SimpleDateFormat("ss", Locale.US).format(new Date(
				System.currentTimeMillis()));
		Logger.addLog(getApplicationContext(), filename, "," + secNow + ","
				+ acc);
	}

	private void initColors() {
		dn = getResources().getDisplayMetrics().density;

		pR = new Paint();
		pR.setColor(Color.RED);
		pR.setTextSize(10 * dn);
		pG = new Paint();
		pG.setColor(Color.GREEN);
		pG.setTextSize(10 * dn);
		pB = new Paint();
		pB.setColor(Color.BLUE);
		pB.setTextSize(10 * dn);
		pBL = new Paint();
		pBL.setColor(Color.BLACK);
		pBL.setTextSize(10 * dn);
		pY = new Paint();
		pY.setColor(Color.YELLOW);
		pY.setTextSize(10 * dn);

		psqBL = new Paint();
		psqBL.setColor(Color.BLACK);
		psqBL.setStyle(Style.STROKE);

		pRa = new Paint();
		pRa.setColor(Color.RED);
		pRa.setAlpha(ALPHA);
		pGa = new Paint();
		pGa.setColor(Color.GREEN);
		pGa.setAlpha(ALPHA);
		pBa = new Paint();
		pBa.setColor(Color.BLUE);
		pBa.setAlpha(ALPHA);
		pYa = new Paint();
		pYa.setColor(Color.YELLOW);
		pYa.setAlpha(ALPHA);
		pBLa = new Paint();
		pBLa.setColor(Color.BLACK);
		pBLa.setAlpha(ALPHA);
	}

	public class MySurfaceView extends SurfaceView implements Runnable {

		private Thread t = null;
		private volatile boolean isAlive = false; // forbidden to save in cache
		private SurfaceHolder holder = null;

		public MySurfaceView(Context context) {
			super(context);

			holder = getHolder();

		}

		@Override
		public void run() {

			while (!t.isInterrupted()) {

				if (!holder.getSurface().isValid()) {
					continue;
				}

				if (!generatingHeatmap) {
					Canvas c = holder.lockCanvas();
					myDraw(c);
					holder.unlockCanvasAndPost(c);
				}

			}
		}

		public void onPause() {

			try {
				t.interrupt();
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			t = null;
		}

		public void onResume() {

			t = new Thread(this);
			t.start();
		}

		/**
		 * draw beacon accuracies<br>
		 * draw heat-map, based on your location to iBeacons<br>
		 * if accuracies are close enough, add a heat point
		 */
		protected void myDraw(Canvas canvas) {

			if (generatingHeatmap) {
				return;
			} else if (generatedHeatMapFromService != null) {
				canvas.drawBitmap(generatedHeatMapFromService, 0, 0, null);
				return;
			}

			if (!initCanvasSizes) {
				initCanvas(canvas);
			} else {
				canvas.drawBitmap(hmb.getBitmap(), 0, 0, null);
			}

			updateRadius();

			canvas.drawRect(margin, margin, margin + roomW * meter, margin
					+ roomH * meter, psqBL);
			canvas.drawCircle(rx, ry, 4, pR);
			canvas.drawCircle(gx, gy, 4, pG);
			canvas.drawCircle(bx, by, 4, pB);
			canvas.drawCircle(yx, yy, 4, pY);

			// show accuracies from iBeacons
			if (toggleIBeacons) {
				canvas.drawCircle(rx, ry, radiusR * meter, pRa);
				canvas.drawCircle(gx, gy, radiusG * meter, pGa);
				canvas.drawCircle(bx, by, radiusB * meter, pBa);
				canvas.drawCircle(yx, yy, radiusY * meter, pYa);
			}

			// if you have set 3 beacons we can use heatMap
			if (useHeatMap) {

				// METHOD_CUSTOM2 is too expensive
				if (method != MainActivity.METHOD_CUSTOM2) {
					hmb.addBeaconAccuracyFilter(System.currentTimeMillis(),
							canvas.getWidth(), canvas.getHeight(), meter,
							new MyPointF(rx, ry, radiusR * meter),
							new MyPointF(gx, gy, radiusG * meter),
							new MyPointF(bx, by, radiusB * meter),
							new MyPointF(yx, yy, radiusY * meter),
							hmb.getHeatPointList());
				}
				
				// drawing heat map
				if (toggleHeatMap){
					hmb.doHeatmapRedraw();					
				}
				
			}						

			// draw logs, heat-map at bottom, circle penetration
			if (toggleLog) {
				drawLogs(canvas);
				drawCircleIntersection(canvas);
			}

			// custom generation
			if (!customGenerationHeatMapStarted
					&& method == MainActivity.METHOD_CUSTOM2
					&& (System.currentTimeMillis() - startTime) / 1000 == MainActivity.METHOD_CUSTOM2_TIME_AGGREGATION) {
				customGenerationHeatMapStarted = true;
				updateNewValues = false;
				unsetRadiuses();
				mBluetoothAdapter.stopLeScan(mLeScanCallback);

				CanvasHelp ch = new CanvasHelp(rx, ry, gx, gy, bx, by, yx, yy,
						meter);
				hmb.custom2GenerateHeatmap(getApplicationContext(), myBeacons,
						MainActivity.METHOD_CUSTOM2, startTime,
						canvas.getWidth(), canvas.getHeight(), ch);
				generatingHeatmap = true;

				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						setContentView(R.layout.generating_heatmap_view);

					}
				});

			}

		}

	}

	private void unsetRadiuses() {
		radiusR = 0f;
		radiusG = 0f;
		radiusB = 0f;
		radiusY = 0f;
	}

	private void vibrate() {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(80);
	}

	private void updateRadius() {
		for (int i = 0; i < myBeacons.size(); i++) {
			switch (((MyBeaconRaw) myBeacons.get(i)).getNumber()) {
			case 1:
				radiusR = (float) ((MyBeaconRaw) myBeacons.get(i))
						.getAccuracy();
				if (logIntoFile) {
					log_beacon(((MyBeaconRaw) myBeacons.get(i)).getAccuracy(),
							Logger.IBEACON_R);
				}
				break;
			case 2:
				radiusG = (float) ((MyBeaconRaw) myBeacons.get(i))
						.getAccuracy();
				if (logIntoFile) {
					log_beacon(((MyBeaconRaw) myBeacons.get(i)).getAccuracy(),
							Logger.IBEACON_G);
				}
				break;
			case 3:
				radiusB = (float) ((MyBeaconRaw) myBeacons.get(i))
						.getAccuracy();
				if (logIntoFile) {
					log_beacon(((MyBeaconRaw) myBeacons.get(i)).getAccuracy(),
							Logger.IBEACON_B);
				}
				break;
			case 4:
				radiusY = (float) ((MyBeaconRaw) myBeacons.get(i))
						.getAccuracy();
				if (logIntoFile) {
					log_beacon(((MyBeaconRaw) myBeacons.get(i)).getAccuracy(),
							Logger.IBEACON_Y);
				}
				break;
			}
		}
	}

	/**
	 * debug mode, draw intersection of circles
	 */
	private void drawCircleIntersection(Canvas canvas) {

		// testing intersection all 3 circles
		List<MyPointF> m = new ArrayList<MyPointF>();
		m.add(new MyPointF(rx, ry, radiusR * meter));
		m.add(new MyPointF(gx, gy, radiusG * meter));
		m.add(new MyPointF(bx, by, radiusB * meter));
		m.add(new MyPointF(yx, yy, radiusY * meter));
		List<PointF> penetrList = IBeaconHeatMap
				.getIntersectionOfThreeCircles(m);

		for (PointF point : penetrList) {
			canvas.drawCircle(point.x, point.y, 4, pBL);
		}
	}

	/**
	 * initialize canvas variables
	 */
	private void initCanvas(Canvas canvas) {
		initCanvasSizes = true;

		hmb.setCanvas(canvas);
		margin = canvas.getHeight() / 20;

		// use canvas.width as main parameter
		if ((canvas.getHeight() - 2 * margin) / roomH * roomW + 2 * margin > canvas
				.getWidth()) {
			meter = (canvas.getWidth() - 2 * margin) / roomW;

			// use canva.height as main parameter
		} else {
			meter = (canvas.getHeight() - 2 * margin) / roomH;
		}

		// red
		rx = (int) (margin + (double) roomW / 2 * meter);
		ry = margin;
		// green
		gx = margin;
		gy = (int) (margin + (double) roomH / 2 * meter);
		// blue
		bx = margin + roomW * meter;
		by = (int) (margin + (double) roomH / 2 * meter);
		// yellow
		yx = (int) (margin + (double) roomW / 2 * meter);
		yy = (int) (margin + (double) roomH * meter);

	}

	/**
	 * draw logs:<br>
	 * - radius in meters of red, green, blue iBeacon<br>
	 * - pixel representation 1 meter<br>
	 * - heat-map scale<br>
	 */
	private void drawLogs(Canvas canvas) {
		if (!customGenerationHeatMapStarted
				&& method == MainActivity.METHOD_CUSTOM2) {
			canvas.drawText(
					getString(R.string.heatmap_generation_start)
							+ ": "
							+ (MainActivity.METHOD_CUSTOM2_TIME_AGGREGATION - ((System
									.currentTimeMillis() - startTime) / 1000)),
					10 * dn, 10 * dn, pBL);
		}
		canvas.drawText("red: " + radiusR + " m", 10 * dn, 20 * dn, pR);
		canvas.drawText("green: " + radiusG + " m", 10 * dn, 30 * dn, pG);
		canvas.drawText("blue: " + radiusB + " m", 10 * dn, 40 * dn, pB);
		canvas.drawText("yellow: " + radiusY + " m", 10 * dn, 50 * dn, pY);
		canvas.drawText("1 meter", 10 * dn, 75 * dn, pBL);
		canvas.drawLine(10, 80 * dn, meter + 10, 80 * dn, pBL);
		canvas.drawLine(10, 75 * dn, 10, 85 * dn, pBL);
		canvas.drawLine(meter + 10, 75 * dn, meter + 10, 85 * dn, pBL);
		canvas.drawBitmap(gradient, 10,
				canvas.getHeight() - gradient.getHeight() - 10, null);
	}

}
