package com.udacity.stockhawk.implementation.controller.stocklist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArraySet;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.hawk.Hawk;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.addstock.AddStockDialog;
import com.udacity.stockhawk.implementation.controller.details.container.StockDetailsContainerActivity;
import com.udacity.stockhawk.implementation.model.PrefUtils;
import com.udacity.stockhawk.implementation.model.StockStore;
import com.udacity.stockhawk.implementation.view.stocklist.StockList;
import com.udacity.stockhawk.implementation.view.stocklist.StockListView;
import com.udacity.stockhawk.implementation.view.stocklist.holder.StockHolder;
import com.udacity.stockhawk.implementation.view.stocklist.holder.StockViewHolder;
import com.udacity.stockhawk.utilities.NetworkUtilities;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import yahoofinance.Stock;

import static com.udacity.stockhawk.implementation.controller.details.StockDetailsFragment.ARGS_SYMBOL;

public class StockListFragment extends Fragment implements StockListController {
    private static final String KEY_DISPLAY_MODE = "absolute";
    private ArraySet<String> symbols;
    private StockList view;
    private AddStockDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = new StockListView(inflater, container);
        view.setListener(this);
        dialog = new AddStockDialog();
        dialog.setListener(this);

        Hawk.init(getContext()).build();

        Observable.timer(30, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(tick -> view.getAdapter().notifyDataSetChanged());

        symbols = PrefUtils.getStocks(getContext());

        setHasOptionsMenu(true);

        view.setAdapter(new RecyclerView.Adapter<StockViewHolder>() {
            @Override
            public StockViewHolder onCreateViewHolder(ViewGroup parent, int type) {
                return new StockViewHolder(parent);
            }

            @Override
            public void onBindViewHolder(StockViewHolder holder, int position) {
                StockStore.getStock(getContext(), symbols.valueAt(position)).subscribe(stock -> {
                    holder.setStock(stock);
                    holder.setDisplayMode(getDisplayMode());
                    holder.setListener(StockListFragment.this);
                    view.hideStockError();
                    view.hideNetworkError();
                    view.setRefreshing(false);
                });
            }

            @Override
            public int getItemCount() {
                return symbols.size();
            }
        });

        return view.getRoot();
    }

    @Override
    public void onClick(Stock stock) {
        if (stock != null)
            startActivity(new Intent(getContext(), StockDetailsContainerActivity.class).
                    putExtra(ARGS_SYMBOL, stock.getSymbol()));
    }

    @Override
    public void onAddClicked() {
        dialog.show(getFragmentManager(), "AddStockDialog");
    }

    @Override
    public void onSwipe(StockHolder holder, int direction) {
        String symbol = holder.getStock().getSymbol();
        symbols.remove(symbol);
        PrefUtils.removeStock(getContext(), symbol);
        view.getAdapter().notifyItemRemoved(symbols.indexOf(symbol));
    }

    @Override
    public void onRefresh() {
        if (!NetworkUtilities.isOnline(getContext()))
            view.showNetworkError();
        else if (PrefUtils.getStocks(getContext()).size() == 0)
            view.showStockError();
        else
            view.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onAdd(String symbol) {
        if (symbol == null || symbol.isEmpty())
            return false;
        if (!NetworkUtilities.isOnline(getContext())) {
            view.showNetworkError();
            return false;
        }
        symbols.add(symbol);
        view.getAdapter().notifyItemInserted(symbols.indexOf(symbol));
        PrefUtils.addStock(getContext(), symbol);
        dialog.dismissAllowingStateLoss();
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        item.setIcon(getDisplayMode() ? R.drawable.ic_percentage : R.drawable.ic_dollar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.action_change_units)
            return super.onOptionsItemSelected(item);

        PrefUtils.toggleDisplayMode(getContext());
        item.setIcon(getDisplayMode() ? R.drawable.ic_percentage : R.drawable.ic_dollar);
        view.getAdapter().notifyDataSetChanged();
        return true;
    }

    @Override
    public void onCancel() {
        dialog.dismissAllowingStateLoss();
    }

    private boolean getDisplayMode() {
        return PrefUtils.getDisplayMode(getContext()).equals(KEY_DISPLAY_MODE);
    }
}