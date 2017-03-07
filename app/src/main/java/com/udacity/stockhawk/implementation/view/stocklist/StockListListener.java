package com.udacity.stockhawk.implementation.view.stocklist;

import android.support.v4.widget.SwipeRefreshLayout;

import com.udacity.stockhawk.implementation.view.stocklist.holder.StockHolder;

public interface StockListListener extends SwipeRefreshLayout.OnRefreshListener {
    void onAddClicked();

    void onSwipe(StockHolder holder, int direction);
}
