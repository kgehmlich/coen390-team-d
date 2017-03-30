package com.coen390.team_d.heartratemonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.models.nosql.HeartRatesDO;

import java.util.ArrayList;
import java.util.concurrent.RunnableFuture;

public class TeamMonitoringActivity extends AppCompatActivity {

  private LineGraphSeries<DataPoint> series;

	private Handler updateHandler;
    private final static int UPDATE_DELAY_SECS = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_team_monitoring);
		
		/*
		//Create graph
		GraphView graph = (GraphView) findViewById(R.id.graphTeam);
		series = new LineGraphSeries<DataPoint>();

		//Set Graph Formatting
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(Color.RED);
		series.setCustomPaint(paint);
		series.setDrawDataPoints(true);
		series.setDataPointsRadius(10);
		//Method for displaying point information when a point is tapped
		series.setOnDataPointTapListener(new OnDataPointTapListener() {
			@Override
			public void onTap(Series series, DataPointInterface dataPoint) {
				Toast.makeText(getApplicationContext()," " + dataPoint,Toast.LENGTH_SHORT).show();
			}
		});

		//graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");
		graph.getGridLabelRenderer().setVerticalAxisTitle("Heart Rate");

		//need manual bounds for scrolling to function
		// set manual Y bounds (Heart Rate)
		graph.getViewport().setYAxisBoundsManual(true);
		graph.getViewport().setMinY(0);
		graph.getViewport().setMaxY(200);

		// set manual X bounds (Time points)
		graph.getViewport().setXAxisBoundsManual(true);
		graph.getViewport().setMinX(0);
		graph.getViewport().setMaxX(20);

		graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
		graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling
		//Creates graph using series
		graph.addSeries(series);
		*/

        updateHandler = new Handler();
        updateHandler.post(updateHeartRates);
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

  /*
	// Function that allows the graph to be real-time updated
	@Override
	protected void onResume(){
		super.onResume();
		//Thread in control of updating data series
		new Thread(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//Calls function responsible for adding data to graph series
						addEntry();
					}
				});
				// sleep to slow down the add of entries.
				try {
					//Values are in milliseconds. This decides how often the graph is updated
					//May need to change to suit our uses
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// manage error if need be...
				}
			}
		}).start();
	}*/
	//TODO: Might be possible to create some kind of multiplexer function to switch between user data
	//TODO: Could also display multiple users at once
	/*
	// add data to graph
	private void addEntry() {
		// here, we choose to display max 30 points on the graph and we scroll to end
		//TODO: needs proper inputs from AWS here.
		double x = 1;
		double y = 1;
		series.appendData(new DataPoint(x, y), true, 30);
		
	}*/


    // Class to asynchronously update the UI with data from server
    private class FetchHeartRates extends AsyncTask<Void, Void, Integer> {
        private ArrayList<HeartRatesDO> hrList;

        protected Integer doInBackground(Void... voids) {
            AWSDatabaseHelper dbHelper = new AWSDatabaseHelper(getApplicationContext());
            hrList = dbHelper.getListOfHeartRates();
            return 0;
        }

        protected void onPostExecute(Integer integer) {
            Toast.makeText(getApplicationContext(), "Heart rates updated (" + hrList.size() + ")", Toast.LENGTH_LONG).show();
            // populate list view with user heart rates
            ListView hrListView = (ListView) findViewById(R.id.listView);

            ArrayList<String> hrListStrings = new ArrayList<>();

            for (HeartRatesDO hr : hrList) {
                String temp = hr.getUserId() + "\t\t" + hr.getHeartRate().toString();
                hrListStrings.add(temp);
            }

            ArrayAdapter adapter = new ArrayAdapter(TeamMonitoringActivity.this, android.R.layout.simple_expandable_list_item_1, hrListStrings) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (hrList.get(position).getAlert()) {
                        view.setBackgroundColor(Color.YELLOW);
                    } else {
                        view.setBackgroundColor(Color.WHITE);
                    }
                    return view;
                }
            };
            hrListView.setAdapter(adapter);
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
