package com.example.bingo;

import java.net.Socket;

public class GestorRed {
    //no se pueden pasar objetos socket directamente entre actividades a través de un Intent
    //esta clase guarda la conexión activa

    private static Socket socket;
    public static void setSocket(Socket s) { socket = s; }
    public static Socket getSocket() { return socket; }
}
