package com.example.pns.bttop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.*;
import android.content.Intent;

public class Main extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    final int ArduinoData = 1;
    final String LOG_TAG = "myLogs";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private static String MacAddress = "00:15:83:00:76:5A"; // MAC-адрес БТ модуля
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ConnectedThred MyThred = null;
    public TextView mytext,mytextV,mytextZ, mytextW;
   public int[] Volt = {1,2,3,4,5,6,7,8,9,10,20};
   public int[] Hertz = {1,3,5,10,100,200};
   public int[] Width = {32,64,96,128,160,192,224};
    Button b0, b1, b2, b3, b4, b5, b6;
    int vo=0,he=0,wi=0;
    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mytext = (TextView) findViewById(R.id.textView8);
        mytextV = (TextView) findViewById(R.id.textView);
        mytextZ = (TextView) findViewById(R.id.textView2);
        mytextW = (TextView) findViewById(R.id.textView12);

        if (btAdapter != null){
            if (btAdapter.isEnabled()){
                mytext.setText("Bluetooth включен.");
            }else
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

        }else
        {
            MyError("Fatal Error", "Bluetooth ОТСУТСТВУЕТ");
        }

        b0 = (Button) findViewById(R.id.button2);//led
        b1 = (Button) findViewById(R.id.button3);//up_voltage
        b2 = (Button) findViewById(R.id.button4);//down_voltage
        b3 = (Button) findViewById(R.id.button5);//up_hertz
        b4 = (Button) findViewById(R.id.button6);//down_hertz
        b5 = (Button) findViewById(R.id.button7); //up_width
        b6 = (Button) findViewById(R.id.button); //down_width




        b0.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MyThred.sendData("0");

            }
        });

        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               if(vo<10) {
                    vo=vo+1;
                    MyThred.sendData("1");
                   mytextV.setText(""+Volt[vo]);
                }
                }
        });

        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(vo>0) {
                   vo=vo-1;
                    MyThred.sendData("2");
                   mytextV.setText(""+Volt[vo]);
               }
            }
        });

        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               if(he<5) {
                    he=he+1;
                    MyThred.sendData("3");
                    mytextZ.setText(""+Hertz[he]);
                }
            }
        });

        b4.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(he>0) {
                    he=he-1;
                    MyThred.sendData("4");
                    mytextZ.setText(""+Hertz[he]);
               }
            }
        });

        b5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wi<6) {
                    wi=wi+1;
                    MyThred.sendData("5");
                    mytextW.setText(""+Width[wi]);
                }
            }
        });

        b6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wi>0) {
                    wi=wi-1;
                    MyThred.sendData("6");
                    mytextW.setText(""+Width[wi]);
                }
            }
        });




    h = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ArduinoData:
                    byte[] readBuf = (byte[]) msg.obj;
                    String strIncom = new String(readBuf, 0, msg.arg1);
                        mytext.setText("Данные от Arduino: " + strIncom);
                    break;

                    }



            }
        };


}

    @Override
    public void onResume() {
        super.onResume();
        if (btAdapter != null){
            if (btAdapter.isEnabled()){
                BluetoothDevice device = btAdapter.getRemoteDevice(MacAddress);
                Log.d(LOG_TAG, "***Получили удаленный Device***"+device.getName());
                mytext.setText("***Получили удаленный Device***"+device.getName());

                try {
                    btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    Log.d(LOG_TAG, "...Создали сокет...");
                    mytext.setText("...Создали сокет...");

                } catch (IOException e) {
                    MyError("Fatal Error", "В onResume() Не могу создать сокет: " + e.getMessage() + ".");
                    mytext.setText("Fatal Error В onResume() Не могу создать сокет: " + e.getMessage() + ".");
                }

                btAdapter.cancelDiscovery();
                Log.d(LOG_TAG, "***Отменили поиск других устройств***");

                Log.d(LOG_TAG, "***Соединяемся...***");
                mytext.setText("***Соединяемся...***");

                try {
                    btSocket.connect();
                    Log.d(LOG_TAG, "***Соединение успешно установлено***");
                    mytext.setText("***Соединение успешно установлено***");
                } catch (IOException e) {
                    try {
                        btSocket.close();
                    } catch (IOException e2) {
                        MyError("Fatal Error", "В onResume() не могу закрыть сокет" + e2.getMessage() + ".");
                        mytext.setText("Fatal Error В onResume() не могу закрыть сокет" + e2.getMessage() + ".");
                    }
                }

                MyThred = new ConnectedThred(btSocket);
                MyThred.start();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "...In onPause()...");

        if (btAdapter != null){
            if (btAdapter.isEnabled()){
                if (MyThred.status_OutStrem() != null) {
                    MyThred.cancel();
                }
                try     {
                    btSocket.close();
                } catch (IOException e2) {
                    MyError("Fatal Error", "В onPause() Не могу закрыть сокет" + e2.getMessage() + ".");
                    mytext.setText("Fatal Error В onPause() не могу закрыть сокет" + e2.getMessage() + ".");
                }
            }
        }
    }

    private void MyError(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }



    private class ConnectedThred extends Thread{
        private final BluetoothSocket copyBtSocket;
        private final OutputStream OutStrem;
        private final InputStream InStrem;

        public ConnectedThred(BluetoothSocket socket){
            copyBtSocket = socket;
            OutputStream tmpOut = null;
            InputStream tmpIn = null;
            try{
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e){}

            OutStrem = tmpOut;
            InStrem = tmpIn;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try{
                    bytes = InStrem.read(buffer);
                    h.obtainMessage(ArduinoData, bytes, -1, buffer).sendToTarget();
                }catch(IOException e){break;}
            }

        }

        public void sendData(String message) {
            byte[] msgBuffer = message.getBytes();
            Log.d(LOG_TAG, "***Отправляем данные: " + message + "***"  );
            mytext.setText("***Отправляем данные: " + message + "***"  );

            try {
                OutStrem.write(msgBuffer);
            } catch (IOException e) {}
        }

        public void cancel(){
            try {
                copyBtSocket.close();
            }catch(IOException e){}
        }

        public Object status_OutStrem(){
            if (OutStrem == null){return null;
            }else{return OutStrem;}
        }
    }
}