package com.coen390.team_d.heartratemonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

//Graph related imports
import android.graphics.Color;
import android.graphics.Paint;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


import zephyr.android.HxMBT.BTClient;
//import zephyr.android.HxMBT.ZephyrProtocol;


public class MainActivity extends AppCompatActivity {
	
	// TAG for logging to console
	private boolean RemoteMonitoringFlag = true;
	private Context mContext = this;
	private static final String TAG = "MainActivity";
	private BluetoothAdapter _btAdapter = null;
	private BTClient _bt;
	private NewConnectedListener _NConnListener;
	//private final static int AVG_HR_COUNT = 10;
	
	private final int HEART_RATE = 0x100;
	private final int INSTANT_SPEED = 0x101;
	private final static int REQUEST_ENABLE_BT = 1;
	
	//Fields for Graph
	private GraphView graph;
	private final int GraphSize 	= 	360000; //Visible graph size, 6 mins, in ms
	private final int GraphLength 	=	3600; //Total Graphed data length, 1 hour, in s
	private long graphStart;
	private long graphEnd;
	private int DatapointCounter;
	private int WaitToScroll;
	private Paint paint = new Paint();
	private LineGraphSeries<DataPoint> series;
	private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
	
	//Fields for HRAverages()
	private Queue<Integer> HRTenSecAvgData = new LinkedList();
	private Queue<Integer> HROneMinAvgData = new LinkedList();
	private int TenSecTotal = 0;
	private int OneMinTotal = 0;
	private float TenSecAvg;
	private float OneMinAvg;
	private int MaxBPM = 200;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		///////////////
		// Set up UI //
		///////////////
		TextView tv;
		tv = (TextView)findViewById(R.id.usernameTextview);
		SharedPreferences prefs = getSharedPreferences("SettingsPreferences",Context.MODE_PRIVATE);
		String name = prefs.getString("name", null);
		if (name != null)tv.setText(name);
		
		tv = (TextView)findViewById(R.id.userAge);
		int age = prefs.getInt("age", 55);
		tv.setText("Age: " + Integer.toString(age));
		
		//////////////////////////////
		// Set up Monitoring Switch //
		//////////////////////////////
		
		Switch MonitoringSwitch = (Switch) findViewById(R.id.MonitoringSwitch);
		MonitoringSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				TextView tv;
				tv = (TextView)findViewById(R.id.LineState);
				if (isChecked) {
					// The Switch is enabled
					RemoteMonitoringFlag = true;
					Toast.makeText(getApplicationContext(),"Monitoring is ON" ,Toast.LENGTH_LONG).show();
					if (tv != null)tv.setText("Online");
					if (tv != null)tv.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
				} else {
					// The Switch is disabled
					RemoteMonitoringFlag = false;
					Toast.makeText(getApplicationContext(),"Monitoring is OFF" ,Toast.LENGTH_LONG).show();
					if (tv != null)tv.setText("Offline");
					if (tv != null)tv.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
				}
			}
		});
		
		//////////////////
		// Set up Graph //
		//////////////////
		setupGraph();
		//////////////////
		// Set up MaxHR //
		//////////////////
		setupMaxHR();
		
		Button alertButton = (Button) findViewById(R.id.alertButton);
		alertButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickAlertButton(v);
			}
		});
		
		
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

		
		////////////////
		// SIMULATION //
		////////////////
		
		Button testBtn = (Button) findViewById(R.id.testButton);
		if (testBtn != null) {
			testBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					
					for (int i = 100; i < 110; i++) {
						Message text1 = Newhandler.obtainMessage(HEART_RATE);
						Bundle b1 = new Bundle();
						b1.putString("HeartRate", String.valueOf(i));
						text1.setData(b1);
						Newhandler.sendMessage(text1);
					}
				}
				
			});
		}
		////////////////////
		// END SIMULATION //
		////////////////////
		
		//Obtaining the handle to act on the CONNECT button
		Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
		if (btnConnect != null)
		{
			btnConnect.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onClickConnectButton();
				}
			});
		}
	}
	
	@Override
	protected void	onDestroy(){
		super.onDestroy();
		onClickDisconnectButton();
	}
	
    private void onClickConnectButton() {

        // Check for bluetooth and get adapter
        _btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Does this device support bluetooth?
        if (_btAdapter == null) {
            // If bluetooth is not supported, show alert and return
            btAlertMsg();
            return;
        }

        // Is bluetooth enabled?
        if (!_btAdapter.isEnabled()) {
            // If not, ask user to enable it
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }


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

        _bt = new BTClient(_btAdapter, BhMacID);
        _NConnListener = new NewConnectedListener(Newhandler,Newhandler);
        _bt.addConnectedEventListener(_NConnListener);

//        TextView tv1 = (TextView)findViewById(R.id.instantBPMTextView);
//        tv1.setText("Heart Rate: 000");

        if (_bt.IsConnected()) {
            _bt.start();
			
			//Reset Graph X bounds
			refreshGraphBounds();

            // Set button text to "Disconnect" and modify click listener
            Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
            if (btnConnect != null) {
                btnConnect.setText("Disconnect");
                btnConnect.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        onClickDisconnectButton();
                    }
                });
            }
        } else {
            // Show toast "Unable to connect to Bluetooth device"
            Toast.makeText(getApplicationContext(), "Unable to connect to Bluetooth device", Toast.LENGTH_LONG).show();
        }
    }

    private void onClickDisconnectButton() {

        if (_bt != null) {
            /*This disconnects listener from acting on received messages*/
            _bt.removeConnectedEventListener(_NConnListener);

            /*Close the communication with the device & throw an exception if failure*/
            _bt.Close();
        }


        // Set button text to "Connect" and modify click listener
        Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
        if (btnConnect != null)
        {
            btnConnect.setText("Connect");
            btnConnect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onClickConnectButton();
                }
            });
        }
    }

	
	private void onClickAlertButton(View v) {
		
		// Send alert through AWS Dynamo DB
		AWSDatabaseHelper dbHelper = new AWSDatabaseHelper(getApplicationContext());
		dbHelper.sendAlert(-1);
		Toast.makeText(getApplicationContext(), "Notification has been sent", Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Adds toolbar menu to this activity
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	/**
	 * Handles menu item clicks
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// If the "Enable Edit" menu button was clicked, make the text inputs editable
		switch (item.getItemId()) {
			case R.id.teamMonitoring:
				goToTeamMonitoringActivity();
				break;
			case R.id.settings:
				goToSettingsActivity();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	/////////////////
	// Graph setup //
	/////////////////
	private void setupGraph(){
		graph = (GraphView) findViewById(R.id.graph);
		
		graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");
		graph.getGridLabelRenderer().setVerticalAxisTitle("BPM");
		
		//need manual bounds for scrolling to function
		// set manual Y bounds (Heart Rate)
		graph.getViewport().setYAxisBoundsManual(true);
		
		// set manual x bounds to have nice steps
		graph.getViewport().setXAxisBoundsManual(true);
		graph.getViewport().setScrollable(true); // enables horizontal scrolling
		graph.getViewport().setScalableY(false); // disables vertical zooming and scrolling
		
		//Setup data series
		setupSeries();
		graph.addSeries(series);
		
		// set date label formatter
		graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, formatter));
		graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
		
		// as we use dates as labels, the human rounding to nice readable numbers
		// is not necessary
		graph.getGridLabelRenderer().setHumanRounding(false);
		
		graph.getViewport().setOnXAxisBoundsChangedListener(new Viewport.OnXAxisBoundsChangedListener() {
			@Override
			public void onXAxisBoundsChanged(double minX, double maxX, Reason reason) {
				Log.d(TAG, "XAxis Bounds Changed : Waiting to scroll to end");
				WaitToScroll = 3;
			}
		});
		
		refreshGraphBounds();
		
	}
	
	private void setupSeries(){
		series = new LineGraphSeries<>();
		//Set Graph Formatting
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(6);
		paint.setColor(Color.RED);
		series.setCustomPaint(paint);
		series.setDrawDataPoints(true);
		series.setDataPointsRadius(4);
		//Method for displaying point information when a point is tapped
		series.setOnDataPointTapListener(new OnDataPointTapListener() {
			@Override
			public void onTap(Series series, DataPointInterface dataPoint) {
				Date d = new Date((long)dataPoint.getX());
				Toast.makeText(getApplicationContext(), formatter.format(d) + " : " + (int) dataPoint.getY() + " BPM",Toast.LENGTH_SHORT).show();
			}
		});
	}
	private void refreshGraphBounds(){
		Date d1 = new Date();
		graphStart = d1.getTime()/120000*120000; //Rounds the bound to the 2 minutes
		graphEnd = graphStart+GraphSize;
		graph.getViewport().setMinX(graphStart);
		graph.getViewport().setMaxX(graphEnd);
		graph.getViewport().setMinY(30);
		if (MaxBPM > 200)
			graph.getViewport().setMaxY(MaxBPM);
		else
			graph.getViewport().setMaxY(200);
	}
	
	private void setupMaxHR(){
		
		//TODO if MAXBPM!=null in SharedPref, MaxBPM = SharedPref.GetMAXBPM()
		SharedPreferences prefs = getSharedPreferences("SettingsPreferences",Context.MODE_PRIVATE);
		int age = prefs.getInt("age", 20);
		MaxBPM = 208 - (7 * age /10);
		Log.d(TAG, "MaxBPM set to : " + MaxBPM);
		TextView tv;
		tv = (TextView)findViewById(R.id.HRMax);
		if (tv != null)tv.setText("MaxHR: " + MaxBPM + " BPM");
	}
	
	private void goToTeamMonitoringActivity() {
		Intent intent = new Intent(MainActivity.this, TeamMonitoringActivity.class);
		startActivity(intent);
	}
	
	private void goToSettingsActivity() {
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivity(intent);
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
				Toast.makeText(getApplicationContext(), "Bluetooth has been enabled. Please click Connect to connect to device.", Toast.LENGTH_SHORT).show();
			}
			else {
				// Show alert
				btAlertMsg();
			}
		}
	}
	
	
	private void btAlertMsg() {
		// Show alert then exit
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.bt_required);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//finishAndRemoveTask();  // requires at least API 21!
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
					int heartRateInt = Integer.parseInt(HeartRatetext);
					tv = (TextView)findViewById(R.id.instantBPMTextView);
					//System.out.println("Heart Rate Info is "+ HeartRatetext);
					//Log.i(TAG, "Heart Rate: " + HeartRatetext);
					if (tv != null)tv.setText("Heart Rate: " + HeartRatetext);
					
					//HR Averages calculation and UI Updates
					HRAverages(heartRateInt);
					//HR Zones Calculation and UI updates
					HRZones(heartRateInt);
					//Add an entry to the graph
					addEntry((double) heartRateInt);
					
					if (RemoteMonitoringFlag){
						RemoteMonitoringUpdate(heartRateInt);
					}
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	private void HRAverages(int HR){
		TextView tv;
		TenSecTotal += HR;
		OneMinTotal += HR;
		HRTenSecAvgData.add(HR);
		HROneMinAvgData.add(HR);
		
		if (HROneMinAvgData.size() > 60){
			OneMinTotal -= HROneMinAvgData.poll();
			OneMinAvg = OneMinTotal/60;
		}
		else {
			OneMinAvg = OneMinTotal/HROneMinAvgData.size();
		}
		
		if (HRTenSecAvgData.size() > 10){
			TenSecTotal -= HRTenSecAvgData.poll();
			TenSecAvg = TenSecTotal/10;
		}
		else {
			TenSecAvg = TenSecTotal/HRTenSecAvgData.size();
		}
		tv = (TextView)findViewById(R.id.shortAvgBPMTextView);
		if (tv != null)tv.setText("10s. Avg: " + TenSecAvg);
		
		tv = (TextView)findViewById(R.id.longAvgBPMTextView);
		if (tv != null)tv.setText("1Min Avg: " + OneMinAvg);
		
	}
	
	private void HRZones(int HR){
		TextView tv;
		int MaxHRPercent;
		String HRzone = new String();
		
		if (HR > MaxBPM) {
			MaxBPM = HR;
		}
		MaxHRPercent = HR*100/MaxBPM;
		//TODO Set MAXBPM as SharedPref entry
		
		switch (MaxHRPercent/10){
			case 9:
				HRzone = "VO2 Max";
				break;
			case 8:
				HRzone = "Anaerobic";
				break;
			case 7:
				HRzone = "Aerobic";
				break;
			case 6:
				HRzone = "W.Control";
				break;
			case 5:
				HRzone = "Moderate";
				break;
			default:
				HRzone = "Rest";
				break;
		}
		tv = (TextView)findViewById(R.id.HRPercent);
		if (tv != null)tv.setText("%MaxHR: " + MaxHRPercent + "%");
		tv = (TextView)findViewById(R.id.HRZone);
		if (tv != null)tv.setText(HRzone);
		
	}
	
	// add data to graph
	private void addEntry(double y) {
		Date x = new Date();
		DatapointCounter += 1000;
		Boolean GraphScroll = false;
		if (WaitToScroll > 0) {
			WaitToScroll--;
			Log.d(TAG, "Scrolling to end in : " + WaitToScroll);
		}
		else if (DatapointCounter > GraphSize - 30000) {
			GraphScroll = true;
		}
		series.appendData(new DataPoint(x, y), GraphScroll, GraphLength);
	}
	
	private void RemoteMonitoringUpdate(int HR){
		AWSDatabaseHelper dbHelper = new AWSDatabaseHelper(getApplicationContext());

		if (HR > MaxBPM)
			dbHelper.sendAlert(HR);
		else if (DatapointCounter % 10000 == 0) {
			dbHelper.updateHeartRate((int)TenSecAvg);
			Log.d(TAG, "AWSDatabase Updated with : " + TenSecAvg);
		}
	}
}