package com.example.bingo;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class Bot { //Es similar al BingoCard pero sin vista
    Cuadro[][] cuadrosBot = new Cuadro[5][5];
    int[] Numeros = new int[5]; //Evita repeticion
    boolean repetido = false;
    int numeroRand = 0;

    Bot(){
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                numeroRand = (int)(Math.random() * (15) + 1 + (i * 15));
                for(int z=0; z<5; z++){
                    if(numeroRand == Numeros[z]){
                        repetido = true;
                    }
                }
                if(repetido == false){
                    Numeros[j] = numeroRand;
                    cuadrosBot[i][j] = new Cuadro(numeroRand, false);
                }else if(repetido == true){
                    j--;
                    repetido = false;
                }
            }
            Log.d("BotCarta", "Numeros Guardados: " + Arrays.toString(Numeros));
        }
        cuadrosBot[2][2].numero = 0;
        cuadrosBot[2][2].estampa = true;

    }
    public Cuadro[][] getCuadros() {
        return cuadrosBot;
    }
}
