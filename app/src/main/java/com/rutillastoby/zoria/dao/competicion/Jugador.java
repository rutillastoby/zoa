package com.rutillastoby.zoria.dao.competicion;

import java.util.HashMap;

public class Jugador {
    private String uid;
    private String tipo;
    private HashMap<String, Integer> preguntas = new HashMap<String, Integer>();
    private HashMap<String, String> puntos = new HashMap<String, String>();

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public HashMap<String, Integer> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(HashMap<String, Integer> preguntas) {
        this.preguntas = preguntas;
    }

    public HashMap<String, String> getPuntos() {
        return puntos;
    }

    public void setPuntos(HashMap<String, String> puntos) {
        this.puntos = puntos;
    }
}
