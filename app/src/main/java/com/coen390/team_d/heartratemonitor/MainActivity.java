package com.coen390.team_d.heartratemonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.coen390.team_d.util.EmailNotification;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonNotification = (Button) findViewById(R.id.btnManual);
        buttonNotification.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast toast = Toast.makeText(getApplicationContext(), "*Alert has been sent *", Toast.LENGTH_LONG);
                toast.show();


            }
        });



    }
}
