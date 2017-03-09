package com.udacity.stockhawk.implementation.model;


import android.os.Parcel;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;

public class Store {
    private static final String KEY_STOCKS = "STOCKS";
    private static final String KEY_DISPLAY_MODE = "DISPLAY_MODE";
    private static List<StockModel> stocks;
    private static PublishSubject<Long> hook = PublishSubject.create();

    public static boolean getDisplayMode() {
        return Hawk.get(KEY_DISPLAY_MODE, true);
    }

    public static boolean toggleDisplayMode() {
        boolean displayMode = !getDisplayMode();
        Hawk.put(KEY_DISPLAY_MODE, displayMode);
        return displayMode;
    }

    public static List<StockModel> getStocks() {
        if (stocks != null)
            return stocks;
        stocks = new ArrayList<>();
        if (Hawk.contains(KEY_STOCKS))
            for (Parcel parcel : Hawk.<ArrayList<Parcel>>get(KEY_STOCKS))
                stocks.add(StockModel.CREATOR.createFromParcel(parcel));
        else
            addStock("NVDA");
        Observable.interval(5, TimeUnit.MINUTES).mergeWith(hook).subscribe(tick -> {
            if (stocks.size() < 1)
                return;
            List<Parcel> parcels = new ArrayList<>();
            for (StockModel stock : stocks) {
                Parcel parcel = Parcel.obtain();
                stock.writeToParcel(parcel, 0);
                parcels.add(parcel);
            }
            Hawk.put(KEY_STOCKS, parcels);
        });
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