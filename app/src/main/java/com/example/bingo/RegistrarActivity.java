package com.example.bingo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrarActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "BingoPrefs";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_IS_FIRST_RUN = "isFirstRun";

    EditText etUsername;
    Button btnSaveName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        etUsername = findViewById(R.id.etUsername);
        btnSaveName = findViewById(R.id.btnSaveName);

        btnSaveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNameAndLaunchMain();
            }
        });
    }

    private void saveNameAndLaunchMain() {
        String username = etUsername.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Introduce un nombre");
            return;
        }

        //guardar datos
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PREF_USERNAME, username);
        editor.putBoolean(PREF_IS_FIRST_RUN, false); //clave
        editor.apply();

        //iniciar act principal
        Intent intent = new Intent(RegistrarActivity.this, MainActivity.class);

        //limpiar actividades
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        //finalizar actividad
        finish();
    }
}