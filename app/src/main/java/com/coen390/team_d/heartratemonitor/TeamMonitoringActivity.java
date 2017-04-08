package com.coen390.team_d.heartratemonitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

//Graph related imports
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amazonaws.models.nosql.HeartRatesDO;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TeamMonitoringActivity extends AppCompatActivity {
	
	// TAG for logging to console
	private static final String TAG = "TeamMonitoringActivity";
	private GraphView graph;
	private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private LineGraphSeries<DataPoint> series;
	private int DatapointCounter;
	private int WaitToScroll;
	private final int GraphSize 	= 	360000; //Visible graph size, 6 mins, in ms
	private final int GraphLength 	=	3600; //Total Graphed data length, 1 hour, in s
	private long graphStart;
	private long graphEnd;
	private Paint paint = new Paint();
	private int age;
	private int MaxBPM;
	HeartRatesDO item;

	private Handler updateHandler;
    private final static int UPDATE_DELAY_SECS = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_team_monitoring);

        updateHandler = new Handler();
        updateHandler.post(updateHeartRates);

        graph = (GraphView) findViewById(R.id.graphTeam);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(30);
        graph.getViewport().setMaxY(220);
		
		
		graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");
		graph.getGridLabelRenderer().setVerticalAxisTitle("BPM");
		
		//need manual bounds for scrolling to function
		// set manual Y bounds (Heart Rate)
		graph.getViewport().setYAxisBoundsManual(true);
		
		// set manual x bounds to have nice steps
		graph.getViewport().setXAxisBoundsManual(true);
		graph.getViewport().setScrollable(true); // enables horizontal scrolling
		graph.getViewport().setScalableY(false); // disables vertical zooming and scrolling
		
		
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
				WaitToScroll = 1;
			}
		});
		
		refreshGraphBounds();
		

        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                item = (HeartRatesDO) parent.getItemAtPosition(position);
				
				//TODO set TextViews with use info
				
                // Clear graph
                graph.removeAllSeries();
				
				series = HeartRateLog.userHRLogs.get(item.getUserId());
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

                // Add this user's data to graph
                graph.addSeries(series);
				
				///////////////
				// Set up UI //
				///////////////
				TextView tv;
				tv = (TextView)findViewById(R.id.usernameTextview);
				String name = item.getUserId();
				if (name != null)tv.setText(name);
				
				
				
				
				double dHR = item.getHeartRate();
				int HR = (int) dHR;
				
				tv = (TextView)findViewById(R.id.instantBPMTextView);
				if (tv != null)tv.setText("Heart Rate: " + HR);
				
				if (item.getAge()!= null) {
					age = (int) (1 * item.getAge());
					MaxBPM = (int) (208 - 0.7 * age);
					//HR Zones Calculation and UI updates
					HRZones(HR, MaxBPM);
					tv = (TextView)findViewById(R.id.userAge);
					tv.setText("Age: " + age);
					tv = (TextView)findViewById(R.id.HRMax);
					if (tv != null)tv.setText("MaxHR: " + MaxBPM + " BPM");
				}
				
				
				
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
		/*
		if (MaxBPM > 200)
			graph.getViewport().setMaxY(MaxBPM);
		else
			graph.getViewport().setMaxY(200);
			 */
	}
	
	
	private void HRZones(int HR, int MaxBPM){
		TextView tv;
		int MaxHRPercent;
		String HRzone = new String();
		
		MaxHRPercent = HR*100/MaxBPM;
		//TODO Set MAXBPM as SharedPref entry
		
		switch (MaxHRPercent/10){
			case 14:
			case 13:
			case 12:
			case 11:
			case 10:
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


    public void onDestroy() {
        super.onDestroy();
        updateHandler.removeCallbacks(updateHeartRates);
    }


    /**
	 * Adds toolbar menu to this activity
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.team_monitoring_menu, menu);
		return true;
	}

	/**
	 * Handles menu item clicks
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// If the "Enable Edit" menu button was clicked, make the text inputs editable
		switch (item.getItemId()) {
			case R.id.settings:
				goToSettingsActivity();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void goToSettingsActivity() {
		Intent intent = new Intent(TeamMonitoringActivity.this, SettingsActivity.class);
		startActivity(intent);
	}

    // Class to asynchronously update the UI with data from server
    private class FetchHeartRates extends AsyncTask<Void, Void, Void> {
        private ArrayList<HeartRatesDO> hrList;

        protected Void doInBackground(Void... voids) {
            AWSDatabaseHelper dbHelper = new AWSDatabaseHelper(getApplicationContext());
            hrList = dbHelper.getListOfHeartRates();
			DatapointCounter += 10000;
			Boolean GraphScroll = false;
			if (WaitToScroll > 0) {
				WaitToScroll--;
				Log.d(TAG, "Scrolling to end in : " + WaitToScroll);
			}
			else if (DatapointCounter > GraphSize - 30000) {
				GraphScroll = true;
			}
			else
				Log.d(TAG, "Scrolling to end when : " + DatapointCounter + " > " + GraphSize + " - 30000");
				

            // add new heart rates to logs
            for (HeartRatesDO hr : hrList) {
                HeartRateLog.addHeartRate(hr,GraphScroll);
            }

            return null;
        }

        protected void onPostExecute(Void v) {
            Toast.makeText(getApplicationContext(), "Heart rates updated (" + hrList.size() + ")", Toast.LENGTH_LONG).show();
			int HR;
			if ( item != null) {
				HR = (int) (1 * HeartRateLog.userHRLogs.get(item.getUserId()).getHighestValueY());
				TextView tv;
				tv = (TextView)findViewById(R.id.instantBPMTextView);
				if (tv != null)tv.setText("Heart Rate: " + HR);
				//HR Zones Calculation and UI updates
				HRZones(HR, MaxBPM);
			}
            // populate list view with user heart rates
            ListView hrListView = (ListView) findViewById(R.id.listView);

            ArrayAdapter<HeartRatesDO> hrAdapter = new ArrayAdapter<HeartRatesDO>(TeamMonitoringActivity.this, android.R.layout.simple_expandable_list_item_1, hrList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    if (hrList.get(position).getAlert()) {
                        view.setBackgroundColor(Color.YELLOW);
                    } else {
                        view.setBackgroundColor(Color.WHITE);
                    }

                    HeartRatesDO item = getItem(position);

                    String itemText = item.getUserId() + (item.getAge() == null ? "" : " (" + item.getAge().intValue() + ")") + "\t\t";

                    if (item.getHeartRate() == -1) {
                        itemText += "MANUAL ALERT";
                    } else {
                        itemText += item.getHeartRate().toString();
                    }

                    view.setText(itemText);
                    return view;
                }
            };

            hrListView.setAdapter(hrAdapter);
        }
    }


    Runnable updateHeartRates = new Runnable() {
        @Override
        public void run() {
            try {
                new FetchHeartRates().execute();
            } finally {
                updateHandler.postDelayed(updateHeartRates, UPDATE_DELAY_SECS*1000);    // Times 1000 for milliseconds
            }
        }
    };
}
