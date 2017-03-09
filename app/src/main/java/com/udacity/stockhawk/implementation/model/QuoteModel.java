package com.udacity.stockhawk.implementation.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class QuoteModel implements Parcelable {
    private static final DecimalFormat FORMAT_PRICE = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

    //Data
    private String symbol;
    private float open;
    private float low;
    private float high;
    private float close;
    private float adjustedClose;
    private Calendar date;
    private Long volume;

    //Color
    @ColorRes
    private int color;
    @ColorRes
    private int darkColor;
    @ColorRes
    private int accentColor;


    public QuoteModel(StockQuote quote) {
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
        symbol = quote.getSymbol();
        open = quote.getOpen().floatValue();
        low = quote.getLow().floatValue();
        high = quote.getHigh().floatValue();
        close = quote.getClose().floatValue();
        adjustedClose = quote.getAdjClose().floatValue();
        date = quote.getDate();
        volume = quote.getVolume();
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

    public Long getVolume() {
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

    //--Parcelable--
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.symbol);
        out.writeFloat(this.high);
        out.writeFloat(this.low);
        out.writeFloat(this.close);
        out.writeFloat(this.adjustedClose);
        out.writeSerializable(this.date);
        out.writeValue(this.volume);
        out.writeInt(this.color);
        out.writeInt(this.darkColor);
        out.writeInt(this.accentColor);
    }

    private QuoteModel(Parcel in) {
        this.symbol = in.readString();
        this.high = in.readFloat();
        this.low = in.readFloat();
        this.close = in.readFloat();
        this.adjustedClose = in.readFloat();
        this.date = (Calendar) in.readSerializable();
        this.volume = (Long) in.readValue(Long.class.getClassLoader());
        this.color = in.readInt();
        this.darkColor = in.readInt();
        this.accentColor = in.readInt();
    }

    public static final Parcelable.Creator<QuoteModel> CREATOR = new Parcelable.Creator<QuoteModel>() {
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