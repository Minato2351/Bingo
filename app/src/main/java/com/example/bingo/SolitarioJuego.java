package com.example.bingo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SolitarioJuego extends AppCompatActivity {
    private MediaPlayer music;

    private GeneradorNumeros generadorNumeros;
    private TextView txtNumero;

    //tiempo entre cada numero
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnableJuego;

    private static final int TIEMPO_ESPERA = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solitario_juego);

        music = MediaPlayer.create(this, R.raw.inicio_juego);
        music.setLooping(true);

        generadorNumeros = new GeneradorNumeros();

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
}
