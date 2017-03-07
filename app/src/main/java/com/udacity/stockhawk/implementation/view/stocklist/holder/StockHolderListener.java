package com.udacity.stockhawk.implementation.view.stocklist.holder;

import yahoofinance.Stock;

public interface StockHolderListener {
    void onClick(Stock stock);
}
