package com.example.bingo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SolitarioMenu extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solitario_menu); // connects to activity_second.xml

        Button Comenzar = findViewById(R.id.btnComenzar);
        Button Regresar = findViewById(R.id.btnRegresar);

        Comenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SolitarioMenu.this, SolitarioJuego.class);
                startActivity(intent);
            }
        });

        Regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SolitarioMenu.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
