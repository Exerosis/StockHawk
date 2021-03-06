package com.udacity.stockhawk.implementation.controller.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.controller.details.container.StockDetailsContainerActivity;
import com.udacity.stockhawk.implementation.view.widget.StockWidgetService;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;

public class StockWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            Log.i("WIDGET", "onUpdate");
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
            Intent intent = new Intent(context, StockWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);

            views.setRemoteAdapter(R.id.stock_widget_list, intent);
            views.setPendingIntentTemplate(R.id.stock_widget_list, getActivity(context, 0, new Intent(context, StockDetailsContainerActivity.class), FLAG_UPDATE_CURRENT));
            manager.updateAppWidget(id, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}