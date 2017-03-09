package com.udacity.stockhawk.implementation.controller.details;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robinhood.spark.SparkAdapter;
import com.udacity.stockhawk.implementation.model.test.StockModel;
import com.udacity.stockhawk.implementation.view.details.StockDetails;
import com.udacity.stockhawk.implementation.view.details.StockDetailsView;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import yahoofinance.histquotes.HistoricalQuote;

public class StockDetailsFragment extends Fragment implements StockDetailsController {
    public static final String ARGS_STOCK = "STOCK";
    private StockDetails view;
    private Subscription subscription;
    private StockModel stock;
    private List<HistoricalQuote> history = new ArrayList<>();

    public static StockDetailsFragment newInstance(Bundle args) {
        StockDetailsFragment fragment = new StockDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static StockDetailsFragment newInstance(StockModel stock) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_STOCK, stock);
        return newInstance(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = new StockDetailsView(inflater, container, (AppCompatActivity) getActivity());
        view.setListener(this);

        view.setAdapter(new SparkAdapter() {
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


        StockModel stock = getArguments().getParcelable(ARGS_STOCK);
        if (stock != null) {
            stock.getColorObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(view::setColor);
            stock.getDarkColorObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(view::setDarkColor);
        }

        onChangePeriod(Period.WEEK);
        return view.getRoot();
    }

    @Override
    public void onChangePeriod(Period period) {
        if (subscription != null)
            subscription.unsubscribe();
        subscription = stock.getHistoryObservable(period).subscribe(history -> {
            this.history = history;
            view.getAdapter().notifyDataSetChanged();
        });
    }
}