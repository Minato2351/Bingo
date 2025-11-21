package com.example.bingo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeneradorNumeros {

    private List<Integer> numerosDisponibles; //1-75
    private ArrayList<Integer> numerosLlamados; //guardar numeros

    //modo solitario
    public GeneradorNumeros() {
        this(System.currentTimeMillis());
    }

    //semilla para multijugador
    public GeneradorNumeros(long seed) {
        numerosDisponibles = new ArrayList<>();
        numerosLlamados = new ArrayList<>();

        numerosLlamados.add(0); //agrega el numero 0 (estrella central del carta)

        for (int i = 1; i <= 75; i++) {
            numerosDisponibles.add(i);
        }

        //random con semilla compartida
        Collections.shuffle(numerosDisponibles, new Random(seed));
    }

    public int llamarSiguienteNumero() {
        if (numerosDisponibles.isEmpty()) {
            return -1; //-1 si ya no hay mas numeros
        }

        //sacar de la baraja
        int numeroLlamado = numerosDisponibles.remove(0);

        //guardar en historial de llamados
        numerosLlamados.add(numeroLlamado);

        return numeroLlamado;
    }

    //letra correspondiente
    public String getLetraParaNumero(int numero) {
        if (numero >= 1 && numero <= 15) {
            return "B";
        } else if (numero >= 16 && numero <= 30) {
            return "I";
        } else if (numero >= 31 && numero <= 45) {
            return "N";
        } else if (numero >= 46 && numero <= 60) {
            return "G";
        } else if (numero >= 61 && numero <= 75) {
            return "O";
        }
        return "";
    }

    public ArrayList<Integer> getNumerosLlamados() {
        return numerosLlamados;
    }

    public int getNumeroActual(){
        return numerosLlamados.get(numerosLlamados.size() - 1);
    }
}
