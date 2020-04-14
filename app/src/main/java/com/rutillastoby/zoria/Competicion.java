package com.rutillastoby.zoria;

public class Competicion {
    private String nombre;
    private String urlImage;
    private int identificador;
    private int pwd;

    public Competicion(String n, String u, int id, int p){
        nombre = n;
        urlImage = u;
        identificador = id;
        pwd=p;
    }

    public String getNombre(){
        return nombre;
    }

    public String getUrlImage(){

        return urlImage;
    }

    public int getIdentificador(){

        return identificador;
    }

    public int  getPwd(){

        return pwd;
    }
}
