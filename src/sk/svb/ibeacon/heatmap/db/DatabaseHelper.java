package sk.svb.ibeacon.heatmap.db;

import java.util.ArrayList;
import java.util.List;

import sk.svb.ibeacon.heatmap.activity.MainActivity;
import sk.svb.ibeacon.heatmap.logic.MyBeaconAverage;
import sk.svb.ibeacon.heatmap.logic.MyBeaconCustom;
import sk.svb.ibeacon.heatmap.logic.MyBeaconCustom2;
import sk.svb.ibeacon.heatmap.logic.MyBeaconMin;
import sk.svb.ibeacon.heatmap.logic.MyBeaconRaw;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;

/**
 * // TODO DO A DATABASE INSTEAD OF THIS preferences monster
 * 
 * @author mbodis
 *
 */
public class DatabaseHelper {

	public static final String TAG = "DatabaseHelper";

	public static final String PREFS = "my_prefs";

	public static final String BEACON_NUM = "num";
	public static final String BEACON_ADDR = "addr";
	public static final String BEACON_COLOR = "col";

	private static final int COUNT_BEACONS = 4;

	public static void saveBeacon(Context ctx, MyBeaconRaw mbc) {
		if (mbc == null)
			return;

		saveBeacon(ctx, mbc.getDeviceAddress(), mbc.getColor(), mbc.getNumber());
	}

	public static void saveBeacon(Context ctx, String btAddr, int color,
			int number) {

		Editor e = ctx.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
				.edit();
		e.putString(BEACON_ADDR + String.valueOf(number),
				String.valueOf(btAddr));
		switch (number) {
		case 1:
			e.putInt(BEACON_COLOR + String.valueOf(number), Color.RED);
			break;
		case 2:
			e.putInt(BEACON_COLOR + String.valueOf(number), Color.GREEN);
			break;
		case 3:
			e.putInt(BEACON_COLOR + String.valueOf(number), Color.BLUE);
			break;
		case 4:
			e.putInt(BEACON_COLOR + String.valueOf(number), Color.YELLOW);
			break;
		}

		e.putInt(BEACON_NUM + String.valueOf(number), number);
		e.commit();
	}

	public static MyBeaconRaw getSavedBeacon(Context ctx, int number) {

		MyBeaconRaw mbc = new MyBeaconRaw();

		SharedPreferences sp = (ctx.getSharedPreferences(PREFS,
				Activity.MODE_PRIVATE));
		if (!sp.contains(BEACON_NUM + number)) {
			return null;
		}

		mbc.setDeviceAddress(sp.getString(BEACON_ADDR + String.valueOf(number),
				null));
		mbc.setColor(sp.getInt(BEACON_COLOR + String.valueOf(number), -1));
		mbc.setNumber(sp.getInt(BEACON_NUM + String.valueOf(number), -1));

		if (mbc.getDeviceAddress() == null || mbc.getColor() == -1
				|| mbc.getNumber() == -1) {
			return null;
		}

		return mbc;
	}

	/*
	 * TODO this isn't very nice
	 */
	public static List<?> getSavedBeacons(Context ctx, int type) {

		if (type == MainActivity.METHOD_RAW) {
			List<MyBeaconRaw> list = new ArrayList<MyBeaconRaw>();

			for (int i = 1; i <= COUNT_BEACONS; i++) {
				MyBeaconRaw mbc = getSavedBeacon(ctx, i);
				if (mbc != null) {
					list.add(mbc);
				}
			}

			return list;
		} else if (type == MainActivity.METHOD_AVERAGE) {
			List<MyBeaconAverage> list = new ArrayList<MyBeaconAverage>();

			for (int i = 1; i <= COUNT_BEACONS; i++) {
				MyBeaconRaw m = getSavedBeacon(ctx, i);
				if (m != null) {
					MyBeaconAverage mbc = new MyBeaconAverage(m);
					list.add(mbc);
				}
			}

			return list;
		} else if (type == MainActivity.METHOD_MIN) {
			List<MyBeaconMin> list = new ArrayList<MyBeaconMin>();

			for (int i = 1; i <= COUNT_BEACONS; i++) {
				MyBeaconRaw m = getSavedBeacon(ctx, i);
				if (m != null) {
					MyBeaconMin mbc = new MyBeaconMin(m);
					list.add(mbc);
				}
			}

			return list;
		} else if (type == MainActivity.METHOD_CUSTOM) {
			List<MyBeaconCustom> list = new ArrayList<MyBeaconCustom>();

			for (int i = 1; i <= COUNT_BEACONS; i++) {
				MyBeaconRaw m = getSavedBeacon(ctx, i);
				if (m != null) {
					MyBeaconCustom mbc = new MyBeaconCustom(m);
					list.add(mbc);
				}
			}

			return list;
		} else if (type == MainActivity.METHOD_CUSTOM2) {
			List<MyBeaconCustom2> list = new ArrayList<MyBeaconCustom2>();

			for (int i = 1; i <= COUNT_BEACONS; i++) {
				MyBeaconRaw m = getSavedBeacon(ctx, i);
				if (m != null) {
					MyBeaconCustom2 mbc = new MyBeaconCustom2(m);
					list.add(mbc);
				}
			}

			return list;
		}

		return null;

	}

	public static boolean resetSavedBeacon(Context ctx) {

		SharedPreferences sp = (ctx.getSharedPreferences(PREFS,
				Activity.MODE_PRIVATE));
		Editor e = sp.edit();
		for (int i = 1; i <= COUNT_BEACONS; i++) {
			e.remove(BEACON_ADDR + String.valueOf(i));
			e.remove(BEACON_COLOR + String.valueOf(i));
			e.remove(BEACON_NUM + String.valueOf(i));
		}

		return e.commit();
	}
}
