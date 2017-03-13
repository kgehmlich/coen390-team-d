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
qsa                toast.show();

                // Email Notificacion call
                try {
                    EmailNotification sender = new EmailNotification("coen390teamd@gmail.com", "heartrate");
                    sender.sendMail("message",
                            "Test",
                            "coen390teamd@gmail.com",
                            "coen390teamd@gmail.com");
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        });



    }
}
