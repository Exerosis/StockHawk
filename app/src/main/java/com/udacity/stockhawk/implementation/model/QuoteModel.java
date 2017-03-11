package com.udacity.stockhawk.implementation.model;

import android.os.Parcel;
import android.support.annotation.ColorRes;

import com.udacity.stockhawk.utilities.Model;
import com.udacity.stockhawk.utilities.Modelable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

import static com.udacity.stockhawk.R.color.green_accent;
import static com.udacity.stockhawk.R.color.green_primary;
import static com.udacity.stockhawk.R.color.grey_accent;
import static com.udacity.stockhawk.R.color.grey_primary;
import static com.udacity.stockhawk.R.color.grey_primary_dark;
import static com.udacity.stockhawk.R.color.red_accent;
import static com.udacity.stockhawk.R.color.red_primary;
import static com.udacity.stockhawk.R.color.red_primary_dark;

public class QuoteModel implements Modelable {
    private static final DecimalFormat FORMAT_ABSOLUTE_CHANGE;
    private static final DecimalFormat FORMAT_PERCENT_CHANGE;
    private static final DecimalFormat FORMAT_PRICE;

    static {
        FORMAT_ABSOLUTE_CHANGE = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        FORMAT_ABSOLUTE_CHANGE.setPositivePrefix("+$");
        FORMAT_PERCENT_CHANGE = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        FORMAT_PERCENT_CHANGE.setMaximumFractionDigits(2);
        FORMAT_PERCENT_CHANGE.setMinimumFractionDigits(2);
        FORMAT_PERCENT_CHANGE.setPositivePrefix("+");
        FORMAT_PRICE = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    }

    //Data
    private String symbol;
    private float open;
    private float low;
    private float high;
    private float close;
    private float adjustedClose;
    private Calendar date;
    private long volume;
    private String percentChange;
    private String absoluteChange;

    //Color
    @ColorRes
    private int color;
    @ColorRes
    private int darkColor;
    @ColorRes
    private int accentColor;


    public QuoteModel(StockQuote quote) {
        this(quote.getChange().floatValue());
        percentChange = FORMAT_PERCENT_CHANGE.format(quote.getChangeInPercent().floatValue() / 100);
        symbol = quote.getSymbol();
        open = quote.getOpen().floatValue();
        low = quote.getDayLow().floatValue();
        high = quote.getDayHigh().floatValue();
        close = quote.getPrice().floatValue();
        adjustedClose = close;
        date = quote.getLastTradeTime();
        volume = quote.getVolume();
    }

    public QuoteModel(HistoricalQuote quote) {
        this(quote.getAdjClose().floatValue() - quote.getOpen().floatValue());
        percentChange = FORMAT_PERCENT_CHANGE.format(100);
        symbol = quote.getSymbol();
        open = quote.getOpen().floatValue();
        low = quote.getLow().floatValue();
        high = quote.getHigh().floatValue();
        close = quote.getClose().floatValue();
        adjustedClose = quote.getAdjClose().floatValue();
        date = quote.getDate();
        volume = quote.getVolume();
    }

    private QuoteModel(float change) {
        absoluteChange = FORMAT_ABSOLUTE_CHANGE.format(change);
        color = change > 0 ? green_primary : change < 0 ? red_primary : grey_primary;
        darkColor = change > 0 ? grey_primary_dark : change < 0 ? red_primary_dark : grey_primary_dark;
        accentColor = change > 0 ? green_accent : change < 0 ? red_accent : grey_accent;
    }

    public String getPrice() {
        return FORMAT_PRICE.format(getAdjustedClose());
    }

    public String getChange() {
        return com.udacity.stockhawk.implementation.model.Store.getDisplayMode() ? percentChange : absoluteChange;
    }

    public String getAbsoluteChange() {
        return absoluteChange;
    }

    public String getPercentChange() {
        return percentChange;
    }

    public String getSymbol() {
        return symbol;
    }

    public float getOpen() {
        return open;
    }

    public float getLow() {
        return low;
    }

    public float getHigh() {
        return high;
    }

    public float getClose() {
        return close;
    }

    public float getAdjustedClose() {
        return adjustedClose;
    }

    public Calendar getDate() {
        return date;
    }

    public long getVolume() {
        return volume;
    }

    public int getColor() {
        return color;
    }

    public int getDarkColor() {
        return darkColor;
    }

    public int getAccentColor() {
        return accentColor;
    }


    @Override
    public boolean equals(Object object) {
        return object instanceof QuoteModel && ((QuoteModel) object).symbol.equals(symbol);
    }

    //--Modelable--
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToModel(Model out) {
        //Data
        out.writeString(symbol);
        out.writeFloat(high);
        out.writeFloat(low);
        out.writeFloat(close);
        out.writeFloat(adjustedClose);
        out.writeObject(date);
        out.writeLong(volume);
        out.writeString(percentChange);
        out.writeString(absoluteChange);

        //Colors
        out.writeInt(color);
        out.writeInt(darkColor);
        out.writeInt(accentColor);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        //Data
        out.writeString(symbol);
        out.writeFloat(high);
        out.writeFloat(low);
        out.writeFloat(close);
        out.writeFloat(adjustedClose);
        out.writeSerializable(date);
        out.writeLong(volume);
        out.writeString(percentChange);
        out.writeString(absoluteChange);

        //Colors
        out.writeInt(color);
        out.writeInt(darkColor);
        out.writeInt(accentColor);
    }

    private QuoteModel(Parcel in) {
        //Data
        symbol = in.readString();
        high = in.readFloat();
        low = in.readFloat();
        close = in.readFloat();
        adjustedClose = in.readFloat();
        date = (Calendar) in.readSerializable();
        volume = in.readLong();
        percentChange = in.readString();
        absoluteChange = in.readString();

        //Colors
        color = in.readInt();
        darkColor = in.readInt();
        accentColor = in.readInt();
    }

    private QuoteModel(Model in) {
        //Data
        symbol = in.readString();
        high = in.readFloat();
        low = in.readFloat();
        close = in.readFloat();
        adjustedClose = in.readFloat();
        date = in.readObject(Calendar.class);
        volume = in.readLong();
        percentChange = in.readString();
        absoluteChange = in.readString();

        //Colors
        color = in.readInt();
        darkColor = in.readInt();
        accentColor = in.readInt();
    }

    public static final Modelable.Creator<QuoteModel> CREATOR = new Modelable.Creator<QuoteModel>() {
        @Override
        public QuoteModel createFromModel(Model in) {
            return new QuoteModel(in);
        }

        @Override
        public QuoteModel createFromParcel(Parcel in) {
            return new QuoteModel(in);
        }

        @Override
        public QuoteModel[] newArray(int size) {
            return new QuoteModel[size];
        }
    };
}