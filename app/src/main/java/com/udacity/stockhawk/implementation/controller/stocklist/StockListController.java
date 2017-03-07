package com.udacity.stockhawk.implementation.controller.stocklist;

import com.udacity.stockhawk.implementation.view.addstock.AddStockListener;
import com.udacity.stockhawk.implementation.view.stocklist.StockListListener;
import com.udacity.stockhawk.implementation.view.stocklist.holder.StockHolderListener;

public interface StockListController extends StockHolderListener, StockListListener, AddStockListener {
}