package sk.svb.ibeacon.heatmap.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * trying other methods to get more precize distance from beacons 
 * @author mbodis
 *
 */
public class MyBeaconCustom extends MyBeaconRaw {

	private static final String TAG = "MyBeaconClassMin";

	private static final int USE_MINS = 2;
	private static final int TIME_INTERVAL = 1000;

	private List<Double> accList = new ArrayList<Double>();
	private long time = EMPTY;

	public MyBeaconCustom(int color, int number, String device,
			String deviceAddress, String uuid) {

		super(color, number, device, deviceAddress, uuid);
	}

	public MyBeaconCustom(MyBeaconRaw raw) {

		super(raw.getColor(), raw.getNumber(), raw.getDevice(), raw
				.getDeviceAddress(), raw.getUUID());
	}

	private double removeMaxAndGetAverageDist() {

		if (accList == null || accList.size() == 0) {
			return -1;
		}

		if (accList.size() > USE_MINS) {
			List<Double> minList = new ArrayList<Double>();

			for (int i = 0; i < USE_MINS; i++) {
				double min = 999;
				int indx = -1;
				for (int j = 0; j < accList.size(); j++) {
					if (accList.get(j) < min) {
						min = accList.get(j);
						indx = j;
					}
				}
				minList.add(accList.get(indx));
				accList.remove(indx);
			}
			accList = minList;
		}

		double d = 0;
		for (Double double1 : accList) {
			d += double1;
		}
		return d / accList.size();
	}

	@Override
	public double getAccuracy() {
		return this.getDistance();
	}

	@Override
	public void setAccuracy(double newAccuracy, long timeNow) {
		// Log.d(TAG, "addAccuracy MyBeaconClassMin");

		if (accList == null) {
			return;
		}

		if (time == EMPTY) {
			setDistance(newAccuracy);
			time = timeNow;
		} else {
			if (timeNow - time > TIME_INTERVAL) {
				time = timeNow;
				
				setDistance(usePreviousValue(removeMaxAndGetAverageDist()));
				this.accList.clear();
			} else {
				this.accList.add(newAccuracy);
			}
		}
	}
	
	private double usePreviousValue(double val){
		if (getDistance() == EMPTY){
			return val;
		}
		
		return (getDistance() + val) / 2;
		
	}
}

