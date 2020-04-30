package com.rutillastoby.zoria.dao.competicion;

import java.util.ArrayList;

public class Pregunta {
    private String id;
    private String texto;
    private int solu;
    private int idPunto;
    private ArrayList<String> resp;
    //Variable para indicar si se tiene que mostrar las opciones de respuesta en la vista del elemento
    private boolean viewRespOpen;
    //Variable para almacenar la respuesta marcada
    private int respSelect;
    //Variable indicando la respuesta enviada a la pregunta si ha sido contestado
    private int responseSend; //0-Para preguntas sin contestar

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
        if(resp==null)
            new ArrayList<String>();
        return resp;
    }

    public void setResp(ArrayList<String> resp) {
        this.resp = resp;
    }

    public boolean isViewRespOpen() {
        return viewRespOpen;
    }

    public void setViewRespOpen(boolean viewRespOpen) {
        this.viewRespOpen = viewRespOpen;
    }

    public int getRespSelect() {
        return respSelect;
    }

    public void setRespSelect(int respSelect) {
        this.respSelect = respSelect;
    }

    public int getResponseSend() {
        return responseSend;
    }

    public void setResponseSend(int responseSend) {
        this.responseSend = responseSend;
    }
}
