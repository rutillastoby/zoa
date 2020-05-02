package com.rutillastoby.zoria;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
                if(nick.length()>=5 && nick.length()<=12){
                  return "true";
                }else{
                    return("Tu nombre debe tener entre 5 Y 12 caracteres");
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
     * METODO PARA MOSTRAR UN ERROR MEDIANTE UN SNACKBAR DE COLOR + VIBRACION
     * @param view
     * @param text
     */
    public static void errorSnack(View view, String text, Context context){
        //Creación del snackbar
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(view.getResources().getColor(R.color.error));
        snackbar.show();

        //Vibrar
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Duraciones vibraciones en milisegundos, fuerza de la vibracion para cada intervalo, repeticion en bucle
            v.vibrate(VibrationEffect.createWaveform(new long[]{100,20,450},new int[]{30,0,200},-1 ));
        } else {
            //Antiguas versiones //deprecated in API 26
            v.vibrate(500);
        }
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

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CONVERTIR UN VECTOR EN BITMAPDESCRIPTOR NECESARIO PARA AGREGAR UN MARCADOR AL MAPA
     * @param context
     * @param vectorResId
     * @return
     */
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
