package com.example.bingo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class LoadingScreen extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_screen);

        //GIF de BINGO (en pantalla de carga)
        imageView = findViewById(R.id.LoadingBingo);
        Glide.with(this).load(R.drawable.bingo_load).into(imageView);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(LoadingScreen.this, MainActivity.class));
            finish(); // Cierra la pantalla de carga para no volver atrás con el botón de retroceso
        }, 8000);

    }
}