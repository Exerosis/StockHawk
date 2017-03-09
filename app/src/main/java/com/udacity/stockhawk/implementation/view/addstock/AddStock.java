package com.udacity.stockhawk.implementation.view.addstock;

import android.app.Dialog;

import com.udacity.stockhawk.mvc.Listenable;
import com.udacity.stockhawk.mvc.ViewBase;

public interface AddStock extends ViewBase, Listenable<AddStockListener> {
    Dialog getDialog();

    void showError();
}
