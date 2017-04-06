package com.coen390.team_d.heartratemonitor;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amazonaws.models.nosql.HeartRatesDO;

import java.util.ArrayList;

public class TeamMonitoringActivity extends AppCompatActivity {

    private GraphView graph;

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

        graph = (GraphView) findViewById(R.id.graphTeam);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(30);
        graph.getViewport().setMaxY(250);

        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HeartRatesDO item = (HeartRatesDO) parent.getItemAtPosition(position);

                // Clear graph
                graph.removeAllSeries();

                // Add this user's data to graph
                graph.addSeries(HeartRateLog.userHRLogs.get(item.getUserId()));
            }
        });
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

            // add new heart rates to logs
            for (HeartRatesDO hr : hrList) {
                HeartRateLog.addHeartRate(hr);
            }

            return 0;
        }

        protected void onPostExecute(Integer integer) {
            Toast.makeText(getApplicationContext(), "Heart rates updated (" + hrList.size() + ")", Toast.LENGTH_LONG).show();
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
