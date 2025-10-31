package com.example.bingo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SolitarioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solitario); // connects to activity_second.xml
    }
}
