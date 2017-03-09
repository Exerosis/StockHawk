package com.udacity.stockhawk.implementation.model.test;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;

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

import static com.udacity.stockhawk.R.color.green_accent;
import static com.udacity.stockhawk.R.color.green_primary;
import static com.udacity.stockhawk.R.color.green_primary_dark;
import static com.udacity.stockhawk.R.color.grey_accent;
import static com.udacity.stockhawk.R.color.grey_primary;
import static com.udacity.stockhawk.R.color.grey_primary_dark;
import static com.udacity.stockhawk.R.color.red_accent;
import static com.udacity.stockhawk.R.color.red_primary;
import static com.udacity.stockhawk.R.color.red_primary_dark;

public class StockModel implements Parcelable {
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
    private final Observable<Integer> darkColorObservable;
    private final Observable<Integer> colorObservable;
    private final Observable<Integer> accentColorObservable;
    private final Map<Period, Observable<List<HistoricalQuote>>> historyObservables = new HashMap<>();

    //Cache
    private final Map<Period, List<HistoricalQuote>> history = new HashMap<>();
    private String symbol;
    private String absoluteChange;
    private String percentChange;
    private String price;
    private int color;
    private int darkColor;
    private int accentColor;

    protected StockModel(String symbol) {
        Observable<StockQuote> quoteObservable = Network.getQuote(symbol);
        quoteObservable.subscribe(quote -> {
            float change = quote.getChange().floatValue();

            color = change > 0 ? green_primary : change < 0 ? red_primary : grey_primary;
            darkColor = change > 0 ? green_primary_dark : change < 0 ? red_primary_dark : grey_primary_dark;
            accentColor = change > 0 ? green_accent : change < 0 ? red_accent : grey_accent;

            absoluteChange = FORMAT_ABSOLUTE_CHANGE.format(change);
            percentChange = FORMAT_PERCENT_CHANGE.format(quote.getChangeInPercent().floatValue());
            price = FORMAT_PRICE.format(quote.getPrice().floatValue());
        });

        colorObservable = Observable.just(color).repeatWhen(o -> quoteObservable);
        darkColorObservable = Observable.just(darkColor).repeatWhen(o -> quoteObservable);
        accentColorObservable = Observable.just(accentColor).repeatWhen(o -> quoteObservable);

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

    public String getChange() {
        return Store.getDisplayMode() ? percentChange : absoluteChange;
    }

    public Observable<String> getChangeObservable() {
        return Store.getDisplayMode() ? percentChangeObservable : absoluteChangeObservable;
    }

    
    @ColorRes
    public int getColor() {
        return color;
    }

    public Observable<Integer> getColorObservable() {
        return colorObservable;
    }

    @ColorRes
    public int getDarkColor() {
        return darkColor;
    }

    public Observable<Integer> getDarkColorObservable() {
        return darkColorObservable;
    }

    @ColorRes
    public int getAccentColor() {
        return accentColor;
    }

    public Observable<Integer> getAccentColorObservable() {
        return accentColorObservable;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}