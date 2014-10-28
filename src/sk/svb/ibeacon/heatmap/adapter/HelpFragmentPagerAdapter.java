package sk.svb.ibeacon.heatmap.adapter;

import java.util.List;

import sk.svb.ibeacon.heatmap.R;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * tutorial fragment adapters
 * @author mbodis
 *
 */
public class HelpFragmentPagerAdapter  extends FragmentPagerAdapter {
	
	public String TAG = "HelpFragmentPagerAdapter";
	
	private final List<Fragment> fragments;	
	Context context;
	
	public HelpFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments, Context context) {
		super(fm);
		this.fragments = fragments;
		this.context = context;		
	}

	@Override
	public Fragment getItem(int position) {		
		return this.fragments.get(position);
	}	
	
	@Override
	public int getCount() {
		return this.fragments.size();
	}
		
	@Override
    public CharSequence getPageTitle(int position) {		
        switch (position) {
	        case 0: return context.getString(R.string.help1_title);
	        case 1: return context.getString(R.string.help2_title);
	      	case 2: return context.getString(R.string.help3_title);
	      	case 3: return context.getString(R.string.help4_title);
	      	case 4: return context.getString(R.string.help5_title);

        }
        return null;
    }
}
