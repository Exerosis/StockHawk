package com.udacity.stockhawk.implementation.model;

import rx.Observable;
import yahoofinance.Stock;

public class StockPref {

    public static Observable<Stock> fetchStock(final String symbol) {
       /* return new Observable<>(new Observab
        return new Observable<>(new Publisher<Stock>() {
            @Override
            public void subscribe(Subscriber<? super Stock> subscriber) {
                try {
                    subscriber.onNext(YahooFinance.getStock(symbol));
                    subscriber.onComplete();
                } catch (IOException e) {
                    subscriber.onError(e.getCause());
                }
            }
        }, new Function<Flowable<Object>, Publisher<?>>() {
            @Override
            public Publisher<?> apply(Flowable<Object> flowable) throws Exception {
                return flowable.delay(10, TimeUnit.SECONDS);
            }
        });*/
        return null;
    }
}
