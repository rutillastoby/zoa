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

public class BackgroundLocation extends Service {

    //Const ========================================================================================
    private static final int LOCATION_INTERVAL = 3000;
    private static final float LOCATION_DISTANCE = 1f;

    //Variables ====================================================================================
    private LocationManager locationManager = null;
    private LocationListener locationListener = new LocationListener();

    //----------------------------------------------------------------------------------------------

    private class LocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("aba", "onLocationChanged: " + location);
            GeneralActivity.singleton.sendLocation(location);
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
        Log.d("aba", "hola");
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
        Log.e("aba", "onDestroy");
        //Desactivar la deteccion de actualizacion de gps
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

        super.onDestroy();
    }
}