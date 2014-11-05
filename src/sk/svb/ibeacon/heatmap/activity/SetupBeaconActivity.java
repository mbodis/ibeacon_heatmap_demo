package sk.svb.ibeacon.heatmap.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sk.svb.ibeacon.heatmap.R;
import sk.svb.ibeacon.heatmap.adapter.IBeaconListAdaprer;
import sk.svb.ibeacon.heatmap.db.DatabaseHelper;
import sk.svb.ibeacon.heatmap.dialog.CustomDialogBuilder;
import sk.svb.ibeacon.heatmap.logic.MyBeaconRaw;
import sk.svb.ibeacon.heatmap.logic.MyIBeaconDevice;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.util.IBeaconUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * assigning beacons a color<br>
 * in this demo we will use a bt-address as unque identifier
 * 
 * @author mbodis
 *
 */
public class SetupBeaconActivity extends Activity {

	private static final String TAG = "SetupBeaconActivity";

	private BluetoothAdapter mBluetoothAdapter;
	private LeScanCallback mLeScanCallback;
	private boolean scanning = false;

	private ListView list;
	private ArrayList<MyIBeaconDevice> iBeaconList;
	private ArrayAdapter<MyBeaconRaw> adapter;

	// period for searching iBeacons 5 sec
	private static final long SCAN_PERIOD = 5000;
	// number scanning of seconds
	private int scanTimer = (int) (SCAN_PERIOD / 1000);

	List<MyBeaconRaw> myList;
	private AlertDialog ad;
	private MyBeaconRaw selecedIBeacon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup_beacons_layout);

		refresMyBeaconList();
	}

	@Override
	protected void onResume() {
		setupLayout();

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		toggleListView(bluetoothAdapter.isEnabled(), true);
		refresh();

		super.onResume();
	}

	@Override
	protected void onPause() {
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
		super.onPause();
	}

	private void setupLayout() {

		findViewById(R.id.loading).setVisibility(View.GONE);
		findViewById(R.id.ibeacon_timer).setVisibility(View.GONE);

		list = (ListView) findViewById(R.id.list);
		adapter = new IBeaconListAdaprer(this, R.layout.row_layout, new ArrayList<MyBeaconRaw>());
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selecedIBeacon = adapter.getItem(position);

				selectDialog();

			}
		});
		registerForContextMenu(list);

		iBeaconList = new ArrayList<MyIBeaconDevice>();

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

		mBluetoothAdapter = bluetoothManager.getAdapter();
		mLeScanCallback = new LeScanCallback() {

			@Override
			public void onLeScan(final BluetoothDevice device, final int rssi,
					final byte[] scanRecord) {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						final BluetoothLeDevice deviceLe = new BluetoothLeDevice(
								device, rssi, scanRecord,
								System.currentTimeMillis());

						if (IBeaconUtils.isThisAnIBeacon(deviceLe)) {
							MyIBeaconDevice ibeacon = new MyIBeaconDevice(
									deviceLe);
							if (!iBeaconList.contains(ibeacon)) {
								iBeaconList.add(ibeacon);
							} else {
								// update accuracy
								for (int i = 0; i < iBeaconList.size(); i++) {

									MyIBeaconDevice ibd = iBeaconList.get(i);

									if (ibd.getAddress().equals(
											ibeacon.getAddress())
											&& ibd.getUUID().equals(
													ibeacon.getUUID())) {
										iBeaconList.set(i, ibeacon);
										break;

									}
								}
							}
						}

						refresh();
					}
				});

			}
		};
	}

	private void selectDialog() {
		CustomDialogBuilder cdb = new CustomDialogBuilder(
				SetupBeaconActivity.this);
		ad = cdb.create();
		cdb.setTitle(getString(R.string.select_color));
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.select_color_layout, null);
		cdb.setView(layout);
		cdb.setNeutralButton(android.R.string.cancel, null);
		OnClickListener lis = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.btn_red) {
					selecedIBeacon.setColor(Color.RED);
					selecedIBeacon.setNumber(1);
				} else if (v.getId() == R.id.btn_green) {
					selecedIBeacon.setColor(Color.GREEN);
					selecedIBeacon.setNumber(2);
				} else if (v.getId() == R.id.btn_blue) {
					selecedIBeacon.setColor(Color.BLUE);
					selecedIBeacon.setNumber(3);
				} else if (v.getId() == R.id.btn_yellow) {
					selecedIBeacon.setColor(Color.BLUE);
					selecedIBeacon.setNumber(4);
				}
				DatabaseHelper.saveBeacon(getApplicationContext(),
						selecedIBeacon);
				ad.hide();
				refresMyBeaconList();
				startScan(null);
			}
		};
		layout.findViewById(R.id.btn_red).setOnClickListener(lis);
		layout.findViewById(R.id.btn_green).setOnClickListener(lis);
		layout.findViewById(R.id.btn_blue).setOnClickListener(lis);
		layout.findViewById(R.id.btn_yellow).setOnClickListener(lis);

		ad = cdb.create();
		ad.show();
	}

	public void startScan(View v) {

		if (!scanning){
			scanning = true;
			// start scan
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			toggleScanLoading(true);
			iBeaconList.clear();
			refresh();
	
			// Stops scanning after a pre-defined scan period.
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "stopping - scan (postDelay)");
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					toggleScanLoading(false);
				}
			}, SCAN_PERIOD);
	
			scanTimer = (int) (SCAN_PERIOD / 1000);
			((TextView) findViewById(R.id.ibeacon_timer)).setText(String
					.valueOf(scanTimer));
			new Timer().schedule(new TimerTask() {
	
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							((TextView) findViewById(R.id.ibeacon_timer))
									.setText(String.valueOf(scanTimer));
						}
					});
	
					scanTimer--;
					if (scanTimer == 0) {
						this.cancel();
						scanning = false;
	
					}
	
				}
			}, 0, 1000);
		}

	}
	
	public void reset(View v){
		DatabaseHelper.resetSavedBeacon(getApplicationContext());
		refresMyBeaconList();
		startScan(null);
	}

	private void refresMyBeaconList() {
		myList = (List<MyBeaconRaw>) DatabaseHelper.getSavedBeacons(
				getApplicationContext(), MainActivity.METHOD_RAW);		
	}

	public void refresh() {

		adapter.clear();

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		// BT enabled
		if (bluetoothAdapter.isEnabled()) {
			toggleListView(true, false);

			// scan ibacons in range
			for (MyIBeaconDevice ibd : iBeaconList) {

				MyBeaconRaw beac = new MyBeaconRaw();
				beac.setDeviceAddress(ibd.getAddress());
				beac.setUuid(ibd.getUUID());
				beac.setDistance(ibd.getAccuracy());
				
				// my saved beacons
				for (MyBeaconRaw mbc : myList) {					
					if (ibd.getAddress().equals(mbc.getDeviceAddress())) {						
						beac.setColor(mbc.getColor());
						beac.setNumber(mbc.getNumber());
					}
				}

				adapter.add(beac);

			}

			// BT disabled
		} else {
			toggleListView(false, false);
		}

		// empty list
		if (adapter.isEmpty()) {
			((View) findViewById(R.id.empty_list)).setVisibility(View.VISIBLE);
		}

		adapter.notifyDataSetChanged();

	}

	private void toggleListView(boolean btbEnable, boolean startup) {
		if (btbEnable) {
			findViewById(R.id.turnOnBt).setVisibility(View.GONE);

			if (startup)
				findViewById(R.id.scanBtn).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.turnOnBt).setVisibility(View.VISIBLE);
			if (startup)
				findViewById(R.id.scanBtn).setVisibility(View.GONE);

		}
		((View) findViewById(R.id.empty_list)).setVisibility(View.GONE);
	}

	public void turnOnBt(View v) {
		Intent intentBluetooth = new Intent();
		intentBluetooth
				.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		startActivity(intentBluetooth);
	}

	private void toggleScanLoading(boolean isLoading) {
		if (isLoading) {
			findViewById(R.id.scanBtn).setVisibility(View.GONE);
			findViewById(R.id.loading).setVisibility(View.VISIBLE);
			findViewById(R.id.ibeacon_timer).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.scanBtn).setVisibility(View.VISIBLE);
			findViewById(R.id.loading).setVisibility(View.GONE);
			findViewById(R.id.ibeacon_timer).setVisibility(View.GONE);

		}
	}

}
