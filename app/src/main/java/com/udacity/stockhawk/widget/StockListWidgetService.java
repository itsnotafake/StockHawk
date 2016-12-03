package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

/**
 * Created by Devin on 12/1/2016.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent){
        return new RemoteViewsFactory(){
            private Cursor data = null;

            @Override
            public void onCreate(){

            }

            @Override
            public void onDataSetChanged(){
                if(data != null){
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(
                        Contract.Quote.uri,
                        Contract.Quote.QUOTE_COLUMNS,
                        null,
                        null,
                        null
                );
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy(){
                if(data != null){
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount(){
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position){
                if(position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)){
                    return null;
                }
                RemoteViews remoteViews = new RemoteViews(
                        getPackageName(), R.layout.widget_stock_list_item);
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                Float price = data.getFloat(Contract.Quote.POSITION_PRICE);
                Float percentChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                remoteViews.setTextViewText(R.id.widget_stock_list_symbol, symbol);
                remoteViews.setTextViewText(R.id.widget_stock_list_price, price.toString());
                remoteViews.setTextViewText(R.id.widget_stock_list_percent_change, percentChange.toString());

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
                if(data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds(){
                return true;
            }
        };
    }
}
