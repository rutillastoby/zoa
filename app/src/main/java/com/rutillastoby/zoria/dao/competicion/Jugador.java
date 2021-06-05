package com.rutillastoby.zoria.dao.competicion;

import java.util.HashMap;

//Representacion de datos de la bd
public class Jugador {
    private HashMap<String, Integer> preguntas;
    private HashMap<String, String> puntos;
    private int fin; //0 no ha finalizado individualmente, 1 si ha finalizado individualmente (segun si se ha atrapado la bandera)

    //------------------------------------------------------------>

    public HashMap<String, Integer> getPreguntas() {
        if (preguntas == null) {
            return new HashMap<String, Integer>();
        }
        return preguntas;
    }

    public HashMap<String, String> getPuntos() {
        if(puntos==null)
            return new HashMap<String, String>();
        return puntos;
    }

    public int getFin() {
        return fin;
    }
}