package com.udacity.stockhawk.implementation.controller.addstock;

import com.udacity.stockhawk.implementation.view.addstock.AddStockListener;
import com.udacity.stockhawk.mvc.Listenable;

public interface AddStockController extends Listenable<AddStockListener> {
    void showError();
}
