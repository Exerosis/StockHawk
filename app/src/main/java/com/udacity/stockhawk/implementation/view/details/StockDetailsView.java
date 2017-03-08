package com.udacity.stockhawk.implementation.view.details;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.details.Period;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.histquotes.HistoricalQuote;

public class StockDetailsView implements StockDetails {
    private static final long DURATION_FADE = 500;
    private final View view;
    private final AppCompatActivity activity;

    @BindView(R.id.stock_details_chart)
    protected SparkView chart;
    @BindView(R.id.stock_details_tabs)
    protected TabLayout tabs;
    @BindView(R.id.stock_details_toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.stock_details_background)
    protected View background;

    @BindColor(R.color.grey_primary)
    protected int grey;
    @BindColor(R.color.grey_primary_dark)
    protected int darkGrey;

    @BindColor(R.color.red_primary)
    protected int red;
    @BindColor(R.color.red_primary_dark)
    protected int darkRed;

    @BindColor(R.color.green_primary)
    protected int green;
    @BindColor(R.color.green_primary_dark)
    protected int darkGreen;

    private StockDetailsListener listener;
    private List<HistoricalQuote> history = new ArrayList<>();
    private int color;
    private int darkColor;

    public StockDetailsView(LayoutInflater inflater, ViewGroup container, AppCompatActivity activity) {
        view = inflater.inflate(R.layout.stock_details_view, container, false);
        this.activity = activity;
        ButterKnife.bind(this, view);

        activity.setSupportActionBar(toolbar);

        color = grey;
        darkColor = darkGrey;

        chart.setCornerRadius(80);
        chart.setAdapter(new SparkAdapter() {
            @Override
            public int getCount() {
                return history.size();
            }

            @Override
            public Object getItem(int index) {
                return history.get(index);
            }

            @Override
            public float getY(int index) {
                return history.get(index).getClose().floatValue();
            }
        });

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (listener != null)
                    listener.periodChanged(Period.valueOf(String.valueOf(tab.getText()).replace(" ", "_").toUpperCase()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void setHistory(List<HistoricalQuote> history) {
        if (history.size() < 2)
            setColor(grey);
        else {
            setColor(history.get(0).getOpen().floatValue() < history.get(history.size() - 1).getClose().floatValue() ? green : red);
            this.history = view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? Lists.reverse(history) : history;
            chart.getAdapter().notifyDataSetChanged();
        }
    }

    private void setColor(int color) {
        if (this.color == color)
            return;
        int darkColor = color == grey ? darkGrey : color == green ? darkGreen : darkRed;

        //Instantly
        chart.setLineColor(color);

        //Animated
        ValueAnimator colorAnimator = ValueAnimator.ofArgb(this.color, color).setDuration(DURATION_FADE);
        colorAnimator.addUpdateListener(animation -> {
            this.color = (int) animation.getAnimatedValue();

            background.setBackgroundColor(this.color);
            toolbar.setBackgroundColor(this.color);
        });

        ValueAnimator darkColorAnimator = ValueAnimator.ofArgb(this.darkColor, darkColor).setDuration(DURATION_FADE);
        darkColorAnimator.addUpdateListener(animation -> {
            this.darkColor = (int) animation.getAnimatedValue();

            activity.getWindow().setStatusBarColor(this.darkColor);
            tabs.setSelectedTabIndicatorColor(this.darkColor);
            tabs.setTabTextColors(this.color, this.darkColor);
        });

        colorAnimator.start();
        darkColorAnimator.start();
    }

    @Override
    public StockDetailsListener getListener() {
        return listener;
    }

    @Override
    public void setListener(StockDetailsListener listener) {
        this.listener = listener;
    }

    @Override
    public View getRoot() {
        return view;
    }

    @Override
    public Bundle getViewState() {
        return null;
    }
}