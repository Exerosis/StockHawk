package com.udacity.stockhawk.implementation.controller.stocklist.container;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.stocklist.StockListFragment;

public class StockListContainerActivity extends AppCompatActivity implements StockListContainerController {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);

        getSupportFragmentManager().beginTransaction().replace(getContainerID(), new StockListFragment()).commit();
    }

    @Override
    public int getContainerID() {
        return R.id.container;
    }
}