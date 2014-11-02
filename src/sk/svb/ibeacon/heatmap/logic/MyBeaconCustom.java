package sk.svb.ibeacon.heatmap.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Averaged over last two values of the last 15 values. Maximum change from the previous position is limited to 0.1 meter. 
 * @author mbodis
 *
 */
public class MyBeaconCustom extends MyBeaconRaw {

	private static final String TAG = "MyBeaconClassMin";

	
	public static final int DISTANCE_LIST_SIZE = 15;
	public static final double DISTANCE_CHANGE_LIMIT= 0.1;

	private List<Double> accList = new ArrayList<Double>();	
	private double lastValue = EMPTY;

	public MyBeaconCustom(int color, int number, String device,
			String deviceAddress, String uuid) {

		super(color, number, device, deviceAddress, uuid);
	}

	public MyBeaconCustom(MyBeaconRaw raw) {

		super(raw.getColor(), raw.getNumber(), raw.getDevice(), raw
				.getDeviceAddress(), raw.getUUID());
	}

	@Override
	public double getAccuracy() {
		if (accList.size() == 0 )
			return -1;
		
		if (accList.size() == 1 )
			return accList.get(0);
		
		while (this.accList.size() > DISTANCE_LIST_SIZE) {
			this.accList.remove(0);
		}
				
		double min1 = 999, min2 = 999;
		
		for (int i=0; i<accList.size(); i++) {
			
			if (accList.get(i) < min1){
				min1 = accList.get(i);
				
				if (min2>min1){
					double s = min2;
					min2 = min1;
					min1 = s;
				}
			}
		}
		
		double newValue = (min1 + min2) / 2;
		double oldValue = lastValue; 
		lastValue = newValue;
		
		if (lastValue == EMPTY){
			return newValue;
		}
		
		if (Math.abs(newValue - oldValue) > DISTANCE_CHANGE_LIMIT){
			newValue = (newValue - oldValue > 0 ) ? (oldValue+DISTANCE_CHANGE_LIMIT) : (oldValue+DISTANCE_CHANGE_LIMIT);
		}
		
		return newValue; 	 	
	}

	@Override
	public void setAccuracy(double accuracy, long time) {
		// Log.d(TAG, "MyBeaconClassAverage setAccuracy() :" + accuracy);

		if (this.accList == null)
			return;
		
		this.accList.add(accuracy);
	}
}

