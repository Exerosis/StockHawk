package com.udacity.stockhawk.utilities;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Transformers {
    public static <T> Observable.Transformer<T, T> MAIN_THREAD() {
        return observable -> observable.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Observable.Transformer<T, T> IO_THREAD() {
        return observable -> observable.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }
}
