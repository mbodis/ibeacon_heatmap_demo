package sk.svb.ibeacon.heatmap.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * saves all values with corresponding time
 * 
 * @author mbodis
 *
 */
public class MyBeaconCustom2 extends MyBeaconRaw implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5118928179791512415L;

	transient private static final String TAG = "MyBeaconCustom2";

	private List<TimePoint> timeList = new ArrayList<TimePoint>();

	public MyBeaconCustom2(int color, int number, String device,
			String deviceAddress, String uuid) {

		super(color, number, device, deviceAddress, uuid);
	}

	public MyBeaconCustom2(MyBeaconRaw raw) {

		super(raw.getColor(), raw.getNumber(), raw.getDevice(), raw
				.getDeviceAddress(), raw.getUUID());
	}

	synchronized public List<TimePoint> getPointsInterval(long from, long to) {

		List<TimePoint> l = new ArrayList<TimePoint>();
		for (TimePoint tp : timeList) {
			if (tp.time >= from && tp.time < to) {
				l.add(tp);
			}
		}

		return l;
	}

	@Override
	synchronized public double getAccuracy() {

		if (timeList.size() == 0)
			return -1;

		// dosn't matter, the important part is generating elswere
		return timeList.get(timeList.size() - 1).value;
	}

	@Override
	synchronized public void setAccuracy(double newAccuracy, long timeNow) {
		// Log.d(TAG, "addAccuracy MyBeaconClassMin");

		if (timeList == null) {
			return;
		}

		this.timeList.add(new TimePoint(newAccuracy, timeNow));
	}

}
