package com.rutillastoby.zoria.dao;

import java.util.HashMap;

public class UsuarioDao {
    private String uid;
    private String foto;
    private String nombre;
    private Integer compeActiva; //Competicion actual (para fragment current)
    private HashMap<String, Integer> competiciones = new HashMap<String, Integer>();

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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getCompeActiva() {
        return compeActiva;
    }

    public void setCompeActiva(Integer compeActiva) {
        this.compeActiva = compeActiva;
    }

    public HashMap<String, Integer> getCompeticiones() {
        return competiciones;
    }

    public void setCompeticiones(HashMap<String, Integer> competiciones) {
        this.competiciones = competiciones;
    }
}
