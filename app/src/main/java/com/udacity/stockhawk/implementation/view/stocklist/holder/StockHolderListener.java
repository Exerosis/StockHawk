package com.udacity.stockhawk.implementation.view.stocklist.holder;


import com.udacity.stockhawk.implementation.model.Stock;

public interface StockHolderListener {
    void onClick(Stock stock);
}
