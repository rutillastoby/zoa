package com.rutillastoby.zoria;

import android.graphics.Color;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class GenericFuntions {



    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA COMPROBAR SI UN NOMBRE DE USUARIO ES VALIDO Y ESTA DISPONIBLE
     * @param nick
     * @return
     */
    public static String checkNick(String nick, ArrayList nombresUsados){

        boolean disponible = true;
        for(int i=0; i<nombresUsados.size();i++){
            if(nick.equals(nombresUsados.get(i))){
                disponible=false;
            }
        }

        if(disponible){
            if(nick.length()>0){
                if(nick.length()>=5 && nick.length()<=15){
                  return "true";
                }else{
                    return("Tu nombre debe tener entre 5 Y 15 caracteres");
                }
            }else{
               return("Debes introducir un nombre.");
            }
        }else{
            return("Ese nombre ya esta registrado.");
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR UN ERROR MEDIANTE UN SNACKBAR DE COLOR
     * @param view
     * @param text
     */
    public static void errorSnack(View view, String text){
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(view.getResources().getColor(R.color.error));
        snackbar.show();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR UN MENSAJE MEDIANTE UN SNACKBAR
     * @param view
     * @param text
     */
    public static void snack(View view, String text){
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(Color.BLACK);
        snackbar.show();
    }
}
