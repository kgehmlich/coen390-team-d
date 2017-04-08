package com.coen390.team_d.heartratemonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//Graph related imports
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
	
	private static final String TAG = "SettingsActivity";
	protected EditText nameEditText;
	protected EditText ageEditText;
	protected TextView nameState;
	protected TextView ageState;
	protected boolean editFlag = false;
	protected Button btnSaveSetting;
	protected Button cancelButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		nameEditText = (EditText) findViewById(R.id.nameEditText);
		ageEditText = (EditText) findViewById(R.id.ageEditText);
		nameState = (TextView) findViewById(R.id.nameState);
		ageState = (TextView) findViewById(R.id.ageState);
		btnSaveSetting =(Button)findViewById(R.id.profileSaveButton);
		cancelButton =(Button)findViewById(R.id.cancelButton);
		nameEditText.setFocusable(false);
		ageEditText.setFocusable(false);
		btnSaveSetting.setVisibility(View.INVISIBLE);
		cancelButton.setVisibility(View.INVISIBLE);
		populateFields();
		Log.d(TAG, "onCreate()");
		populateFields();
    
    
    SharedPreferences prefs = getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);
    String name = prefs.getString("name", null);
    if (name != null) nameEditText.setText(name);

    int age = prefs.getInt("age", 0);
    if (age != 0) ageEditText.setText(Integer.toString(age));
		
		
		btnSaveSetting.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (onClickSaveButton(v)){
					finish();
				}
				
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}
	
	//Repopulate the fields with the data saved in the SharedPreference, or clear them if no saved data
	void populateFields(){
		SharedPreferences prefs = getSharedPreferences("SettingsPreferences",Context.MODE_PRIVATE);
		String name = prefs.getString("name", null);
		int age = prefs.getInt("age", -1);
		
		if(name != null) {
			nameEditText.setText(name);
		}
		else{
			nameEditText.setText("");
		}
		if(age != -1) {
			ageEditText.setText(Integer.toString(age));
		}
		else{
			ageEditText.setText("");
		}
	}

	private boolean onClickSaveButton(View v) {
		// Validate age and name edit text
        String nameString = nameTextBox.getText().toString();
        int age = Integer.valueOf(ageTextBox.getText().toString());

        if (!nameString.matches("[A-Za-z]+([ -][A-Za-z]+)*")) {
            nameTextBox.setError("Only letters, hyphens, and spaces are allowed. Must end with a letter.");
            return;
        }

        if (nameString.length() > 25) {
            nameTextBox.setError("Must be 25 characters or fewer.");
            return false;
        }

        if (age > 65 || age < 18) {
            ageTextBox.setError("Must be between 18-65.");
            return false;
        }


        SharedPreferences prefs =
                getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("age", age);
        editor.putString("name", nameString);
        editor.commit();

        Toast.makeText(getApplicationContext(), "Profile saved", Toast.LENGTH_SHORT).show();
        return true;
	}


	/**
	 * Adds toolbar menu to this activity
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		return true;
	}
	
	/**
	 * Handles menu item clicks
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// If the "Enable Edit" menu button was clicked, make the text inputs editable
		switch (item.getItemId()) {
			case R.id.enableEdit:
				// User chose the "Edit" item, make fields editable...
				if (!editFlag)
					toggleEditable();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}
	//Toggle the editability of the fields and the visibility of the save, cancel & edit buttons
	void toggleEditable(){
		//if editflag = true, content was editable, will switch to uneditable
		if (editFlag){
			nameEditText.setFocusable(false);
			ageEditText.setFocusable(false);
			editFlag = false;
			btnSaveSetting.setVisibility(View.INVISIBLE);
			cancelButton.setVisibility(View.INVISIBLE);
			populateFields();
			finish();
		}
		//if editflag = false, content was not editable, will switch to editable
		else {
			nameEditText.setFocusableInTouchMode(true);
			ageEditText.setFocusableInTouchMode(true);
			editFlag = true;
			btnSaveSetting.setVisibility(View.VISIBLE);
			cancelButton.setVisibility(View.VISIBLE);
		}
		invalidateOptionsMenu();
	}
}
