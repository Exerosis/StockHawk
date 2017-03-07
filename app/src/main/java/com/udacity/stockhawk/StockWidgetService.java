package com.udacity.stockhawk;

import android.content.Intent;
import android.support.v4.util.ArraySet;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.implementation.model.PrefUtils;
import com.udacity.stockhawk.implementation.model.StockStore;

public class StockWidgetService extends RemoteViewsService {
    private ArraySet<String> symbols = new ArraySet<>();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("WIDGET", "getViews");
        symbols = PrefUtils.getStocks(StockWidgetService.this);

        return new RemoteViewsFactory() {

            @Override
            public void onCreate() {
                onDataSetChanged();
            }

            @Override
            public void onDataSetChanged() {
                symbols = PrefUtils.getStocks(StockWidgetService.this);
            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                return symbols.size();
            }

            @Override
            public RemoteViews getViewAt(int index) {
                RemoteViews view = new RemoteViews(StockWidgetService.this.getPackageName(), R.layout.stock_widget_holder);
                StockStore.getStock(StockWidgetService.this, symbols.valueAt(index)).subscribe(stock -> {
                    view.setTextViewText(R.id.textView, stock.getSymbol());
                });
                return view;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int index) {
                return index;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
