package com.balaenterprises.arduinointegration;

import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartDisplay extends AppCompatActivity {
    XYPlot plot;
    DataStorage dataStorage;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        plot = (XYPlot) findViewById(R.id.plot);
        dataStorage= new DataStorage(this,DataStorage.DATABASE_NAME,null,DataStorage.DATABASE_VERSION);
        db = dataStorage.getWritableDatabase();
        // create a couple arrays of y-values to plot:
        Cursor c = dataStorage.Get_Temperature(db);
        final List<Number> XAxis_Temperature = new ArrayList<>();
        final List<Number> YAxis_Time = new ArrayList<>();
        if(c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US);
                try {
                    Date date = simpleDateFormat.parse(c.getString(c.getColumnIndex(DataStorage.TEMPERATURE_DATE)));
                    YAxis_Time.add(date.getMinutes());
                    XAxis_Temperature.add(Float.parseFloat(c.getString(c.getColumnIndex(DataStorage.TEMPERATURE_VALUE))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                c.moveToNext();

            }
            // turn the above arrays into XYSeries':
            // (Y_VALS_ONLY means use the element index as the x value)
            XYSeries series1 = new SimpleXYSeries(
                    XAxis_Temperature, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Time");
            // create formatters to use for drawing a series using LineAndPointRenderer
            // and configure them from xml:
            LineAndPointFormatter series1Format = new LineAndPointFormatter();
            series1Format.setPointLabelFormatter(new PointLabelFormatter());
            series1Format.configure(getApplicationContext(),
                    R.xml.line_point_formatter_with_labels);

            // just for fun, add some smoothing to the lines:
            series1Format.setInterpolationParams(
                    new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));


            // add a new series' to the xyplot:
            plot.addSeries(series1, series1Format);
            plot.setTitle("Temperature Graph");
            plot.setDomainLabel("Temperature");
            plot.setRangeLabel("Time");
            plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    int i = Math.round(((Number) obj).floatValue());
                    return toAppendTo.append(YAxis_Time.get(i));
                }
                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
            });
        }else{
            plot.setVisibility(View.INVISIBLE);
            Snackbar.make(findViewById(android.R.id.content),"No Temperature Data to Show",Snackbar.LENGTH_INDEFINITE).show();
        }

    }

    }


