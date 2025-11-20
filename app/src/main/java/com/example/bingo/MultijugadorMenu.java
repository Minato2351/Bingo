package com.example.bingo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MultijugadorMenu extends AppCompatActivity {
    private MediaPlayer music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multijugador_menu);

        music = MediaPlayer.create(this, R.raw.menu);
        music.setLooping(true);

        Button btnCrear = findViewById(R.id.btnCrear);
        Button btnUnirse = findViewById(R.id.btnUnirse);
        EditText txtID = findViewById(R.id.txtID);

        btnCrear.setOnClickListener(v -> {
            Intent intent = new Intent(MultijugadorMenu.this, SalaEsperaActivity.class);
            intent.putExtra("isHost", true);
            startActivity(intent);
        });

        btnUnirse.setOnClickListener(v -> {
            String idIngresado = txtID.getText().toString().trim();
            if (!idIngresado.isEmpty()) {
                Intent intent = new Intent(MultijugadorMenu.this, SalaEsperaActivity.class);
                intent.putExtra("isHost", false);
                intent.putExtra("hostIp", idIngresado); //id es la ip
                startActivity(intent);
            } else {
                txtID.setError("Ingresa el ID de la sala");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //iniciar musica
        if (music != null) {
            music.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //pausar musica
        if (music != null && music.isPlaying()) {
            music.pause();
            //reiniciar
            music.seekTo(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (music != null) {
            music.stop();
            music.release();
            music = null;
        }
    }
}
