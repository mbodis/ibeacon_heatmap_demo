package sk.svb.ibeacon.heatmap.activity;

import sk.svb.ibeacon.heatmap.R;
import sk.svb.ibeacon.heatmap.db.DatabaseHelper;
import sk.svb.ibeacon.heatmap.dialog.CustomDialogBuilder;
import sk.svb.ibeacon.heatmap.support.MySupport;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * setting up iBeacons<br>
 * choosing accuracy method<br>
 * setting room size<br>
 * action bar (help, about, licences)<br>
 *  
 * first time launch show help 
 *  
 * @author mbodis
 *
 */
public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	public static final int METHOD_RAW = 0;
	public static final int METHOD_AVERAGE = 1;	
	public static final int METHOD_MIN = 2;
	public static final int METHOD_CUSTOM = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((Spinner) findViewById(R.id.acc_method)).setSelection(1);

		// first time - launch help
		if (firstTimeLaunch()){
			Intent intent = new Intent(getApplicationContext(),
					HelpFragmentActivity.class);
			startActivity(intent);
		}
		
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.err_no_btle_support), Toast.LENGTH_LONG)
					.show();
			finish();
		}
	}

	@Override
	protected void onResume() {
		// remove hocus-focus
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_help) {
			Intent intent = new Intent(getApplicationContext(),
					HelpFragmentActivity.class);
			startActivity(intent);
			return true;
		}
		if (id == R.id.action_about) {
			aboutDialog();
			return true;
		}
		if (id == R.id.action_licence) {
			licenceDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean firstTimeLaunch(){
		SharedPreferences sp = (getSharedPreferences(DatabaseHelper.PREFS,
				Activity.MODE_PRIVATE));
		boolean result = sp.getBoolean("first_time", true);
		if (result)
			sp.edit().putBoolean("first_time", false).commit();
		
		return result;
	}

	private void licenceDialog() {

		CustomDialogBuilder cdb = new CustomDialogBuilder(MainActivity.this);
		AlertDialog ad = cdb.create();
		cdb.setTitle(R.string.licence_title);
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.licences_dialog, null);
		((TextView) layout.findViewById(R.id.textView1)).setText(Html
				.fromHtml(MySupport.loadFile(getResources(), R.raw.licence)));
		cdb.setView(layout);

		cdb.setNeutralButton(android.R.string.ok, null);
		ad = cdb.create();
		ad.show();
	}

	private void aboutDialog() {
		CustomDialogBuilder cdb = new CustomDialogBuilder(MainActivity.this);
		AlertDialog ad = cdb.create();
		cdb.setTitle(R.string.about_title);
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.about_dialog, null);		
		cdb.setView(layout);
		cdb.setNeutralButton(android.R.string.ok, null);
		ad = cdb.create();
		ad.show();
	}

	public void setupIBeacons(View view) {
		Intent intent = new Intent(getApplicationContext(),
				SetupBeaconActivity.class);
		startActivity(intent);
	}

	public void showBeacons(View view) {
		if (DatabaseHelper.getSavedBeacons(getApplicationContext(),
				getSelectedMethod()).size() < 3) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.err_need_3_ibeacon_for_heatmap),
					Toast.LENGTH_SHORT).show();
		}

		Intent intent = new Intent(getApplicationContext(),
				ShowBeaconsActivity.class);
		intent.putExtra("method", getSelectedMethod());
		if (getRoomHeight() < getRoomWidth()) {
			intent.putExtra("r_width", getRoomHeight());
			intent.putExtra("r_height", getRoomWidth());
		} else {
			intent.putExtra("r_width", getRoomWidth());
			intent.putExtra("r_height", getRoomHeight());
		}
		startActivity(intent);
	}

	private int getSelectedMethod() {
		return ((Spinner) findViewById(R.id.acc_method))
				.getSelectedItemPosition();
	}

	private int getRoomWidth() {
		return Integer.valueOf(((EditText) findViewById(R.id.room_width))
				.getText().toString());
	}

	private int getRoomHeight() {
		return Integer.valueOf(((EditText) findViewById(R.id.room_height))
				.getText().toString());
	}

}
