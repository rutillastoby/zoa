package com.rutillastoby.zoria;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rutillastoby.zoria.dao.competicion.Punto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    //Referencias
    private FloatingActionButton fabScanner, fabCurrentPosition;
    private ImageView ivBackMap;
    private GeneralActivity ga;
    private ConstraintLayout lyWarningGPS;

    //Variables
    LocationListener locationListener;
    Location currentLocation;
    SupportMapFragment mMapFragment;
    GoogleMap map;
    ArrayList<Marker> instanciatedMarker = new ArrayList<Marker>(); //Variable para almacenar los puntos instanciados en mapa

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //Inicializar el mapa
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        mMapFragment.getMapAsync(this);

        //Inicializar variables
        initVar(view);

        return view;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View v) {
        View view = v;
        //Referencias
        ga = ((GeneralActivity) getActivity());
        fabScanner = view.findViewById(R.id.fabScanner);
        fabCurrentPosition = view.findViewById(R.id.fabCurrentPosition);
        ivBackMap = view.findViewById(R.id.ivBackMap);
        lyWarningGPS = view.findViewById(R.id.lyWarningGPS);

        //Escuchadores de clicks
        fabScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Mostrar fragmento de escanner
                ga.showScannerFragment();
            }
        });

        ivBackMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Volver a la vista de la competicion
                ga.showPrincActivityNotChange();
            }
        });

        fabCurrentPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Llamada al metodo para mover vista hasta mi ubicacion
                moveViewMap();
            }
        });

        //Escuchador de ubicacion
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        //Clase declarada al vuelo que actuará ante los cambios de ubicacion
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                ga.sendLocation(location);
            }

            @Override
            public void onProviderDisabled(String provider) {
                currentLocation = null;
                ga.sendLocation(null);
                //Mostrar mensaje de gps desactivado
                lyWarningGPS.setVisibility(View.VISIBLE);
                fabCurrentPosition.hide();
            }

            @Override
            public void onProviderEnabled(String provider) {
                //Ocultar mensaje de gps desactivado
                lyWarningGPS.setVisibility(View.GONE);
                fabCurrentPosition.show(); //Mostrar boton de ir a mi ubicacion
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        //Comprobar permisos de localizacion
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Suscribir el escuchador previamente instanciado para que actualice posicion cada 3 segundos y 20 metros
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 2, locationListener);

        //Si la ubicacion está desactivada mostramos mensaje visual
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lyWarningGPS.setVisibility(View.VISIBLE);
            fabCurrentPosition.hide(); //Ocultar boton de ir a mi ubicacion
        }else{
            lyWarningGPS.setVisibility(View.GONE);
        }
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        //Si no se tiene la ubicacion activada posicionar en lugar por defecto, si no en su ubicacion
        if(!moveViewMap()){
            //Posicion manual
            LatLng latLng = new LatLng(37.3996770, -2.4282695781);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
        }

        //Ocultar botones de la interfaz del mapa
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);

        //El padding hace que la brujula que aparece en la interfaz del mapa al
        // rotarlo se desplace y no se coloque con la flecha de volver
        map.setPadding(0, 100, 0, 0);

        //Mapa de tipo terreno
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        //Mostrar mi ubicación en el mapa
        map.setMyLocationEnabled(true);

        //Al cargar el mapa inicialmente llamar al metodo para cargar los puntos
        ga.initLoadPointsMap();
    }
    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR EN EL MAPA AQUELLOS PUNTOS QUE NO TENGAMOS REGISTRADOS
     * @param allPoints
     * @param myPoints
     */
    public void loadPoints(HashMap<String, Punto> allPoints, HashMap<String, String> myPoints) {
        //Resetear todos los puntos agregados anteriormente
        for (int i = 0; i < instanciatedMarker.size(); i++) {
            instanciatedMarker.get(i).remove();
        }

        //Recorrer el listado completo de preguntas y comprobar cuales de ellas estan disponibles para el usuario
        for (Map.Entry<String, Punto> point : allPoints.entrySet()) {
            boolean show = true;
            for (Map.Entry<String, String> mPoint : myPoints.entrySet()) {
                //Si el punto en cuestion lo tenemos en posesion no lo mostraremos en el mapa
                if (point.getKey().equals(mPoint.getKey())) {
                    show = false;
                    //break;
                }
            }

            //Si no tenemos el punto escaneado, lo agregamos al mapa
            if (show) {
                //Generar la ubicacion del punto
                LatLng locationPoint = new LatLng(point.getValue().getLat(), point.getValue().getLon());
                BitmapDescriptor icon = null;

                //Seleccionar icono del punto
                switch (point.getValue().getNivel()) {
                    case 1:
                        icon = GenericFuntions.bitmapDescriptorFromVector(getContext(), R.drawable.ic_level1);
                        break;
                    case 2:
                        icon = GenericFuntions.bitmapDescriptorFromVector(getContext(), R.drawable.ic_level2);
                        break;
                    case 3:
                        icon = GenericFuntions.bitmapDescriptorFromVector(getContext(), R.drawable.ic_level3);
                        break;
                    case 4:
                        icon = GenericFuntions.bitmapDescriptorFromVector(getContext(), R.drawable.ic_qp);
                        break;
                    case 5:
                        icon = GenericFuntions.bitmapDescriptorFromVector(getContext(), R.drawable.ic_flag);
                }

                //Anadir marcador al mapa
                if (map != null) {
                    instanciatedMarker.add(
                        map.addMarker(new MarkerOptions()
                            .position(locationPoint)
                            .title(point.getValue().getNombre())
                            .icon(icon)
                            .anchor(0.5f, 0.5f))
                    );
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOVER LA VISTA DEL MAPA HASTA LA UBICACION DEL USUARIO, DEVUELVE TRUE O FALSE SEGUN
     * SI HA PODIDO MOVER LA CAMARA
     */
    public boolean moveViewMap() {
        //Proceso para obtener la ultima ubicación activa de gps
        LocationManager mLocationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(getContext().LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) { continue; }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) { bestLocation = l; }
        }

        //Si la ubicación se ha podido obtener movemos la vista del mapa
        if (bestLocation!=null) {
            LatLng latLng = new LatLng(bestLocation.getLatitude(), bestLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
            map.animateCamera(cameraUpdate);
            return true;
        }else{
            return false;
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR UN DIALOGO CON LA INFORMACIÓN DEL PUNTO ESCANEADO
     */
    public void alertDialogScannedPoint(String name, int level, boolean duplicate) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialog = inflater.inflate(R.layout.dialog_escaned_point, null);
        ConstraintLayout lyNormalPointEscaned = dialog.findViewById(R.id.lyNormalPointEscaned);
        ConstraintLayout lyQuestPointEscaned = dialog.findViewById(R.id.lyQuestPointEscaned);
        ConstraintLayout lyDuplicateEscaned = dialog.findViewById(R.id.lyDuplicateEscaned);
        ConstraintLayout lyFlagEscaned = dialog.findViewById(R.id.lyFlagEscaned);
        lyQuestPointEscaned.setVisibility(View.GONE);
        lyNormalPointEscaned.setVisibility(View.GONE);
        lyDuplicateEscaned.setVisibility(View.GONE);
        lyFlagEscaned.setVisibility(View.GONE);
        AlertDialog.Builder builder = null;

        //Comprobar si es un codigo de pregunta, normal o es un escaneo duplicado (Ya escaneado previamente)
        if (duplicate) {
            //CODIGO YA ESCANEADO
            lyDuplicateEscaned.setVisibility(View.VISIBLE);
            builder = new AlertDialog.Builder(getContext());

            builder.setView(dialog)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

        } else if (level == 4) {
            //CODIGO DE PREGUNTA
            lyQuestPointEscaned.setVisibility(View.VISIBLE);
            builder = new AlertDialog.Builder(getContext());

            builder.setView(dialog)
                    .setPositiveButton("Ver pregunta", new DialogInterface.OnClickListener() {
                        //Al hacer clic en ver pregunta
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ga.showQuestionsFragment();
                        }
                    }).setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

        } else if (level == 5){
            //CODIGO BANDERA
            lyFlagEscaned.setVisibility(View.VISIBLE);
            builder = new AlertDialog.Builder(getContext());

            builder.setView(dialog)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    //Marcar la competicion como finalizada para mi unicamente
                    ga.sendGetFlag();
                    ga.checkFragmentCurrent(); //Volver a la vista de competicion
                    }
                });

        }else {
            //CODIGO NORMAL
            TextView tvPoints = dialog.findViewById(R.id.tvDialogPoints);
            TextView tvNamePoint = dialog.findViewById(R.id.tvDialogNamePoint);
            ImageView ivIconPointDialog = dialog.findViewById(R.id.ivIconPointDialog);
            builder = new AlertDialog.Builder(getContext());

            //Mostrar layout correspondiente
            lyNormalPointEscaned.setVisibility(View.VISIBLE);

            //Establecer datos a los elementos del dialogo
            tvNamePoint.setText("CÓDIGO " + name.toUpperCase() + " REGISTRADO!");
            tvPoints.setText("+ " + level + ((level == 1) ? " punto." : " puntos."));
            switch (level){
                case 1: ivIconPointDialog.setImageResource(R.drawable.ic_level1); break;
                case 2: ivIconPointDialog.setImageResource(R.drawable.ic_level2); break;
                case 3: ivIconPointDialog.setImageResource(R.drawable.ic_level3); break;
            }

            //Mostrar dialogo con información del punto escaneado
            builder.setView(dialog)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {}
                });
        }

        //Mostrar ventana
        builder.show();
    }

}