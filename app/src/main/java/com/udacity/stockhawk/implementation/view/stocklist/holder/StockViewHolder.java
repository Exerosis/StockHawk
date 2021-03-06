package com.udacity.stockhawk.implementation.view.stocklist.holder;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
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
    protected ViewStub stub;
    private SparkView chart;

    private Subscription historySubscription;
    private Subscription quoteSubscription;
    private StockHolderListener listener;
    private StockModel stock;
    private HistoryModel history;

    public StockViewHolder(ViewGroup container) {
        super(LayoutInflater.from(container.getContext()).inflate(R.layout.stock_view_holder, container, false));
        ButterKnife.bind(this, getRoot());

        stub.setLayoutResource(R.layout.spark_chart_layout);
        chart = (SparkView) stub.inflate();

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

        if (quoteSubscription != null)
            quoteSubscription.unsubscribe();
        if (historySubscription != null)
            historySubscription.unsubscribe();

        quoteSubscription = stock.getQuoteSubject().subscribe(quote -> {
            symbol.setText(quote.getSymbol());
            symbol.setContentDescription("Stock, " + stock.getSymbol().replace("", " ").trim());
            price.setText(quote.getPrice());
            price.setContentDescription("Price, " + quote.getPrice());
            String change = quote.getChange(getRoot().getContext());
            this.change.setText(change);
            this.change.setBackgroundColor(ContextCompat.getColor(getRoot().getContext(), quote.getColor()));
            this.change.setContentDescription((quote.getColor() == R.color.grey_primary ? "" : quote.getColor() == R.color.red_primary ? "Down " : "Up ") + change.substring(1));
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
    public void saveState(Bundle out) {

    }

    @Override
    public void loadState(Bundle in) {

    }
}