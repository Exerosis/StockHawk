package com.udacity.stockhawk.implementation.view.widget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.details.Period;
import com.udacity.stockhawk.implementation.model.HistoryModel;
import com.udacity.stockhawk.implementation.model.QuoteModel;
import com.udacity.stockhawk.implementation.model.StockModel;
import com.udacity.stockhawk.implementation.model.fetchers.Store;

import java.util.List;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.VISIBLE;
import static com.udacity.stockhawk.implementation.controller.details.StockDetailsFragment.ARGS_STOCK;

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("TEST", "onGetViewFactory");

        return new RemoteViewsFactory() {
            private List<StockModel> stocks;
            private HistoryModel history;
            private SparkView chart;
            private float thickness;
            private int height;
            private int width;

            @Override
            public void onCreate() {
                stocks = Store.getStocks(getApplicationContext());

                chart = (SparkView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.spark_chart_layout, null);
                chart.setAdapter(new SparkAdapter() {
                    @Override
                    public int getCount() {
                        return history == null ? 0 : history.getQuotes().size();
                    }

                    @Override
                    public Object getItem(int index) {
                        return history.getQuotes().get(index);
                    }

                    @Override
                    public float getY(int index) {
                        return history.getQuotes().get(index).getAdjustedClose();
                    }
                });
                width = (int) applyDimension(COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.chart_width), getResources().getDisplayMetrics());
                height = (int) applyDimension(COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
                thickness = applyDimension(COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

                chart.setLineWidth(thickness);
                chart.layout(0, 0, width, height);
            }

            @Override
            public void onDestroy() {
                stocks = null;
                chart = null;
                history = null;
            }

            @Override
            public void onDataSetChanged() {
            }

            @Override
            public int getCount() {
                return stocks.size();
            }

            @Override
            public RemoteViews getViewAt(int index) {
                RemoteViews view = new RemoteViews(getApplicationContext().getPackageName(), R.layout.stock_view_holder);
                StockModel stock = stocks.get(index);
                QuoteModel quote = stock.getQuote();

                view.setViewVisibility(R.id.stock_holder_chart, VISIBLE);
                view.setTextViewText(R.id.stock_holder_price, String.valueOf(quote.getPrice()));
                view.setTextViewText(R.id.stock_holder_symbol, quote.getSymbol());
                view.setTextViewText(R.id.stock_holder_change, quote.getChange(getApplicationContext()));
                view.setInt(R.id.stock_holder_change, "setBackgroundColor", ContextCompat.getColor(StockWidgetService.this, quote.getColor()));

                view.setOnClickFillInIntent(R.id.stock_holder_layout, new Intent().putExtra(ARGS_STOCK, stock));

                synchronized (this) {
                    history = stock.getHistory(Period.MONTH);
                    chart.getAdapter().notifyDataSetChanged();
                    chart.setLineColor(ContextCompat.getColor(StockWidgetService.this, history.getColor()));
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    chart.draw(canvas);
                    view.setImageViewBitmap(R.id.stock_holder_image, bitmap);
                }
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