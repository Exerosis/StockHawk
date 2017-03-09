package com.udacity.stockhawk.implementation.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.implementation.controller.details.Period;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class StockModel implements Parcelable {
    private final BehaviorSubject<QuoteModel> quoteSubject = BehaviorSubject.create();
    private final BehaviorSubject<Map<Period, HistoryModel>> historiesSubject = BehaviorSubject.create();

    private final ListMultimap<Period, QuoteModel> histories = ArrayListMultimap.create();
    private String symbol;
    private QuoteModel quote;

    private StockModel(String symbol, QuoteModel restoredQuote) {
        this.symbol = symbol;

        Observable<StockQuote> observable = Observable.fromCallable(() -> {
            return YahooFinance.get("NVDA").getQuote();
        }).repeatWhen(o -> Observable.interval(20, TimeUnit.SECONDS)).replay(1).autoConnect();
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(val -> {
            System.out.println(val);
        });

        Observable<StockQuote> quoteObservable = Network.getQuote(symbol);
        quoteObservable.subscribe(quote -> {
            quoteSubject.onNext(this.quote = new QuoteModel(quote));
            historiesSubject.onNext(getHistories());
        });

        if (restoredQuote == null)
            quoteObservable.toBlocking().first();

        for (Period period : Period.values())
            Network.getHistory(symbol, period).subscribe(history -> {
                if (history.size() < 2)
                    return;
                histories.get(period).clear();
                for (HistoricalQuote quote : history)
                    histories.put(period, new QuoteModel(quote));
                historiesSubject.onNext(getHistories());
            });
    }

    public StockModel(String symbol) {
        this(symbol, null);
    }

    public BehaviorSubject<QuoteModel> getQuoteSubject() {
        return quoteSubject;
    }

    public QuoteModel getQuote() {
        return quote;
    }

    public Observable<HistoryModel> getHistorySubject(Period period) {
        return historiesSubject.filter(histories -> histories.containsKey(period)).map(histories -> histories.get(period));
    }

    //TODO maybe .replay(1).autoConnect()
    public Observable<Map<Period, HistoryModel>> getHistoriesSubject() {
        return historiesSubject.filter(histories -> {
            for (Period period : Period.values())
                if (!histories.containsKey(period))
                    return false;
            return true;
        });
    }

    public HistoryModel getHistory(Period period) {
        List<QuoteModel> history = histories.get(period);
        history = Lists.reverse(history);
        history.add(this.quote);
        return new HistoryModel(history);
    }

    public Map<Period, HistoryModel> getHistories() {
        Map<Period, HistoryModel> histories = new HashMap<>();
        for (Period period : Period.values())
            histories.put(period, getHistory(period));
        return histories;
    }

    public String getSymbol() {
        return symbol;
    }


    //--Parcelable--
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.symbol);
        out.writeParcelable(this.quote, flags);

        for (Period period : Period.values())
            out.writeList(histories.get(period));
    }

    public static final Parcelable.Creator<StockModel> CREATOR = new Parcelable.Creator<StockModel>() {
        @Override
        public StockModel createFromParcel(Parcel in) {
            StockModel stock = new StockModel(in.readString(), in.readParcelable(QuoteModel.class.getClassLoader()));
            for (Period period : Period.values())
                stock.histories.putAll(period, in.createTypedArrayList(QuoteModel.CREATOR));
            return stock;
        }

        @Override
        public StockModel[] newArray(int size) {
            return new StockModel[size];
        }
    };
}
