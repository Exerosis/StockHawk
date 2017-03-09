package com.udacity.stockhawk.implementation.view.details;

import android.support.annotation.ColorRes;

import com.robinhood.spark.SparkAdapter;
import com.udacity.stockhawk.mvc.Adaptable;
import com.udacity.stockhawk.mvc.Listenable;
import com.udacity.stockhawk.mvc.ViewBase;

public interface StockDetails extends ViewBase, Listenable<StockDetailsListener>, Adaptable<SparkAdapter> {
    void setColor(@ColorRes int colorID, @ColorRes int darkColorID);
}