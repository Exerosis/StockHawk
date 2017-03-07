package com.udacity.stockhawk.implementation.model;

import android.content.Context;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.orhanobut.hawk.Hawk;
import com.udacity.stockhawk.implementation.controller.details.Period;
import com.udacity.stockhawk.utilities.NetworkUtilities;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class StockStore {
    private static final Map<String, Stock> stockCache = new HashMap<>();
    private static final Map<String, Observable<Stock>> stockObservables = new HashMap<>();
    private static final Table<String, Period, List<HistoricalQuote>> historyCache = HashBasedTable.create();
    private static final Table<String, Period, Observable<List<HistoricalQuote>>> historyObservables = HashBasedTable.create();

    public static Observable<List<HistoricalQuote>> getHistory(Context context, String symbol, Period period) {
        if (historyObservables.contains(symbol, period))
            return historyObservables.get(symbol, period);

        Observable<List<HistoricalQuote>> observable = Observable.<List<HistoricalQuote>>create(subscriber -> {
            if (historyCache.contains(symbol, period))
                subscriber.onNext(historyCache.get(symbol, period));
            else
                getStock(context, symbol).observeOn(Schedulers.io()).subscribe(stock -> {
                    try {
                        Calendar from = Calendar.getInstance();
                        Interval interval = Interval.WEEKLY;
                        switch (period) {
                            case WEEK: {
                                from.add(Calendar.WEEK_OF_YEAR, -1);
                                interval = Interval.DAILY;
                                break;
                            }
                            case MONTH: {
                                from.add(Calendar.MONTH, -1);
                                interval = Interval.DAILY;
                                break;
                            }
                            case YEAR: {
                                from.add(Calendar.YEAR, -1);
                                interval = Interval.WEEKLY;
                                break;
                            }
                            default: {
                                from.add(Calendar.MONTH, -6);
                                interval = Interval.WEEKLY;
                                break;
                            }
                        }
                        List<HistoricalQuote> history = stock.getHistory(from, interval);
                        historyCache.put(symbol, period, history);
                        subscriber.onNext(history);
                    } catch (IOException exception) {
                        subscriber.onError(exception);
                    }
                });
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        historyObservables.put(symbol, period, observable);
        return observable;
    }


    public static Observable<Stock> getStock(Context context, String symbol) {
        if (stockObservables.containsKey(symbol))
            return stockObservables.get(symbol);
        Observable<Stock> observable = fetch(context, symbol).doOnNext(stock -> write(symbol, stock)).
                switchIfEmpty(read(symbol)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        stockObservables.put(symbol, observable);
        return observable;
    }

    private static Observable<Stock> fetch(Context context, String symbol) {
        if (NetworkUtilities.isOnline(context))
            return Observable.fromCallable(() -> YahooFinance.get(symbol)).onErrorResumeNext(Observable.empty());
        return Observable.empty();
    }

    private static Stock write(String symbol, Stock stock) {
        stockCache.put(symbol, stock);
        Hawk.put(symbol, stock);
        return stock;
    }

    private static Observable<Stock> read(String symbol) {
        if (stockCache.containsKey(symbol))
            return Observable.fromCallable(() -> stockCache.get(symbol));
        if (Hawk.contains(symbol))
            return Observable.fromCallable(() -> Hawk.get(symbol));
        return Observable.empty();
    }
}