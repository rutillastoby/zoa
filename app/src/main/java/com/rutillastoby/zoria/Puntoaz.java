package com.rutillastoby.zoria;

public class Puntoaz {
    private double lat;
    private double lon;
    private String nombre;
    private int nivel;
    private String identificador;

    public Puntoaz(String id, double lat, double lon, String nom, int niv){
        identificador = id;
        this.lat = lat;
        this.lon=lon;
        nombre=nom;
        nivel=niv;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Double getLat(){
        return  lat;
    }

    public Double getLon(){
        return  lon;
    }

    public int getNivel(){
        return nivel;
    }

    public String getNombre(){
        return nombre;
    }
}
