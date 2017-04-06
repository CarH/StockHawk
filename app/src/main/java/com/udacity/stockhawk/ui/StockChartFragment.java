package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.graphics.Color.rgb;


public class StockChartFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int STOCK_CHART_LOADER_ID = 100;
    private static final String SYMBOL_KEY = "symbol";
    private static final String COMPANY_NAME_KEY = "company_name";

    private final String[] projection = new String[] {Contract.Quote.COLUMN_HISTORY};
    private final int COL_HISTORY = 0;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stock_chart)
    LineChart stockChart;
    private String mSymbol;
    private long mMinTimestamp;
    private MyXValueFormatter mXValueFormatter;


    public static StockChartFragment getInstance(String symbol, String companyName) {
        StockChartFragment scf = new StockChartFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SYMBOL_KEY, symbol);
        bundle.putString(COMPANY_NAME_KEY, companyName);
        scf.setArguments(bundle);
        return scf;
    }

    class MyXValueFormatter implements IAxisValueFormatter {

        private final Date mDate;
        private SimpleDateFormat mDateFormat;

        public MyXValueFormatter() {
            this.mDateFormat = new SimpleDateFormat("MM/yyyy");
            this.mDate = new Date();
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long realTimestamp = (long) value + mMinTimestamp;
            return getMonthYear(realTimestamp);
        }


        public String getMonthYear(long realTimestamp) {
            mDate.setTime(realTimestamp);
            return mDateFormat.format(mDate);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stock_chart, container, false);
        ButterKnife.bind(this, root);
        mSymbol = getArguments().getString(SYMBOL_KEY);
        mXValueFormatter = new MyXValueFormatter();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(STOCK_CHART_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Contract.Quote.makeUriForStock(mSymbol),
                projection,
                null,
                null,
                null
                );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() == 0) {
            return;
        }
        cursor.moveToFirst();

        final String[] splitQuotes = cursor.getString(COL_HISTORY).split("\\n");
        List<String> historyList = new ArrayList<>(Arrays.asList(splitQuotes));
        plotChart(getSortedQuotes(historyList));
    }

    @NonNull
    private List<Pair<Long, Float>> getSortedQuotes(List<String> historyList) {
        List<Pair<Long, Float>> quotes = new ArrayList<>(historyList.size());

        String[] dateCoteSplit;
        for (int i = 0; i < historyList.size(); i++) {
            // turn your cursor into Entry objects
            dateCoteSplit = historyList.get(i).split(",");
            if (dateCoteSplit.length == 2) {
                Pair<Long, Float> quote =
                        new Pair<>(Long.valueOf(dateCoteSplit[0]), Float.valueOf(dateCoteSplit[1]));
                quotes.add(quote);
            }
        }
        Collections.sort(quotes,
                new Comparator<Pair<Long, Float>>() {
            @Override
            public int compare(Pair<Long, Float> lv, Pair<Long, Float> rv) {
                if (lv.first < rv.first)
                    return -1;
                if (lv.first > rv.first)
                    return 1;
                return 0;
            }
        });
        return quotes;
    }

    private void plotChart(List<Pair<Long, Float>> quotes) {
        mMinTimestamp = quotes.get(0).first;
        Timber.d("mMinTimestamp: " + mMinTimestamp);

        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < quotes.size(); i++) {
            Pair<Long, Float> quote = quotes.get(i);
            entries.add(new Entry(quote.first - mMinTimestamp, quote.second));
        }

        if (!entries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(entries, "Close Stock Price");
//            dataSet.setColor(Color.rgb(117, 117, 117));
            dataSet.setColor(rgb(29, 233, 182));
            dataSet.setHighLightColor(rgb(117, 117, 117));
//            dataSet.setCircleColor(rgb(117, 117, 117));
//            dataSet.setCircleRadius(0.050f);
            dataSet.setDrawCircles(false);

            LineData lineData = new LineData(dataSet);
            XAxis axisX = stockChart.getXAxis();
            axisX.setValueFormatter(mXValueFormatter);
            axisX.setPosition(XAxis.XAxisPosition.BOTTOM);

            stockChart.getAxisRight().setDrawLabels(false);

            Description desc = new Description();
            desc.setText("");
            stockChart.setDescription(desc);
            stockChart.setData(lineData);
            stockChart.invalidate();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
