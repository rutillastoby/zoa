package com.rutillastoby.zoria.dao;

import java.util.ArrayList;
import java.util.HashMap;

public class UsuarioDao {
    private String uid;
    private String foto;
    private String nombre;
    private Integer compeActiva; //Competicion actual (para fragment current)

    //Parametros dinamicos (No almacenados en la base de datos)
    private ArrayList<Integer> competitionsRegistered; //Listado con las competiciones en las que el usuario esta registrado

    //------------------------------------------------------------>

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

    public void setCompetitionsRegistered(ArrayList<Integer> competitions) {
        this.competitionsRegistered = competitions;
    }

    public ArrayList<Integer> getCompetitionsRegistered(){
        return competitionsRegistered;
    }
}
