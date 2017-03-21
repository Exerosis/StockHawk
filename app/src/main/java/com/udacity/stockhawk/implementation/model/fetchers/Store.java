package com.udacity.stockhawk.implementation.model.fetchers;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.widget.StockWidget;
import com.udacity.stockhawk.implementation.model.StockModel;
import com.udacity.stockhawk.utilities.Model;

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

    private static List<StockModel> stocks;
    private static Subscription subscription;

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
    public static List<StockModel> getStocks(Context context) {
        if (stocks != null)
            return stocks;
        stocks = Collections.synchronizedList(new ArrayList<>());
        SharedPreferences preferences = context.getSharedPreferences(PREFS_STOCKS, MODE_PRIVATE);
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
                Intent intent = new Intent(context, StockWidget.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
                int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, StockWidget.class));
                widgetManager.notifyAppWidgetViewDataChanged(ids, R.id.stock_widget_list);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                context.sendBroadcast(intent);
                try {
                    stock.refresh();
                    SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_STOCKS, MODE_PRIVATE).edit();
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
        subscription = REFRESH_OBSERVABLE.subscribe(tick -> {
            for (StockModel stock : stocks)
                try {
                    stock.refresh();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
    }

    private static void unsubscribe() {
        if (subscription != null)
            subscription.unsubscribe();
    }
}