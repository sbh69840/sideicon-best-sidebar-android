package shiv.raj.admin.sideicon.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import shiv.raj.admin.sideicon.AboutPager;
import shiv.raj.admin.sideicon.AdActivity;


/**
 * Created by admin on 27/01/2017.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {
    private String tabTitles[]=new String[]{"ads","About"};
    public TabsPagerAdapter(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new AdActivity();
            case 1:
                return new AboutPager();

        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
