package com.example.bingo;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MultijugadorMenu extends AppCompatActivity {
    private MediaPlayer music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multijugador_menu);

        music = MediaPlayer.create(this, R.raw.menu);
        music.setLooping(true);
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
