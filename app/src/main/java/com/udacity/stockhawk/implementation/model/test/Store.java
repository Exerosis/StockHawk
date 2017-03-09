package com.udacity.stockhawk.implementation.model.test;


import android.support.v4.util.ArraySet;

import com.orhanobut.hawk.Hawk;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;

public class Store {
    private static final String KEY_STOCKS = "STOCKS";
    private static ArraySet<StockModel> stocks;
    private static PublishSubject<Long> hook = PublishSubject.create();

    public static Collection<StockModel> getStocks() {
        if (stocks == null) {
            if (Hawk.contains(KEY_STOCKS))
                stocks = Hawk.get(KEY_STOCKS);
            else
                stocks = new ArraySet<>();
            Observable.interval(5, TimeUnit.MINUTES).mergeWith(hook).subscribe(tick -> {
                if (stocks.size() > 0)
                    Hawk.put(KEY_STOCKS, stocks);
            });
        }
        return stocks;
    }

    public static StockModel getStock(String symbol) {
        for (StockModel stock : stocks)
            if (stock.getSymbol().equalsIgnoreCase(symbol))
                return stock;
        return addStock(symbol);
    }

    public static StockModel addStock(String symbol) {
        StockModel stock = new StockModel(symbol);
        stocks.add(stock);
        save();
        return stock;
    }

    public static ArraySet<StockModel> removeStock(StockModel stock) {
        stocks.remove(stock);
        save();
        return stocks;
    }

    public static void save() {
        hook.onNext(1L);
    }
}