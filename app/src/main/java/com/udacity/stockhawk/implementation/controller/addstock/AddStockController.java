package com.udacity.stockhawk.implementation.controller.addstock;

import android.support.annotation.StringRes;

import com.udacity.stockhawk.implementation.view.addstock.AddStockListener;
import com.udacity.stockhawk.mvc.Listenable;

public interface AddStockController extends Listenable<AddStockListener> {
    void showError(@StringRes int error);
}
