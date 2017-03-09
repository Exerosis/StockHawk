package com.udacity.stockhawk.implementation.view.stocklist;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.implementation.view.stocklist.holder.StockHolder;
import com.udacity.stockhawk.implementation.view.stocklist.holder.StockViewHolder;

import butterknife.BindBitmap;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StockListView implements StockList {
    private final View view;
    private final Snackbar networkError;
    private final Snackbar stockError;
    private final Paint paint = new Paint();
    private StockListListener listener;

    @BindView(R.id.stock_list_recycler_view)
    protected RecyclerView recyclerView;
    @BindView(R.id.stock_list_refresh_layout)
    protected SwipeRefreshLayout refreshLayout;
    @BindView(R.id.stock_list_fab)
    protected FloatingActionButton fab;

    @BindString(R.string.network_error)
    protected String networkErrorMessage;
    @BindString(R.string.stock_error)
    protected String stockErrorMessage;

    @BindColor(R.color.red_accent)
    protected int red;

    @BindBitmap(R.drawable.ic_delete)
    protected Bitmap deleteIcon;

    public StockListView(LayoutInflater inflater, ViewGroup container) {
        view = inflater.inflate(R.layout.stock_list_view, container, false);
        ButterKnife.bind(this, view);

        paint.setColor(red);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder holder, int direction) {
                listener.onRemove(((StockHolder) holder).getStock());
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView view, RecyclerView.ViewHolder holder, float dX, float dY, int state, boolean active) {
                super.onChildDraw(canvas, view, holder, dX, dY, state, active);
                if (state != ItemTouchHelper.ACTION_STATE_SWIPE)
                    return;

                View itemView = holder.itemView;
                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / 3;

                RectF background;
                RectF icon;
                if (itemView.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
                    background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                    icon = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                } else {
                    background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                    icon = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                }
                canvas.drawRect(background, paint);
                canvas.drawBitmap(deleteIcon, null, icon, paint);
            }
        }).attachToRecyclerView(recyclerView);

        fab.setOnClickListener(fab -> {
            if (listener != null)
                listener.onAddClicked();
        });

        networkError = Snackbar.make(container, networkErrorMessage, Snackbar.LENGTH_INDEFINITE);
        stockError = Snackbar.make(container, stockErrorMessage, Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    public StockListListener getListener() {
        return listener;
    }

    @Override
    public void setListener(StockListListener listener) {
        this.listener = listener;
        refreshLayout.setOnRefreshListener(listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public RecyclerView.Adapter<StockViewHolder> getAdapter() {
        return recyclerView.getAdapter();
    }

    @Override
    public void setAdapter(@NonNull RecyclerView.Adapter<StockViewHolder> adapter) {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        refreshLayout.setRefreshing(refreshing);
    }

    @Override
    public void showNetworkError() {
        networkError.show();
    }

    @Override
    public void hideNetworkError() {
        networkError.dismiss();
    }

    @Override
    public void showStockError() {
        stockError.show();
    }

    @Override
    public void hideStockError() {
        stockError.dismiss();
    }

    @Override
    public View getRoot() {
        return view;
    }

    @Override
    public Bundle getViewState() {
        return null;
    }
}