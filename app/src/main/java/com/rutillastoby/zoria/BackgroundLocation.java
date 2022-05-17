package com.rutillastoby.zoria;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;

public class BackgroundLocation extends Service {

    //Const ========================================================================================
    private static final int LOCATION_INTERVAL = 4000;
    private static final float LOCATION_DISTANCE = 3f;

    //Variables ====================================================================================
    private LocationManager locationManager = null;
    private LocationListener locationListener = new LocationListener();

    //----------------------------------------------------------------------------------------------

    private class LocationListener implements android.location.LocationListener {
        Location lastLocation = null;

        @Override
        public void onLocationChanged(Location location) {

            //Obtener la frecuencia con la que se actualiza la ubicacion, por defecto 100 metros
            float locationFrequencyDistance = 100f;
            if(GeneralActivity.singleton != null){
                locationFrequencyDistance = GeneralActivity.singleton.getActiveCompetition().getUbiFreq();
            }

            //Inicializacion
            if(lastLocation == null){
                lastLocation = location;
            }

            //Actuar si se alcanza la distancia de frecuencia de actualizacion
            if(GenericFuntions.distanceBetween(location, lastLocation) > locationFrequencyDistance) {
                if(GeneralActivity.singleton != null) {
                    GeneralActivity.singleton.sendLocation(location);
                }
                lastLocation = location;
                Log.d("aba", "Alcanzada la distancia de actualizacion");
            }
        }


        @Override
        public void onProviderDisabled(String provider) {
            Log.d("aba", "onProviderDisabled: " + provider);
            //Cambiar estado de los elementos del fragmento del mapa
            GeneralActivity.singleton.getMapF().setStatusLocationProvider(false);
        }


        @Override
        public void onProviderEnabled(String provider) {
            Log.d("aba", "onProviderEnabled: " + provider);
            //Cambiar estado de los elementos del fragmento de mapa
            GeneralActivity.singleton.getMapF().setStatusLocationProvider(true);
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "zoa_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "zoa channel 01", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.getLocation))
                    .setSmallIcon(R.drawable.ic_favicon).build();

            startForeground(1, notification);
        }

        //Crear el location manager
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        //Comprobar permisos de localizacion
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Actuar si estan otorgados
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener);
            } catch (java.lang.SecurityException ex) {
                Log.d("aba", "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d("aba", "network provider does not exist, " + ex.getMessage());
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public IBinder onBind(Intent arg0) { return null; }

    //----------------------------------------------------------------------------------------------

    /**
     * Ejecucion al destruir o detener el servicio
     */
    @Override
    public void onDestroy() {
        //Desactivar la deteccion de actualizacion de gps
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

        super.onDestroy();
    }
}