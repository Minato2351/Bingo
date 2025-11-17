package com.example.bingo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneradorNumeros {

    //todos los num posibles, 1-75
    private List<Integer> numerosDisponibles;

    //guardar numeros
    private ArrayList<Integer> numerosLlamados;

    public GeneradorNumeros() {
        numerosDisponibles = new ArrayList<>();
        numerosLlamados = new ArrayList<>();

        for (int i = 1; i <= 75; i++) {
            numerosDisponibles.add(i);
        }

        //ordenar random
        Collections.shuffle(numerosDisponibles);
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
        numerosLlamados.add(0);
        return numerosLlamados;
    }
}
