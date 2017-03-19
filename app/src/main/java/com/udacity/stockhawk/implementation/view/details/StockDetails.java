package com.udacity.stockhawk.implementation.view.details;

import android.support.annotation.ColorRes;

import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.implementation.model.QuoteModel;
import com.udacity.stockhawk.mvc.Adaptable;
import com.udacity.stockhawk.mvc.Listenable;
import com.udacity.stockhawk.mvc.ViewBase;

public interface StockDetails extends ViewBase, Listenable<StockDetailsListener>, Adaptable<SparkAdapter> {
    void setColor(@ColorRes int colorID, @ColorRes int darkColorID);

    void setOnScrubListener(SparkView.OnScrubListener listener);

    void setAbsoluteChange(String absoluteChange);

    void setPercentChange(String percentChange);

    void setQuote(QuoteModel quote);
}