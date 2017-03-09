package com.udacity.stockhawk.implementation.view.stocklist.holder;

import com.udacity.stockhawk.implementation.model.StockModel;

public interface StockHolderListener {
    void onClick(StockModel stock);
}
