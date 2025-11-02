package com.example.bingo;

import android.content.Intent;
import android.content.SharedPreferences;
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

    public static final String PREFS_NAME = "BingoPrefs";
    public static final String PREF_IS_FIRST_RUN = "isFirstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_screen);

        //GIF de BINGO (en pantalla de carga)
        imageView = findViewById(R.id.LoadingBingo);
        Glide.with(this).load(R.drawable.bingo_load).into(imageView);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isFirstRun = prefs.getBoolean(PREF_IS_FIRST_RUN, true);

            //condicion para mostrar o no el registro de username
            Intent intent;
            if (isFirstRun) {
                //ir a registrar
                intent = new Intent(LoadingScreen.this, RegistrarActivity.class);
            } else {
                //ir al menu
                intent = new Intent(LoadingScreen.this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        }, 8000);

    }
}