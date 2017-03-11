package com.udacity.stockhawk.implementation.model;

import android.os.Parcel;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

import com.udacity.stockhawk.utilities.Model;
import com.udacity.stockhawk.utilities.Modelable;

import java.util.List;

import static com.udacity.stockhawk.R.color.green_primary;
import static com.udacity.stockhawk.R.color.grey_primary;
import static com.udacity.stockhawk.R.color.red_primary;

public class HistoryModel implements Modelable {
    @ColorRes
    private int color = 0, darkColor = 0, accentColor = 0;
    private List<QuoteModel> quotes;

    public HistoryModel(@NonNull List<QuoteModel> quotes) {
        this.quotes = quotes;

        float first = 0, last = 0;
        if (quotes.size() > 1) {
            first = quotes.get(0).getOpen();
            last = quotes.get(quotes.size() - 1).getAdjustedClose();
        }
        color = first > last ? green_primary : first < last ? red_primary : grey_primary;
        darkColor = first > last ? green_primary : first < last ? red_primary : grey_primary;
        accentColor = first > last ? green_primary : first < last ? red_primary : grey_primary;
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