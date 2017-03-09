package com.udacity.stockhawk.implementation.controller.details.container;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;

import static com.udacity.stockhawk.implementation.controller.details.StockDetailsFragment.ARGS_STOCK;
import static com.udacity.stockhawk.implementation.controller.details.StockDetailsFragment.newInstance;

public class StockDetailsContainerActivity extends AppCompatActivity implements StockDetailsContainerController {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);
        getSupportFragmentManager().beginTransaction().replace(getContainerID(), newInstance(getIntent().getExtras().getString(ARGS_STOCK))).commit();
    }

    @Override
    public int getContainerID() {
        return R.id.container;
    }
}