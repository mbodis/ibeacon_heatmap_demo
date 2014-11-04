package sk.svb.ibeacon.heatmap.logic;

import android.util.Log;

/**
 * keep 30 last values
 * return average from last 30 values 
 * @author mbodis
 *
 */
public class MyBeaconAverage extends MyBeaconRaw {

	public static final int DISTANCE_LIST_SIZE = 30;
	public static final String TAG = "MyBeaconClassAverage";

	double last = EMPTY;

	public MyBeaconAverage(int color, int number, String device,
			String deviceAddress, String uuid) {

		super(color, number, device, deviceAddress, uuid);
	}

	public MyBeaconAverage(MyBeaconRaw raw) {
		super(raw.getColor(), raw.getNumber(), raw.getDevice(), raw
				.getDeviceAddress(), raw.getUUID());
	}

	@Override
	synchronized public double getAccuracy() {
		// Log.d(TAG, "MyBeaconClassAverage getAccuracy()");
		if (accList == null || accList.size() == 0) {
			return EMPTY;
		}
		
		while (this.accList.size() > DISTANCE_LIST_SIZE) {
			this.accList.remove(0);
		}

		double d = 0;
		for (Double double1 : accList) {
			d += double1;
		}

		double res = d / accList.size();
		 return res;		
	}

	@Override
	synchronized public void setAccuracy(double accuracy, long time) {
		// Log.d(TAG, "MyBeaconClassAverage setAccuracy() :" + accuracy);

		if (this.accList == null)
			return;
		
		this.accList.add(accuracy);
	}

}
