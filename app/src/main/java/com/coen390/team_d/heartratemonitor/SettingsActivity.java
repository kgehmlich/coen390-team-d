package com.coen390.team_d.heartratemonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//Graph related imports
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

public class SettingsActivity extends AppCompatActivity {

    EditText nameTextBox;
    EditText ageTextBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnSaveSetting = (Button) findViewById(R.id.profileSaveButton);
        btnSaveSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSaveButton(v);
            }
        });

        nameTextBox = (EditText) findViewById(R.id.nameEditText);
        ageTextBox = (EditText) findViewById(R.id.ageEditText);

        SharedPreferences prefs = getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);
        String name = prefs.getString("name", null);
        if (name != null) nameTextBox.setText(name);

        int age = prefs.getInt("age", 0);
        if (age != 0) ageTextBox.setText(Integer.toString(age));
    }

    private void onClickSaveButton(View v) {

        //TODO Validate age and name edit text

        SharedPreferences prefs =
                getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);

        EditText ageText = (EditText) findViewById(R.id.ageEditText);
        EditText nameText = (EditText) findViewById(R.id.nameEditText);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("age", Integer.valueOf(ageText.getText().toString()));
        editor.putString("name", nameText.getText().toString());
        editor.commit();

        Toast.makeText(getApplicationContext(), "Profile saved", Toast.LENGTH_SHORT).show();
    }

    /**
     * Adds toolbar menu to this activity
     */
    /*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		return true;
	}*/

    /**
     * Handles menu item clicks
     */
	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// If the "Enable Edit" menu button was clicked, make the text inputs editable
		switch (item.getItemId()) {
			case R.id.enableEdit:
				//TODO enableEdit() function
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}*/
}
