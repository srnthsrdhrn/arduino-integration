package com.balaenterprises.arduinointegration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by singapore on 07-08-2016.
 */

class DataStorage extends SQLiteOpenHelper {
    private Context context;
    private String crash_report_store="crash.txt";
    private String data_store ="values.txt";
    private String max_data="max.txt";
    public static String DATABASE_NAME = "arduino.db";
    public static int DATABASE_VERSION = 1;

    //Employee table details
    private String TEMPERATURE_TABLE = "employee";
    private String TEMPERATURE_PRIMARY_KEY = "id";
    public static String  TEMPERATURE_DATE= "date";
    public static String TEMPERATURE_VALUE = "value";
    private String TEMPERATURE_DATA_CREATE = " CREATE TABLE IF NOT EXISTS " + TEMPERATURE_TABLE + "(" + TEMPERATURE_PRIMARY_KEY +
            " INTEGER PRIMARY KEY AUTOINCREMENT," + TEMPERATURE_DATE + " TEXT NOT NULL," + TEMPERATURE_VALUE +
            " FLOAT NOT NULL )";


    public DataStorage(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TEMPERATURE_DATA_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void Store_Temperature(SQLiteDatabase db,float temperature){
        Cursor c=db.rawQuery("SELECT * FROM "+ TEMPERATURE_TABLE+" ORDER BY "+ TEMPERATURE_DATE,null);
        int a = c.getCount();
        Date date2 = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a",Locale.US);
        String formatted_date = simpleDateFormat.format(date2);
        if(c.moveToFirst()) {
            for(int i=0;i<a;i++) {
                try {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -1);
                long time = calendar.getTimeInMillis();
                String dates=c.getString(c.getColumnIndex(TEMPERATURE_DATE));
                Date check = simpleDateFormat.parse(dates);
                long check_time = check.getTime();
                    if(time>check_time){
                        db.execSQL("DELETE FROM "+ TEMPERATURE_TABLE+ " WHERE "+ TEMPERATURE_DATE +"=?",new String[]{dates});
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                c.moveToNext();
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(TEMPERATURE_VALUE, temperature);
            contentValues.put(TEMPERATURE_DATE, formatted_date);
            db.insert(TEMPERATURE_TABLE, null, contentValues);
        }else{
            ContentValues contentValues = new ContentValues();
            contentValues.put(TEMPERATURE_VALUE, temperature);
            contentValues.put(TEMPERATURE_DATE, formatted_date);
            db.insert(TEMPERATURE_TABLE, null, contentValues);
        }
        c.close();
    }

    public Cursor Get_Temperature(SQLiteDatabase db){
        return  db.rawQuery("SELECT * FROM "+ TEMPERATURE_TABLE,null);
    }

    Data getData() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String dates = simpleDateFormat.format(date);
        Data data= new Data(0,0,0,0.0,dates);
        String file_data = readFromFile(data_store);
        String[] values = file_data.split(";");
        if(values.length==5) {
            data.red = Integer.parseInt(values[0]);
            data.blue = Integer.parseInt(values[1]);
            data.green = Integer.parseInt(values[2]);
            data.temperature = Double.parseDouble(values[3]);
            data.date=values[4];
        }
            return data;

    }

    void storeData(Data data) {
        String file_data = data.red +";" +data.blue +";"+data.green +";"+data.temperature+";"+data.date;
        writeToFile(file_data,data_store);
    }

    Data getMaxData(){
        Data data = new Data();
        String file_data = readFromFile(max_data);
        String[] values = file_data.split(";");
        if(values.length==3) {
            data.red = Integer.parseInt(values[0]);
            data.blue = Integer.parseInt(values[1]);
            data.green = Integer.parseInt(values[2]);
            return data;
        }else{
            return null;
        }
    }

    void StoreMaxData(Data data){
        String file_data = data.red +";" +data.blue +";"+data.green;
        writeToFile(file_data,max_data);
    }
    private void writeToFile(String data,String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(String filename) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public void StoreCrashReport(String throwable){
        context.deleteFile(crash_report_store);
        writeToFile(throwable,crash_report_store);
    }
    public String GetCrashReport(){
        return readFromFile(crash_report_store);
    }
}
