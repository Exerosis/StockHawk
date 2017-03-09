package com.udacity.stockhawk.implementation.view.stocklist;

import android.support.v4.widget.SwipeRefreshLayout;

import com.udacity.stockhawk.implementation.model.StockModel;

public interface StockListListener extends SwipeRefreshLayout.OnRefreshListener {
    void onAddClicked();

    void onRemove(StockModel stock);
}
