package com.udacity.stockhawk.implementation.view.details;

import com.udacity.stockhawk.implementation.controller.details.Period;

public interface StockDetailsListener {
    void periodChanged(Period period);
}
