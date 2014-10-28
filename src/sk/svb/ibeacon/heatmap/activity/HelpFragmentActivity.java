package sk.svb.ibeacon.heatmap.activity;

import java.util.List;
import java.util.Vector;

import sk.svb.ibeacon.heatmap.R;
import sk.svb.ibeacon.heatmap.adapter.HelpFragmentPagerAdapter;
import sk.svb.ibeacon.heatmap.fragment.HelpPageFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.ImageView;

/**
 * help activity with fragments in horizontal scroll view 
 * @author mbodis
 *
 */
public class HelpFragmentActivity extends FragmentActivity {

	public String TAG = "HelpFragmentActivity";

	private HelpFragmentPagerAdapter mPagerAdapter;
	public static ViewPager mViewPager;

	private ImageView[] imagePager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.help_viewpager_layout);
		this.initialisePaging();
		setPageChangeListener();

		imagePager = new ImageView[5];
		imagePager[0] = (ImageView) findViewById(R.id.wizard_pager_img_1);
		imagePager[1] = (ImageView) findViewById(R.id.wizard_pager_img_2);
		imagePager[2] = (ImageView) findViewById(R.id.wizard_pager_img_3);
		imagePager[3] = (ImageView) findViewById(R.id.wizard_pager_img_4);
		imagePager[4] = (ImageView) findViewById(R.id.wizard_pager_img_5);
	}

	private void selectPage(int which) {
		for (int i = 0; i < imagePager.length; i++) {
			if (i == which) {
				imagePager[i].setImageDrawable(getResources().getDrawable(
						R.drawable.btn_circle_selected));
			} else {
				imagePager[i].setImageDrawable(getResources().getDrawable(
						R.drawable.btn_circle_normal));
			}
		}
	}

	private void setPageChangeListener() {
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				selectPage(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if (Build.VERSION.SDK_INT >= 11) {
					invalidateOptionsMenu();
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	/**
	 * Initialize the fragments to be paged
	 */
	private void initialisePaging() {
		List<Fragment> fragments = new Vector<Fragment>();
		Bundle b1 = new Bundle();
		b1.putInt("page", 1);
		fragments.add(Fragment.instantiate(this,
				HelpPageFragment.class.getName(), b1));
		Bundle b2 = new Bundle();
		b2.putInt("page", 2);
		fragments.add(Fragment.instantiate(this,
				HelpPageFragment.class.getName(), b2));
		Bundle b3 = new Bundle();
		b3.putInt("page", 3);
		fragments.add(Fragment.instantiate(this,
				HelpPageFragment.class.getName(), b3));
		Bundle b4 = new Bundle();
		b4.putInt("page", 4);
		fragments.add(Fragment.instantiate(this,
				HelpPageFragment.class.getName(), b4));
		Bundle b5 = new Bundle();
		b5.putInt("page", 5);
		fragments.add(Fragment.instantiate(this,
				HelpPageFragment.class.getName(), b5));

		this.mPagerAdapter = new HelpFragmentPagerAdapter(
				super.getSupportFragmentManager(), fragments,
				getApplicationContext());
		mViewPager = (ViewPager) super.findViewById(R.id.viewpager);
		mViewPager.setAdapter(this.mPagerAdapter);
	}

}
