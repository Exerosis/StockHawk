package com.udacity.stockhawk.implementation.controller.addstock;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.udacity.stockhawk.implementation.view.addstock.AddStock;
import com.udacity.stockhawk.implementation.view.addstock.AddStockListener;
import com.udacity.stockhawk.implementation.view.addstock.AddStockView;

public class AddStockDialog extends DialogFragment implements AddStockController {
    private AddStock view;
    private AddStockListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle state) {
        view = new AddStockView(LayoutInflater.from(getActivity()));
        view.setListener(listener);
        return view.getDialog();
    }

    @Override
    public AddStockListener getListener() {
        return listener;
    }

    @Override
    public void setListener(AddStockListener listener) {
        this.listener = listener;
        if (view != null)
            view.setListener(listener);
    }

    @Override
    public void showError(@StringRes int error) {
        view.showError(error);
    }
}
