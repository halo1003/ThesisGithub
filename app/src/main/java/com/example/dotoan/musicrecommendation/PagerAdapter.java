package com.example.dotoan.musicrecommendation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.dotoan.musicrecommendation.signup.fragment.fragment_email_pass;
import com.example.dotoan.musicrecommendation.signup.fragment.fragment_username;

/**
 * Created by DOTOAN on 9/19/2017.
 */

public class PagerAdapter extends FragmentPagerAdapter {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new fragment_email_pass();
            case 1:
                return new fragment_username();
            default:
                break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
