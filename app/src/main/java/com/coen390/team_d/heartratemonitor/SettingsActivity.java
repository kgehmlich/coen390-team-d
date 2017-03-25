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

public class SettingsActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Button btnSaveSetting =(Button)findViewById(R.id.profileSaveButton);
		btnSaveSetting.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickSaveButton(v);
			}
		});
	}

	private void onClickSaveButton(View v) {

		//TODO Validate age and name edit text

		SharedPreferences prefs =
				getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);

		EditText ageText =(EditText)findViewById(R.id.ageEditText);
		EditText nameText =(EditText)findViewById(R.id.nameEditText);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("age", Integer.valueOf(ageText.getText().toString()));
		editor.putString("name", nameText.getText().toString());
		editor.commit();
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
				//TODO enableEdit() function
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
}
