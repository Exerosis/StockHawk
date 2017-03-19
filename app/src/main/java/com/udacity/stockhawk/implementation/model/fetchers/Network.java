package com.udacity.stockhawk.implementation.model.fetchers;

import com.udacity.stockhawk.implementation.controller.details.Period;
import com.udacity.stockhawk.implementation.model.QuoteModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class Network {
    private static final Map<String, Stock> STOCKS = new HashMap<>();

    public static QuoteModel getQuote(String symbol) throws Exception {
        return new QuoteModel(getStock(symbol).getQuote(true));
    }

    public static List<QuoteModel> getHistory(QuoteModel quote, Period period) throws Exception {
        Calendar from = Calendar.getInstance();
        Interval interval;
        switch (period) {
            case WEEK: {
                from.add(Calendar.WEEK_OF_YEAR, -1);
                interval = Interval.DAILY;
                break;
            }
            case MONTH: {
                from.add(Calendar.MONTH, -1);
                interval = Interval.DAILY;
                break;
            }
            case YEAR: {
                from.add(Calendar.YEAR, -1);
                interval = Interval.WEEKLY;
                break;
            }
            default: {
                from.add(Calendar.MONTH, -6);
                interval = Interval.WEEKLY;
                break;
            }
        }

        List<QuoteModel> quotes = new ArrayList<>();
        for (HistoricalQuote historicalQuote : getStock(quote.getSymbol()).getHistory(from, interval))
            quotes.add(new QuoteModel(historicalQuote));
        quotes.set(0, quote);
        Collections.reverse(quotes);
        return quotes;
    }

    public static Stock getStock(String symbol) throws Exception {
        if (STOCKS.containsKey(symbol))
            return STOCKS.get(symbol);
        Stock stock = YahooFinance.get(symbol);
        if (stock == null)
            throw new NoSuchElementException("Stock could not be found!");
        STOCKS.put(symbol, stock);
        return stock;
    }
}