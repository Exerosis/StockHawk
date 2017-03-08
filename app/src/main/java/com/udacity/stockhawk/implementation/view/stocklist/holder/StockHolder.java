package com.udacity.stockhawk.implementation.view.stocklist.holder;

import com.udacity.stockhawk.implementation.model.Stock;
import com.udacity.stockhawk.mvc.Listenable;
import com.udacity.stockhawk.mvc.ViewBase;

public interface StockHolder extends ViewBase, Listenable<StockHolderListener> {
    void setStock(Stock stock);

    Stock getStock();
}