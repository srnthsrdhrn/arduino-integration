package com.balaenterprises.arduinointegration;

/**
 * Created by singapore on 07-08-2016.
 */

public class Data {
    public int red, blue, green;
    public double temperature;
    public String date;

    public Data(int red, int blue, int green, double temperature,String date) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.temperature = temperature;
        this.date=date;
    }

    public Data(int red, int blue, int green) {
        this.red = red;
        this.blue = blue;
        this.green = green;
    }
    public Data(){

    }
}
