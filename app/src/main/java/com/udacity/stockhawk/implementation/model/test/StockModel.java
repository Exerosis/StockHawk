package com.udacity.stockhawk.implementation.model.test;

import com.udacity.stockhawk.implementation.controller.details.Period;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import rx.Observable;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class StockModel {
    private static final DecimalFormat FORMAT_ABSOLUTE_CHANGE;
    private static final DecimalFormat FORMAT_PERCENT_CHANGE;
    private static final DecimalFormat FORMAT_PRICE;

    static {
        FORMAT_ABSOLUTE_CHANGE = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        FORMAT_ABSOLUTE_CHANGE.setPositivePrefix("+$");
        FORMAT_PERCENT_CHANGE = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        FORMAT_PERCENT_CHANGE.setMaximumFractionDigits(2);
        FORMAT_PERCENT_CHANGE.setMinimumFractionDigits(2);
        FORMAT_PERCENT_CHANGE.setPositivePrefix("+");
        FORMAT_PRICE = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    }

    //Observables
    private final Observable<String> priceObservable;
    private final Observable<String> absoluteChangeObservable;
    private final Observable<String> percentChangeObservable;
    private final Map<Period, Observable<List<HistoricalQuote>>> historyObservables = new HashMap<>();

    //Cache
    private final Map<Period, List<HistoricalQuote>> history = new HashMap<>();
    private String symbol;
    private String absoluteChange;
    private String percentChange;
    private String price;

    protected StockModel(String symbol) {
        Observable<StockQuote> quoteObservable = Network.getQuote(symbol);
        quoteObservable.subscribe(quote -> {
            absoluteChange = FORMAT_ABSOLUTE_CHANGE.format(quote.getChange().floatValue());
            percentChange = FORMAT_PERCENT_CHANGE.format(quote.getChangeInPercent().floatValue());
            price = FORMAT_PRICE.format(quote.getPrice().floatValue());
        });

        priceObservable = Observable.just(price).repeatWhen(o -> quoteObservable);
        absoluteChangeObservable = Observable.just(absoluteChange).repeatWhen(o -> quoteObservable);
        percentChangeObservable = Observable.just(percentChange).repeatWhen(o -> quoteObservable);

        for (Period period : Period.values()) {
            Observable<List<HistoricalQuote>> historyObservable = Network.getHistory(symbol, period);
            historyObservable.subscribe(history -> this.history.put(period, history));
            historyObservables.put(period, Observable.just(history.get(period)).repeatWhen(o -> historyObservable).replay(1).autoConnect());
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPrice() {
        return price;
    }

    public Observable<String> getPriceObservable() {
        return priceObservable;
    }

    public String getAbsoluteChange() {
        return absoluteChange;
    }

    public Observable<String> getAbsoluteChangeObservable() {
        return absoluteChangeObservable;
    }

    public String getPercentChange() {
        return percentChange;
    }

    public Observable<String> getPercentChangeObservable() {
        return percentChangeObservable;
    }

    public List<HistoricalQuote> getHistory(Period period) {
        return history.get(period);
    }

    public Observable<List<HistoricalQuote>> getHistoryObservable(Period period) {
        return historyObservables.get(period);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof StockModel && ((StockModel) object).symbol.equalsIgnoreCase(symbol);
    }
}