package com.example.bingo;

import android.graphics.Bitmap;
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
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class SolitarioJuego extends AppCompatActivity implements BingoCard.OnCardChangedListener, SensorEventListener {
    private MediaPlayer music, mpResultado;

    //private a protected pq las usaremos en multijugador juego

    protected GeneradorNumeros generadorNumeros; //Genera numeros a preionar y los guardaen un array
    private BingoCard bingoCard; //Genera una carta Bingo y guarda cuales se han seleccionado
    protected TextView txtNumero;
    private Button rainbowButton;

    //variables sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 15.0f; //sensibilidad, modificar en caso
    private long lastShakeTime = 0;

    //tiempo entre cada numero
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected Runnable runnableJuego;

    //variables utilizados para verificar si termina el juego o no
    private ArrayList<Integer> numerosLlamados;
    private ArrayList<Integer> numerosCarta;

    //numero de bots
    int numBots = 0;
    private ArrayList<Bot> Bots;
    //MutableLiveData<Boolean> botWin = new MutableLiveData<>(false); saber el numbot
    MutableLiveData<Integer> botWinIndex = new MutableLiveData<>(-1);
    protected boolean detenerGenerador = false;

    //nombre del rival en multijugador
    protected String nombreRival = "Rival";

    private static final int TIEMPO_ESPERA = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solitario_juego);

        music = MediaPlayer.create(this, R.raw.inicio_juego);
        music.setLooping(true);

        //sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        numBots = getIntent().getIntExtra("numBots",0);
        Bots = new ArrayList<>();
        if(numBots != 0){
            createBots();
        }

        /*--------------------------------------------------
    BINGO!!! por ahora es un button pero le agregaremos un sensor*/
        numerosCarta = new ArrayList<>();
        numerosLlamados = new ArrayList<>();
        rainbowButton = findViewById(R.id.rainbowButton); //BINGO!!
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
        rainbowButton.setEnabled(false); //desactivado por default

        rainbowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarAccionBingo(); // Llamamos al nuevo método compartido
            }
        });

        /*botWin.observe(this, isWin -> {
            if (isWin != null && isWin) {
                detenerGenerador = true;
                mostrarDialogo(false);
            }
        }); numbot*/

        botWinIndex.observe(this, winningBot -> {
            if (winningBot != -1) {
                detenerGenerador = true;
                mostrarDialogo(false, winningBot); //id del bot
            }
        });

        generadorNumeros = new GeneradorNumeros();
        bingoCard = findViewById(R.id.bingoCard);

        bingoCard.setOnCardChangedListener(this); //actividad como listener

        txtNumero = findViewById(R.id.txtNumero);

        runnableJuego = new Runnable() {
            @Override
            public void run() {
                if(detenerGenerador){
                    return;
                }
                llamarSiguienteNumero();
            }
        };

        llamarSiguienteNumero();
    }

    //accion boton bingo y sensor
    protected void realizarAccionBingo() {
        numerosLlamados = generadorNumeros.getNumerosLlamados();

        numerosCarta.clear();
        ArrayList<Integer> ganadores = obtenerLineaGanadora();

        if (ganadores != null && !ganadores.isEmpty()) {
            numerosCarta.addAll(ganadores);
            boolean allFound = true;
            for (int num : numerosCarta) {
                if (!numerosLlamados.contains(num)) {
                    allFound = false;
                    break;
                }
            }

            if (allFound) {
                mostrarDialogo(true, -1);
            } else {
                mostrarDialogo(false, -3);
            }
        }
    }

    //se llama cada vez que se toque la carta
    @Override
    public void onCardChanged() {
        //revisar si hay bingo para cambiar estado del botón
        if (!obtenerLineaGanadora().isEmpty()) {
            rainbowButton.setEnabled(true);
        } else {
            rainbowButton.setEnabled(false);
        }
    }

    //devuelve los numeros de la linea con bingo
    protected ArrayList<Integer> obtenerLineaGanadora() {
        ArrayList<Integer> lineaGanadora = new ArrayList<>();
        Cuadro[][] cuadros = bingoCard.getCuadros();

        if (cuadros == null) {
            return lineaGanadora;
        }

        //revisa filas
        for (int j = 0; j <5; j++) {
            if (cuadros[0][j].estampa && cuadros[1][j].estampa && cuadros[2][j].estampa &&
            cuadros[3][j].estampa && cuadros[4][j].estampa) {
                for (int i = 0; i < 5; i++) {
                    lineaGanadora.add(cuadros[i][j].numero); //fila completa, guardamos numeros
                }
                return lineaGanadora;
            }
        }

        //revisa columnas
        for (int i = 0; i <5; i++) {
            if (cuadros[i][0].estampa && cuadros[i][1].estampa && cuadros[i][2].estampa &&
                    cuadros[i][3].estampa && cuadros[i][4].estampa) {
                for (int j = 0; j < 5; j++) {
                    lineaGanadora.add(cuadros[i][j].numero); //col completa, guardamos numeros
                }
                return lineaGanadora;
            }
        }

        //diagonal izq-der
        if (cuadros[0][0].estampa && cuadros[1][1].estampa && cuadros[2][2].estampa &&
                cuadros[3][3].estampa && cuadros[4][4].estampa) {

            for(int k=0; k<5; k++) {
                lineaGanadora.add(cuadros[k][k].numero);
            }
            return lineaGanadora;
        }

        //diagonal der-izq
        if (cuadros[4][0].estampa && cuadros[3][1].estampa && cuadros[2][2].estampa &&
                cuadros[1][3].estampa && cuadros[0][4].estampa) {

            for(int k=0; k<5; k++) {
                lineaGanadora.add(cuadros[4-k][k].numero);
            }
            return lineaGanadora;
        }

        return lineaGanadora; //vacia si no hay bingo
    }

    private void llamarSiguienteNumero() {
        int num = generadorNumeros.llamarSiguienteNumero();

        if (num != -1) {
            String letra = generadorNumeros.getLetraParaNumero(num);
            txtNumero.setText(letra + "-" + num);

            efectoSonido();
            if(numBots != 0){
                runBots(); // los bots revisan si tienen el numero
            }
            Log.d("BotPresentados", "Numeros Presentados: " + generadorNumeros.getNumerosLlamados());
            handler.postDelayed(runnableJuego, TIEMPO_ESPERA); //prox llamada
        } else {
            txtNumero.setText("GAME");  //se terminan los numeros
            mostrarDialogo(false, -1);
        }
    }

    //cuadro de fin de juego
    protected void mostrarDialogo(boolean victoria, int botIndex) {
        //detener generador num
        if (handler != null && runnableJuego != null) {
            handler.removeCallbacks(runnableJuego);
        }

        //detener musica y reproducir la de victoria/derrota
        if (music != null && music.isPlaying()) {
            music.pause();
        }

        int sonidoRes = victoria ? R.raw.victoria : R.raw.derrota;

        detenerMusicaResultado();

        mpResultado = MediaPlayer.create(this, sonidoRes);
        if (mpResultado != null) {
            mpResultado.start();
            mpResultado.setOnCompletionListener(mp -> {
                mp.release();
                mpResultado = null; //limpiar ref
            });
        }

        actualizarHistorial(victoria); //actualizar record v/d

        //conf dialogo
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fin_juego);
        dialog.setCancelable(false); //no se quita tocando afuera
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView txtTitulo = dialog.findViewById(R.id.txtTitulo);
        TextView txtMensaje = dialog.findViewById(R.id.txtMensaje);
        Button btnReiniciar = dialog.findViewById(R.id.btnReiniciar);
        Button btnMenu = dialog.findViewById(R.id.btnMenu);

        //si esta en multijugador quitamos el boton de reinciar
        if (this instanceof MultijugadorJuego) {
            btnReiniciar.setVisibility(View.GONE);
        }

        //configurar los textos dependiendo el resultado
        if (victoria) {
            txtTitulo.setText("BINGO!");
            txtTitulo.setTextColor(Color.parseColor("#4CAF50"));
            txtMensaje.setText("¡Ganaste la partida, felicidades!");
        } else {
            txtTitulo.setText("PERDISTE!");
            txtTitulo.setTextColor(Color.parseColor("#F44336"));
            if (botIndex >= 0) {
                //gana un bot
                txtMensaje.setText("Bot " + (botIndex + 1) + " ha ganado la partida.");
            } else if (botIndex == -2) {
                //gana otro usuario
                txtMensaje.setText("¡" + nombreRival + " ha cantado BINGO!");
            } else if (botIndex == -3) {
                //BINGO FALSO, en multiplayer, mensaje y nombre del rival
                if (this instanceof MultijugadorJuego) {
                    txtMensaje.setText("¡BINGO FALSO! " + nombreRival + " gana la partida automáticamente.");
                } else {
                    //solo mensaje
                    txtMensaje.setText("¡BINGO FALSO! Has marcado números que no han salido.");
                }
            } else {
                //se acaban los numeros
                txtMensaje.setText("Se acabaron los números, suerte para la próxima!");
            }
        }

        btnReiniciar.setOnClickListener(v -> {
            detenerMusicaResultado();
            dialog.dismiss();
            recreate();
        });

        btnMenu.setOnClickListener(v -> {
            detenerMusicaResultado();
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void actualizarHistorial(boolean esVictoria) {
        //BingoPrefs
        SharedPreferences prefs = getSharedPreferences("BingoPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        //obtener valores actuales y sumar 1 segun el caso
        if (esVictoria) {
            int victorias = prefs.getInt("victorias", 0);
            editor.putInt("victorias", victorias + 1);
        } else {
            int derrotas = prefs.getInt("derrotas", 0);
            editor.putInt("derrotas", derrotas + 1);
        }

        editor.apply(); //guardar cambios
    }

    /* ------------------------------  BOTS  ------------------------------- */
    private void createBots(){
        for(int i=0; i<numBots; i++){
            Bots.add(new Bot());
        }
    }
    private void runBots(){
        int lastCalledNumber = generadorNumeros.getNumeroActual();
        for(int num=0; num<numBots; num++){
            boolean found = false;
            for(int i=0; i<5 && !found; i++){
                for(int j=0; j<5; j++){
                    if(Bots.get(num).cuadrosBot[i][j].numero == lastCalledNumber){
                        Bots.get(num).cuadrosBot[i][j].estampa = true;
                        found = true;
                        if (!obtenerLineaGanadoraBot(num).isEmpty()) {
                            //botWin.setValue(true); numbot
                            botWinIndex.setValue(num);
                            return;
                        }
                        break;
                    }
                }
            }
        }
    }

    //devuelve los numeros de la linea con bingo
    private ArrayList<Integer> obtenerLineaGanadoraBot(int num) {
        ArrayList<Integer> lineaGanadora = new ArrayList<>();
        Cuadro[][] cuadrosBot = Bots.get(num).getCuadros();

        if (cuadrosBot == null) {
            return lineaGanadora;
        }

        //revisa filas
        for (int j = 0; j <5; j++) {
            if (cuadrosBot[0][j].estampa && cuadrosBot[1][j].estampa && cuadrosBot[2][j].estampa &&
                    cuadrosBot[3][j].estampa && cuadrosBot[4][j].estampa) {
                for (int i = 0; i < 5; i++) {
                    lineaGanadora.add(cuadrosBot[i][j].numero); //fila completa, guardamos numeros
                }
                return lineaGanadora;
            }
        }

        //revisa columnas
        for (int i = 0; i <5; i++) {
            if (cuadrosBot[i][0].estampa && cuadrosBot[i][1].estampa && cuadrosBot[i][2].estampa &&
                    cuadrosBot[i][3].estampa && cuadrosBot[i][4].estampa) {
                for (int j = 0; j < 5; j++) {
                    lineaGanadora.add(cuadrosBot[i][j].numero); //col completa, guardamos numeros
                }
                return lineaGanadora;
            }
        }

        //diagonal izq-der
        if (cuadrosBot[0][0].estampa && cuadrosBot[1][1].estampa && cuadrosBot[2][2].estampa &&
                cuadrosBot[3][3].estampa && cuadrosBot[4][4].estampa) {

            for(int k=0; k<5; k++) {
                lineaGanadora.add(cuadrosBot[k][k].numero);
            }
            return lineaGanadora;
        }

        //diagonal der-izq
        if (cuadrosBot[4][0].estampa && cuadrosBot[3][1].estampa && cuadrosBot[2][2].estampa &&
                cuadrosBot[1][3].estampa && cuadrosBot[0][4].estampa) {

            for(int k=0; k<5; k++) {
                lineaGanadora.add(cuadrosBot[4-k][k].numero);
            }
            return lineaGanadora;
        }

        return lineaGanadora; //vacia si no hay bingo
    }
    /*--------------------------------------------------
    MUSICA*/
    private void detenerMusicaResultado() {
        if (mpResultado != null) {
            if (mpResultado.isPlaying()) {
                mpResultado.stop();
            }
            mpResultado.release();
            mpResultado = null;
        }
    }

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

        //registrar sensor
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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

        //eliminar sensor
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
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

        detenerMusicaResultado();
    }

    private void terminarJuego(){

    }

    //SENSOR--------------------------------------
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //magnitud
            float currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = currentAcceleration - SensorManager.GRAVITY_EARTH;

            if (delta > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastShakeTime) > 1000) { //1seg entre sacudida
                    lastShakeTime = currentTime;

                    //solo funciona si el boton se activa
                    if (rainbowButton.isEnabled()) {
                        realizarAccionBingo();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
