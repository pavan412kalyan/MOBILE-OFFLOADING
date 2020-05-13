package com.example.test1;

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
import android.graphics.Color;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener listener;
    String mygpslat;
    String mygpslon;


    long timetaken;




    Button buttonON, buttonOFF, showPaired, listen, sendmsg,sendmatrix,matrixmaster,monitior;
    TextView status, received, battery,displayMultiplicationStatus,slavedistance,displayTime;
    EditText editText1,editText2,editText3,editText4,editText5,editText6,editText7,editText8;
    ListView devicelist;
    String phoneId;

    TextView connectedDevices;
    TextView monitoring;

HashMap<String,String> deviceslist=new HashMap<String,String>();
HashMap<String,String> available=new HashMap<String,String>();

    BluetoothDevice bluetoothDevice;

    int myposition = 0;
    int alivestatus=0;

    String text1,text2,text3,text4,text5,text6,text7,text8;
    String finalText1,finalText2,finalText3,finalText4;

    BluetoothAdapter mybluetoothAdapter;
    Intent btEnablingIntent;
    int requestCodeForEnable;
    BluetoothDevice[] btArray;

    int batterylevel;
    String displayMsg;
    int counter = 0;

    SendReceive sendReceive;
    SendReceive sendReceiveMatrix;
    SendReceive ask;

    int[] displayarr1 = new int[4];
    int[] displayarr2 = new int[4];
    int[] displayarr3 = new int[4];
    int[] displayarr4 = new int[4];

    TextView textView1,textView2,textView3,textView4;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "MYAPP";
    private static final UUID MY_UUID = UUID.fromString("4c27e22c-82e1-11ea-bc55-0242ac130003");

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batterylevel = level;
            battery.setText(String.valueOf(level) + "%");
        }
    };










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
               battery.setText(String.valueOf(batterylevel)+"lons"+mygpslon +"lats"+mygpslat);
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




////////






        mybluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable=1;

        if(!mybluetoothAdapter.disable())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,requestCodeForEnable);

        }

        bluetoothOnMethod();
        bluetoothOFFMethod();
        showPaired();
        listenconnection();
        onlistclick();
        sendmsg();
        sendReceiveMatrix();
        monitior();

        matrixmaster.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                long startTime = System.nanoTime();
                String[] StringEditTextValues1 = editText1.getText().toString().split(" ");
                String[] StringEditTextValues2 = editText2.getText().toString().split(" ");
                String[] StringEditTextValues3 = editText3.getText().toString().split(" ");
                String[] StringEditTextValues4 = editText4.getText().toString().split(" ");
                String[] StringEditTextValues5 = editText5.getText().toString().split(" ");
                String[] StringEditTextValues6 = editText6.getText().toString().split(" ");
                String[] StringEditTextValues7 = editText7.getText().toString().split(" ");
                String[] StringEditTextValues8 = editText8.getText().toString().split(" ");

                int[] editTextValues1 = new int[4];
                int[] editTextValues2 = new int[4];
                int[] editTextValues3 = new int[4];
                int[] editTextValues4 = new int[4];
                int[] editTextValues5 = new int[4];
                int[] editTextValues6 = new int[4];
                int[] editTextValues7 = new int[4];
                int[] editTextValues8 = new int[4];

                for(int i=0;i<4;i++)
                {
                    editTextValues1[i] = Integer.parseInt(StringEditTextValues1[i]);
                    editTextValues2[i] = Integer.parseInt(StringEditTextValues2[i]);
                    editTextValues3[i] = Integer.parseInt(StringEditTextValues3[i]);
                    editTextValues4[i] = Integer.parseInt(StringEditTextValues4[i]);
                    editTextValues5[i] = Integer.parseInt(StringEditTextValues5[i]);
                    editTextValues6[i] = Integer.parseInt(StringEditTextValues6[i]);
                    editTextValues7[i] = Integer.parseInt(StringEditTextValues7[i]);
                    editTextValues8[i] = Integer.parseInt(StringEditTextValues8[i]);
                }

                int[][] array1 = new int[4][4];
                int[][] array2 = new int[4][4];

                int[][] finalResult = new int[4][4];

                for(int i=0;i<4;i++)
                {
                    for(int j=0;j<4;j++)
                    {
                        if(i==0)
                        {
                            array1[i][j] = editTextValues1[j];
                        }
                        else if(i==1)
                        {
                            array1[i][j] =editTextValues3[j];
                        }
                        else if(i==2)
                        {
                            array1[i][j] =editTextValues5[j];
                        }
                        else
                        {
                            array1[i][j] =editTextValues7[j];
                        }
                    }
                }


                for(int i=0;i<4;i++)
                {
                    for(int j=0;j<4;j++)
                    {
                        if(i==0)
                        {
                            array2[i][j] = editTextValues2[j];
                        }
                        else if(i==1)
                        {
                            array2[i][j] =editTextValues4[j];
                        }
                        else if(i==2)
                        {
                            array2[i][j] =editTextValues6[j];
                        }
                        else
                        {
                            array2[i][j] =editTextValues8[j];
                        }
                    }
                }

                for(int i=0;i<4;i++)
                {
                    for(int j=0;j<4;j++)
                    {
                        finalResult[i][j] = 0;
                        for(int k=0;k<4;k++)
                        {
                            finalResult[i][j] = finalResult[i][j]+array1[i][k]*array2[k][j];
                        }
                    }
                }

                for(int i=0;i<4;i++)
                {
                    String sentArray = "";
                    for(int j=0;j<4;j++)
                    {
                        sentArray = sentArray.concat(String.valueOf(finalResult[i][j])+" ");
                    }
                    if(i==0)
                    {
                        textView1.setText(sentArray);
                    }
                    else if(i==1)
                    {
                        textView2.setText(sentArray);
                    }
                    else if(i==2)
                    {
                        textView3.setText(sentArray);
                    }
                    else
                    {
                        textView4.setText(sentArray);
                    }
                }

                long endTime = System.nanoTime();
                long s=endTime-startTime;
                displayTime.setText("TIME TAKEN BY MASTER: "+String.valueOf(s)+" nano seconds");

            }




        }





        );



    }

    private void monitior() {


        monitior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject jsonObjectnew = new JSONObject();
                try {
                    jsonObjectnew.put("phoneid",phoneId);
                    jsonObjectnew.put("monitor","1");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonString= jsonObjectnew.toString();

               // String jsonString ="{\"monitor\":\"1\"}";
                sendReceive.write(jsonString.getBytes());
            }
        });





    }


    private void findViewByIds() {
        buttonON=(Button)findViewById(R.id.bton);
        buttonOFF=(Button)findViewById(R.id.btoff);
        showPaired=(Button)findViewById(R.id.showPaired) ;

       // connectedeviceslist=(ListView) findViewById(R.id.showcdevice);
        devicelist=(ListView)findViewById(R.id.list_devices);


        status=(TextView)findViewById(R.id.status);
        listen=(Button)findViewById(R.id.listen);
        received=(TextView)findViewById(R.id.received);
        sendmsg=(Button)findViewById(R.id.sendMsg);
        battery=(TextView)findViewById(R.id.battery);
        sendmatrix=(Button)findViewById(R.id.matrix);
        editText1 = (EditText)findViewById(R.id.editText1);
        editText2 = (EditText)findViewById(R.id.editText2);
        editText3 = (EditText)findViewById(R.id.editText3);
        editText4 = (EditText)findViewById(R.id.editText4);
        editText5 = (EditText)findViewById(R.id.editText5);
        editText6 = (EditText)findViewById(R.id.editText6);
        editText7 = (EditText)findViewById(R.id.editText7);
        editText8 = (EditText)findViewById(R.id.editText8);

        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);
        displayTime = (TextView)findViewById(R.id.timetakentext);

        displayMultiplicationStatus = (TextView)findViewById(R.id.textViewDisplay);

        matrixmaster = (Button)findViewById(R.id.matrixmaster);

        slavedistance=(TextView)findViewById(R.id.distance);

        connectedDevices=(TextView)findViewById(R.id.connectedDevices);

        monitior=(Button)findViewById(R.id.monitor);

        monitoring=(TextView)findViewById(R.id.monitoring);



    }







    private void sendmsg() {

        sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String jsonString ="{\"status\":\"0\"}";
                sendReceive.write(jsonString.getBytes());
            }
        });


    }

    public String convertToString(int[] arr)
    {
        String s= "";
        for(int i=0;i<arr.length;i++)
        {
            if(arr[i] != 0)
            {
                s = s.concat(String.valueOf(arr[i])+" ");
            }

        }
        return s;
    }


    public void sendReceiveMatrix() {

        sendmatrix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c=5;
                String[] StringEditTextValues1 = editText1.getText().toString().split(" ");
                String[] StringEditTextValues2 = editText2.getText().toString().split(" ");
                String[] StringEditTextValues3 = editText3.getText().toString().split(" ");
                String[] StringEditTextValues4 = editText4.getText().toString().split(" ");
                String[] StringEditTextValues5 = editText5.getText().toString().split(" ");
                String[] StringEditTextValues6 = editText6.getText().toString().split(" ");
                String[] StringEditTextValues7 = editText7.getText().toString().split(" ");
                String[] StringEditTextValues8 = editText8.getText().toString().split(" ");

                switch(counter)
                {
                    case 0:
                        text1 = StringEditTextValues1[0]+" "+StringEditTextValues1[1]+" ";
                        text3 = StringEditTextValues3[0]+" "+StringEditTextValues3[1];
                        finalText1 = text1+text3;

                        text1 = StringEditTextValues1[2]+" "+StringEditTextValues1[3];
                        text3 = " "+StringEditTextValues3[2]+" "+StringEditTextValues3[3];
                        finalText2 = text1+text3;

                        text2 = StringEditTextValues2[0]+" "+StringEditTextValues2[1]+" ";
                        text4 = StringEditTextValues4[0]+" "+StringEditTextValues4[1];
                        finalText3 = text2+text4;

                        text6 = StringEditTextValues6[0]+" "+StringEditTextValues6[1]+" ";
                        text8 = StringEditTextValues8[0]+" "+StringEditTextValues8[1];
                        finalText4 = text6+text8;
                        break;

                    case 1:
                        text1 = StringEditTextValues1[0]+" "+StringEditTextValues1[1]+" ";
                        text3 = StringEditTextValues3[0]+" "+StringEditTextValues3[1];
                        finalText1 = text1+text3;

                        text1 = StringEditTextValues1[2]+" "+StringEditTextValues1[3];
                        text3 = " "+StringEditTextValues3[2]+" "+StringEditTextValues3[3];
                        finalText2 = text1+text3;

                        text2 = StringEditTextValues2[2]+" "+StringEditTextValues2[3];
                        text4 = " "+StringEditTextValues4[2]+" "+StringEditTextValues4[3];
                        finalText3 = text2+text4;

                        text6 = StringEditTextValues6[2]+" "+StringEditTextValues6[3];
                        text8 = " "+StringEditTextValues8[2]+" "+StringEditTextValues8[3];
                        finalText4 = text6+text8;
                        break;

                    case 2:
                        text5 = StringEditTextValues5[0]+" "+StringEditTextValues5[1]+" ";
                        text7 = StringEditTextValues7[0]+" "+StringEditTextValues7[1];
                        finalText1 = text5+text7;

                        text5 = StringEditTextValues5[2]+" "+StringEditTextValues5[3];
                        text7 = " "+StringEditTextValues7[2]+" "+StringEditTextValues7[3];
                        finalText2 = text5+text7;

                        text2 = StringEditTextValues2[0]+" "+StringEditTextValues2[1]+" ";
                        text4 = StringEditTextValues4[0]+" "+StringEditTextValues4[1];
                        finalText3 = text2+text4;

                        text6 = StringEditTextValues6[0]+" "+StringEditTextValues6[1]+" ";
                        text8 = StringEditTextValues8[0]+" "+StringEditTextValues8[1];
                        finalText4 = text6+text8;
                        break;
                    case 3:
                        text5 = StringEditTextValues5[0]+" "+StringEditTextValues5[1]+" ";
                        text7 = StringEditTextValues7[0]+" "+StringEditTextValues7[1];
                        finalText1 = text5+text7;

                        text5 = StringEditTextValues5[2]+" "+StringEditTextValues5[3];
                        text7 = " "+StringEditTextValues7[2]+" "+StringEditTextValues7[3];
                        finalText2 = text5+text7;

                        text2 = StringEditTextValues2[2]+" "+StringEditTextValues2[3];
                        text4 = " "+StringEditTextValues4[2]+" "+StringEditTextValues4[3];
                        finalText3 = text2+text4;

                        text6 = StringEditTextValues6[2]+" "+StringEditTextValues6[3];
                        text8 = " "+StringEditTextValues8[2]+" "+StringEditTextValues8[3];
                        finalText4 = text6+text8;
                        break;

                    default:
                        Toast.makeText(getApplicationContext(),"Matrix Multiplication is Completed.Please Enter Another Matrix to Proceed",Toast.LENGTH_LONG);
                        editText1.setText("");
                        editText2.setText("");
                        editText3.setText("");
                        editText4.setText("");
                        editText5.setText("");
                        editText6.setText("");
                        editText7.setText("");
                        editText8.setText("");
                        break;



                }

            if(counter<4)
            {
                String jsonString ="{ \"matrix_A\" : \""+finalText1+"\",\"matrix_B\" :\""+finalText2+"\",\"matrix_C\":\""+finalText3+"\",\"matrix_D\":\""+finalText4+"\"}";
                //String jsonString="{\"message\":\"123\",\"DateOfRegistration\":\"2012-10-21T00:00:00+05:30\",\"Status\":0}";
                sendReceiveMatrix.write(jsonString.getBytes());
            }
            else
            {
                counter = 0;
                for(int i=0;i<4;i++)
                {
                    displayarr1[i] = 0;
                    displayarr2[i] = 0;
                    displayarr3[i] = 0;
                    displayarr4[i] = 0;
                }
            }


            }
        });



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
                        //  String jsonString ="{\"ask\":\"0\"}";

                        //  sendReceive.write(jsonString.getBytes());


                          Thread t = new Thread() {
                              @Override
                              public void run() {
                                  while (!isInterrupted()) {
                                      try {
                                          String jsonString ="{\"status\":\"0\"}";
                                          sendReceive.write(jsonString.getBytes());
                                          Thread.sleep(3000);  //1000ms = 1 sec
                                          runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  if(alivestatus == 1)
                                                  {
                                                      displayMultiplicationStatus.setText(phoneId+" is alive");
                                                      displayMultiplicationStatus.setTextColor(Color.parseColor("#008000"));
                                                      alivestatus = 0;


                                                  }
                                                  else
                                                  {
                                                      displayMultiplicationStatus.setText(phoneId+" is dead");
                                                      displayMultiplicationStatus.setTextColor(Color.parseColor("#FF0000"));


//                                                 available.put(phoneId,"dead");
                                                  }

                                              }
                                          });

                                      } catch (InterruptedException e) {
                                          e.printStackTrace();
                                      }
                                  }
                              }
                          };

//                          Thread t1 = new Thread() {
//                              @Override
//                              public void run() {
//                                  while (!isInterrupted()) {
//                                      try {
//                                          Thread.sleep(3000);  //1000ms = 1 sec
//                                          runOnUiThread(new Runnable() {
//                                              @Override
//                                              public void run() {
//                                                  if(displayMultiplicationStatus.getText().toString().contains("dead"))
//                                                  {
//                                                      if(deviceslist.containsKey(phoneId))
//                                                      {
//                                                          deviceslist.remove(phoneId);
//                                                      }
//
//                                                      if(deviceslist != null)
//                                                      {
//                                                          String listdevice="";
//                                                          for (String name: deviceslist.keySet()){
//                                                              String key = name.toString();
//                                                              String value = deviceslist.get(name).toString();
//                                                              // displayMsg=displayMsg+"<"+key+">"+"AVAILABLE DEVICES";
//                                                              listdevice=listdevice+ key+" : " + value+"\n";
//
//                                                          }
//
//                                                          connectedDevices.setText(listdevice);
//                                                      }
//                                                  }
//
//                                              }
//                                          });
//
//                                      } catch (InterruptedException e) {
//                                          e.printStackTrace();
//                                      }
//                                  }
//                              }
//                          };

                          t.start();
                          //t1.start();


                         break;
                      case STATE_CONNECTION_FAILED:
                          status.setText("CONN FAILED");
                          break;
                      case STATE_MESSAGE_RECEIVED:
                          byte[] readBuff = (byte [])msg.obj;
                          String tempMsg = new String(readBuff,0,msg.arg1);
                         // received.setText(received.getText()+ tempMsg+ String.valueOf(batterylevel));
                          JsonObject jsonObject = new JsonParser().parse(tempMsg).getAsJsonObject();


                          if(jsonObject.has("response")&&jsonObject.has("phoneid"))
                          {

                              phoneId =jsonObject.get("phoneid").getAsString();
                              String response =jsonObject.get("response").getAsString();
                              try {
                                  sleep(3000);
                              } catch (InterruptedException e) {
                                  e.printStackTrace();
                              }
                              if(response=="1")
                              {
                                 Toast.makeText(getApplicationContext(),phoneId+"  HAS ACCEPTED",Toast.LENGTH_SHORT);
                                 connectedDevices.setTextColor(Color.parseColor("#008000"));


                              }
                              else {
                                  Toast.makeText(getApplicationContext(),phoneId+"  HAS NOT ACCEPTED",Toast.LENGTH_SHORT);
                                  connectedDevices.setTextColor(Color.parseColor("#FF0000"));

                              }






                          }


                          //monitoringcode
                          if(jsonObject.has("monitoring"))
                          {
                              int batt = jsonObject.get("batterylevel").getAsInt();
                              String pid=jsonObject.get("phoneid").getAsString();


                             monitoring.setText("STARTED MONITORING:.."+pid+"-level is "+String.valueOf(batt));



                          }






                              if(jsonObject.has("checkStatus")&&jsonObject.has("phoneid"))
                          {
                              alivestatus=1;
                              phoneId =jsonObject.get("phoneid").getAsString();

                           //   available.put(phoneId,"alive");


                          }

                          if(jsonObject.has("lat") && jsonObject.has("lon")) {
                             String lats=jsonObject.get("lat").getAsString();
                              received.setText(received.getText() + lats);

                          }
                       if(jsonObject.has("batterylevel") && jsonObject.has("phoneid")&&jsonObject.has("lat") && jsonObject.has("lon")) {
                            int batterylevel = jsonObject.get("batterylevel").getAsInt();
                            status.setText("CONNECTED TO -->"+jsonObject.get("phoneid").getAsString());

                           String pid=jsonObject.get("phoneid").getAsString();
                           String slaveLat=jsonObject.get("lat").getAsString();
                           String slaveLon=jsonObject.get("lon").getAsString();
                           double x1= Double.parseDouble(mygpslat);
                           double y1= Double.parseDouble(mygpslon);
                          double x2= Double.parseDouble(slaveLat);
                          double y2= Double.parseDouble(slaveLon);
                         double dis= 1000*distance(x1,y1,x2,y2);
                         slavedistance.setText(String.valueOf(dis)+"--"+pid+"m");

                           //Toast.makeText(getApplicationContext(),String.valueOf(dis),Toast.LENGTH_SHORT);



                         //  if (batterylevel > 25 ) {
                        //////////////////////////////////////
                           String data="";

                          if (batterylevel > 25 && dis<120) {
                              String phd= jsonObject.get("phoneid").getAsString();

                                displayMsg = jsonObject.get("phoneid").getAsString() + "SATISFIED-->" + String.valueOf(batterylevel);
                                deviceslist.put(phoneId, String.valueOf(batterylevel));

///////////////////////////////////////////////////////////////
                              for (String name: deviceslist.keySet()){
                                  String key = name.toString();
                                  String value = deviceslist.get(name).toString();
                                  // displayMsg=displayMsg+"<"+key+">"+"AVAILABLE DEVICES";
                                  data=data+ key+" : " + value+"\n";

                              }




                             String filename="batterylist";
                              File folder = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath());
                              File file = new File(folder, filename+".txt");
                              FileOutputStream stream = null;
                              try {
                                  stream = new FileOutputStream(file);
                              } catch (FileNotFoundException e) {
                                  e.printStackTrace();
                              }
                              try {
                                  stream.write(data.getBytes());
                                  Toast.makeText(getApplicationContext(), "Data written to file "+filename+".txt", Toast.LENGTH_SHORT).show();
                              } catch (IOException e) {
                                  e.printStackTrace();
                              } finally {
                                  try {
                                      stream.close();
                                  } catch (IOException e) {
                                      e.printStackTrace();
                                  }
                              }





                         //////////////////////////////////////////////


                               JSONObject jsonObjectnew = new JSONObject();
                               try {
                                   jsonObjectnew.put("phoneid",phd);
                                   jsonObjectnew.put("ask","1");
                                  // jsonObjectnew.put("distance",String.valueOf(dis));




                               } catch (JSONException e) {
                                   e.printStackTrace();
                               }

                               String jsonString= jsonObjectnew.toString();
                               sendReceive.write(jsonString.getBytes());





                               String listdevice="";
                               for (String name: deviceslist.keySet()){
                                   String key = name.toString();
                                   String value = deviceslist.get(name).toString();
                                  // displayMsg=displayMsg+"<"+key+">"+"AVAILABLE DEVICES";
                                   listdevice=listdevice+ key+" : " + value+"\n";

                               }

                               connectedDevices.setText(listdevice);




                            } else {
                                displayMsg = jsonObject.get("phoneid").getAsString() + "FAILED CONSTRAINTS->" + String.valueOf(batterylevel);
                            }



                          // received.setText(received.getText() + displayMsg);
                           received.setText(displayMsg);


                       }
                          if(jsonObject.has("message"))
                      {
                        String msgs = jsonObject.get("message").getAsString();
                          received.setText(received.getText() + msgs);



                      }

                          if(jsonObject.has("result"))
                          {
                              String msgs = jsonObject.get("result").getAsString();
                              String phoneid = jsonObject.get("phoneid").getAsString();

                              received.setText("MULTIPLICATION OF ARRAY by ->"+phoneid+msgs);



                              switch(counter)
                              {
                                  case 0:
                                      textView1.setText("");
                                      textView2.setText("");
                                      textView3.setText("");
                                      textView4.setText("");
                                      String[] tokens = msgs.split(" ");
                                      displayarr1[0] = Integer.parseInt(tokens[0]);
                                      displayarr1[1] = Integer.parseInt(tokens[1]);
                                      displayarr2[0] = Integer.parseInt(tokens[2]);
                                      displayarr2[1] = Integer.parseInt(tokens[3]);
                                      String arr1str = convertToString(displayarr1);
                                      String arr2str = convertToString(displayarr2);
                                      textView1.setText(arr1str);
                                      textView2.setText(arr2str);
                                      break;
                                  case 1:
                                      String[] tokens1 = msgs.split(" ");
                                      displayarr1[2] = Integer.parseInt(tokens1[0]);
                                      displayarr1[3] = Integer.parseInt(tokens1[1]);
                                      displayarr2[2] = Integer.parseInt(tokens1[2]);
                                      displayarr2[3] = Integer.parseInt(tokens1[3]);
                                      String arr1strfull = convertToString(displayarr1);
                                      String arr2strfull = convertToString(displayarr2);
                                      textView1.setText(arr1strfull);
                                      textView2.setText(arr2strfull);
                                      break;
                                  case 2:
                                      String[] tokens2 = msgs.split(" ");
                                      displayarr3[0] = Integer.parseInt(tokens2[0]);
                                      displayarr3[1] = Integer.parseInt(tokens2[1]);
                                      displayarr4[0] = Integer.parseInt(tokens2[2]);
                                      displayarr4[1] = Integer.parseInt(tokens2[3]);
                                      String arr3str = convertToString(displayarr3);
                                      String arr4str = convertToString(displayarr4);
                                      textView3.setText(arr3str);
                                      textView4.setText(arr4str);
                                      break;
                                  case 3:
                                      String[] tokens3 = msgs.split(" ");
                                      displayarr3[2] = Integer.parseInt(tokens3[0]);
                                      displayarr3[3] = Integer.parseInt(tokens3[1]);
                                      displayarr4[2] = Integer.parseInt(tokens3[2]);
                                      displayarr4[3] = Integer.parseInt(tokens3[3]);
                                      String arr3strfull = convertToString(displayarr3);
                                      String arr4strfull = convertToString(displayarr4);
                                      textView3.setText(arr3strfull);
                                      textView4.setText(arr4strfull);
                                      break;
                              }
                              counter++;


                          }



                          break;

                  }

            return true;
        }
    });

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
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

                  //
                    sendReceive=new SendReceive(socket);
                    sendReceive.start();

                   //send reveice matridx
                    sendReceiveMatrix=new SendReceive(socket);
                    sendReceiveMatrix.start();

                //askquestion
                    ask=new SendReceive(socket);
                    ask.start();

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

                //sendrecdeive matrxi

                sendReceiveMatrix=new SendReceive(socket);
                sendReceiveMatrix.start();
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





    private void showPaired() {
        showPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Set<BluetoothDevice> pairedDevices = mybluetoothAdapter.getBondedDevices();
                int index=0;
                String[] strings = new String[pairedDevices.size()];
                String[] mac = new String[pairedDevices.size()];

                if(pairedDevices.size()>0)
                {
                    btArray = new  BluetoothDevice[pairedDevices.size()];
                    for(BluetoothDevice device : pairedDevices)
                   {    btArray[index]=device;

                       strings[index]=device.getName();

                       mac[index] = device.getAddress(); // MAC address

                       index++;

                   }

                    ArrayAdapter<String>  devices_array = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,strings);

                    devicelist.setAdapter(devices_array );



                }

            }
        });




    }


    private void onlistclick() {

        devicelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(position+"position");
                myposition=position;
                System.out.println("----"+btArray[position]);

                bluetoothDevice = btArray[position];
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
            if(resultCode==RESULT_OK){Toast.makeText(getApplicationContext(),"BT ENABLED",Toast.LENGTH_LONG);}
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
