package com.udacity.stockhawk.implementation.model;

import com.udacity.stockhawk.implementation.controller.details.Period;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

public class Network {
    private static final Map<String, Stock> STOCKS = new HashMap<>();

    public static QuoteModel getQuote(String symbol) throws IOException {
        return new QuoteModel(getStock(symbol).getQuote(true));
    }

    public static Map<Period, HistoryModel> getHistories(String symbol) throws IOException {
        Map<Period, HistoryModel> histories = new HashMap<>();
        for (Period period : Period.values())
            histories.put(period, getHistory(symbol, period));
        return histories;
    }

    public static HistoryModel getHistory(String symbol, Period period) throws IOException {
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

        List<QuoteModel> quotes = new ArrayList<>();
        for (HistoricalQuote historicalQuote : getStock(symbol).getHistory(from, interval))
            quotes.add(new QuoteModel(historicalQuote));
        Collections.reverse(quotes);
        quotes.add(getQuote(symbol));
        return new HistoryModel(quotes);
    }

    public static Stock getStock(String symbol) throws IOException {
        if (STOCKS.containsKey(symbol))
            return STOCKS.get(symbol);
        Stock stock = YahooFinance.get(symbol);
        STOCKS.put(symbol, stock);
        return stock;
    }


    public static <T> Observable.Transformer<T, T> getTransformer() {
        return observable -> observable.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}