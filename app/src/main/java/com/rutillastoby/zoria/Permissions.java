package com.rutillastoby.zoria;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class Permissions {
    public static final int PERMISSION_ALL = 333;

    //----------------------------------------------------------------------------------------------

    /**
     * Obtener el listado de permisos
     * @return
     */
    public static String[] getPermissions(){
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        //A partir de la version 29 solicitar permiso explicito
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

        return permissions.toArray(new String[0]);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA COMPROBAR SI SE TIENEN PERMISOS ACTIVADOS PASANDO EL LISTADO DE LOS MISMOS
     * @param context
     * @return
     */
    public static boolean hasPermissions(Context context) {
        //Comprobar si se tienen los permisos adecuados
        if (context != null) {
            for (String permission : getPermissions()) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }
}