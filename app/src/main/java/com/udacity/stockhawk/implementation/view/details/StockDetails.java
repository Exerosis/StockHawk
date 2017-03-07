package com.udacity.stockhawk.implementation.view.details;

import com.udacity.stockhawk.mvc.Listenable;
import com.udacity.stockhawk.mvc.ViewBase;

import java.util.List;

import yahoofinance.histquotes.HistoricalQuote;

public interface StockDetails extends ViewBase, Listenable<StockDetailsListener> {
    void setHistory(List<HistoricalQuote> stock);
}
