package com.coen390.team_d.heartratemonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            case R.id.toggleMonitoring:
                //TODO toggleMonitoring() function
                break;
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
    
    private void goToTeamMonitoringActivity() {
        Intent intent = new Intent(MainActivity.this, TeamMonitoringActivity.class);
        startActivity(intent);
    }
    
    private void goToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
    
}
