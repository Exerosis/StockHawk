package com.udacity.stockhawk.implementation.model;

import android.content.Context;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.details.Period;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

import static rx.Observable.interval;

public class Stock {
    private static final DecimalFormat FORMAT_CHANGE;
    private static final DecimalFormat FORMAT_PRICE;
    private static final DecimalFormat FORMAT_PERCENT_CHANGE;

    static {
        FORMAT_PRICE = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        FORMAT_CHANGE = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        FORMAT_CHANGE.setPositivePrefix("+$");
        FORMAT_PERCENT_CHANGE = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        FORMAT_PERCENT_CHANGE.setMaximumFractionDigits(2);
        FORMAT_PERCENT_CHANGE.setMinimumFractionDigits(2);
        FORMAT_PERCENT_CHANGE.setPositivePrefix("+");
    }

    private final Map<Period, Observable<List<HistoricalQuote>>> historyObservables = new HashMap<>();
    private final yahoofinance.Stock stock;

    private Observable<StockQuote> quoteObservable;

    public Stock(yahoofinance.Stock stock) {
        this.stock = stock;
    }

    public Observable<Integer> getColor() {
        return getQuote().map(quote -> {
            float change = quote.getChange().floatValue();
            return change > 0 ? R.color.green_primary : change == 0 ? R.color.grey_primary : R.color.red_primary;
        });
    }

    public Observable<Integer> getHistoricalColor(Period period) {
        return getHistory(period).map(history -> {
            if (history.size() < 2)
                return R.color.grey_primary;
            float first = history.get(0).getOpen().floatValue();
            float last = history.get(history.size() - 1).getClose().floatValue();
            return first < last ? R.color.green_primary : first == last ? R.color.grey_primary : R.color.red_primary;
        });
    }

    public Observable<List<HistoricalQuote>> getHistory(Period period) {
        if (historyObservables.containsKey(period))
            return historyObservables.get(period);
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
            List<HistoricalQuote> history = stock.getHistory(from, interval);
            Collections.reverse(history);
            getQuote().observeOn(Schedulers.io()).subscribe(quote -> history.add(new HistoricalQuote(stock.getSymbol(), Calendar.getInstance(),
                    quote.getOpen(), quote.getDayLow(), quote.getDayHigh(), quote.getPrice(), quote.getPrice(), quote.getVolume())));
            return history;
        }).retry(5).replay(1).autoConnect().repeatWhen(o -> interval(6, TimeUnit.HOURS)).
                observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io());
        historyObservables.put(period, observable);
        return observable;
    }

    public Observable<StockQuote> getQuote() {
        if (quoteObservable == null)
            quoteObservable = Observable.fromCallable(() -> stock.getQuote(true)).replay(1).autoConnect().repeatWhen(o -> interval(60, TimeUnit.SECONDS)).
                    observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io());
        return quoteObservable;
    }

    public Observable<String> getPrice() {
        return getQuote().map(quote -> FORMAT_PRICE.format(quote.getPrice().floatValue()));
    }

    public Observable<String> getChange(Context context) {
        return getQuote().map(quote -> {
            if (PrefUtils.getDisplayMode(context))
                return FORMAT_CHANGE.format(quote.getChange().floatValue());
            else
                return FORMAT_PERCENT_CHANGE.format(quote.getChangeInPercent().floatValue() / 100);
        });
    }

    public Observable<String> getAbsoluteChange() {
        return getQuote().map(quote -> FORMAT_CHANGE.format(quote.getChange().floatValue()));
    }

    public Observable<String> getPercentChange() {
        return getQuote().map(quote -> FORMAT_PERCENT_CHANGE.format(quote.getChangeInPercent().floatValue()));
    }

    public String getSymbol() {
        return stock.getSymbol();
    }
}
