package com.udacity.stockhawk.implementation.view.stocklist.holder;

import com.udacity.stockhawk.mvc.Listenable;
import com.udacity.stockhawk.mvc.ViewBase;

import yahoofinance.Stock;

public interface StockHolder extends ViewBase, Listenable<StockHolderListener> {
    void setStock(Stock stock);

    Stock getStock();

    void setDisplayMode(boolean displayMode);
}