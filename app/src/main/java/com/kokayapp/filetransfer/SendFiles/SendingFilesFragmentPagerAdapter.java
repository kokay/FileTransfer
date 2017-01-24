package com.kokayapp.filetransfer.SendFiles;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Koji on 12/26/2016.
 */

public class SendingFilesFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList;

    public SendingFilesFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Koji Okayasu";
    }
}
