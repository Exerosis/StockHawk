package com.udacity.stockhawk.implementation.view.stocklist.holder;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.model.Stock;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockViewHolder extends RecyclerView.ViewHolder implements StockHolder {
    @BindView(R.id.stock_holder_symbol)
    protected TextView symbolView;
    @BindView(R.id.stock_holder_price)
    protected TextView priceView;
    @BindView(R.id.stock_holder_change)
    protected TextView changeView;

    private StockHolderListener listener;
    private Stock stock;
    private boolean displayMode;

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
        symbolView.setText(stock.getSymbol());
        stock.getColor().subscribe(color -> changeView.setBackgroundColor(ContextCompat.getColor(getRoot().getContext(), color)));
        stock.getPrice().subscribe(priceView::setText);
        stock.getChange(getRoot().getContext()).subscribe(changeView::setText);
    }

    @Override
    public Stock getStock() {
        return stock;
    }
}