package com.udacity.stockhawk.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utility;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by Devin on 11/18/2016.
 */

public class ChartActivity extends AppCompatActivity {

    public static final String CHART_STOCK_SYMBOL = "css";
    public static final String CHART_STOCK_HISTORY = "csh";
    private String mStockSymbol;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    @BindView(R.id.chart_container)
    public LinearLayout chart_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chart);
        ButterKnife.bind(this);
        mStockSymbol = getIntent().getStringExtra(CHART_STOCK_SYMBOL);

        //do all of the chart work on a worker thread
        final Runnable task = new Runnable() {
            public void run() {
                setChart(getIntent().getStringArrayExtra(CHART_STOCK_HISTORY));
            }
        };
        scheduler.schedule(task, 0, TimeUnit.SECONDS);
        //Timber.e(getIntent().getStringArrayExtra(CHART_STOCK_HISTORY).toString());
    }

    private void setChart(String[] historyData) {
        //Set up our String arrays
        //splitHistory is a temp String[] that holds the x-value in [0] and the y-value in [1] while-
        //we are adding values to dataSeries
        historyData = Utility.reverseHistoryData(historyData);
        String[] splitHistory;
        float starting_value = 0;
        float finishing_value = 0;

        //Initialize some of our Charting objects
        XYSeries dataSeries = new XYSeries(mStockSymbol + this.getString(R.string.chart_legend_suffix));
        final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYSeriesRenderer priceRenderer = new XYSeriesRenderer();
        final XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();

        //Add entries to dataseries and add X-Axis labels
        for (int i = 0; i < historyData.length; i++) {
            splitHistory = historyData[i].split(",");
            if (i == 0) {
                starting_value = Float.valueOf(splitHistory[1]);
            }
            dataSeries.add(
                    i,
                    Float.valueOf(splitHistory[1])
            );
            if (i % 25 == 0) {
                multiRenderer.addXTextLabel(i, Utility.convertFromMilliToDate(splitHistory[0]));
            }
            finishing_value = Float.valueOf(splitHistory[1]);
        }

        //Fix up X-Axis settings
        multiRenderer.setXLabels(0);
        multiRenderer.setXAxisMin(-10);
        multiRenderer.setXAxisMax(115);
        multiRenderer.setXLabelsAlign(Paint.Align.CENTER);

        //Add dataset to dataseries and finish setting seriesRenderer settings
        dataset.addSeries(dataSeries);
        priceRenderer.setLineWidth(2);
        priceRenderer.setFillPoints(true);
        if (starting_value < finishing_value) {
            priceRenderer.setColor(Color.GREEN);
        } else {
            priceRenderer.setColor(Color.RED);
        }
        priceRenderer.setDisplayBoundingPoints(true);

        //Finish setting chart settings
        multiRenderer.setChartTitle(mStockSymbol + " " + this.getString(R.string.chart_title_suffix));
        multiRenderer.setXTitle(this.getString(R.string.chart_xTitle));
        multiRenderer.setYTitle(this.getString(R.string.chart_yTitle));
        multiRenderer.setChartTitleTextSize(54);
        multiRenderer.setAxisTitleTextSize(36);
        multiRenderer.setLabelsTextSize(36);
        multiRenderer.setShowLegend(false);
        multiRenderer.setZoomEnabled(false);
        multiRenderer.setExternalZoomEnabled(false);
        multiRenderer.setZoomButtonsVisible(false);
        multiRenderer.setClickEnabled(false);
        multiRenderer.setPanEnabled(false);
        multiRenderer.setInScroll(false);
        multiRenderer.setShowLabels(true);
        multiRenderer.setXLabelsAlign(Paint.Align.CENTER);
        multiRenderer.setYLabelsAlign(Paint.Align.LEFT);
        multiRenderer.setShowGridX(true);
        multiRenderer.setShowGridY(true);
        multiRenderer.setYLabelsPadding(-15);
        multiRenderer.setTextTypeface("sans-serif", Typeface.NORMAL);
        multiRenderer.setOrientation(
                XYMultipleSeriesRenderer.Orientation.HORIZONTAL);

        //Begin adding all the stuff
        multiRenderer.addSeriesRenderer(priceRenderer);
        final Context context = this;

        //Execute UI changes on UI thread
        ChartActivity.this.runOnUiThread(new Runnable() {
            Context c = context;
            XYMultipleSeriesDataset ds = dataset;
            XYMultipleSeriesRenderer mr = multiRenderer;

            public void run() {
                GraphicalView chart = ChartFactory.getLineChartView(c, ds, mr);
                chart_container.removeAllViews();
                chart_container.addView(chart);
            }
        });
    }
}