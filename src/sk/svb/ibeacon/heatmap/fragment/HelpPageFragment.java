package sk.svb.ibeacon.heatmap.fragment;

import sk.svb.ibeacon.heatmap.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * tutorial fragment
 * 
 * @author mbodis
 *
 */
public class HelpPageFragment extends Fragment {

	private ScrollView mScrollView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		mScrollView = (ScrollView) inflater.inflate(
				R.layout.help_fragmnet_layout, container, false);

		Bundle bundle = getArguments();
		if (bundle != null) {

			int page = bundle.getInt("page");
			switch (page) {

			case 1:
				((ImageView) mScrollView.findViewById(R.id.fragment_image))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.ic_launcher));
				((TextView) mScrollView.findViewById(R.id.fragment_text))
						.setText(R.string.help1);
				break;
			case 2:
				((ImageView) mScrollView.findViewById(R.id.fragment_image))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.help_2_300));
				((TextView) mScrollView.findViewById(R.id.fragment_text))
						.setText(R.string.help2);

				break;
			case 3:
				((ImageView) mScrollView.findViewById(R.id.fragment_image))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.help_3_300));
				((TextView) mScrollView.findViewById(R.id.fragment_text))
						.setText(R.string.help3);
				break;
			case 4:
				((ImageView) mScrollView.findViewById(R.id.fragment_image))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.help_4_300));
				((TextView) mScrollView.findViewById(R.id.fragment_text))
						.setText(R.string.help4);

				break;
			case 5:
				((ImageView) mScrollView.findViewById(R.id.fragment_image))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.help_5_300));
				((TextView) mScrollView.findViewById(R.id.fragment_text))
						.setText(R.string.help5);

				break;
			}

		}
		return mScrollView;
	}
}
