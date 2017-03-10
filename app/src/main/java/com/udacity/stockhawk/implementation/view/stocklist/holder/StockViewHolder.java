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
import com.udacity.stockhawk.implementation.model.HistoryModel;
import com.udacity.stockhawk.implementation.model.StockModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;

public class StockViewHolder extends RecyclerView.ViewHolder implements StockHolder {
    @BindView(R.id.stock_holder_symbol)
    protected TextView symbol;
    @BindView(R.id.stock_holder_price)
    protected TextView price;
    @BindView(R.id.stock_holder_change)
    protected TextView change;
    @BindView(R.id.stock_holder_chart)
    protected SparkView chart;

    private Subscription historySubscription;
    private Subscription quoteSubscription;
    private StockHolderListener listener;
    private StockModel stock;
    private HistoryModel history;

    public StockViewHolder(ViewGroup container) {
        super(LayoutInflater.from(container.getContext()).inflate(R.layout.stock_view_holder, container, false));
        ButterKnife.bind(this, getRoot());

        chart.setCornerRadius(80);

        chart.setAdapter(new SparkAdapter() {
            @Override
            public int getCount() {
                return history == null ? 0 : history.getQuotes().size();
            }

            @Override
            public Object getItem(int index) {
                return history.getQuotes().get(index);
            }

            @Override
            public float getY(int index) {
                if (index == 0)
                    return history.getQuotes().get(index).getOpen();
                return history.getQuotes().get(index).getAdjustedClose();
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

        if (quoteSubscription != null)
            quoteSubscription.unsubscribe();
        if (historySubscription != null)
            historySubscription.unsubscribe();

        quoteSubscription = stock.getQuoteSubject().subscribe(quote -> {
            price.setText(quote.getPrice());
            change.setText(quote.getChange());
            change.setBackgroundColor(ContextCompat.getColor(getRoot().getContext(), quote.getColor()));
        });
        historySubscription = stock.getHistorySubject(Period.MONTH).subscribe(history -> {
            this.history = history;
            chart.setLineColor(ContextCompat.getColor(getRoot().getContext(), history.getColor()));
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