package com.example.bingo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bingo.GestorRed;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SalaEsperaActivity extends AppCompatActivity {

    private TextView tvRoomID, tvStatus;
    private Button btnIniciarPartida;
    private MediaPlayer music;
    private boolean isHost;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Thread connectionThread;
    private final int PORT = 8888;

    private boolean iniciandoJuego = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala_espera);

        tvRoomID = findViewById(R.id.tvRoomID);
        tvStatus = findViewById(R.id.tvStatus);
        btnIniciarPartida = findViewById(R.id.btnIniciarPartida);

        music = MediaPlayer.create(this, R.raw.sala_espera);
        music.setLooping(true);
        music.start();

        //datos del menu
        isHost = getIntent().getBooleanExtra("isHost", false);

        if (isHost) {
            setupServer();
        } else {
            String hostIp = getIntent().getStringExtra("hostIp");
            setupClient(hostIp);
        }
    }

    //host
    private void setupServer() {
        String ip = getLocalIpAddress();
        tvRoomID.setText("ID DE SALA: " + ip);
        tvStatus.setText("Esperando jugadores...");

        connectionThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                //esperar conexion
                clientSocket = serverSocket.accept();

                //leer el nombre enviado por el cliente
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final String nombreRival = in.readLine();

                //enviar el nombre hosto a cliente
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                SharedPreferences prefs = getSharedPreferences("BingoPrefs", MODE_PRIVATE);
                String miNombre = prefs.getString("username", "Host");
                out.println(miNombre);

                //jugador conectado
                runOnUiThread(() -> {
                    tvStatus.setText("¡" + nombreRival + " se ha unido!");
                    btnIniciarPartida.setVisibility(View.VISIBLE);
                    btnIniciarPartida.setOnClickListener(v -> startGame(nombreRival));
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> tvStatus.setText("Error al crear servidor"));
            }
        });
        connectionThread.start();
    }

    //cliente
    private void setupClient(String hostIp) {
        tvRoomID.setVisibility(View.GONE);
        tvStatus.setText("Conectando a: " + hostIp + "...");

        connectionThread = new Thread(() -> {
            try {
                //conectar
                clientSocket = new Socket(hostIp, PORT);

                //obtener el nombre guardado del usuario y enviar nombre al servidor
                SharedPreferences prefs = getSharedPreferences("BingoPrefs", MODE_PRIVATE);
                String miNombre = prefs.getString("username", "Invitado");
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(miNombre);

                //recibir nombre del host
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String nombreHost = in.readLine();

                runOnUiThread(() -> tvStatus.setText("Conectado con " + nombreHost + ". Esperando inicio..."));

                //bucle esperando start
                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    if (mensaje.startsWith("START:")) {
                        //extraer semilla
                        String[] partes = mensaje.split(":");
                        long seed = Long.parseLong(partes[1]);

                        runOnUiThread(() -> {
                            Intent intent = new Intent(SalaEsperaActivity.this, MultijugadorJuego.class);
                            intent.putExtra("seed", seed); //misma semilla que el host
                            intent.putExtra("isHost", false);
                            intent.putExtra("nombreRival", nombreHost); //pasar nombre host al juego
                            GestorRed.setSocket(clientSocket);
                            iniciandoJuego = true;
                            startActivity(intent);
                            finish();
                        });
                        break; //salir bucle
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvStatus.setText("Error: No se encontró la partida");
                    mostrarToastPersonalizado("Verifica el ID (IP)", R.drawable.alert);
                });
            }
        });
        connectionThread.start();
    }

    private void startGame(String nombreRival) {
        //generar semilla comun en base al tiempo
        long seed = System.currentTimeMillis();

        //enviar start y semilla al cliente
        new Thread(() -> {
            try {
                if (clientSocket != null) {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("START:" + seed);

                    //iniciar el juego
                    runOnUiThread(() -> {
                        Intent intent = new Intent(SalaEsperaActivity.this, MultijugadorJuego.class);
                        intent.putExtra("seed", seed); //pasar la semilla
                        intent.putExtra("isHost", true);
                        intent.putExtra("nombreRival", nombreRival);

                        GestorRed.setSocket(clientSocket);
                        iniciandoJuego = true;
                        startActivity(intent);
                        finish();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //obtener la ipx1
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        try {
            return InetAddress.getByAddress(
                            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array())
                    .getHostAddress();
        } catch (UnknownHostException e) {
            return "Error IP";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (music != null) music.release();

        //cerrar el server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //client socket solo se cierra si no vamos a jugar
        if (!iniciandoJuego) {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //toast
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