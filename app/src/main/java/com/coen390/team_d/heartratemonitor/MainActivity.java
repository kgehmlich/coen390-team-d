package com.coen390.team_d.heartratemonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;


public class MainActivity extends AppCompatActivity {

    // TAG for logging to console
    private static final String TAG = "MainActivity";

    private BluetoothAdapter _btAdapter = null;
    private BTClient _bt;
    private NewConnectedListener _NConnListener;
    ZephyrProtocol _protocol;
    private final int HEART_RATE = 0x100;
    private final int INSTANT_SPEED = 0x101;

    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ///////////////////////////////
        // Set up bluetooth receiver //
        ///////////////////////////////
        /*Sending a message to android that we are going to initiate a pairing request*/
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);

        /////////////////////////////////////////
        // Check for bluetooth and get adapter //
        /////////////////////////////////////////
        _btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Does this device support bluetooth?
        if (_btAdapter == null) {
            // If bluetooth is not supported...
            // show alert and quit
            btAlertAndExit();
        }

        // Is bluetooth enabled?
        if (!_btAdapter.isEnabled()) {
            // If not, ask user to enable it
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //Obtaining the handle to act on the CONNECT button
        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
        String ErrorText  = "Not Connected to HxM ! !";
        tv.setText(ErrorText);

        Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
        if (btnConnect != null)
        {
            btnConnect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String BhMacID = "00:07:80:9D:8A:E8";
                    //String BhMacID = "00:07:80:88:F6:BF";
                    _btAdapter = BluetoothAdapter.getDefaultAdapter();

                    Set<BluetoothDevice> pairedDevices = _btAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0)
                    {
                        for (BluetoothDevice device : pairedDevices)
                        {
                            if (device.getName().startsWith("HXM"))
                            {
                                BhMacID = device.getAddress();
                                break;

                            }
                        }


                    }

                    //BhMacID = btDevice.getAddress();
                    BluetoothDevice Device = _btAdapter.getRemoteDevice(BhMacID);
                    String DeviceName = Device.getName();
                    _bt = new BTClient(_btAdapter, BhMacID);
                    _NConnListener = new NewConnectedListener(Newhandler,Newhandler);
                    _bt.addConnectedEventListener(_NConnListener);

                    TextView tv1 = (EditText)findViewById(R.id.labelHeartRate);
                    tv1.setText("000");

                    tv1 = (EditText)findViewById(R.id.labelInstantSpeed);
                    tv1.setText("0.0");

                    ((TextView) findViewById(R.id.MACaddr)).setText(BhMacID);

                    //tv1 = 	(EditText)findViewById(R.id.labelSkinTemp);
                    //tv1.setText("0.0");

                    //tv1 = 	(EditText)findViewById(R.id.labelPosture);
                    //tv1.setText("000");

                    //tv1 = 	(EditText)findViewById(R.id.labelPeakAcc);
                    //tv1.setText("0.0");
                    if(_bt.IsConnected())
                    {
                        _bt.start();
                        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                        String ErrorText  = "Connected to HxM "+DeviceName;
                        tv.setText(ErrorText);

                        //Reset all the values to 0s

                    }
                    else
                    {
                        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                        String ErrorText  = "Unable to Connect !";
                        tv.setText(ErrorText);
                    }
                }
            });
        }
        /*Obtaining the handle to act on the DISCONNECT button*/
        Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        if (btnDisconnect != null)
        {
            btnDisconnect.setOnClickListener(new View.OnClickListener() {
                @Override
				/*Functionality to act if the button DISCONNECT is touched*/
                public void onClick(View v) {
					/*Reset the global variables*/
                    TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                    String ErrorText  = "Disconnected from HxM!";
                    tv.setText(ErrorText);

					/*This disconnects listener from acting on received messages*/
                    _bt.removeConnectedEventListener(_NConnListener);
					/*Close the communication with the device & throw an exception if failure*/
                    _bt.Close();

                }
            });
        }

        // At this point bluetooth should be available and enabled
        // Attempt to find HxM monitor in paired devices
        /*Set<BluetoothDevice> pairedDevices = _btAdapter.getBondedDevices();

        String hxmMacId = null;

        if (pairedDevices.size() > 0) {
            // get name and address of each paired device
            for (BluetoothDevice device : pairedDevices) {
                String devName = device.getName();
                String devAddr = device.getAddress();
                String devClass = device.getBluetoothClass().toString();

                Log.d(TAG, "name: " + devName + ", addr: " + devAddr + ", class: " + devClass);

                if (devName.startsWith("HXM")) {
                    hxmMacId = devAddr;
                    break;
                }
            }
        }


        if (hxmMacId != null) {
            BluetoothDevice hxmDevice = _btAdapter.getRemoteDevice(hxmMacId);
            String hxmDeviceName = hxmDevice.getName();
            _bt = new BTClient(_btAdapter, hxmMacId);
            _NConnListener = new NewConnectedListener(Newhandler,Newhandler);
            _bt.addConnectedEventListener(_NConnListener);

            if (_bt.IsConnected()) {
                _bt.start();
                TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                String errorText = "Connected to HxM " + hxmDeviceName;
                tv.setText(errorText);
            }
            else {
                TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                String errorText = "Unable to connect to HxM";
                tv.setText(errorText);
            }
        }*/
    }


    /**
     * Called when an activity that was created by this activity returns a result code
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Is this being called after trying to enable bluetooth?
        if (requestCode == REQUEST_ENABLE_BT) {
            // If yes, check the result
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is ENABLED", Toast.LENGTH_SHORT).show();
            }
            else {
                // Show alert then exit
               btAlertAndExit();
            }
        }
    }


    private void btAlertAndExit() {
        // Show alert then exit
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.bt_required);
        builder.setCancelable(false);
        builder.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishAndRemoveTask();  // requires at least API 21!
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }




    private class BTBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            BluetoothDevice device = _btAdapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("Bond state", "BOND_STATED = " + device.getBondState());
        }
    }
    private class BTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BTIntent", intent.getAction());
            Bundle b = intent.getExtras();
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
            try {
                BluetoothDevice device = _btAdapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
                Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
                byte[] pin = (byte[])m.invoke(device, "1234");
                m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
                Object result = m.invoke(device, pin);
                Log.d("BTTest", result.toString());
            } catch (SecurityException e1) {
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    final  Handler Newhandler = new Handler(){
        public void handleMessage(Message msg)
        {
            TextView tv;
            switch (msg.what)
            {
                case HEART_RATE:
                    String HeartRatetext = msg.getData().getString("HeartRate");
                    tv = (EditText)findViewById(R.id.labelHeartRate);
                    System.out.println("Heart Rate Info is "+ HeartRatetext);
                    if (tv != null)tv.setText(HeartRatetext);
                    break;

                case INSTANT_SPEED:
                    String InstantSpeedtext = msg.getData().getString("InstantSpeed");
                    tv = (EditText)findViewById(R.id.labelInstantSpeed);
                    if (tv != null)tv.setText(InstantSpeedtext);

                    break;

            }
        }

    };
}
