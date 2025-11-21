package com.example.bingo;

import android.os.Bundle;
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
        // Al llamar a super, SolitarioJuego inicia su propia partida automáticamente.
        // Nosotros debemos detenerla y reiniciarla con nuestros datos sincronizados.

        //recuperar datos del intent
        long seed = getIntent().getLongExtra("seed", -1);

        //config conexion
        socket = GestorRed.getSocket();
        if (socket != null) {
            setupNetwork();
        } else {
            Toast.makeText(this, "Error: Conexión perdida", Toast.LENGTH_SHORT).show();
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
                    if (msg.equals("BINGO_WIN")) {
                        //rival gana
                        runOnUiThread(() -> {
                            //detener juego
                            detenerGenerador = true;
                            if (handler != null) handler.removeCallbacks(runnableJuego);

                            //pantalla derrota
                            mostrarDialogo(false, -1);
                        });
                        break; //salir bucle
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
                //avisar cliente o servidor
                new Thread(() -> {
                    if (out != null) out.println("BINGO_WIN");
                }).start();

                //mostrar victoria local
                mostrarDialogo(true, -1);

            } else {
                //bingo incorrecto
                mostrarDialogo(false, -1);
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
}