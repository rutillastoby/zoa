package com.rutillastoby.zoria.dao;

/**
 * CLASE PARA CONFECCIONAR LISTADO DE DATOS PARA RELLENAR LAS FILAS DEL RANKING
 */
public class RankingUserData implements Comparable<RankingUserData>{
    private String imageProfile;
    private String nameProfile;
    private int points;
    private int level1P, level2P, level3P, level4P;
    private boolean flag;
    private boolean myUser;

    /**
     * CONSTRUCTOR POR DEFECTO
     */
    public RankingUserData(){
        level1P=0;
        level2P=0;
        level3P=0;
        level4P=0;
        points=0;
        flag=false;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA UTILIZAR EL SORT Y ORDENAR EL ARRAYLIST POR PUNTUACION TOTAL
     * @param o
     * @return
     */
    public int compareTo(RankingUserData o) {
        if (points < o.points) {
            return 1;
        }
        if (points > o.points) {
            return -1;
        }
        return 0;
    }

    //----------------------------------------------------------------------------------------------

    public String getImageProfile() {
        return imageProfile;
    }

    public void setImageProfile(String imageProfile) {
        this.imageProfile = imageProfile;
    }

    public String getNameProfile() {
        return nameProfile;
    }

    public void setNameProfile(String nameProfile) {
        this.nameProfile = nameProfile;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getLevel1P() {
        return level1P;
    }

    public void setLevel1P(int level1P) {
        this.level1P = level1P;
    }

    public int getLevel2P() {
        return level2P;
    }

    public void setLevel2P(int level2P) {
        this.level2P = level2P;
    }

    public int getLevel3P() {
        return level3P;
    }

    public void setLevel3P(int level3P) {
        this.level3P = level3P;
    }

    public int getLevel4P() {
        return level4P;
    }

    public void setLevel4P(int level4P) {
        this.level4P = level4P;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean isMyUser() {
        return myUser;
    }

    public void setMyUser(boolean myUser) {
        this.myUser = myUser;
    }
}