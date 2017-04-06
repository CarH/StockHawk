package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.udacity.stockhawk.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockChartActivity extends AppCompatActivity {

    @BindView(R.id.pager)
    ViewPager mViewPager;

    @BindView(R.id.stock_chart_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.stock_tab_widget)
    TabLayout mTabLayout;

    private StockFragmentPagerAdapter mPagerAdapter;

    private StockTabListener mTabListener;
    private int mSelectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_chart);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        Bundle bundle = getIntent().getExtras();
        ArrayList<String> symbols = bundle.getStringArrayList("symbols");
        ArrayList<String> companies = bundle.getStringArrayList("company_names");

        if (savedInstanceState == null)
            mSelectedPosition = bundle.getInt("selected_position");
        else
            mSelectedPosition = savedInstanceState.getInt("selected_position");

        mPagerAdapter =
                new StockFragmentPagerAdapter(getSupportFragmentManager(), symbols, companies);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(mSelectedPosition);

        // Set the toolbar title to the company name of the selected stock
        getSupportActionBar().setTitle(mPagerAdapter.getCompanyName(mSelectedPosition));

        mTabListener = new StockTabListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTabLayout.addOnTabSelectedListener(mTabListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("selected_position", mSelectedPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTabLayout.removeOnTabSelectedListener(mTabListener);
    }

    private class StockTabListener implements TabLayout.OnTabSelectedListener {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mSelectedPosition = tab.getPosition();
            mToolbar.setTitle(mPagerAdapter.getCompanyName(tab.getPosition()));
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }
}
