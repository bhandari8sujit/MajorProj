package com.example.sujit.customerapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewInfoOfRide extends AppCompatActivity {

    Button buttonRideInfo;
    TextView textView6, textView7,textView8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_info_of_ride);

        buttonRideInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ViewInfoOfRide.this, MainActivity.class));
            }
        });
    }
}
