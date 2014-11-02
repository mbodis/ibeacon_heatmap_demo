package sk.svb.ibeacon.heatmap.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Heat map generation contains of comparison of all values from 1 second interval for each iBeacon. Visualization shows average of last second.
 * @author mbodis
 *
 */
public class MyBeaconCustom2 extends MyBeaconRaw{
	private static final String TAG = "MyBeaconCustom2";

	private static final int USE_MINS = 2;
	private static final int TIME_INTERVAL = 2000; // milis
	
	private static final int TIME = 1000;

	private List<TimePoint> timeList = new ArrayList<TimePoint>();

	public MyBeaconCustom2(int color, int number, String device,
			String deviceAddress, String uuid) {

		super(color, number, device, deviceAddress, uuid);
	}

	public MyBeaconCustom2(MyBeaconRaw raw) {

		super(raw.getColor(), raw.getNumber(), raw.getDevice(), raw
				.getDeviceAddress(), raw.getUUID());
	}

	private void removeOldValues(long now) {
		for (int i = 0; i < timeList.size(); i++) {
			if (now - timeList.get(i).time > TIME_INTERVAL
					&& timeList.size() > 1) {
				timeList.remove(i);
				i--;
			}
		}
	}

	@Override
	public double getAccuracy() {

		if (timeList.size() == 0)
			return -1;

		if (timeList.size() == 1)
			return timeList.get(0).value;

		long l = System.currentTimeMillis();
		removeOldValues(l);			

		double d = 0;
		int s = timeList.size();
		for (int i = 0; i < s; i++) {

			if (l - timeList.get(i).time< TIME) {
				d += timeList.get(i).value;				
			}
		}

		return d/s;
	}
	
	public List<TimePoint> getTimesLastSecond(long now){
				
		List<TimePoint> tl = new ArrayList<TimePoint>();
		
		for (TimePoint t : timeList) {
			if (now - t.time < TIME){
				tl.add(t);
			}
		}
		return tl;
	}

	@Override
	public void setAccuracy(double newAccuracy, long timeNow) {
		// Log.d(TAG, "addAccuracy MyBeaconClassMin");

		if (timeList == null) {
			return;
		}
		
		this.timeList.add(new TimePoint(newAccuracy, timeNow));
	}
}
