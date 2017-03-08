package com.udacity.stockhawk.implementation.model;

import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import yahoofinance.YahooFinance;

import static rx.Observable.fromCallable;

public class StockStore {
    private static Map<String, Observable<Stock>> stockObservables = new HashMap<>();

    public static Observable<Stock> getStock(String symbol) {
        if (stockObservables.containsKey(symbol))
            return stockObservables.get(symbol);
        Observable<Stock> observable = fromCallable(() -> Hawk.<Stock>get(symbol)).filter(stock -> stock != null).mergeWith(
                fromCallable(() -> new Stock(YahooFinance.get(symbol))).doOnNext(stock -> Hawk.put(symbol, stock))).subscribeOn(Schedulers.io()).
                unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).retry(5).replay(1).autoConnect();
//        Observable<Stock> observable = Observable.<Stock>create(subscriber -> {
//            try {
//                subscriber.onNext(new Stock(YahooFinance.get(symbol)));
//            } catch (IOException e) {
//                subscriber.onError(e);
//            }
//        }).subscribeOn(Schedulers.io()).
//                unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(stock -> {

        });
        stockObservables.put(symbol, observable);
        return observable;
    }
}