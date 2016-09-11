package com.balaenterprises.arduinointegration;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static com.balaenterprises.arduinointegration.R.id.progressBar_red_textview;

public class MainActivity extends AppCompatActivity {
    private static final String NAME = "Arduino";
    ProgressBar[] progressBars;
    TextView[] textViews;
    Button settings,connect, s_btn,shift,new_btn, temperature_graph;
    int Select=0;
    TextView date,s_value;
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter bluetoothAdapter;
    DataTransfer dataTransfer;
    DataStorage dataStorage;
    boolean connected=false;
    AlertDialog dialog;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataStorage = new DataStorage(this,DataStorage.DATABASE_NAME,null,DataStorage.DATABASE_VERSION);
        db= dataStorage.getWritableDatabase();
        progressBars = new ProgressBar[3];
        textViews = new TextView[3];
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Arduino");
        date = (TextView) findViewById(R.id.date);
        s_value= (TextView) findViewById(R.id.S_value);


        progressBars[0] = (ProgressBar) findViewById(R.id.progressBar_red);
        progressBars[1]= (ProgressBar) findViewById(R.id.progressBar_blue);
        progressBars[2]= (ProgressBar) findViewById(R.id.progressBar_green);

        Data maxData = dataStorage.getMaxData();
        if(maxData!=null) {
            progressBars[0].setMax(maxData.red);
            progressBars[1].setMax(maxData.blue);
            progressBars[2].setMax(maxData.green);
        }else{

            progressBars[0].setMax(25);
            progressBars[1].setMax(25);
            progressBars[2].setMax(25);
        }
        // Initializing the Text Views
        textViews[0]= (TextView) findViewById(progressBar_red_textview);
        textViews[1] = (TextView) findViewById(R.id.progressBar_blue_textview);
        textViews[2]= (TextView) findViewById(R.id.progressBar_green_textview);

        Data data = dataStorage.getData();
        if(data!=null) {
            date.setText(data.date);
            s_value.setText("S="+data.red);
            textViews[0].setText(data.red+"");
            textViews[1].setText(data.blue+"");
            textViews[2].setText(data.green+"");
            progressBars[0].setProgress(data.red);
            progressBars[1].setProgress(data.blue);
            progressBars[2].setProgress(data.green);
        }else{
            date.setText(data.date);
            s_value.setText("S=0");
            textViews[0].setText("0");
            textViews[1].setText("0");
            textViews[2].setText("0");
            progressBars[0].setProgress(0);
            progressBars[1].setProgress(0);
            progressBars[2].setProgress(0);
        }
        settings= (Button) findViewById(R.id.settings);
        connect=(Button)findViewById(R.id.connect);
        new_btn= (Button) findViewById(R.id.new_btn);
        s_btn = (Button) findViewById(R.id.s_btn);
        shift= (Button) findViewById(R.id.shift);

        new_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                s_value.setText("S=0");
                textViews[0].setText("0");
                textViews[1].setText("0");
                textViews[2].setText("0");
                progressBars[0].setProgress(0);
                progressBars[1].setProgress(0);
                progressBars[2].setProgress(0);
                Date date1 = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                String dates= "Date: "+simpleDateFormat.format(date1);
                date.setText(dates);
                send_new();
                Toast.makeText(MainActivity.this,"Values Reset on "+dates,Toast.LENGTH_SHORT).show();
            }
        });
        temperature_graph = (Button) findViewById(R.id.temperature);

        temperature_graph.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this,ChartDisplay.class));
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connected)
                Toast.makeText(MainActivity.this,"Disconnected",Toast.LENGTH_SHORT).show();
                MainActivity.this.finish();
                startActivity(new Intent(MainActivity.this,Settings.class));
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        shift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(Select){
                    case 0:
                        Select =1;
                        shift.setText("Shift - Blue");
                        s_value.setText("S="+progressBars[Select].getProgress());
                        break;
                    case 1:
                        Select =2;
                        shift.setText("Shift - Green");
                        s_value.setText("S="+progressBars[Select].getProgress());
                        break;
                    case 2:
                        Select =0;
                        shift.setText("Shift - Red");
                        s_value.setText("S="+progressBars[Select].getProgress());
                        break;
                }
            }
        });
        s_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int prog = progressBars[Select].getProgress();
                int max = progressBars[Select].getMax();
                if(prog!=max) {
                    progressBars[Select].setProgress(prog + 1);
                    textViews[Select].setText((prog + 1) + "");
                    s_value.setText("S="+(prog+1));
                    send();
                }else{
                    Toast.makeText(MainActivity.this,"Maximum Reached",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void connect(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter!=null){
            if(bluetoothAdapter.isEnabled()) {

                if(!bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.startDiscovery();
                }
                Set<BluetoothDevice> paired_devices= bluetoothAdapter.getBondedDevices();
                if(!paired_devices.isEmpty()){
                    for(BluetoothDevice device: paired_devices){
                        listAdapter.add(device.getName()+"\n"+device.getAddress());
                    }
                    listAdapter.notifyDataSetChanged();
                    Snackbar snackbar =Snackbar.make(findViewById(android.R.id.content),"If your Device is not displayed",Snackbar.LENGTH_LONG).
                            setAction("Click Here", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
                                }
                            });
                    snackbar.setActionTextColor(getResources().getColor(android.R.color.holo_red_light));
                    snackbar.show();
                    builder.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String[] strings = listAdapter.getItem(i).split("\n");
                            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strings[1]);
                            new Client().execute(device);
                        }
                    });

                }else{
                    Snackbar.make(findViewById(android.R.id.content),"No Paired Devices",Snackbar.LENGTH_INDEFINITE)
                            .setAction("Click Here", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
                                }
                            });
                }
                builder.setTitle("Paired Devices");

                dialog = builder.create();
                dialog.show();


               // new Server().execute(bluetoothAdapter);
            }else{
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent,1);
            }
        }else {
            Toast.makeText(this,"This Device Does not Support Bluetooth",Toast.LENGTH_SHORT).show();
        }


    }

    private class DataTransfer extends AsyncTask<Void,String,Void>{
        OutputStream outputStream;
        BluetoothSocket socket;
        InputStream inputStream;
        DataTransfer(BluetoothSocket socket ){

            try {
                this.socket = socket;
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String[] value = values[0].split(";");
            if(value.length==3) {
                Select = 0;
                shift.setText("Shift - Red");
                s_value.setText("S=" + value[0]);

                int red = Integer.parseInt(value[0]);
                int blue = Integer.parseInt(value[1]);
                int green = Integer.parseInt(value[2]);
                boolean flag1, flag2, flag3;
                if (red < progressBars[0].getProgress()) {
                    red = progressBars[0].getProgress();
                    flag1 = false;
                } else
                    flag1 = true;
                if (blue < progressBars[1].getProgress()) {
                    blue = progressBars[1].getProgress();
                    flag2 = false;
                } else
                    flag2 = true;
                if (green < progressBars[2].getProgress()) {
                    green = progressBars[2].getProgress();
                    flag3 = false;
                } else
                    flag3 = true;
                textViews[0].setText(red + "");
                textViews[1].setText(blue + "");
                textViews[2].setText(green + "");
                progressBars[0].setProgress(red);
                progressBars[1].setProgress(blue);
                progressBars[2].setProgress(green);
                if (!flag1 && !flag2 && !flag3) {
                    send();
                }
            }else if(value.length==4){
                Date date1 = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                String dates= "Date: "+simpleDateFormat.format(date1);
                date.setText(dates);
                Toast.makeText(MainActivity.this,"Values Reset on "+dates,Toast.LENGTH_SHORT).show();
                Select = 0;
                shift.setText("Shift - Red");
                s_value.setText("S=0");
                textViews[0].setText("0");
                textViews[1].setText("0");
                textViews[2].setText("0");
                progressBars[0].setProgress(0);
                progressBars[1].setProgress(0);
                progressBars[2].setProgress(0);
            }else if(value.length==1){
                float temp = Float.parseFloat(value[0]);
                dataStorage.Store_Temperature(db,temp);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                int available=inputStream.available();
                byte[] buffer = new byte[1024];  // buffer store for the stream
                int bytes; // bytes returned from read()
                // Keep listening to the InputStream until an exception occurs
                while (true) {
                    try {
                        // Read from the InputStream
                        bytes = inputStream.read(buffer);
                        // Send the obtained bytes to the UI activity
                        String string = new String(buffer, 0,bytes);
                        publishProgress(string);
                    } catch (IOException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                Toast.makeText(MainActivity.this,"Connection Error, PLease check if the device is online",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            return null;
        }
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {

            }
        }
        public void cancel() {
            try {
                if(socket!=null)
                    socket.close();
            } catch (IOException e) { }
        }
    }

    private class Server extends AsyncTask<BluetoothAdapter,Void,Void>{
        BluetoothServerSocket bluetoothServerSocket;
        @Override
        protected Void doInBackground(BluetoothAdapter... bluetoothAdapters) {
            try {
                bluetoothServerSocket=bluetoothAdapters[0].listenUsingInsecureRfcommWithServiceRecord(NAME,MY_UUID);
                BluetoothSocket socket;
                // Keep listening until exception occurs or a socket is returned
                while (true) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Bluetooth Hotspot Started",Toast.LENGTH_SHORT).show();
                            }
                        });
                        socket = bluetoothServerSocket.accept();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this,"Timed Out Try connecting again",Toast.LENGTH_LONG).show();
                        break;
                    }
                    // If a connection was accepted
                    if (socket != null) {
                        // Do work to manage the connection (in a separate thread)
                        manageConnectedSocket(socket);
                        try {
                            bluetoothServerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"Connected", Toast.LENGTH_LONG).show();
            }
        });
        connected=true;
        dataTransfer= new DataTransfer( mmSocket);
        dataTransfer.execute();
        send();
    }

    private class Client extends AsyncTask<BluetoothDevice,Void,Void>{
        @Override
        protected Void doInBackground(BluetoothDevice... bluetoothDevices) {
            try {
                BluetoothSocket Socket = bluetoothDevices[0].createRfcommSocketToServiceRecord(MY_UUID);
                // Cancel discovery because it will slow down the connection
                bluetoothAdapter.cancelDiscovery();

                try {
                    // Connect the device through the socket. This will block
                    // until it succeeds or throws an exception
                    Socket.connect();
                } catch (IOException connectException) {
                    // Unable to connect; close the socket and get out
                    try {
                        Socket.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Connection Error",Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (IOException closeException) { }
                    return null;
                }

                // Do work to manage the connection (in a separate thread)
                manageConnectedSocket(Socket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private void send(){
        if(connected) {
            Data data = new Data();
            data.red =progressBars[0].getProgress();
            data.blue =progressBars[1].getProgress();
            data.green =progressBars[2].getProgress();
            String message = data.red + ";" + data.blue + ";" + data.green;
            dataTransfer.write(message.getBytes());
        }
    }
    public void send_new(){
        if(connected){
            String message = "0;0;0;0";
            dataTransfer.write(message.getBytes());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Data data = new Data();
        data.red =progressBars[0].getProgress();
        data.blue =progressBars[1].getProgress();
        data.green =progressBars[2].getProgress();
        data.date = date.getText().toString();
        dataStorage.storeData(data);
        if(dataTransfer!=null)
            dataTransfer.cancel();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_CANCELED){
            Toast.makeText(this,"Bluetooth Should be enabled to Connect",Toast.LENGTH_SHORT).show();

        }
    }
}
