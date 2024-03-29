package com.rutillastoby.zoria;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
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
                    return("Tu nombre debe tener entre 5 y 12 caracteres");
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
        Drawable vectorDrawable = context.getResources().getDrawable(vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER UNA IMAGEN DE PERFIL CON BORDES REDONDEADOS EN LA VISTA A PARTIR DE UNA URL
     * @param context
     * @param url Direccion de la imagen a cargar
     * @param imageView Elemento sobre el que cargar la imagen
     */
    public static void chargeImageRound(final Context context, String url, final ImageView imageView){
        Picasso.get().load(url).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Bitmap source = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                RoundedBitmapDrawable drawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), source);
                drawable.setCircular(true);
                drawable.setCornerRadius(Math.max(source.getWidth() / 2.0f, source.getHeight() / 2.0f));
                imageView.setImageDrawable(drawable);
            }
            @Override public void onError(Exception e) {}
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ABRIR LA PAGINA DE LA APLICACION DE GOOGLE PLAY
     * @param context
     */
    public static void openPlayStore(Context context){
        //Abrir google play para valorar app
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA REPRODUCIR UN SONIDO OBTENIENDO EL VOLUMEN DE TONO DE LLAMADA NO DE MULTIMEDIA
     */
    public static void playSound(Context context, int resource){
        try {
            MediaPlayer player = new MediaPlayer();
            Resources res = context.getResources();
            AssetFileDescriptor afd = res.openRawResourceFd(resource);
            player.reset();
            player.setAudioStreamType(AudioManager.STREAM_RING);
            //Establecer el recurso pasado por parametro
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //----------------------------------------------------------------------------------------------

    /**
     * Calcular la distancia en metros entre 2 ubicaciones
     * @param first
     * @param second
     * @return
     */
    public static float distanceBetween(Location first, Location second) {
        return first.distanceTo(second);
    }
}
