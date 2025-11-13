package com.example.bingo;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SolitarioJuego extends AppCompatActivity {
    private MediaPlayer music;

    private GeneradorNumeros generadorNumeros; //Genera numeros a preionar y los guardaen un array
    private BingoCard bingoCard; //Genera una carta Bingo y guarda cuales se han seleccionado
    private TextView txtNumero;

    //tiempo entre cada numero
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnableJuego;

    //variables utilizados para verificar si termina el juego o no
    private ArrayList<Integer> numerosLlamados;
    private ArrayList<Integer> numerosCarta;

    private static final int TIEMPO_ESPERA = 3000; // 3 segundos


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solitario_juego);

        music = MediaPlayer.create(this, R.raw.inicio_juego);
        music.setLooping(true);

        /*--------------------------------------------------
    BINGO!!! por ahora es un button pero le agregaremos un sensor*/
        numerosCarta = new ArrayList<>();
        numerosLlamados = new ArrayList<>();
        Button rainbowButton = findViewById(R.id.rainbowButton); //BINGO!!
        GradientDrawable rainbow = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#D8B4E2"), // Purple
                        Color.parseColor("#ADD8E6"), // Blue
                        Color.parseColor("#90EE90"), // Green
                        Color.parseColor("#FFFACD"), // Yellow
                        Color.parseColor("#FFD580")  // Orange
                }
        );
        rainbow.setCornerRadius(20f);
        rainbowButton.setBackground(rainbow);
        rainbowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numerosLlamados = generadorNumeros.getNumerosLlamados();
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        numerosCarta.add(bingoCard.cuadros[i][j].numero);
                    }
                }
                Log.d("BingoNumeros", "Lista: " + numerosLlamados);
                Log.d("BingoCarta", "Carta: " + numerosCarta);

            }
        });

        generadorNumeros = new GeneradorNumeros();
        bingoCard = findViewById(R.id.bingoCard);

        txtNumero = findViewById(R.id.txtNumero);

        runnableJuego = new Runnable() {
            @Override
            public void run() {
                llamarSiguienteNumero();
            }
        };

        llamarSiguienteNumero();
    }

    private void llamarSiguienteNumero() {
        int num = generadorNumeros.llamarSiguienteNumero();

        if (num != -1) {
            String letra = generadorNumeros.getLetraParaNumero(num);
            txtNumero.setText(letra + "-" + num);

            efectoSonido();

            handler.postDelayed(runnableJuego, TIEMPO_ESPERA); //prox llamada
        } else {
            txtNumero.setText("GAME");  //se terminan los numeros
            Toast.makeText(this, "Se acabaron los números",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*--------------------------------------------------
    MUSICA*/
    private void efectoSonido() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.pop);

        //listener para liberar memoria
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });

        mp.start();
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

        if (handler != null && runnableJuego != null) {
            handler.removeCallbacks(runnableJuego);
        }

        if (music != null) {
            music.stop();
            music.release();
            music = null;
        }
    }

    private void terminarJuego(){

    }
}
