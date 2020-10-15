package com.example.robot_controller_group8;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.robot_controller_group8.ui.main.BluetoothConnectionService;
import com.example.robot_controller_group8.ui.main.BluetoothPopUp;
import com.example.robot_controller_group8.ui.main.GridMap;
import com.example.robot_controller_group8.ui.main.MapInformation;
import com.example.robot_controller_group8.ui.main.MapTabFragment;
import com.example.robot_controller_group8.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;

    private static GridMap gridMap;
    static TextView xAxisTextView, yAxisTextView, directionAxisTextView;
    static TextView robotStatusTextView;

    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;
    private static UUID myUUID;
    ProgressDialog myDialog;

    private static final String TAG = "Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(9999);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        // Set up sharedPreferences
        MainActivity.context = getApplicationContext();
        this.sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction","None");
        editor.putString("connStatus", "Disconnected");
        editor.commit();

//        Button printMDFStringButton = (Button) findViewById(R.id.printMDFString);
//        printMDFStringButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String message = "Explored : " + GridMap.getPublicMDFExploration();
//                editor = sharedPreferences.edit();
//                editor.putString("message", BluetoothPopUp.getMessageReceivedTextView().getText() + "\n" + message);
//                editor.commit();
//                refreshMessageReceived();
//                message = "Obstacle : " + GridMap.getPublicMDFObstacle() + "0";
//                editor.putString("message", BluetoothPopUp.getMessageReceivedTextView().getText() + "\n" + message);
//                editor.commit();
//                refreshMessageReceived();
//            }
//        });

        // Toolbar
        Button bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popup = new Intent(MainActivity.this, BluetoothPopUp.class);
                startActivity(popup);
            }
        });


//        Button mapInformationButton = (Button) findViewById(R.id.mapInfoButton);
//        mapInformationButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                editor.putString("mapJsonObject", String.valueOf(gridMap.getCreateJsonObject()));
//                editor.commit();
//                Intent popup = new Intent(MainActivity.this, MapInformation.class);
//                startActivity(popup);
//            }
//        });


        // Map
        gridMap = new GridMap(this);
        gridMap = findViewById(R.id.mapView);
        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        directionAxisTextView = findViewById(R.id.directionAxisTextView);

        // Robot Status
        robotStatusTextView = findViewById(R.id.robotStatusTextView);

        myDialog = new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

//        f1 = (Button) findViewById(R.id.f1ActionButton);
//        f2 = (Button) findViewById(R.id.f2ActionButton);
//        reconfigure = (Button) findViewById(R.id.configureButton);

//        if (sharedPreferences.contains("F1")) {
//            f1.setContentDescription(sharedPreferences.getString("F1", ""));
//            showLog("setText for f1Btn: " + f1.getContentDescription().toString());
//        }
//        if (sharedPreferences.contains("F2")) {
//            f2.setContentDescription(sharedPreferences.getString("F2", ""));
//            showLog("setText for f2Btn: " + f2.getContentDescription().toString());
//        }
//
//        f1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showLog("Clicked f1Btn");
//                if (!f1.getContentDescription().toString().equals("empty"))
//                    MainActivity.printMessage(f1.getContentDescription().toString());
//                showLog("f1Btn value: " + f1.getContentDescription().toString());
//                showLog("Exiting f1Btn");
//            }
//        });
//
//        f2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showLog("Clicked f2Btn");
//                if (!f2.getContentDescription().toString().equals("empty"))
//                    MainActivity.printMessage(f2.getContentDescription().toString());
//                showLog("f2Btn value: " + f2.getContentDescription().toString());
//                showLog("Exiting f2Btn");
//            }
//        });
//
//        reconfigure.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showLog("Clicked reconfigureBtn");
//                reconfigureFragment.show(getFragmentManager(), "Reconfigure Fragment");
//                showLog("Exiting reconfigureBtn");
//            }
//        });
    }

//    public static Button getF1() { return f1; }
//
//    public static Button getF2() { return f2; }

    public static GridMap getGridMap() {
        return gridMap;
    }

    public static TextView getRobotStatusTextView() {  return robotStatusTextView; }

    public static void sharedPreferences() {
        sharedPreferences = MainActivity.getSharedPreferences(MainActivity.context);
        editor = sharedPreferences.edit();
    }

    // Send message to bluetooth
    public static void printMessage(String message) {
        showLog("Entering printMessage");
        editor = sharedPreferences.edit();

        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
        showLog(message);
        editor.putString("message", BluetoothPopUp.getMessageReceivedTextView().getText() + "\n" + message);
        editor.commit();
        refreshMessageReceived();
        showLog("Exiting printMessage");
    }

    public static void printMessage(String name, int x, int y) throws JSONException {
        showLog("Entering printMessage");
        sharedPreferences();

        JSONObject jsonObject = new JSONObject();
        String message;

        switch(name) {
//            case "starting":
            case "waypoint":
                jsonObject.put(name, name);
                jsonObject.put("x", x);
                jsonObject.put("y", y);
                message = "WP|[" + x + "," + y + "]";
                break;
            default:
                message = "Unexpected default for printMessage: " + name;
                break;
        }
        editor.putString("message", BluetoothPopUp.getMessageReceivedTextView().getText() + "\n" + message);
        editor.commit();
        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
        showLog("Exiting printMessage");
    }

    public static void refreshMessageReceived() {
        BluetoothPopUp.getMessageReceivedTextView().setText(sharedPreferences.getString("message", ""));
    }


    public void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
        printMessage("Direction is set to " + direction);

    }

    public static void refreshLabel() {
        xAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[0]-1));
        yAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[1]-1));
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
    }

    public static void receiveMessage(String message) {
        showLog("Entering receiveMessage");
        sharedPreferences();
        editor.putString("message", sharedPreferences.getString("message", "") + "\n" + message);
        editor.commit();
        showLog("Exiting receiveMessage");
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if(status.equals("connected")){
                try {
                    myDialog.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d(TAG, "mBroadcastReceiver5: Device now connected to "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Device now connected to "+mDevice.getName(), Toast.LENGTH_LONG).show();
                editor.putString("connStatus", "Connected to " + mDevice.getName());
//                TextView connStatusTextView = findViewById(R.id.connStatusTextView);
//                connStatusTextView.setText("Connected to " + mDevice.getName());
            }
            else if(status.equals("disconnected")){
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Disconnected from "+mDevice.getName(), Toast.LENGTH_LONG).show();
//                mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
//                mBluetoothConnection.startAcceptThread();

                editor.putString("connStatus", "Disconnected");
//                TextView connStatusTextView = findViewById(R.id.connStatusTextView);
//                connStatusTextView.setText("Disconnected");

                myDialog.show();
            }
            editor.commit();
        }
    };

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            showLog("receivedMessage: message --- " + message);
            String[] septext = message.split("!");
            String hextext = "";
            String hextext2 = "";
            for(int u=0; u <septext.length; u++) {
                try {
                    if (septext[u].length() < 4) {
                        switch (septext[u]) {
                            case "G0":
                            case "F01":
                                gridMap.moveRobot("forward");
                                break;
                            case "F02":
                                for(int w=0;w<2;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F03":
                                for(int w=0;w<3;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F04":
                                for(int w=0;w<4;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F05":
                                for(int w=0;w<5;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F06":
                                for(int w=0;w<6;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(200);
                                }
                                break;
                            case "F07":
                                for(int w=0;w<7;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F08":
                                for(int w=0;w<8;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F09":
                                for(int w=0;w<9;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F10":
                                for(int w=0;w<10;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F11":
                                for(int w=0;w<11;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F12":
                                for(int w=0;w<12;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F13":
                                for(int w=0;w<13;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F14":
                                for(int w=0;w<14;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F15":
                                for(int w=0;w<15;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F16":
                                for(int w=0;w<16;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F17":
                                for(int w=0;w<17;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F18":
                                for(int w=0;w<18;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F19":
                                for(int w=0;w<19;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "F20":
                                for(int w=0;w<20;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    Thread.sleep(400);
                                }
                                break;
                            case "R0":
                                gridMap.moveRobot("right");
                                break;
                            case "L0":
                                gridMap.moveRobot("left");
                                break;
                            case "B0":
                                gridMap.moveRobot("back");
                                break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (septext[u].substring(0, 3).equalsIgnoreCase("MDF")) {

                        String[] seperatedtextMDF = septext[u].split("\\|");
                        hextext = seperatedtextMDF[1];
                        //gridMap.setPublicMDFExploration(hextext);
                        if(seperatedtextMDF.length == 3 )
                        {
                            hextext2 = seperatedtextMDF[2];

                        }

                        gridMap.mapDescriptorExplored(hextext);
                    }
                } catch (Exception e) {
                    showLog("Fail to update Map");
                }

                try {
                    if (septext[u].substring(0, 2).equalsIgnoreCase("IM")) {
                        String[] seperatedtext = septext[u].split("\\|");
                        int x = Integer.parseInt(seperatedtext[2]);
                        int y = Integer.parseInt(seperatedtext[3]);
                        int id = Integer.parseInt(seperatedtext[1]);

                        gridMap.drawImageNumberCell(x, y, id);
                    }
                } catch (Exception e) {
                    showLog("Adding Image Failed");
                }

//                try {
//                    if (septext[u].substring(0, 2).equalsIgnoreCase("OB")) {
//                        String[] seperatedtext1 = septext[u].split("\\|");
//                        for (int k = 1; k < seperatedtext1.length; k++) {
//                            String[] seperatedtext2 = seperatedtext1[k].split(",");
//                            int x = Integer.parseInt(seperatedtext2[0]) + 1;
//                            int y = Integer.parseInt(seperatedtext2[1]) + 1;
//                            gridMap.setObstacleCoord(x, y);
//                        }
//
//
//                    }
//                } catch (Exception e) {
//                    showLog("Adding Image Failed");
//                }

//                if (gridMap.getAutoUpdate() || MapTabFragment.manualUpdateRequest) {
//                    try {
//                        gridMap.setReceivedJsonObject(new JSONObject(message));
//                        gridMap.updateMapInformation();
//                        MapTabFragment.manualUpdateRequest = false;
//                        showLog("messageReceiver: try decode successful");
//                    } catch (JSONException e) {
//                        showLog("messageReceiver: try decode unsuccessful");
//                    }
//                }

                sharedPreferences();
                String receivedText = sharedPreferences.getString("message", "") + "\n" + message ;
                editor.putString("message", receivedText);
                editor.commit();
                refreshMessageReceived();

                if(hextext2 != "") {
                    gridMap.mapDescriptorObstacle(hextext2);
                    sharedPreferences();
                    String receivedText2 = sharedPreferences.getString("message", "") + "\n\n --------------------------------------------------------------------------------\n" + "ExploredMDF: " + hextext + "\n" + "Obstacle: \n" + hextext2;
                    editor.putString("message", receivedText2);
                    editor.commit();
                    refreshMessageReceived();
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    mBTDevice = (BluetoothDevice) data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, filter2);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }
}