package com.udacity.stockhawk.implementation.model;

import com.orhanobut.hawk.Hawk;
import com.udacity.stockhawk.utilities.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class Store {
    private static final String KEY_STOCKS = "STOCKS";
    private static final String KEY_DISPLAY_MODE = "DISPLAY_MODE";
    private static final PublishSubject<Long> SAVE_HOOK = PublishSubject.create();
    private static final PublishSubject<Long> REFRESH_HOOK = PublishSubject.create();
    private static final Observable<Long> SAVE_OBSERVABLE = Observable.interval(30, TimeUnit.SECONDS).mergeWith(SAVE_HOOK).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io());
    private static final Observable<Long> REFRESH_OBSERVABLE = Observable.interval(1, TimeUnit.MINUTES).mergeWith(REFRESH_HOOK).mergeWith(SAVE_HOOK).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io());

    private static List<StockModel> stocks;
    private static Subscription saveSubscription;
    private static Subscription refreshSubscription;

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
            for (Model model : Hawk.<ArrayList<Model>>get(KEY_STOCKS))
                stocks.add(StockModel.CREATOR.createFromModel(model));
        subscribe();
        return stocks;
    }

    public static Observable<Integer> addStock(String symbol) {
        return Observable.fromCallable(() -> {
            StockModel stock = StockModel.newInstance(symbol);
            stocks.add(stock);
            if (!stocks.isEmpty())
                subscribe();
            save();
            return stocks.indexOf(stock);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static int removeStock(StockModel stock) {
        int index = stocks.indexOf(stock);
        stocks.remove(stock);
        if (stocks.isEmpty())
            unsubscribe();
        save();
        return index;
    }

    public static void save() {
        SAVE_HOOK.onNext(1L);
    }

    public static void refresh() {
        REFRESH_HOOK.onNext(1L);
    }

    private static void subscribe() {
        refreshSubscription = REFRESH_OBSERVABLE.subscribe(tick -> {
            for (StockModel stock : stocks)
                stock.refresh();
        });
        saveSubscription = SAVE_OBSERVABLE.subscribe(tick -> {
            if (stocks.isEmpty())
                return;
            List<Model> models = new ArrayList<>();
            for (StockModel stock : stocks)
                models.add(Model.obtain(stock));
            Hawk.put(KEY_STOCKS, models);
        });
    }

    private static void unsubscribe() {
        saveSubscription.unsubscribe();
        refreshSubscription.unsubscribe();
    }
}