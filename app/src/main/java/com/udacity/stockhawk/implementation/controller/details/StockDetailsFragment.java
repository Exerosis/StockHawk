package com.udacity.stockhawk.implementation.controller.details;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.udacity.stockhawk.implementation.model.Stock;
import com.udacity.stockhawk.implementation.model.StockStore;
import com.udacity.stockhawk.implementation.view.details.StockDetails;
import com.udacity.stockhawk.implementation.view.details.StockDetailsView;

import rx.Subscription;

public class StockDetailsFragment extends Fragment implements StockDetailsController {
    public static final String ARGS_SYMBOL = "SYMBOL";
    private StockDetails view;
    private Subscription subscription;
    private Stock stock;

    public static StockDetailsFragment newInstance(String symbol) {
        Bundle args = new Bundle();
        args.putString(ARGS_SYMBOL, symbol);
        StockDetailsFragment fragment = new StockDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = new StockDetailsView(inflater, container, (AppCompatActivity) getActivity());
        view.setListener(this);

        StockStore.getStock(getArguments().getString(ARGS_SYMBOL)).subscribe(stock -> {
            for (Period period : Period.values())
                stock.getHistory(period).subscribe();
            this.stock = stock;
        });
        for (Period period : Period.values())
            StockStore.getStock(getArguments().getString(ARGS_SYMBOL)).subscribe(stock -> stock.getHistory(period).subscribe());

        periodChanged(Period.WEEK);
        return view.getRoot();
    }

    @Override
    public void periodChanged(Period period) {
        if (subscription != null)
            subscription.unsubscribe();
        subscription = stock.getHistory(period).subscribe(view::setHistory);
    }
}