package com.udacity.stockhawk.implementation.view.stocklist.holder;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.details.Period;
import com.udacity.stockhawk.implementation.model.test.StockModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import yahoofinance.histquotes.HistoricalQuote;

public class StockViewHolder extends RecyclerView.ViewHolder implements StockHolder {
    @BindView(R.id.stock_holder_symbol)
    protected TextView symbol;
    @BindView(R.id.stock_holder_price)
    protected TextView price;
    @BindView(R.id.stock_holder_change)
    protected TextView change;
    @BindView(R.id.stock_holder_chart)
    protected SparkView chart;

    private final Subscription[] subscriptions = new Subscription[4];
    private StockHolderListener listener;
    private StockModel stock;
    private List<HistoricalQuote> history = new ArrayList<>();

    public StockViewHolder(ViewGroup container) {
        super(LayoutInflater.from(container.getContext()).inflate(R.layout.stock_view_holder, container, false));
        ButterKnife.bind(this, getRoot());

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

        itemView.setOnClickListener(view -> {
            if (listener != null)
                listener.onClick(stock);
        });
    }

    @Override
    public void setStock(StockModel stock) {
        this.stock = stock;
        symbol.setText(stock.getSymbol());

        for (Subscription subscription : subscriptions)
            if (subscription != null)
                subscription.unsubscribe();

        subscriptions[1] = stock.getPriceObservable().subscribeOn(AndroidSchedulers.mainThread()).subscribe(price::setText);
        subscriptions[0] = stock.getColorObservable().subscribeOn(AndroidSchedulers.mainThread()).subscribe(color -> change.setBackgroundColor(ContextCompat.getColor(getRoot().getContext(), color)));
        subscriptions[2] = stock.getChangeObservable().subscribeOn(AndroidSchedulers.mainThread()).subscribe(change::setText);
        subscriptions[3] = stock.getHistoryObservable(Period.MONTH).subscribeOn(AndroidSchedulers.mainThread()).subscribe(history -> {
            this.history = history;
            chart.getAdapter().notifyDataSetChanged();
        });
    }

    @Override
    public StockModel getStock() {
        return stock;
    }

    @Override
    public StockHolderListener getListener() {
        return listener;
    }

    @Override
    public void setListener(StockHolderListener listener) {
        this.listener = listener;
    }

    @Override
    public View getRoot() {
        return itemView;
    }

    @Override
    public Bundle getViewState() {
        return null;
    }
}