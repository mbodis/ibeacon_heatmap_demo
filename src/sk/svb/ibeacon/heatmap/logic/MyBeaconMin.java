package sk.svb.ibeacon.heatmap.logic;

import java.util.ArrayList;
import java.util.List;


/**
 * return average from USE_MINS minimal values in last (TIME_INTERVAL/2000) seconds
 * @author mbodis
 *
 */
public class MyBeaconMin extends MyBeaconRaw {

	private static final String TAG = "MyBeaconClassMin";

	private static final int USE_MINS = 2;
	private static final int TIME_INTERVAL = 2000; // milis
	
	private List<TimePoint> timeList = new ArrayList<TimePoint>();

	public MyBeaconMin(int color, int number, String device,
			String deviceAddress, String uuid) {		

		super(color, number, device, deviceAddress, uuid);
	}

	public MyBeaconMin(MyBeaconRaw raw) {

		super(raw.getColor(), raw.getNumber(), raw.getDevice(), raw
				.getDeviceAddress(), raw.getUUID());
	}

//	private double saveMinAndGetAverageDist() {
//
//		if (accList == null || accList.size() == 0) {
//			return -1;
//		}
//
//		if (accList.size() > USE_MINS) {
//			List<Double> minList = new ArrayList<Double>();
//
//			for (int i = 0; i < USE_MINS; i++) {
//				double min = 999;
//				int indx = -1;
//				for (int j = 0; j < accList.size(); j++) {
//					if (accList.get(j) < min) {
//						min = accList.get(j);
//						indx = j;
//					}
//				}
//				minList.add(accList.get(indx));
//				accList.remove(indx);
//			}
//			accList = minList;
//		}
//
//		double d = 0;
//		for (Double double1 : accList) {
//			d += double1;
//		}
//		return d / accList.size();
//	}

	private void removeOldValues(long now){				
		for (int i=0; i<timeList.size(); i++) {
			if (now - timeList.get(i).time <= TIME_INTERVAL && timeList.size()>1){
				timeList.remove(i);
				i--;
			}
		}
	}
	
	@Override
	public double getAccuracy() {		
		
		if (timeList.size() == 0 )
			return -1;
		
		if (timeList.size() == 1 )
			return timeList.get(0).value;
		
		
		double min1 = 999, min2 = 999;
		for (int i=0; i<timeList.size(); i++) {
			if (timeList.get(i).value < min1){
				min1 = timeList.get(0).value;
				
				if (min2>min1){
					double s = min2;
					min2 = min1;
					min1 = s;
				}
			}
		}
		
		return (min1 + min2) / 2;
	}

	@Override
	public void setAccuracy(double newAccuracy, long timeNow) {
		// Log.d(TAG, "addAccuracy MyBeaconClassMin");

		if (timeList == null) {
			return;
		}
	
		removeOldValues(timeNow);
		this.timeList.add(new TimePoint(newAccuracy, timeNow));
	}
		
}
