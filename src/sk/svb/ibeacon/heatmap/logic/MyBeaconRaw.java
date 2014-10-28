package sk.svb.ibeacon.heatmap.logic;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * saves only one accuracy value
 * @author mbodis
 *
 */
public class MyBeaconRaw {

	public static final int EMPTY = -1;
	public static final String TAG = "MyBeaconClassRaw";

	private int color = -1;
	private int number;
	private String device;
	private String deviceAddress;
	private String uuid;

	public List<Double> accList = new ArrayList<Double>();

	public MyBeaconRaw() {
	}

	public MyBeaconRaw(int color, int number, String device,
			String deviceAddress, String uuid) {
		this.color = color;
		this.number = number;
		this.device = device;
		this.deviceAddress = deviceAddress;
		this.uuid = uuid;

	}

	private double distance = EMPTY;

	public boolean state = false;
	public boolean menu = false;

	private double accuracy;

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}

	public String getUUID() {
		return uuid;
	}

	public void setDeviceAddress(String address) {
		this.deviceAddress = address;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public double getDistance() {
		return this.distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getAccuracy() {
		// Log.d(TAG, "MyBeaconClassRaw");
		return accuracy;
	}

	public void setAccuracy(double accuracy, long time) {
		// Log.d(TAG, "MyBeaconClassRaw");
		this.accuracy = accuracy;
	}

	public List<Double> getAccList() {
		return accList;
	}

	public void setAccList(List<Double> accList) {
		this.accList = accList;
	}

}
