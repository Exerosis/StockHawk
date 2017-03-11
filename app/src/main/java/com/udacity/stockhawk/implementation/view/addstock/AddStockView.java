package com.udacity.stockhawk.implementation.view.addstock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

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

    public AddStockView(LayoutInflater inflater, Bundle state) {
        view = inflater.inflate(R.layout.add_stock_view, null);
        ButterKnife.bind(this, view);
        if (state != null)
            input.setText(state.getString(STATE_INPUT));
    }

    public AddStockView(LayoutInflater inflater) {
        this(inflater, null);
    }

    @Override
    public Dialog getDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getRoot().getContext());
        builder.setView(getRoot());

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int id, KeyEvent event) {
                if (listener != null)
                    listener.onAdd(input.getText().toString());
                return true;
            }
        });
        builder.setMessage(text);
        builder.setPositiveButton(add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (listener != null)
                    listener.onAdd(input.getText().toString());
            }
        });
        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (listener != null)
                    listener.onCancel();
            }
        });
        Dialog dialog = builder.create();

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
    public Bundle getViewState() {
        Bundle bundle = new Bundle();
        bundle.putString(STATE_INPUT, input.getText().toString());
        return bundle;
    }
}
