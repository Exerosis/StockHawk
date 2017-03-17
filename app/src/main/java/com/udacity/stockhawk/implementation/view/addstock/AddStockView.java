package com.udacity.stockhawk.implementation.view.addstock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.udacity.stockhawk.R;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AddStockView implements AddStock {
    private static final String STATE_INPUT = "INPUT";
    private final View view;
    private AddStockListener listener;

    @BindView(R.id.add_stock_input)
    protected EditText input;
    @BindString(R.string.add_stock_text)
    protected String text;
    @BindString(R.string.add_stock_add)
    protected String add;
    @BindString(R.string.add_stock_cancel)
    protected String cancel;

    public AddStockView(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.add_stock_view, null);
        ButterKnife.bind(this, view);
    }

    @Override
    public Dialog getDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getRoot().getContext());
        builder.setView(getRoot());

        input.setOnEditorActionListener((view1, id, event) -> {
            if (listener != null)
                listener.onAdd(input.getText().toString());
            return true;
        });
        builder.setMessage(text);
        builder.setPositiveButton(add, null);
        builder.setNegativeButton(cancel, (dialog, id) -> {
            if (listener != null)
                listener.onCancel();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button -> {
            if (listener != null)
                listener.onAdd(input.getText().toString());
        }));
        Window window = dialog.getWindow();
        if (window != null)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void showError(@StringRes int error) {
        input.setError(getRoot().getResources().getString(error));
        input.clearComposingText();
    }

    @Override
    public AddStockListener getListener() {
        return listener;
    }

    @Override
    public void setListener(AddStockListener listener) {
        this.listener = listener;
    }

    @Override
    public View getRoot() {
        return view;
    }

    @Override
    public void saveState(Bundle out) {
        out.putString(STATE_INPUT, input.getText().toString());
    }

    @Override
    public void loadState(Bundle in) {
        input.setText(in.getString(STATE_INPUT));
    }
}
