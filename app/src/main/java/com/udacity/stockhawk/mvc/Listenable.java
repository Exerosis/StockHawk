package com.udacity.stockhawk.mvc;

public interface Listenable<T> {
    T getListener();

    void setListener(T listener);
}
