package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.text.Layout;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utility;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.ChartActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Devin on 12/1/2016.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent){
        return new RemoteViewsFactory(){
            private Cursor cursor = null;

            @Override
            public void onCreate(){

            }

            @Override
            public void onDataSetChanged(){
                if(cursor != null){
                    cursor.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                cursor = getContentResolver().query(
                        Contract.Quote.uri,
                        Contract.Quote.QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL
                );
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy(){
                if(cursor != null){
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount(){
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position){
                if(position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)){
                    return null;
                }
                RemoteViews remoteViews = new RemoteViews(
                        getPackageName(), R.layout.widget_stock_list_item);

                //Attain the 3 values from the cursor that will be placed into the widget list item
                String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
                Float price = cursor.getFloat(Contract.Quote.POSITION_PRICE);
                Float percentChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                //Initialize DecimalFormat items for formatting prices and price change
                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

                //sadlkfjasdlkf
                String price_string = dollarFormat.format(price);
                String percentChange_string = percentageFormat.format(percentChange / 100);

                //Assign values to corresponding textviews in Widget list item
                remoteViews.setTextViewText(R.id.widget_stock_list_symbol, symbol);
                remoteViews.setTextViewText(R.id.widget_stock_list_price, price_string);
                remoteViews.setTextViewText(R.id.widget_stock_list_percent_change, percentChange_string);

                //launch ChartActivity onClick
                final Intent launchChartIntent = new Intent();
                launchChartIntent.putExtra(ChartActivity.CHART_STOCK_SYMBOL, symbol);
                String[] formattedHistory = Utility.formatHistoryData(
                        cursor.getString(Contract.Quote.POSITION_HISTORY));
                launchChartIntent.putExtra(ChartActivity.CHART_STOCK_HISTORY, formattedHistory);
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item, launchChartIntent);

                return remoteViews;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views){
            }

            @Override
            public RemoteViews getLoadingView(){
                return new RemoteViews(getPackageName(), R.layout.widget_stock_list_item);
            }

            @Override
            public int getViewTypeCount(){
                return 1;
            }

            @Override
            public long getItemId(int position){
                if(cursor.moveToPosition(position))
                    return cursor.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds(){
                return true;
            }
        };
    }
}
