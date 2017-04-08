package com.example.d_agueg.max_min;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText input;
    String digits;
    int age;
    TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void saveAge (View view)
    {
        input = (EditText) findViewById(R.id.textInput);
        output = (TextView) findViewById(R.id.textOutput);
        digits = String.valueOf(input.getText());


        if (digits.isEmpty())
            output.setText("No input");

       else {

        age = Integer.parseInt(digits);


        if (age<18 || age>65){
            output.setText("Illegal");
            Toast toast = Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT);
            toast.show();
        }
        else
            output.setText("Legal");
            Toast toast = Toast.makeText(this, "Success", Toast.LENGTH_SHORT);
            toast.show();

        }
    }

}


