package sk.svb.ibeacon.heatmap.logic;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.IBeaconDevice;

/**
 * use: uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice Class
 * @author mbodis
 *
 */
public class MyIBeaconDevice extends IBeaconDevice {

	public MyIBeaconDevice(BluetoothLeDevice device) {
		super(device);
	}

	/**
	 * @author mbodis for this demo is enought to compare bt-address
	 */
	@Override
	public boolean equals(Object object) {
		boolean sameSame = false;

		if (object != null && object instanceof IBeaconDevice) {
			if (
			// this.getUUID().equals(((IBeaconDevice) object).getUUID())&&
			this.getAddress().equals(((IBeaconDevice) object).getAddress())) {
				sameSame = true;
			}
		}

		return sameSame;
	}
}
