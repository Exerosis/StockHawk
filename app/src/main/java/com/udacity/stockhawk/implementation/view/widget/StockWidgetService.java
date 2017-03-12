package com.udacity.stockhawk.implementation.view.widget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
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

import static com.udacity.stockhawk.implementation.controller.details.StockDetailsFragment.ARGS_STOCK;

public class StockWidgetService extends RemoteViewsService {
    private HistoryModel history;
    private List<StockModel> stocks;
    private SparkView chart;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("TEST", "onGetViewFactory");

        chart = new SparkView(this);
        chart.setCornerRadius(80);
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
                if (index == 0)
                    return history.getQuotes().get(index).getOpen();
                return history.getQuotes().get(index).getAdjustedClose();
            }
        });


        return new RemoteViewsFactory() {
            @Override
            public void onCreate() {
                onDataSetChanged();
            }

            @Override
            public void onDataSetChanged() {
                stocks = Store.getStocks();
            }

            @Override
            public void onDestroy() {
                stocks = null;
                history = null;
                chart = null;
            }

            @Override
            public int getCount() {
                return stocks.size();
            }

            @Override
            public RemoteViews getViewAt(int index) {
                RemoteViews view = new RemoteViews(StockWidgetService.this.getPackageName(), R.layout.stock_view_holder);
                StockModel stock = stocks.get(index);

                QuoteModel quote = stock.getQuote();
                view.setTextViewText(R.id.stock_holder_price, String.valueOf(quote.getPrice()));
                view.setTextViewText(R.id.stock_holder_symbol, quote.getSymbol());
                view.setTextViewText(R.id.stock_holder_change, quote.getChange());
                view.setInt(R.id.stock_holder_change, "setBackgroundColor", ContextCompat.getColor(StockWidgetService.this, quote.getColor()));

                view.setOnClickFillInIntent(R.id.stock_holder_layout, new Intent().putExtra(ARGS_STOCK, stock));

                history = stock.getHistory(Period.MONTH);
                chart.getAdapter().notifyDataSetChanged();

                //TODO find the correct values to frame this view :|
                int width = 0;
                int height = 0;
                // view.setImageViewBitmap(R.id.image_chart_view, getBitmapFromView(chart, width, height));
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

    public static Bitmap getBitmapFromView(View view, int width, int height) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(canvas);
        return bitmap;
    }
}