package com.udacity.stockhawk.implementation.model.test;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.udacity.stockhawk.implementation.controller.details.Period;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public class Network {
    private static final Map<String, Observable<Stock>> stockObservables = new HashMap<>();
    private static final Map<String, Observable<StockQuote>> quoteObservables = new HashMap<>();
    private static final Table<String, Period, Observable<List<HistoricalQuote>>> historyObservables = HashBasedTable.create();
    private static final Map<String, Stock> stocks = new HashMap<>();
    private static final PublishSubject<Long> hook = PublishSubject.create();

    public static Observable<StockQuote> getQuote(String symbol) {
        if (quoteObservables.containsKey(symbol))
            return quoteObservables.get(symbol);
        Observable<StockQuote> observable = Observable.fromCallable(() -> {
            if (stocks.containsKey(symbol))
                return stocks.get(symbol).getQuote(true);
            Stock stock = YahooFinance.get(symbol);
            stocks.put(symbol, stock);
            return stock.getQuote();
        }).repeatWhen(o -> Observable.interval(1, TimeUnit.MINUTES).mergeWith(hook)).replay(1).autoConnect();
        quoteObservables.put(symbol, observable);
        return observable;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Observable<List<HistoricalQuote>> getHistory(String symbol, Period period) {
        if (historyObservables.contains(symbol, period))
            return historyObservables.get(symbol, period);
        Calendar from = Calendar.getInstance();
        Interval interval;
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

        Observable<List<HistoricalQuote>> observable = Observable.fromCallable(() -> {
            if (stocks.containsKey(symbol))
                return stocks.get(symbol).getHistory(from, interval);
            Stock stock = YahooFinance.get(symbol, from, interval);
            stocks.put(symbol, stock);
            return stock.getHistory();
        }).repeatWhen(o -> Observable.interval(6, TimeUnit.HOURS)).replay(1).autoConnect();
        historyObservables.put(symbol, period, observable);
        return observable;
    }

    public static Observable<Stock> getStock(String symbol) {
        return Observable.fromCallable(() -> {
            if (stocks.containsKey(symbol))
                return stocks.get(symbol);
            Stock stock = YahooFinance.get(symbol);
            stocks.put(symbol, stock);
            return stock;
        });
    }

    public static void refresh() {
        hook.onNext(1L);
    }
}