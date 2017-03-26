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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.models.nosql.HeartRatesDO;

import java.util.ArrayList;
import java.util.concurrent.RunnableFuture;

public class TeamMonitoringActivity extends AppCompatActivity {

	private Handler updateHandler;
    private final static int UPDATE_DELAY_SECS = 10;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_team_monitoring);

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

            ArrayAdapter adapter = new ArrayAdapter(TeamMonitoringActivity.this, android.R.layout.simple_expandable_list_item_1, hrListStrings);
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
