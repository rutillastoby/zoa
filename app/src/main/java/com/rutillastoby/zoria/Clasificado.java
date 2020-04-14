package com.rutillastoby.zoria;

public class Clasificado {
    private String nombre;
    private String fotoPerfil;
    private int puntosN1,puntosN2,puntosN3;

    public Clasificado(){
    };

    public Clasificado(String n, String f, int n1, int n2, int n3){
        nombre=n;
        fotoPerfil=f;
        puntosN1=n1;
        puntosN2=n2;
        puntosN3=n3;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public int getPuntosN1() {
        return puntosN1;
    }

    public int getPuntosN2() {
        return puntosN2;
    }

    public int getPuntosN3() {
        return puntosN3;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public void setPuntosN1(int puntosN1) {
        this.puntosN1 = puntosN1;
    }

    public void setPuntosN2(int puntosN2) {
        this.puntosN2 = puntosN2;
    }

    public void setPuntosN3(int puntosN3) {
        this.puntosN3 = puntosN3;
    }

    public int getTotal(){
        return puntosN1+(puntosN2*2)+(puntosN3*3);
    }
}
