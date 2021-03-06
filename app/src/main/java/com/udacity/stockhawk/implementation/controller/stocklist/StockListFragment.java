package com.udacity.stockhawk.implementation.controller.stocklist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.addstock.AddStockDialog;
import com.udacity.stockhawk.implementation.controller.details.container.StockDetailsContainerActivity;
import com.udacity.stockhawk.implementation.model.StockModel;
import com.udacity.stockhawk.implementation.model.fetchers.Store;
import com.udacity.stockhawk.implementation.view.stocklist.StockList;
import com.udacity.stockhawk.implementation.view.stocklist.StockListView;
import com.udacity.stockhawk.implementation.view.stocklist.holder.StockViewHolder;
import com.udacity.stockhawk.utilities.NetworkUtilities;

import java.util.List;
import java.util.NoSuchElementException;

import static com.udacity.stockhawk.R.string.dialog_duplicate_stock;
import static com.udacity.stockhawk.R.string.dialog_error_blank;
import static com.udacity.stockhawk.R.string.dialog_error_network;
import static com.udacity.stockhawk.R.string.dialog_error_unexpected;
import static com.udacity.stockhawk.R.string.dialog_invalid_stock;
import static com.udacity.stockhawk.implementation.controller.details.StockDetailsFragment.ARGS_STOCK;

public class StockListFragment extends Fragment implements StockListController {
    private StockList view;
    private AddStockDialog dialog;
    private List<StockModel> stocks;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = new StockListView(inflater, container, (AppCompatActivity) getActivity());
        view.setListener(this);
        dialog = new AddStockDialog();
        dialog.setListener(this);

        setHasOptionsMenu(true);
        stocks = Store.getStocks(getContext());
        if (stocks.isEmpty())
            view.showStockError();

        view.setAdapter(new RecyclerView.Adapter<StockViewHolder>() {
            @Override
            public StockViewHolder onCreateViewHolder(ViewGroup parent, int type) {
                return new StockViewHolder(parent);
            }

            @Override
            public void onBindViewHolder(StockViewHolder holder, int position) {
                holder.setStock(stocks.get(position));
                holder.setListener(StockListFragment.this);
                view.hideStockError();
            }

            @Override
            public int getItemCount() {
                return stocks.size();
            }
        });

        return view.getRoot();
    }

    @Override
    public void onRefresh() {
        if (!NetworkUtilities.isOnline(getContext()))
            view.showNetworkError();
        else {
            Store.refresh();
            view.hideNetworkError();
        }
        if (stocks.isEmpty())
            view.showStockError();
        view.setRefreshing(false);
    }

    @Override
    public void onRemove(StockModel stock) {
        view.getAdapter().notifyItemRemoved(Store.removeStock(getContext(), stock));
        if (stocks.isEmpty())
            view.showStockError();
    }

    @Override
    public void onAdd(String symbol) {
        if (!NetworkUtilities.isOnline(getContext()))
            dialog.showError(dialog_error_network);
        else if (symbol == null || symbol.isEmpty())
            dialog.showError(dialog_error_blank);
        else
            Store.addStock(getContext(), symbol).subscribe(index -> {
                view.getAdapter().notifyItemInserted(index);
                view.hideNetworkError();
                view.hideStockError();
                dialog.dismissAllowingStateLoss();
            }, throwable -> {
                if (throwable instanceof IllegalArgumentException)
                    dialog.showError(dialog_duplicate_stock);
                else if (throwable instanceof NoSuchElementException)
                    dialog.showError(dialog_invalid_stock);
                else
                    dialog.showError(dialog_error_unexpected);
            });
    }

    @Override
    public void onCancel() {
        dialog.dismissAllowingStateLoss();
    }

    @Override
    public void onClick(StockModel stock) {
        startActivity(new Intent(getContext(), StockDetailsContainerActivity.class).putExtra(ARGS_STOCK, stock));
    }

    @Override
    public void onAddClicked() {
        dialog.show(getFragmentManager(), "AddStockDialog");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_stock_list, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        item.setIcon(Store.getDisplayMode(getContext()) ? R.drawable.ic_percentage : R.drawable.ic_dollar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.action_change_units)
            return super.onOptionsItemSelected(item);
        item.setIcon(Store.toggleDisplayMode(getContext()) ? R.drawable.ic_percentage : R.drawable.ic_dollar);
        onRefresh();
        return true;
    }

    @Override
    public void onStop() {
        Store.save(getContext());
        super.onStop();
    }
}