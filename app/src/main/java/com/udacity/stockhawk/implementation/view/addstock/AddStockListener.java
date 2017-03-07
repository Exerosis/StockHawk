package com.udacity.stockhawk.implementation.view.addstock;

public interface AddStockListener {
    boolean onAdd(String string);

    void onCancel();
}
