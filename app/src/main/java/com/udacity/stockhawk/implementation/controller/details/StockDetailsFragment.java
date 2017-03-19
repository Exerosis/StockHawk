package com.udacity.stockhawk.implementation.controller.details;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robinhood.spark.SparkAdapter;
import com.udacity.stockhawk.implementation.model.HistoryModel;
import com.udacity.stockhawk.implementation.model.QuoteModel;
import com.udacity.stockhawk.implementation.model.StockModel;
import com.udacity.stockhawk.implementation.view.details.StockDetails;
import com.udacity.stockhawk.implementation.view.details.StockDetailsView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;

import static com.udacity.stockhawk.implementation.model.QuoteModel.FORMAT_ABSOLUTE_CHANGE;
import static com.udacity.stockhawk.implementation.model.QuoteModel.FORMAT_PERCENT_CHANGE;

public class StockDetailsFragment extends Fragment implements StockDetailsController {
    public static final String ARGS_STOCK = "STOCK";
    private Map<Period, HistoryModel> histories = new HashMap<>();
    private StockDetails view;
    private Period period = Period.MONTH;
    private Subscription subscription;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle in) {
        view = new StockDetailsView(inflater, container, (AppCompatActivity) getActivity());
        view.setListener(this);

        view.setAdapter(new SparkAdapter() {
            @Override
            public int getCount() {
                return histories.get(period) == null ? 0 : histories.get(period).getQuotes().size();
            }

            @Override
            public Object getItem(int index) {
                return histories.get(period).getQuotes().get(index);
            }

            @Override
            public float getY(int index) {
                return ((QuoteModel) getItem(index)).getAdjustedClose();
            }
        });

        view.setOnScrubListener(value -> {
            List<QuoteModel> quotes = histories.get(period).getQuotes();
            refreshQuote(value == null ? quotes.get(quotes.size() - 1) : (QuoteModel) value);
        });

        view.loadState(in);

        StockModel stock = getArguments().getParcelable(ARGS_STOCK);
        if (stock != null)
            subscription = stock.getHistoriesSubject().subscribe(histories -> {
                this.histories = histories;
                refresh();
            });
        return view.getRoot();
    }

    @Override
    public void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        view.saveState(out);
        super.onSaveInstanceState(out);
    }

    @Override
    public void onChangePeriod(Period period) {
        this.period = period;
        refresh();
    }

    private void refresh() {
        view.getAdapter().notifyDataSetChanged();
        HistoryModel history = histories.get(period);
        if (history == null)
            return;
        view.setColor(history.getColor(), history.getDarkColor());
        refreshQuote(history.getQuotes().get(history.getQuotes().size() - 1));
    }

    public void refreshQuote(QuoteModel quote) {
        QuoteModel first = histories.get(period).getQuotes().get(0);
        float change = quote.getAdjustedClose() - first.getAdjustedClose();
        view.setQuote(quote);
        view.setAbsoluteChange(FORMAT_ABSOLUTE_CHANGE.format(change));
        view.setPercentChange(FORMAT_PERCENT_CHANGE.format((change / first.getAdjustedClose())));
    }
}