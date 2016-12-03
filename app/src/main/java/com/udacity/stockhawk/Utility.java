package com.udacity.stockhawk;

import java.util.Date;

import timber.log.Timber;

/**
 * Created by Devin on 11/21/2016.
 */

public class Utility {

    public static String[] formatHistoryData(String history){
        String[] formattedHistory = history.split("\\r?\\n");
        return formattedHistory;
    }

    public static String convertFromMilliToDate(String s){
        Date date = new Date(Long.valueOf(s));
        int day = 1 + date.getDay();
        int month =  1 + date.getMonth();
        int year = 1900 + date.getYear();
        String sDate = month + "/" + day + "/" + year;
        return sDate;
    }

    public static String[] reverseHistoryData(String[] historyData){
        String[] reverseHistoryData = new String[historyData.length];
        for(int i = 0; i < historyData.length; i++){
            reverseHistoryData[(reverseHistoryData.length-1)-i] = historyData[i];
        }
        return reverseHistoryData;
    }
}
