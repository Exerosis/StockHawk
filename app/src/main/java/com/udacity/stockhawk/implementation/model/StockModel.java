package com.udacity.stockhawk.implementation.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.udacity.stockhawk.implementation.controller.details.Period;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.subjects.BehaviorSubject;

import static com.udacity.stockhawk.implementation.model.Network.getTransformer;

public class StockModel implements Parcelable {
    private final BehaviorSubject<QuoteModel> quoteSubject;
    private final BehaviorSubject<Map<Period, HistoryModel>> historiesSubject;

    public static StockModel newInstance(@NonNull String symbol) throws IOException {
        return new StockModel(Network.getQuote(symbol), Network.getHistories(symbol));
    }

    private StockModel(@NonNull QuoteModel quote, @NonNull Map<Period, HistoryModel> histories) {
        this.quoteSubject = BehaviorSubject.create(quote);
        this.historiesSubject = BehaviorSubject.create(histories);

    }


    public QuoteModel getQuote() {
        return quoteSubject.getValue();
    }

    public Observable<QuoteModel> getQuoteSubject() {
        return quoteSubject.compose(getTransformer());
    }

    public Map<Period, HistoryModel> getHistories() {
        return historiesSubject.getValue();
    }

    public Observable<Map<Period, HistoryModel>> getHistoriesSubject() {
        return historiesSubject.compose(getTransformer());
    }

    public HistoryModel getHistory(Period period) {
        return getHistories().get(period);
    }

    public Observable<HistoryModel> getHistorySubject(Period period) {
        return historiesSubject.filter(history -> history.containsKey(period)).map(history -> history.get(period)).compose(getTransformer());
    }

    public void refresh() {
        try {
            QuoteModel quote = Network.getQuote(getQuote().getSymbol());
            quoteSubject.onNext(quote);
            for (HistoryModel history : historiesSubject.getValue().values())
                history.getQuotes().set(history.getQuotes().size() - 1, quote);
            historiesSubject.onNext(historiesSubject.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //--Parcelable--
    @Override
    public int describeContents() {
        return 0;
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

    public static final Parcelable.Creator<StockModel> CREATOR = new Parcelable.Creator<StockModel>() {
        @Override
        public StockModel createFromParcel(Parcel in) {
            QuoteModel quote = in.readParcelable(QuoteModel.class.getClassLoader());
            if (quote == null)
                throw new IllegalStateException("Quote cannot be null!");

            for (StockModel stock : Store.getStocks())
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