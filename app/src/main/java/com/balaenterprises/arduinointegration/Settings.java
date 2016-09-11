package com.balaenterprises.arduinointegration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends AppCompatActivity {
    Button set;
    EditText red,blue,green;
    DataStorage dataStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        set= (Button) findViewById(R.id.set);
        red = (EditText) findViewById(R.id.redet);
        blue = (EditText) findViewById(R.id.blueet);
        green = (EditText) findViewById(R.id.greenet);
        dataStorage = new DataStorage(this,DataStorage.DATABASE_NAME,null,DataStorage.DATABASE_VERSION);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int sred= Integer.parseInt(red.getText().toString());
                int sblue= Integer.parseInt(blue.getText().toString());
                int sgreen = Integer.parseInt(green.getText().toString());
                Data data = new Data(sred,sblue,sgreen);
                dataStorage.StoreMaxData(data);
                Toast.makeText(Settings.this,"Max Value Updated",Toast.LENGTH_SHORT).show();
                red.setText("");
                green.setText("");
                blue.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this,MainActivity.class));
    }
}
