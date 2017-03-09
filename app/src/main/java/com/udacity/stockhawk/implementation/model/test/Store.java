package com.udacity.stockhawk.implementation.model.test;


import android.support.v4.util.ArraySet;

import com.orhanobut.hawk.Hawk;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;

public class Store {
    private static final String KEY_STOCKS = "STOCKS";
    private static final String KEY_DISPLAY_MODE = "DISPLAY_MODE";
    private static ArraySet<StockModel> stocks;
    private static PublishSubject<Long> hook = PublishSubject.create();

    public static boolean getDisplayMode() {
        return Hawk.get(KEY_DISPLAY_MODE, true);
    }

    public static boolean toggleDisplayMode() {
        boolean displayMode = !getDisplayMode();
        Hawk.put(KEY_DISPLAY_MODE, displayMode);
        return displayMode;
    }


    public static ArraySet<StockModel> getStocks() {
        if (stocks == null) {
            if (Hawk.contains(KEY_STOCKS))
                stocks = Hawk.get(KEY_STOCKS);
            else {
                stocks = new ArraySet<>();
                addStock("NVDA");
            }
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

    public static int removeStock(StockModel stock) {
        int index = stocks.indexOf(stock);
        stocks.remove(stock);
        save();
        return index;
    }

    public static void save() {
        hook.onNext(1L);
    }
}