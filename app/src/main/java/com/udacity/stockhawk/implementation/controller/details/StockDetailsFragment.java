package com.udacity.stockhawk.implementation.controller.details;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.udacity.stockhawk.implementation.model.StockStore;
import com.udacity.stockhawk.implementation.view.details.StockDetails;
import com.udacity.stockhawk.implementation.view.details.StockDetailsView;

import rx.Subscription;
import rx.schedulers.Schedulers;

public class StockDetailsFragment extends Fragment implements StockDetailsController {
    public static final String ARGS_SYMBOL = "SYMBOL";
    private StockDetails view;
    private Subscription subscription;

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

        for (Period period : Period.values())
            StockStore.getHistory(getContext(), getArguments().getString(ARGS_SYMBOL), period).subscribeOn(Schedulers.io()).subscribe();

        periodChanged(Period.WEEK);
        return view.getRoot();
    }

    @Override
    public void periodChanged(Period period) {
        if (subscription != null)
            subscription.unsubscribe();
        subscription = StockStore.getHistory(getContext(), getArguments().getString(ARGS_SYMBOL), period).subscribe(view::setHistory);
    }
}