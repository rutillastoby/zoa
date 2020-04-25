package com.rutillastoby.zoria.dao.competicion;

import java.util.ArrayList;

public class Pregunta {
    private String id;
    private String texto;
    private int solu;
    private int idPunto;
    private ArrayList<String> resp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getSolu() {
        return solu;
    }

    public void setSolu(int solu) {
        this.solu = solu;
    }

    public int getIdPunto() {
        return idPunto;
    }

    public void setIdPunto(int idPunto) {
        this.idPunto = idPunto;
    }

    public ArrayList<String> getResp() {
        return resp;
    }

    public void setResp(ArrayList<String> resp) {
        this.resp = resp;
    }
}
