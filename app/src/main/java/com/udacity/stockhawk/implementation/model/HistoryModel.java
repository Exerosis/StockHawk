package com.udacity.stockhawk.implementation.model;

import android.os.Parcel;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

import com.udacity.stockhawk.utilities.Model;
import com.udacity.stockhawk.utilities.Modelable;

import java.util.ArrayList;
import java.util.List;

import static com.udacity.stockhawk.R.color.green_primary;
import static com.udacity.stockhawk.R.color.grey_primary;
import static com.udacity.stockhawk.R.color.red_primary;

public class HistoryModel implements Modelable {
    @ColorRes
    private int color = 0, darkColor = 0, accentColor = 0;
    private List<QuoteModel> quotes;

    public HistoryModel() {
        this(new ArrayList<>());
    }

    public HistoryModel(@NonNull List<QuoteModel> quotes) {
        this.quotes = quotes;
        refresh();
    }

    public void refresh() {
        float last = 0, first = 0;
        if (quotes.size() > 1) {
            last = quotes.get(0).getOpen();
            first = quotes.get(quotes.size() - 1).getAdjustedClose();
        }
        color = last > first ? red_primary : last < first ? green_primary : grey_primary;
        darkColor = last > first ? red_primary : last < first ? green_primary : grey_primary;
        accentColor = last > first ? red_primary : last < first ? green_primary : grey_primary;
    }

    public List<QuoteModel> getQuotes() {
        return quotes;
    }

    @ColorRes
    public int getColor() {
        return color;
    }

    @ColorRes
    public int getDarkColor() {
        return darkColor;
    }

    @ColorRes
    public int getAccentColor() {
        return accentColor;
    }


    //--Modelable--
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToModel(Model out) {
        out.writeInt(color);
        out.writeInt(darkColor);
        out.writeInt(accentColor);
        out.writeList(quotes);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(color);
        out.writeInt(darkColor);
        out.writeInt(accentColor);
        out.writeTypedList(quotes);
    }

    private HistoryModel(Parcel in) {
        color = in.readInt();
        darkColor = in.readInt();
        accentColor = in.readInt();
        quotes = in.createTypedArrayList(QuoteModel.CREATOR);
    }

    private HistoryModel(Model in) {
        color = in.readInt();
        darkColor = in.readInt();
        accentColor = in.readInt();
        quotes = in.readList(QuoteModel.CREATOR);
    }

    public static final Modelable.Creator<HistoryModel> CREATOR = new Modelable.Creator<HistoryModel>() {
        @Override
        public HistoryModel createFromModel(Model in) {
            return new HistoryModel(in);
        }

        @Override
        public HistoryModel createFromParcel(Parcel in) {
            return new HistoryModel(in);
        }

        @Override
        public HistoryModel[] newArray(int size) {
            return new HistoryModel[size];
        }
    };
}