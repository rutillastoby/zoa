package com.rutillastoby.zoria.dao;

import com.rutillastoby.zoria.dao.competicion.Hora;
import com.rutillastoby.zoria.dao.competicion.Jugador;
import com.rutillastoby.zoria.dao.competicion.Pregunta;
import com.rutillastoby.zoria.dao.competicion.Punto;
import java.util.HashMap;

public class CompeticionDao {
    private String foto;
    private int pwd;
    private String nombre;
    private Hora hora;
    private int id;
    private int tipo; //Tipo: 0 diurno, 1 nocturno.
    private int res; //Resultados: 0 no mostrar resultados, 1 si mostrar resultados
    private int ubi; //0 No obtener ubicaciones usuarios, 1 Obtener ubicaciones usuarios
    private HashMap<String, Pregunta> preguntas;
    private HashMap<String, Jugador> jugadores;
    private HashMap<String, Punto> puntos;


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

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }

    public HashMap<String, Pregunta> getPreguntas() {
        if(preguntas==null)
             return new HashMap<String, Pregunta>();
        return preguntas;
    }

    public void setPreguntas(HashMap<String, Pregunta> preguntas) {
        this.preguntas = preguntas;
    }

    public HashMap<String, Jugador> getJugadores() {
        if(jugadores==null)
             return new HashMap<String, Jugador>();
        return jugadores;
    }

    public void setJugadores(HashMap<String, Jugador> jugadores) {
        this.jugadores = jugadores;
    }

    public HashMap<String, Punto> getPuntos() {
        if(puntos==null)
             return new HashMap<String, Punto>();
        return puntos;
    }

    public void setPuntos(HashMap<String, Punto> puntos) {
        this.puntos = puntos;
    }

    public int getUbi() {
        return ubi;
    }

    public void setUbi(int ubi) {
        this.ubi = ubi;
    }
}
