package com.udacity.stockhawk.implementation.view.stocklist;

import android.support.v7.widget.RecyclerView;

import com.udacity.stockhawk.implementation.view.stocklist.holder.StockViewHolder;
import com.udacity.stockhawk.mvc.Adaptable;
import com.udacity.stockhawk.mvc.Listenable;
import com.udacity.stockhawk.mvc.ViewBase;

public interface StockList extends ViewBase, Listenable<StockListListener>, Adaptable<RecyclerView.Adapter<StockViewHolder>> {
    void setRefreshing(boolean refreshing);

    void showNetworkError();

    void hideNetworkError();

    void showStockError();

    void hideStockError();
}
