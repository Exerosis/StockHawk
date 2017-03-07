package com.udacity.stockhawk.implementation.view.stocklist.holder;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.Stock;
import yahoofinance.quotes.stock.StockQuote;

public class StockViewHolder extends RecyclerView.ViewHolder implements StockHolder {
    private static final DecimalFormat FORMAT_DOLLAR_PLUS;
    private static final DecimalFormat FORMAT_DOLLAR;
    private static final DecimalFormat FORMAT_PERCENT;

    @BindView(R.id.stock_holder_symbol)
    protected TextView symbolView;
    @BindView(R.id.stock_holder_price)
    protected TextView priceView;
    @BindView(R.id.stock_holder_change)
    protected TextView changeView;

    @BindColor(R.color.red_accent)
    protected int red;
    @BindColor(R.color.green_accent)
    protected int green;

    private StockHolderListener listener;
    private Stock stock;
    private boolean displayMode;

    static {
        FORMAT_DOLLAR = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        FORMAT_DOLLAR_PLUS = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        FORMAT_DOLLAR_PLUS.setPositivePrefix("+$");
        FORMAT_PERCENT = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        FORMAT_PERCENT.setMaximumFractionDigits(2);
        FORMAT_PERCENT.setMinimumFractionDigits(2);
        FORMAT_PERCENT.setPositivePrefix("+");
    }

    public StockViewHolder(ViewGroup container) {
        super(LayoutInflater.from(container.getContext()).inflate(R.layout.stock_view_holder, container, false));
        ButterKnife.bind(this, getRoot());
        itemView.setOnClickListener(view -> {
            if (listener != null)
                listener.onClick(stock);
        });
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

    @Override
    public void setStock(Stock stock) {
        this.stock = stock;

        StockQuote quote = stock.getQuote();

        //Change
        float change = quote.getChange().floatValue();
        changeView.setBackgroundColor(change > 0 ? green : red);
        if (displayMode)
            changeView.setText(FORMAT_PERCENT.format(quote.getChangeInPercent()));
        else
            changeView.setText(FORMAT_DOLLAR_PLUS.format(change));

        //Price
        priceView.setText(FORMAT_DOLLAR.format(quote.getPrice()));

        //Symbol
        symbolView.setText(stock.getSymbol());
    }

    @Override
    public Stock getStock() {
        return stock;
    }

    @Override
    public void setDisplayMode(boolean displayMode) {
        this.displayMode = displayMode;
    }
}
