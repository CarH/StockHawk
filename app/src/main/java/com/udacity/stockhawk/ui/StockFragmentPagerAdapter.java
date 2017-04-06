package com.udacity.stockhawk.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by CarH on 16/02/2017.
 */
public class StockFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<String> mSymbols;
    private ArrayList<String> mCompanyName;

    public StockFragmentPagerAdapter(FragmentManager fm, ArrayList<String> symbols, ArrayList<String> companies) {
        super(fm);
        mSymbols = symbols;
        mCompanyName = companies;
    }

    public String getCompanyName(int position) { return mCompanyName.get(position); }

    @Override
    public Fragment getItem(int position) {
        return StockChartFragment.getInstance(mSymbols.get(position), mCompanyName.get(position));
    }

    @Override
    public int getCount() {
        return mSymbols.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mSymbols.get(position);
    }
}
