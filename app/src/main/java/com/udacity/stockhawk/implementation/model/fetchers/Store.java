package com.udacity.stockhawk.implementation.model.fetchers;

import android.content.Context;
import android.content.SharedPreferences;

import com.udacity.stockhawk.implementation.model.StockModel;
import com.udacity.stockhawk.utilities.Model;
import com.udacity.stockhawk.utilities.ObservableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static android.content.Context.MODE_PRIVATE;
import static com.udacity.stockhawk.utilities.Transformers.IO_THREAD;
import static com.udacity.stockhawk.utilities.Transformers.MAIN_THREAD;

public class Store {
    private static final String PREFS_STOCKS = "STOCKS";
    private static final String PREFS_DISPLAY_MODE = "DISPLAY_MODE";
    private static final String KEY_DISPLAY_MODE = "DISPLAY_MODE";
    private static final PublishSubject<Long> REFRESH_HOOK = PublishSubject.create();
    private static final Observable<Long> REFRESH_OBSERVABLE = Observable.interval(30, TimeUnit.MINUTES).mergeWith(REFRESH_HOOK).compose(IO_THREAD());

    private static ObservableList<StockModel> stocks;
    private static Subscription refreshSubscription;

    public static boolean getDisplayMode(Context context) {
        return context.getSharedPreferences(PREFS_DISPLAY_MODE, MODE_PRIVATE).getBoolean(KEY_DISPLAY_MODE, true);
    }

    public static boolean toggleDisplayMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_DISPLAY_MODE, MODE_PRIVATE);
        boolean displayMode = !preferences.getBoolean(KEY_DISPLAY_MODE, false);
        preferences.edit().putBoolean(KEY_DISPLAY_MODE, displayMode).apply();
        return displayMode;
    }

    @SuppressWarnings("unchecked")
    public static ObservableList<StockModel> getStocks(Context context) {
        if (stocks != null)
            return stocks;
        stocks = ObservableList.create(Collections.synchronizedList(new ArrayList<>()));
        SharedPreferences preferences = context.getSharedPreferences(PREFS_DISPLAY_MODE, MODE_PRIVATE);
        for (String json : (Collection<String>) preferences.getAll().values())
            stocks.add(StockModel.CREATOR.createFromModel(Model.obtain(json)));
        if (!stocks.isEmpty())
            subscribe();
        else
            unsubscribe();
        refresh();
        return stocks;
    }

    public static List<StockModel> getStocksUnsafe() {
        return stocks;
    }

    public static Observable<Integer> addStock(Context context, String symbol) {
        return Observable.fromCallable(() -> {
            for (StockModel stock : stocks)
                if (stock.getSymbol().equals(symbol))
                    throw new IllegalArgumentException("Stock already added!");
            StockModel stock = StockModel.newInstance(symbol);
            getStocks(context).add(stock);
            subscribe();
            Schedulers.io().createWorker().schedule(() -> {
                try {
                    stock.refresh();
                    SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_DISPLAY_MODE, MODE_PRIVATE).edit();
                    editor.putString(stock.getSymbol(), Model.obtain(stock).toString());
                    editor.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return stocks.indexOf(stock);
        }).compose(MAIN_THREAD());
    }

    public static int removeStock(Context context, StockModel stock) {
        int index = stocks.indexOf(stock);
        stocks.remove(stock);
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_DISPLAY_MODE, MODE_PRIVATE).edit();
        editor.remove(stock.getSymbol());
        editor.apply();
        if (stocks.isEmpty())
            unsubscribe();
        return index;
    }

    public static void save(Context context) {
        if (stocks.isEmpty())
            return;
        Schedulers.computation().createWorker().schedule(() -> {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_DISPLAY_MODE, MODE_PRIVATE).edit();
            for (StockModel stock : stocks) {
                try {
                    stock.refresh();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                editor.putString(stock.getSymbol(), Model.obtain(stock).toString());
            }
            editor.apply();
        });
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
    }

    private static void unsubscribe() {
        refreshSubscription.unsubscribe();
    }
}