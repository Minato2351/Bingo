package com.example.bingo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SolitarioMenu extends AppCompatActivity {
    private MediaPlayer music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solitario_menu); // connects to activity_second.xml

        music = MediaPlayer.create(this, R.raw.menu);
        music.setLooping(true);

        Button Comenzar = findViewById(R.id.btnComenzar);
        Button Regresar = findViewById(R.id.btnRegresar);
        Button leftArrow = findViewById(R.id.leftArrow);
        Button rightArrow = findViewById(R.id.rightArrow);
        TextView TextBotNum = findViewById(R.id.TextBotNum);

        Comenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SolitarioMenu.this, SolitarioJuego.class);
                String text = TextBotNum.getText().toString();
                int value = Integer.parseInt(text);
                intent.putExtra("numBots", value); //manda al SolitarioJuego el numBots
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

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = TextBotNum.getText().toString();
                int value = Integer.parseInt(text);
                value--;
                if(value<0){
                    Toast.makeText(SolitarioMenu.this, "Limite Inferior", Toast.LENGTH_SHORT).show();
                }else{
                    TextBotNum.setText(String.valueOf(value));
                }
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = TextBotNum.getText().toString();
                int value = Integer.parseInt(text);
                value++;
                if(value>3){
                    Toast.makeText(SolitarioMenu.this, "Limite Superior", Toast.LENGTH_SHORT).show();
                }else{
                    TextBotNum.setText(String.valueOf(value));
                }
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
