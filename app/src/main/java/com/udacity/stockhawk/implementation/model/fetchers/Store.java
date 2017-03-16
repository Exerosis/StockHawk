package com.udacity.stockhawk.implementation.model.fetchers;

import com.orhanobut.hawk.Hawk;
import com.udacity.stockhawk.implementation.model.StockModel;
import com.udacity.stockhawk.utilities.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static com.udacity.stockhawk.utilities.Transformers.IO_THREAD;

public class Store {
    private static final String KEY_STOCKS = "STOCKS";
    private static final String KEY_DISPLAY_MODE = "DISPLAY_MODE";
    private static final PublishSubject<Long> SAVE_HOOK = PublishSubject.create();
    private static final PublishSubject<Long> REFRESH_HOOK = PublishSubject.create();
    private static final Observable<Long> SAVE_OBSERVABLE = Observable.interval(2, TimeUnit.MINUTES).mergeWith(SAVE_HOOK).compose(IO_THREAD());
    private static final Observable<Long> REFRESH_OBSERVABLE = Observable.interval(30, TimeUnit.MINUTES).mergeWith(REFRESH_HOOK).compose(IO_THREAD());

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
        stocks = Collections.synchronizedList(new ArrayList<>());
        if (Hawk.contains(KEY_STOCKS))
            for (String json : Hawk.<ArrayList<String>>get(KEY_STOCKS))
                stocks.add(StockModel.CREATOR.createFromModel(Model.obtain(json)));
        if (!stocks.isEmpty())
            subscribe();
        refresh();
        return stocks;
    }

    public static Observable<Integer> addStock(String symbol) {
        return Observable.fromCallable(() -> {
            for (StockModel stock : stocks)
                if (stock.getQuote().getSymbol().equals(symbol))
                    throw new IllegalArgumentException("Stock already added!");
            StockModel stock = StockModel.newInstance(symbol);
            stocks.add(stock);
            subscribe();
            refresh();
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
                try {
                    stock.refresh();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
        saveSubscription = SAVE_OBSERVABLE.subscribe(tick -> {
            if (stocks.isEmpty())
                return;
            List<String> models = new ArrayList<>();
            for (StockModel stock : stocks)
                models.add(Model.obtain(stock).toString());
            Hawk.put(KEY_STOCKS, models);
        });
    }

    private static void unsubscribe() {
        saveSubscription.unsubscribe();
        refreshSubscription.unsubscribe();
    }
}