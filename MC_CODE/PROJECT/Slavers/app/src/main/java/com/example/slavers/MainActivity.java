package com.example.slavers;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener listener;
    String mygpslat;
    String mygpslon;


   long timetaken=0;
    Button buttonON, buttonOFF, showPaired, listen, sendmsg, sendbatteryinfo;
    TextView status, received, battery;
    ListView devicelist;
    String response="0";
    TextView timer;


    String distanceat="-m";



    int myposition = 0;
    BluetoothAdapter mybluetoothAdapter;
    Intent btEnablingIntent;
    int requestCodeForEnable;
    BluetoothDevice[] btArray;

    int batterylevel;
    BluetoothDevice mybtid;
    String bluetooth_name;
    String deviceHardwareAddress;


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "MYAPP";
    private static final UUID MY_UUID = UUID.fromString("4c27e22c-82e1-11ea-bc55-0242ac130003");


    AlertDialog alertDialog;

    SendReceive sendReceive, sendReceiveStatus;
    SendReceive sendReceivebattery;
    SendReceive sendjson;
    SendReceive calculate;



    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batterylevel = level;
            battery.setText(String.valueOf(level) + "%");
        }
    };


//////ALERT


/////////////




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewByIds();

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));



        ///////////////location code

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mygpslon=String.valueOf(location.getLongitude());
                mygpslat=String.valueOf(location.getLatitude());
                System.out.println("LATS"+mygpslat+"  "+mygpslon);
                battery.setText(String.valueOf(batterylevel)+"\n "+"lons "+mygpslon +"lats "+mygpslat);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, listener);



        ////////////////
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you wish to proceed?for the computaion");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(MainActivity.this, "Start Battery Monitoring Power Tutor Application", Toast.LENGTH_LONG).show();
                          response="1";

                    }
                });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {


            public void onClick(DialogInterface dialog, int which) {
                response="0";

                finish();
            }
        });

         alertDialog = alertDialogBuilder.create();
     //  alertDialog.show();






        mybluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable = 1;
        //deviceHardwareAddress = mybluetoothAdapter.getAddress();

        if(!mybluetoothAdapter.disable())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,requestCodeForEnable);

        }

        bluetoothOnMethod();
        bluetoothOFFMethod();
        listenconnection();
        onlistclick();
        sendmsg();
        sendbatteryinfo();





    }






    private void findViewByIds() {
        buttonON=(Button)findViewById(R.id.bton);
        buttonOFF=(Button)findViewById(R.id.btoff);
        devicelist=(ListView)findViewById(R.id.list_devices);
        status=(TextView)findViewById(R.id.status);
        listen=(Button)findViewById(R.id.listen);
        received=(TextView)findViewById(R.id.received);
        sendmsg=(Button)findViewById(R.id.sendMsg);
        battery=(TextView)findViewById(R.id.battery);
        sendbatteryinfo=(Button)findViewById(R.id.sendbatteryinfo);

        timer=(TextView)findViewById(R.id.timer);


    }




    private void sendbatteryinfo() {

        sendbatteryinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                bluetooth_name = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
                //String jsonString ="{\"phoneid\":\""+bluetooth_name+"\",\"batterylevel\": "+ String.valueOf(batterylevel) +"}";
                //String jsonString ="{\"phoneid\":\""+bluetooth_name+"\",\"batterylevel\":\""+ String.valueOf(batterylevel)+"\",\"lat\":\""+mygpslat+"\",\"lon\":\""+mygpslon+"\"}";

                JSONObject jsonObjectnew = new JSONObject();
                try {
                    jsonObjectnew.put("phoneid",bluetooth_name);
                    jsonObjectnew.put("batterylevel",String.valueOf(batterylevel));
                    jsonObjectnew.put("lat",String.valueOf(mygpslat));
                    jsonObjectnew.put("lon",String.valueOf(mygpslon));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonString= jsonObjectnew.toString();

                sendReceivebattery.write(jsonString.getBytes());

            }
        });

    }


    private void sendmsg() {

        sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // String string = "MESSAGE IS PHOTON ENERGY";

                String jsonString="{\"message\":\".\",\"DateOfRegistration\":\"2012-10-21T00:00:00+05:30\",\"Status\":0}";


                sendReceive.write(jsonString.getBytes());


            }
        });

    }

    public int[][] MatrixMultiplication(String numbers, String numbers1)
    {



        String[] tokens = numbers.split(" ");

        int[][] finalArray = new int[tokens.length/2][tokens.length/2];

        int i = 0;
        for(int k=0;k<tokens.length/2;k++)
        {
            for(int g=0;g<tokens.length/2;g++)
            {
                finalArray[k][g] = Integer.parseInt(tokens[i]);
                i++;
            }
        }

        String[] tokens1 = numbers1.split(" ");

        int[][] finalArray1 = new int[tokens1.length/2][tokens1.length/2];

        int j = 0;
        for(int q=0;q<tokens1.length/2;q++)
        {
            for(int w=0;w<tokens1.length/2;w++)
            {
                finalArray1[q][w] = Integer.parseInt(tokens1[j]);
                j++;
            }
        }

        int[][] finalResult = new int[tokens.length/2][tokens.length/2];

        for(int l=0;l<tokens.length/2;l++)
        {
            for(int m=0;m<tokens.length/2;m++)
            {
                finalResult[l][m]= 0;
                for(int y=0;y<tokens.length/2;y++)
                {
                    finalResult[l][m]= finalResult[l][m]+finalArray[l][y]*finalArray1[y][m];
                }
            }
        }






        return finalResult;


    }




    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case STATE_LISTENING :
                    status.setText("LISTENING");
                    break;
                case  STATE_CONNECTING:
                    status.setText("CONNECTING");
                    break;
                case STATE_CONNECTED:
                    status.setText("CONNECTED");

                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("CONN FAILED");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte [])msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);





                  //  received.setText(received.getText()+ tempMsg+ String.valueOf(batterylevel));
                  //  received.setText(received.getText()+ tempMsg);
                    JsonObject jsonObject = new JsonParser().parse(tempMsg).getAsJsonObject();

                    bluetooth_name = Settings.Secure.getString(getContentResolver(), "bluetooth_name");



                    if(jsonObject.has("monitor"))
                    {

                        bluetooth_name = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
                        //String jsonString ="{\"phoneid\":\""+bluetooth_name+"\",\"batterylevel\": "+ String.valueOf(batterylevel) +"}";
                        //String jsonString ="{\"phoneid\":\""+bluetooth_name+"\",\"batterylevel\":\""+ String.valueOf(batterylevel)+"\",\"lat\":\""+mygpslat+"\",\"lon\":\""+mygpslon+"\"}";

                        JSONObject jsonObjectnew = new JSONObject();
                        try {
                            jsonObjectnew.put("monitoring","1");
                            jsonObjectnew.put("phoneid",bluetooth_name);
                            jsonObjectnew.put("batterylevel",String.valueOf(batterylevel));
                            jsonObjectnew.put("lat",String.valueOf(mygpslat));
                            jsonObjectnew.put("lon",String.valueOf(mygpslon));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String jsonString= jsonObjectnew.toString();

                        sendReceivebattery.write(jsonString.getBytes());





                    }



                    if(jsonObject.has("ask"))
                    {
                        //distanceat=jsonObject.get("distance").getAsString();
                       // Toast.makeText(getApplicationContext(),distanceat.toString(),Toast.LENGTH_SHORT);
                        alertDialog.show();
                        String jsonString ="{\"phoneid\":\""+bluetooth_name+"\",\"response\":\""+String.valueOf(response)+"\"}";


                        sendReceiveStatus.write(jsonString.getBytes());
                    }


                    if(jsonObject.has("status"))
                    {
                        String jsonString ="{\"phoneid\":\""+bluetooth_name+"\",\"checkStatus\":\""+String.valueOf(1)+"\"}";
                        sendReceiveStatus.write(jsonString.getBytes());
                    }



                    if(jsonObject.has("matrix_A")&&jsonObject.has("matrix_B")&&jsonObject.has("matrix_C")&&jsonObject.has("matrix_D"))
                    {
                        received.setText("matrixA" +jsonObject.get("matrix_A").getAsString()+"matrixB" +jsonObject.get("matrix_B").getAsString()+"matrixC" +jsonObject.get("matrix_C").getAsString()+"matrixD" +jsonObject.get("matrix_D").getAsString());

                        long startTime = System.nanoTime();

                        String numbers =jsonObject.get("matrix_A").getAsString();

                        String numbers1 =jsonObject.get("matrix_C").getAsString();

                        String numbers2 =jsonObject.get("matrix_B").getAsString();

                        String numbers3 =jsonObject.get("matrix_D").getAsString();

                        int[][] resultOutput1 = MatrixMultiplication(numbers,numbers1);

                        int[][] resultOutput2 = MatrixMultiplication(numbers2,numbers3);

                        int[][] finalResult = new int[2][2];

                        for(int i=0;i<2;i++)
                        {
                            for(int j=0;j<2;j++)
                            {
                                finalResult[i][j] = resultOutput1[i][j]+resultOutput2[i][j];
                            }
                        }

                        String sentArray = "";
                        for(int e=0;e<2;e++)
                        {
                            for(int r=0;r<2;r++)
                            {
                                sentArray = sentArray.concat(String.valueOf(finalResult[e][r]));
                                if(e!=1 || r!=1)
                                {
                                    sentArray = sentArray.concat(" ");
                                }
                            }
                        }

                        long endTime = System.nanoTime();
                        timetaken=endTime-startTime;
                        timer.setText(timer.getText()+"\n"+"TIME TAKEN:"+timetaken+"nano seconds");



                        String jsonString ="{\"phoneid\":\""+bluetooth_name+"\",\"result\":\""+sentArray+"\"}";
                        sendReceive.write(jsonString.getBytes());

                    }
                    if(jsonObject.has("message"))
                    {
                        received.setText(received.getText()+ jsonObject.get("message").getAsString());

                    }





                    break;

            }

            return true;
        }
    });


    private class  Serverclass extends Thread
    {
        private BluetoothServerSocket serverSocket;
        public Serverclass() {
            try {
                serverSocket = mybluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            BluetoothSocket socket=null;
            while (socket==null)
            {

                try {

                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);


                }

                if(socket!=null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);


                    ///FOR MESSAGE
                    sendReceive=new SendReceive(socket);
                    sendReceive.start();


                    ////FOR BATTERY INFO
                    sendReceivebattery= new SendReceive(socket);
                    sendReceivebattery.start();
                      //for caluclate
                    calculate= new SendReceive(socket);
                    calculate.start();

                    sendReceiveStatus=new SendReceive(socket);
                    sendReceiveStatus.start();




                    break;

                }

            }


        }
    }

    private class  Clientclass extends Thread
    {
        private  BluetoothDevice device;
        private  BluetoothSocket socket;

        public  Clientclass(BluetoothDevice device1)
        {
            device = device1;
            try {
                socket=device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        public void  run()
        {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive=new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }


        }


    }

    private class  SendReceive extends Thread{

        private  final  BluetoothSocket bluetoothSocket;
        private  final InputStream inputStream;
        private  final OutputStream outputStream;

        public  SendReceive(BluetoothSocket socket)
        {
            bluetoothSocket =socket;
            InputStream tempIn = null;
            OutputStream tempOut =null;
            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;

        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try {
                    bytes=  inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();



                } catch (IOException e) {
                    e.printStackTrace();
                }


            }


        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }



    }



    private void listenconnection() {

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Serverclass serverclass = new Serverclass();
                serverclass.start();
            }
        });



    }



    private void onlistclick() {

        devicelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(position+"position");
                myposition=position;
                System.out.println("----c"+btArray[position]);
                mybtid=btArray[position];
                Clientclass clientclass = new Clientclass(btArray[position]);
                clientclass.start();
                status.setText("Connecting...");

            }
        });

    }



    private void bluetoothOFFMethod() {
        buttonOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mybluetoothAdapter.isEnabled())
                {mybluetoothAdapter.disable();}
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==requestCodeForEnable)
        {
            if(resultCode==RESULT_OK){
                Toast.makeText(getApplicationContext(),"BT ENABLED",Toast.LENGTH_LONG);}
        }
        else if(resultCode==RESULT_CANCELED){Toast.makeText(getApplicationContext(),"BT DIS_ENABLED",Toast.LENGTH_LONG);}

    }


    private void bluetoothOnMethod() {
        buttonON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mybluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "BT UNAV", Toast.LENGTH_LONG);
                } else if (!mybluetoothAdapter.isEnabled()) {
                    startActivityForResult(btEnablingIntent,requestCodeForEnable);

                }
                else if(mybluetoothAdapter.isEnabled()){ Toast.makeText(getApplicationContext(), "ALREADY ENABLED", Toast.LENGTH_LONG);
                }
            }

        });

    }
}
