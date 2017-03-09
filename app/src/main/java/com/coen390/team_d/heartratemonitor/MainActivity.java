package com.coen390.team_d.heartratemonitor;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter btAdapter;
    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /////////////////////////////////////////
        // Check for bluetooth and get adapter //
        /////////////////////////////////////////
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Does this device support bluetooth?
        if (btAdapter == null) {
            // If bluetooth is not supported...
            // show alert and quit
            btAlertAndExit();
        }

        // Is bluetooth enabled?
        if (!btAdapter.isEnabled()) {
            // If not, ask user to enable it
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
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
}
