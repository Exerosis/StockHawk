package com.udacity.stockhawk.implementation.model;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.udacity.stockhawk.implementation.controller.details.Period;
import com.udacity.stockhawk.implementation.model.fetchers.Network;
import com.udacity.stockhawk.implementation.model.fetchers.Store;
import com.udacity.stockhawk.utilities.Model;
import com.udacity.stockhawk.utilities.Modelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.subjects.BehaviorSubject;

import static com.udacity.stockhawk.utilities.Transformers.MAIN_THREAD;

public class StockModel implements Modelable {
    private final BehaviorSubject<QuoteModel> quoteSubject;
    private final BehaviorSubject<Map<Period, HistoryModel>> historiesSubject;

    public static StockModel newInstance(@NonNull String symbol) throws Exception {
        QuoteModel quote = Network.getQuote(symbol);

        Map<Period, HistoryModel> histories = new HashMap<>(4);
        histories.put(Period.MONTH, new HistoryModel(Network.getHistory(quote, Period.MONTH)));
        histories.put(Period.SIX_MONTH, new HistoryModel());
        histories.put(Period.YEAR, new HistoryModel());
        histories.put(Period.WEEK, new HistoryModel());
        return new StockModel(quote, histories);
    }

    private StockModel(@NonNull QuoteModel quote, @NonNull Map<Period, HistoryModel> histories) {
        this.quoteSubject = BehaviorSubject.create(quote);
        this.historiesSubject = BehaviorSubject.create(histories);
    }

    public String getSymbol() {
        return getQuote().getSymbol();
    }

    public QuoteModel getQuote() {
        return quoteSubject.getValue();
    }

    public Observable<QuoteModel> getQuoteSubject() {
        return quoteSubject.compose(MAIN_THREAD());
    }

    public Map<Period, HistoryModel> getHistories() {
        return historiesSubject.getValue();
    }

    public Observable<Map<Period, HistoryModel>> getHistoriesSubject() {
        return historiesSubject.compose(MAIN_THREAD());
    }

    public HistoryModel getHistory(Period period) {
        return getHistories().get(period);
    }

    public Observable<HistoryModel> getHistorySubject(Period period) {
        return historiesSubject.filter(history -> history.containsKey(period)).map(history -> history.get(period)).compose(MAIN_THREAD());
    }

    public void refresh() throws Exception {
        QuoteModel quote = Network.getQuote(getQuote().getSymbol());
        quoteSubject.onNext(quote);

        Map<Period, HistoryModel> histories = historiesSubject.getValue();
        for (Period period : Period.values()) {
            List<QuoteModel> quotes = histories.get(period).getQuotes();
            if (quotes.isEmpty())
                quotes.addAll(Network.getHistory(quote, period));
//            else
//                quotes.set(0, quote);
            histories.get(period).refresh();
        }
        historiesSubject.onNext(histories);
    }

    //--Modelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToModel(Model out) {
        out.writeModelable(getQuote());
        out.writeMap(getHistories());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(getQuote(), flags);
        Map<Period, HistoryModel> histories = getHistories();
        out.writeInt(histories.size());
        for (Map.Entry<Period, HistoryModel> entry : histories.entrySet()) {
            out.writeInt(entry.getKey().ordinal());
            out.writeParcelable(entry.getValue(), flags);
        }
    }

    public static final Modelable.Creator<StockModel> CREATOR = new Modelable.Creator<StockModel>() {
        @Override
        public StockModel createFromModel(Model in) {
            QuoteModel quote = in.readModelable(QuoteModel.CREATOR);
            for (StockModel stock : Store.getStocksUnsafe())
                if (stock.getQuote().equals(quote))
                    return stock;
            return new StockModel(quote, in.readMap(Period.class, HistoryModel.CREATOR));
        }

        @Override
        public StockModel createFromParcel(Parcel in) {
            QuoteModel quote = in.readParcelable(QuoteModel.class.getClassLoader());
            if (quote == null)
                throw new IllegalStateException("Quote cannot be null!");
            for (StockModel stock : Store.getStocksUnsafe())
                if (stock.getQuote().equals(quote))
                    return stock;

            Map<Period, HistoryModel> history = new HashMap<>();
            int size = in.readInt();
            for (int i = 0; i < size; i++)
                history.put(Period.values()[in.readInt()], in.readParcelable(HistoryModel.class.getClassLoader()));

            if (history.isEmpty())
                throw new IllegalStateException("History cannot be empty!");

            return new StockModel(quote, history);
        }

        @Override
        public StockModel[] newArray(int size) {
            return new StockModel[size];
        }
    };
}