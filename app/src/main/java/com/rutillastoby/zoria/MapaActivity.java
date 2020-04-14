package com.rutillastoby.zoria;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.instacart.library.truetime.TrueTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int CODIGO_RESPUESTA = 455;
    private int identificadorCompe; //Identificador de la competicion
    private ArrayList<Puntoaz> listPuntosAZ = null;
    private ArrayList<String> puntosAnadidos = null;
    private ArrayList<Marker> listMarcadores = null;
    private int segundosActualizar = 3, contadorSegundos=0;
    private boolean listPuntos = false, PuntosAnadidos = false;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase baseDatos;
    private CountDownTimer contador=null;
    private Context context;
    private boolean completado;
    private GoogleMap mMap;
    private long fechaFinMill;
    private MediaPlayer mpRegistrado;
    private Vibrator v;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context=this;
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mpRegistrado=MediaPlayer.create(this, R.raw.login);

        //Empezar cuenta atras
        fechaFinMill = getIntent().getLongExtra("fechaFin",0 );
        tiempo(fechaFinMill);

        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        baseDatos = FirebaseDatabase.getInstance();
        //Obtener el identificador de la competicion
        identificadorCompe = getIntent().getIntExtra("id", -1);
        listMarcadores = new ArrayList<Marker>();
        obtenerTodosPuntos();
    }

    @Override
    protected void onPause() {
        if(contador!=null)
            contador.cancel();

        baseDatos.getReference("usuarios/"+user.getUid()+"/ubicacion/lat").setValue("null");
        baseDatos.getReference("usuarios/"+user.getUid()+"/ubicacion/lon").setValue("null");
        super.onPause();
    }



    public void tiempo(long fecha){
        Date fechFin = new Date();
        fechFin.setTime(fecha);
        //Calcular el tiempo restante
        long tiempoRestante = fechFin.getTime() - TrueTime.now().getTime();


        //Comenzar contador de cuenta atras
         contador = new CountDownTimer(tiempoRestante,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Toast.makeText(context, ""+millisUntilFinished, Toast.LENGTH_SHORT).show();
                Location location = getLastKnownLocation();

                if(segundosActualizar==contadorSegundos){
                    contadorSegundos=0;
                    if (location!=null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        baseDatos.getReference("usuarios/"+user.getUid()+"/ubicacion/lat").setValue(latitude);
                        baseDatos.getReference("usuarios/"+user.getUid()+"/ubicacion/lon").setValue(longitude);
                    }
                }else{
                    contadorSegundos++;
                }
            }

            @Override
            public void onFinish() {
                //Al acabar llamarse de forma recursiva para ocultar la pantalla principal
                //actuarHoras();
                finish();
            }
        }.start();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        Location location = getLastKnownLocation();

        if (location!=null) {
            location.getTime();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

        }else{
            Toast.makeText(this, "Debes activar el GPS...", Toast.LENGTH_SHORT).show();
            LatLng inicial = new LatLng(37.351710, -2.440441);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(inicial));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(inicial, 10));
        }


        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setMyLocationEnabled(true);


    }

    /**
     * METODO PARA ACERCARSE A NUESTRA UBICACION
     * @param view
     */
    public void botonUbicacion(View view) {


/*        LocationManager locationManager = (LocationManager)
                getSystemService(this.LOCATION_SERVICE);
        Criteria criteria = new Criteria();*/



        Location location = getLastKnownLocation();

        if (location!=null) {
            //location.getTime();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
            mMap.animateCamera(cameraUpdate);
        }else{
           Toast.makeText(this, "Debes activar el GPS...", Toast.LENGTH_SHORT).show();
        }


    }

    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    //----------------------------------------------------------------------------------------------

    public void obtenerTodosPuntos() {
        // Poner a la escucha el nodo de competiciones
        baseDatos.getReference("competiciones/" + identificadorCompe+"/puntos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                listPuntosAZ = new ArrayList<Puntoaz>();
                double lat = 0, lon = 0;
                String nombrePunto = "";
                int nivel = -1;

                for (DataSnapshot punto : dataSnapshot.getChildren()) {
                    Log.d("oki", punto.getKey());
                    for(DataSnapshot dato : punto.getChildren()) {

                        switch (dato.getKey()) {
                            case "nombre":
                                nombrePunto = dato.getValue().toString();
                                break;
                            case "lat":
                                lat = Double.parseDouble(dato.getValue().toString());
                                Log.d("oki", "leido"+dato.getValue().toString());
                                break;
                            case "lon":
                                lon = Double.parseDouble(dato.getValue().toString());
                                Log.d("oki", "leidol"+dato.getValue().toString());
                                break;
                            case "nivel":
                                nivel = Integer.parseInt(dato.getValue().toString());


                                break;
                        }
                    }

                    //Crear el punto leido y agregarlo al array
                    Puntoaz pAZ = new Puntoaz(punto.getKey(), lat, lon, nombrePunto, nivel);
                    listPuntosAZ.add(pAZ);
                }
                //Poner a la escucha los puntos que ya tenemos registrados si es la primera ejecucion
                if(puntosAnadidos==null)
                    obtenerPuntosAnadidos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * OBTENER LOS PUNTOS QUE YA TENEMOS REGISTRADOS
     */
    public void obtenerPuntosAnadidos(){

        baseDatos.getReference("usuarios/" + user.getUid()+"/competiciones/"+identificadorCompe+"/puntos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Almacenarlos puntos registrados
                puntosAnadidos = new ArrayList<String>();

                //Si tenemos registrado el ese punto lo eliminamos del listado para no mostrarlo
                for (DataSnapshot punto : dataSnapshot.getChildren()) {
                    Log.d("oki", "oli");

                    for(int i =0; i<listPuntosAZ.size();i++  ){
                        if(listPuntosAZ.get(i).getIdentificador().equals(punto.getKey())){
                            listPuntosAZ.remove(i);
                        }
                    }
                    puntosAnadidos.add(punto.getKey());
                }

                //Llamada al metodo para mostrar los puntos en el mapa
                mostrarPuntos();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR LOS PUNTOS QUE NO TENEMOS COGIDOS EN EL MAPA
     */
    public void mostrarPuntos(){
        //Eliminar Todos los puntos
        for(int i=0; i<listMarcadores.size();i++){
            listMarcadores.get(i).remove();
        }
        //Añadir los puntos restantes no registrados al mapa
        for (int i=0; i<listPuntosAZ.size(); i++){

            //Generar la ubicacion del punto
            LatLng ubicacion = new LatLng(listPuntosAZ.get(i).getLat(), listPuntosAZ.get(i).getLon());
            //En funcion del nivel obtener el icono
            int height = (int)dipToPixels(this, 18);
            int width = (int)dipToPixels(this, 18);
            BitmapDrawable imagen = null;
            Bitmap b=null, icono=null;
            Log.d("oki", ubicacion.latitude+" - "+ubicacion.longitude);


            switch (listPuntosAZ.get(i).getNivel()){
                case 1:
                    imagen=(BitmapDrawable)getResources().getDrawable(R.drawable.f1);
                    b=imagen.getBitmap();
                    icono = Bitmap.createScaledBitmap(b, width, height, false);
                    break;
                case 2:
                    imagen=(BitmapDrawable)getResources().getDrawable(R.drawable.f2);
                    b=imagen.getBitmap();
                    icono = Bitmap.createScaledBitmap(b, width, height, false);
                    break;
                case 3:
                    imagen=(BitmapDrawable)getResources().getDrawable(R.drawable.f3);
                    b=imagen.getBitmap();
                    icono = Bitmap.createScaledBitmap(b, width, height, false);
                    break;
            }

            //Anadir marcador al mapa
            Marker marcador = mMap.addMarker(new MarkerOptions().position(ubicacion)
                    .title(listPuntosAZ.get(i).getNombre()).icon(BitmapDescriptorFactory.fromBitmap(icono)).anchor(0.5f,0.5f));
            listMarcadores.add(marcador);
        }
    }


    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    //----------------------------------------------------------------------------------------------

    public void leerCodigo(View view){
        Intent i = new Intent(this, ScanerActivity.class);
        i.putExtra("registrados", puntosAnadidos);
        i.putExtra("fechaFin", fechaFinMill);

        startActivityForResult(i, CODIGO_RESPUESTA);

    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER EL CODIGO LEIDO CON EL LECTOR
     * @param reqCode
     * @param resCode
     * @param datos
     */
    public void onActivityResult(int reqCode, int resCode, Intent datos){
        tiempo(fechaFinMill);
        if(reqCode== CODIGO_RESPUESTA && resCode==RESULT_OK){
            //Establecer codigo en el cuadro de texto
            if(datos.getBooleanExtra("yaLeido", true)){
                //CODIGO YA LEIDO
                //Toast.makeText(this, "YA ESTA LEIDO", Toast.LENGTH_SHORT).show();
                AlertDialog dialogoLeido = new AlertDialog.Builder(this)
                        .setView(R.layout.dialogo_codigo_ya_registrado) //Layout personalizado
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setCancelable(false)
                        .show();

                /////////////VIBRAR
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //Duraciones vibraciones en milisegundos, fuerza de la vibracion para cada intervalo, repeticion en bucle
                    v.vibrate(VibrationEffect.createWaveform(new long[]{100,20,450},new int[]{30,0,200},-1 ));
                } else {
                    //Antiguas versiones
                    //deprecated in API 26
                    v.vibrate(500);
                }

            }else{
                //REGISTRAR CODIGO
                final String punto = datos.getStringExtra("codigo");
                int nivel=-1;
                String nombre="";

                for(Puntoaz p : listPuntosAZ){
                    if(p.getIdentificador().equals(punto)) {
                        Log.d("codigo", p.getIdentificador());
                        nivel=p.getNivel();
                        nombre =p.getNombre();
                    }
                }

                //Si no encuetra el nivel del codigo significa que no existe ese codigo en el listado
                // y por lo tanto se indica
                if(nivel!=-1) {
                    //CODIGO VALIDO

                completado=false;
                    LayoutInflater inflater = MapaActivity.this.getLayoutInflater();
                    final View mView = inflater.inflate(R.layout.dialogo_codigo_valido, null);

                    final AlertDialog dialogoLeido = new AlertDialog.Builder(context)
                            .setView(mView) //Layout personalizado
                     .setCancelable(false)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(completado==false){
                                finishAffinity();
                            }
                        }
                    }).show();

                    final Button button = dialogoLeido.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setVisibility(View.INVISIBLE);


                    //Guardar en nuestro nodo el codigo leido.
                    final int finalNivel = nivel;
                    final String finalNombre = nombre;
                    baseDatos.getReference("usuarios/" + user.getUid() + "/competiciones/" + identificadorCompe + "/puntos/" + punto).setValue(nivel, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                finishAffinity();
                            } else {
                                baseDatos.getReference("usuarios/" + user.getUid() + "/competiciones/" + identificadorCompe + "/log/"+ finalNombre).setValue(TrueTime.now().toString());

                                button.setVisibility(View.VISIBLE);
                                final ConstraintLayout lyValido = (ConstraintLayout) mView.findViewById(R.id.lyValido);
                                lyValido.setVisibility(View.VISIBLE);
                                final ConstraintLayout lyCargando = (ConstraintLayout) mView.findViewById(R.id.lyCargando);
                                lyCargando.setVisibility(View.INVISIBLE);



                                completado =true;
                                //Efectuar sonido
                                mpRegistrado.start();
                                /////////////VIBRAR
                                // Vibrate for 500 milliseconds
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    //Duraciones vibraciones en milisegundos, fuerza de la vibracion para cada intervalo, repeticion en bucle
                                    v.vibrate(VibrationEffect.createOneShot(200, 150));
                                } else {
                                    //Antiguas versiones
                                    //deprecated in API 26
                                    v.vibrate(500);
                                }


                                TextView tvTextoDCV = (TextView) mView.findViewById(R.id.tvNivelRegistrado);
                                if(finalNivel ==1) {
                                    tvTextoDCV.setText("+ " + finalNivel + " punto");
                                }else
                                    tvTextoDCV.setText("+ "+ finalNivel +" puntos");

                            }
                        }
                    });

                    CountDownTimer aa=new CountDownTimer(10000,1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Log.d("ERRORE", "EEE");
                        }

                        @Override
                        public void onFinish() {
                            Log.d("ERRORE", "EEE");
                            if(!completado){
                                final ConstraintLayout lyConect = (ConstraintLayout) mView.findViewById(R.id.lyConec);
                                lyConect.setVisibility(View.VISIBLE);
                                final ConstraintLayout lyCargando = (ConstraintLayout) mView.findViewById(R.id.lyCargando);
                                lyCargando.setVisibility(View.INVISIBLE);
                                button.setVisibility(View.VISIBLE);
                            }
                        }
                    }.start();
                    }else{
                    //Codigo no valido
                    AlertDialog dialogoLeido = new AlertDialog.Builder(this)
                            .setView(R.layout.dialogo_codigo_no_existe) //Layout personalizado
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setCancelable(false)
                            .show();
                    //Toast.makeText(this, "Ese no es un codigo valido", Toast.LENGTH_SHORT).show();

                    /////////////VIBRAR
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //Duraciones vibraciones en milisegundos, fuerza de la vibracion para cada intervalo, repeticion en bucle
                        v.vibrate(VibrationEffect.createWaveform(new long[]{100,20,450},new int[]{30,0,200},-1 ));
                    } else {
                        //Antiguas versiones
                        //deprecated in API 26
                        v.vibrate(500);
                    }
                }
            }
        }
        super.onActivityResult(reqCode, resCode, datos);
    }


}
