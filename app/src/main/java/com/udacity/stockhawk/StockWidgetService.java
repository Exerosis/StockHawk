package com.udacity.stockhawk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.implementation.controller.details.Period;
import com.udacity.stockhawk.implementation.model.test.StockModel;
import com.udacity.stockhawk.implementation.model.test.Store;

import java.util.ArrayList;
import java.util.List;

import yahoofinance.histquotes.HistoricalQuote;

public class StockWidgetService extends RemoteViewsService {
    private List<HistoricalQuote> history = new ArrayList<>();
    private ArraySet<StockModel> stocks = new ArraySet<>();
    private SparkView chart;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("TEST", "onGetViewFactory");
        chart = new SparkView(this);
        chart.setCornerRadius(80);
        chart.setAdapter(new SparkAdapter() {
            @Override
            public int getCount() {
                return history.size();
            }

            @Override
            public Object getItem(int index) {
                return history.get(index);
            }

            @Override
            public float getY(int index) {
                return history.get(index).getClose().floatValue();
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
                stocks.clear();
            }

            @Override
            public int getCount() {
                return stocks.size();
            }

            @Override
            public RemoteViews getViewAt(int index) {
                RemoteViews view = new RemoteViews(StockWidgetService.this.getPackageName(), R.layout.stock_widget_view_holder);
                StockModel stock = stocks.valueAt(index);

                view.setTextViewText(R.id.stock_holder_price, stock.getPrice());
                view.setTextViewText(R.id.stock_holder_symbol, stock.getSymbol());
                view.setTextViewText(R.id.stock_holder_change, stock.getChange());
                view.setInt(R.id.stock_holder_change, "setBackgroundColor", stock.getColor());

                history = stock.getHistory(Period.MONTH);
                chart.getAdapter().notifyDataSetChanged();

                //TODO find the correct values to frame this view :|
                int width = 0;
                int height = 0;
                view.setImageViewBitmap(R.id.stock_holder_chart, getBitmapFromView(chart, width, height));
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

    /**
     * This method provided by Romain Guy, so it should do the job better, especially it includes case for listViews
     */
    public static Bitmap getBitmapFromView(View view, int width, int height) {

        //Pre-measure the view so that height and width don't remain null.
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        // Create bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);

        //Create a canvas with the specified bitmap to draw into
        Canvas canvas = new Canvas(bitmap);

        // if it's scrollView we get gull size
        canvas.translate(-view.getScrollX(), -view.getScrollY());
        //Render this view (and all of its children) to the given Canvas
        view.draw(canvas);
        return bitmap;
    }
}
