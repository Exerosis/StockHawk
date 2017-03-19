package com.udacity.stockhawk.implementation.view.details;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.details.Period;
import com.udacity.stockhawk.implementation.model.QuoteModel;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("ConstantConditions")
public class StockDetailsView implements StockDetails {
    private static final long DURATION_FADE = 500;
    private static final String STATE_SELECTED_TAB = "SELECTED_TAB";
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
    @BindView(R.id.stock_details_symbol)
    protected TextView symbol;
    @BindView(R.id.stock_details_absolute_change)
    protected TextView absoluteChange;
    @BindView(R.id.stock_details_percent_change)
    protected TextView percentChange;
    @BindView(R.id.stock_details_price)
    protected TextView price;
    @BindView(R.id.stock_details_date)
    protected TextView date;

    @BindColor(R.color.grey_primary)
    protected int color;
    @BindColor(R.color.grey_primary_dark)
    protected int darkColor;

    private StockDetailsListener listener;

    public StockDetailsView(LayoutInflater inflater, ViewGroup container, AppCompatActivity activity) {
        view = inflater.inflate(R.layout.stock_details_view, container, false);
        this.activity = activity;
        ButterKnife.bind(this, view);

        activity.setSupportActionBar(toolbar);

        chart.setCornerRadius(40);


        chart.setScrubEnabled(true);

        tabs.getTabAt(1).select();

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (listener != null)
                    listener.onChangePeriod(Period.values()[tab.getPosition()]);
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
    public void setColor(@ColorRes int colorID, @ColorRes int darkColorID) {
        int color = ContextCompat.getColor(view.getContext(), colorID);
        int darkColor = ContextCompat.getColor(view.getContext(), darkColorID);
        if (this.color == color || this.darkColor == darkColor)
            return;

        tabs.setTabTextColors(color, darkColor);

        ValueAnimator colorAnimator = ValueAnimator.ofArgb(this.color, color).setDuration(DURATION_FADE);
        colorAnimator.addUpdateListener(animation -> {
            this.color = (int) animation.getAnimatedValue();

            absoluteChange.setTextColor(this.color);
            percentChange.setTextColor(this.color);
            chart.setLineColor(this.color);
            background.setBackgroundColor(this.color);
            toolbar.setBackgroundColor(this.color);
            tabs.setSelectedTabIndicatorColor(this.color);
        });
        colorAnimator.start();

        ValueAnimator darkColorAnimator = ValueAnimator.ofArgb(this.darkColor, darkColor).setDuration(DURATION_FADE);
        darkColorAnimator.addUpdateListener(animation -> {
            this.darkColor = (int) animation.getAnimatedValue();

            activity.getWindow().setStatusBarColor(this.darkColor);
        });
        darkColorAnimator.start();
    }

    @Override
    public void setOnScrubListener(SparkView.OnScrubListener listener) {
        chart.setScrubListener(listener);
    }

    @Override
    public void setAbsoluteChange(String absoluteChange) {
        this.absoluteChange.setText('(' + absoluteChange + ')');
    }

    @Override
    public void setPercentChange(String percentChange) {
        this.percentChange.setText(percentChange);
    }

    @Override
    public void setQuote(QuoteModel quote) {
        symbol.setText(quote.getSymbol());
        price.setText(quote.getPrice());
        date.setText(quote.getDate());
        setPercentChange(quote.getPercentChange());
        setAbsoluteChange(quote.getAbsoluteChange());
    }


    @Override
    public void saveState(Bundle out) {
        out.putInt(STATE_SELECTED_TAB, tabs.getSelectedTabPosition());
    }

    @Override
    public void loadState(Bundle in) {
        if (in != null)
            tabs.getTabAt(in.getInt(STATE_SELECTED_TAB)).select();
    }

    @Override
    public SparkAdapter getAdapter() {
        return chart.getAdapter();
    }

    @Override
    public void setAdapter(@NonNull SparkAdapter adapter) {
        chart.setAdapter(adapter);
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
}