package com.primewebtech.darts;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Button button1 = (Button) findViewById(R.id.button1);
        GradientDrawable drawable = (GradientDrawable)button1.getBackground();
        drawable.setStroke(2, Color.WHITE);

    }
}
