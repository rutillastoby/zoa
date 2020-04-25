package com.rutillastoby.zoria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    //Referencias
    private FloatingActionButton fabScanner;
    private ImageView ivBackMap;
    private GeneralActivity ga;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //Inicializar el mapa
        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        mMapFragment.getMapAsync(this);

        //Inicializar variables
        initVar(view);

        return view;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View v){
        View view = v;
        //Referencias
        ga = ((GeneralActivity)getActivity());
        fabScanner = view.findViewById(R.id.fabScanner);
        ivBackMap = view.findViewById(R.id.ivBackMap);

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
                ga.showPrincActivityNotChange();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Posicionar el mapa en una localización y con un nivel de zoom
        LatLng latLng = new LatLng(36.679582, -5.444791);
        // Un zoom mayor que 13 hace que el emulador falle, pero un valor deseado para
        // callejero es 17 aprox.
        float zoom = 13;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        // Colocar un marcador en la misma posición
        map.addMarker(new MarkerOptions().position(latLng));
        // Más opciones para el marcador en:
        // https://developers.google.com/maps/documentation/android/marker

        // Otras configuraciones pueden realizarse a través de UiSettings
        // UiSettings settings = getMap().getUiSettings();



        //Mapa de tipo terreno
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

    }


}
