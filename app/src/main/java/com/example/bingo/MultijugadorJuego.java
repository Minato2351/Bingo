package com.example.bingo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MultijugadorJuego extends SolitarioJuego {

    private Socket socket;
    private PrintWriter out;
    private Thread listenerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String nombreRecibido = getIntent().getStringExtra("nombreRival");
        if (nombreRecibido != null) {
            this.nombreRival = nombreRecibido; //sobreescribir variable padre
        }

        //recuperar datos del intent
        long seed = getIntent().getLongExtra("seed", -1);

        //config conexion
        socket = GestorRed.getSocket();
        if (socket != null) {
            setupNetwork();
        } else {
            mostrarToastPersonalizado("Verifica el ID (IP)", R.drawable.alert);
        }

        //sync generador de numeros
        if (seed != -1) {
            //detener runnable
            if (handler != null && runnableJuego != null) {
                handler.removeCallbacks(runnableJuego);
            }

            //reiniciar generador con semilla compartida
            generadorNumeros = new GeneradorNumeros(seed);

            //limpiar texto visual para iniciar desde 0
            if (txtNumero != null) {
                txtNumero.setText("LISTOS...");
            }

            //reiniciar bandera de control
            detenerGenerador = false;

            //bucle de juego ahora con generador sync
            handler.postDelayed(runnableJuego, 2000);
        }
    }

    private void setupNetwork() {
        try {
            //output para enviar mensajes
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //escuchar mensajes del rival
        listenerThread = new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg;
                while ((msg = in.readLine()) != null) {
                    //el rival tiene bingo, pierdes
                    if (msg.equals("BINGO_WIN")) {
                        runOnUiThread(() -> {
                            //detener juego
                            detenerGenerador = true;
                            if (handler != null) handler.removeCallbacks(runnableJuego);
                            //pantalla derrota
                            mostrarDialogo(false, -2);
                        });
                        break; //salir bucle
                    } else if (msg.equals("RIVAL_PERDIO")) {
                        runOnUiThread(() -> {
                            detenerGenerador = true;
                            if (handler != null) handler.removeCallbacksAndMessages(null);
                            //victoria
                            mostrarDialogo(true, -1);
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listenerThread.start();
    }

    @Override
    protected void realizarAccionBingo() {
        ArrayList<Integer> llamados = generadorNumeros.getNumerosLlamados();
        ArrayList<Integer> ganadores = obtenerLineaGanadora();

        if (ganadores != null && !ganadores.isEmpty()) {
            boolean allFound = true;
            for (int num : ganadores) {
                if (!llamados.contains(num)) {
                    allFound = false;
                    break;
                }
            }

            if (allFound) {
                //avisar cliente o servidor que ganaste
                new Thread(() -> {
                    if (out != null) out.println("BINGO_WIN");
                }).start();

                //mostrar victoria local
                mostrarDialogo(true, -1);

            } else {
                //bingo incorrecto, avisar al rival que perdiste por bingo falso
                new Thread(() -> {
                    if (out != null) out.println("RIVAL_PERDIO");
                }).start();

                mostrarDialogo(false, -3);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //cerrar conexion
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mostrarToastPersonalizado(String mensaje, int iconoResId) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(mensaje);

        ImageView image = layout.findViewById(R.id.toast_icon);
        if (iconoResId != 0) {
            image.setImageResource(iconoResId);
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}