package com.rutillastoby.zoria.dao;

import com.rutillastoby.zoria.dao.competicion.Hora;
import com.rutillastoby.zoria.dao.competicion.Jugador;
import com.rutillastoby.zoria.dao.competicion.Pregunta;

import java.util.HashMap;

public class CompeticionDao {
    private String foto;
    private int pwd;
    private String nombre;
    private Hora hora;
    private int id;
    private HashMap<String, Pregunta> preguntas;
    private HashMap<String, Jugador> jugadores;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPwd() {
        return pwd;
    }

    public void setPwd(int pwd) {
        this.pwd = pwd;
    }

    public Hora getHora() {
        return hora;
    }

    public void setHora(Hora hora) {
        this.hora = hora;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public HashMap<String, Pregunta> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(HashMap<String, Pregunta> preguntas) {
        this.preguntas = preguntas;
    }

    public HashMap<String, Jugador> getJugadores() {
        return jugadores;
    }

    public void setJugadores(HashMap<String, Jugador> jugadores) {
        this.jugadores = jugadores;
    }
}
