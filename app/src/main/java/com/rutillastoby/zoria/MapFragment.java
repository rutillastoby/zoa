package com.rutillastoby.zoria;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

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
    View locationButton;
    GoogleMap map;

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

        //Estado inicial
        lyWarningGPS.setVisibility(View.GONE);

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
                Log.d("aaa", "ubicacion change" + location);
            }

            @Override
            public void onProviderDisabled(String provider) {
                currentLocation = null;
                //Mostrar mensaje de gps desactivado
                lyWarningGPS.setVisibility(View.VISIBLE);
                fabCurrentPosition.hide(); //Ocultar boton de ir a mi ubicacion
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 20, locationListener);

        //Si la ubicacion está desactivada mostramos mensaje visual
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lyWarningGPS.setVisibility(View.VISIBLE);
            fabCurrentPosition.hide(); //Ocultar boton de ir a mi ubicacion
        }
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        // Posicionar el mapa en una localización y con un nivel de zoom
        LatLng latLng = new LatLng(36.679582, -5.444791);
        // Un zoom mayor que 13 hace que el emulador falle, pero un valor deseado para
        // callejero es 17 aprox.
        float zoom = 13;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        // Colocar un marcador en la misma posición
        map.addMarker(new MarkerOptions().position(latLng));


        //Boton de ir a mi ubicacion en el mapa
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);


        //El padding hace que la brujula que aparece en la interfaz del mapa al
        // rotarlo se desplace y no se coloque con la flecha de volver
        map.setPadding(0, 100, 0, 0);


        //Mapa de tipo terreno
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //Mostrar mi ubicación en el mapa
        map.setMyLocationEnabled(true);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOVER LA VISTA DEL MAPA HASTA LA UBICACION DEL USUARIO
     */
    public void moveViewMap() {
        //Proceso para obtener la ultima ubicación activa de gps
        LocationManager mLocationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(getContext().LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
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
        }
    }

}