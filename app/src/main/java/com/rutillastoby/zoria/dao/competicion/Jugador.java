package com.rutillastoby.zoria.dao.competicion;

import java.util.HashMap;

public class Jugador {
    private String tipo;
    private HashMap<String, Integer> preguntas;
    private HashMap<String, String> puntos;
    private int fin; //0 no ha finalizado, 1 si ha finalizado

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public HashMap<String, Integer> getPreguntas() {
        if (preguntas == null)
            return new HashMap<String, Integer>();
        return preguntas;
    }

    public void setPreguntas(HashMap<String, Integer> preguntas) {
        this.preguntas = preguntas;
    }

    public HashMap<String, String> getPuntos() {
        if(puntos==null)
            return new HashMap<String, String>();
        return puntos;
    }

    public void setPuntos(HashMap<String, String> puntos) {
        this.puntos = puntos;
    }

    public int getFin() {
        return fin;
    }

    public void setFin(int fin) {
        this.fin = fin;
    }
}
