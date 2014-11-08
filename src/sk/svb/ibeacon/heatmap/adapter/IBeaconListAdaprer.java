package sk.svb.ibeacon.heatmap.adapter;

import java.util.List;

import sk.svb.ibeacon.heatmap.R;
import sk.svb.ibeacon.heatmap.logic.MyBeaconRaw;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * iBeacon list adapter, if saved beacon show color + "saved"
 * 
 * @author mbodis
 *
 */
public class IBeaconListAdaprer extends ArrayAdapter<MyBeaconRaw> {

	private static final String TAG = "IBeaconListAdaprer";

	private Context context;
	private List<MyBeaconRaw> myList = null;

	static class ViewHolder {
		TextView row_addr;
		TextView row_major;
		TextView row_minor;
		TextView row_uuid;
		TextView row_number;
		ImageView row_color;
		TextView row_saved;
	}

	public IBeaconListAdaprer(Context context, int textViewResourceId,
			List<MyBeaconRaw> objects) {
		super(context, R.layout.row_layout, objects);
		this.context = context;
		this.myList = objects;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View rowView = convertView;
		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			rowView = inflater.inflate(R.layout.row_layout, null);
			// configure view holder
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.row_addr = (TextView) rowView
					.findViewById(R.id.row_addr);
			viewHolder.row_major = (TextView) rowView
					.findViewById(R.id.row_major);
			viewHolder.row_minor = (TextView) rowView
					.findViewById(R.id.row_minor);
			viewHolder.row_uuid = (TextView) rowView
					.findViewById(R.id.row_UUID);
			viewHolder.row_number = (TextView) rowView
					.findViewById(R.id.row_number);
			viewHolder.row_saved = (TextView) rowView
					.findViewById(R.id.row_saved);
			viewHolder.row_color = (ImageView) rowView
					.findViewById(R.id.row_color);

			rowView.setTag(viewHolder);
		}

		// fill data
		ViewHolder holder = (ViewHolder) rowView.getTag();
		if (myList.get(position).getDeviceAddress() == null) {
			holder.row_addr.setVisibility(View.GONE);
		} else {
			holder.row_addr.setText(context.getString(R.string.address)
					+ myList.get(position).getDeviceAddress());
			holder.row_addr.setVisibility(View.VISIBLE);
		}

		holder.row_major.setVisibility(View.VISIBLE);
		holder.row_minor.setVisibility(View.VISIBLE);

		if (myList.get(position).getUUID() == null) {
			holder.row_uuid.setVisibility(View.GONE);
		} else {
			holder.row_uuid.setText(context.getString(R.string.uuid)
					+ myList.get(position).getUUID());
			holder.row_uuid.setVisibility(View.VISIBLE);
		}

		if (myList.get(position).getNumber() > 0) {
			holder.row_number.setText(context.getString(R.string.number)
					+ myList.get(position).getNumber());
			holder.row_color
					.setBackgroundColor(myList.get(position).getColor());

			holder.row_number.setVisibility(View.VISIBLE);
			holder.row_saved.setVisibility(View.VISIBLE);
			holder.row_color.setVisibility(View.VISIBLE);
		} else {
			holder.row_number.setVisibility(View.GONE);
			holder.row_saved.setVisibility(View.GONE);
			holder.row_color.setVisibility(View.GONE);

		}

		return rowView;

	}

}
