package com.rutillastoby.zoria.dao.competicion;

import java.util.HashMap;

public class Jugador {
    private String uid;
    private String tipo;
    private HashMap<String, Integer> preguntas;
    private HashMap<String, Long> puntos;

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
        if(preguntas==null)
            return new HashMap<String, Integer>();
        return preguntas;
    }

    public void setPreguntas(HashMap<String, Integer> preguntas) {
        this.preguntas = preguntas;
    }

    public HashMap<String, Long> getPuntos() {
        if(puntos==null)
            return new HashMap<String, Long>();
        return puntos;
    }

    public void setPuntos(HashMap<String, Long> puntos) {
        this.puntos = puntos;
    }
}
