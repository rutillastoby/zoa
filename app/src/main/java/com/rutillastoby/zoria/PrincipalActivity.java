package com.rutillastoby.zoria;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

public class PrincipalActivity extends AppCompatActivity {
    private static final int CODIGO_RESPUESTA = 455;

    private int identificadorCompe; //Identificador de la competicion
    private Button bClasificacion;

    private ArrayList<String> puntosAnadidos;
    private ArrayList<Puntoaz> listPuntosAZ;


    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase baseDatos;

    private Date fechInicio, fechFin;
    private Context context;
    private CountDownTimer contador, contadorIni;
    private TextView tvContador, tvContadorIni;
    private boolean completado=false;
    private long tiempoRestante=0;

    private ProgressBar pbCangandoPrincipal;
    private LinearLayout lyTiempo, lyBotones, lyPuntos,lyPuntosInf;
    private ConstraintLayout lyIni, lyFin;

    private MediaPlayer mpRegistrado;
    private Vibrator v;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        context=this;
        tvContador = findViewById(R.id.tvTiempo);
        tvContadorIni = findViewById(R.id.tvContadorIni);
        bClasificacion =findViewById(R.id.bClasificacion);
        bClasificacion.setEnabled(true);
        pbCangandoPrincipal = (ProgressBar) findViewById(R.id.pbPrincipal);
        lyBotones = (LinearLayout) findViewById(R.id.lyBotones);
        lyPuntosInf = (LinearLayout) findViewById(R.id.lyPuntosInf);
        lyPuntos = (LinearLayout) findViewById(R.id.lyPuntos);
        lyTiempo = (LinearLayout) findViewById(R.id.lyTiempo);
        lyFin = (ConstraintLayout) findViewById(R.id.lyFin);
        lyIni = (ConstraintLayout) findViewById(R.id.lyIni);

        pbCangandoPrincipal.setVisibility(View.VISIBLE);
        lyBotones.setVisibility(View.GONE);
        lyPuntos.setVisibility(View.GONE);
        lyPuntosInf.setVisibility(View.GONE);
        lyTiempo.setVisibility(View.GONE);
        lyFin.setVisibility(View.INVISIBLE);
        lyIni.setVisibility(View.INVISIBLE);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mpRegistrado=MediaPlayer.create(this, R.raw.login);

        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        baseDatos = FirebaseDatabase.getInstance();
        //Obtener el identificador de la competicion
        identificadorCompe = getIntent().getIntExtra("id", -1);

        setTitle(getIntent().getStringExtra("nombre"));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        obtenerTodosPuntos();
        obtenerPuntuaciones();
        obtenerHora();

    }

    public void obtenerHora(){
        baseDatos.getReference("competiciones/"+identificadorCompe+"/hora").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot hora : dataSnapshot.getChildren()) {
                    //Obtener horas
                    if(hora.getKey().equals("inicio")){

                            fechInicio = new Date();
                            fechInicio.setTime(Long.parseLong(hora.getValue().toString()));

                    }else if (hora.getKey().equals("fin")){
                        fechFin = new Date();
                        fechFin.setTime(Long.parseLong(hora.getValue().toString()));
                    }
                }

                //MOSTRAR EL CONTENIDO
                pbCangandoPrincipal.setVisibility(View.GONE);
                lyBotones.setVisibility(View.VISIBLE);
                lyPuntos.setVisibility(View.VISIBLE);
                lyPuntosInf.setVisibility(View.VISIBLE);
                lyTiempo.setVisibility(View.VISIBLE);
                lyFin.setVisibility(View.INVISIBLE);
                lyIni.setVisibility(View.INVISIBLE);
                //En funcion de las horas actuar
                actuarHoras();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * METODO PARA REALIZAR LAS ACCIONES SEGUN LA HORA ACTUAL, LA DE INICIO Y FIN
     */
    public void actuarHoras(){
        //AUN NO HA COMENZADO LA COMPETICION
        if(TrueTime.now().before(fechInicio)){
            lyFin.setVisibility(View.INVISIBLE);
            lyIni.setVisibility(View.VISIBLE);
            lyBotones.setVisibility(View.GONE);
            pbCangandoPrincipal.setVisibility(View.GONE);

            //Calcular el tiempo restante
            tiempoRestante = fechInicio.getTime() - TrueTime.now().getTime();

            //Comenzar contador de cuenta atras
            if (contadorIni!=null)
                contadorIni.cancel();
            contadorIni = new CountDownTimer(tiempoRestante,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tiempoRestante = millisUntilFinished;
                    actualizarContadorIni();
                }

                @Override
                public void onFinish() {
                    //Al acabar llamarse de forma recursiva para ocultar la pantalla principal
                    actuarHoras();
                    //finishAffinity();
                }
            }.start();


            //LA COMPETICION HA TERMINADO
        }else if (TrueTime.now().after(fechFin)){
            lyFin.setVisibility(View.VISIBLE);
            lyIni.setVisibility(View.INVISIBLE);
            lyBotones.setVisibility(View.GONE);
            pbCangandoPrincipal.setVisibility(View.GONE);
            //Detener contador
            if (contador!=null)
                contador.cancel();

            //LA COMPETICION SE ESTA EJECUTANDO
        }else{
            lyFin.setVisibility(View.INVISIBLE);
            lyIni.setVisibility(View.INVISIBLE);
            lyBotones.setVisibility(View.VISIBLE);

            //Calcular el tiempo restante
            tiempoRestante = fechFin.getTime() - TrueTime.now().getTime();

            /*//Bloquear Clasificacion media hora antes
            if(tiempoRestante<= 1800000){
                bClasificacion.setEnabled(false);
            }*/

            //Comenzar contador de cuenta atras
            if (contador!=null)
                contador.cancel();
            contador = new CountDownTimer(tiempoRestante,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tiempoRestante = millisUntilFinished;
                    actualizarContador();
                    if(tiempoRestante<= 1800000){
                        bClasificacion.setEnabled(false);
                    }
                }

                @Override
                public void onFinish() {
                    //Al acabar llamarse de forma recursiva para ocultar la pantalla principal
                    actuarHoras();
                    //finishAffinity();
                }
            }.start();
        }
    }

    /**
     * METODO PARA ACTUALIZAR EL MARCADOR DE TIEMPO, EL TEXTO
     */
    public void actualizarContador(){
        int seconds = (int) (tiempoRestante / 1000) % 60 ;
        int minutes = (int) ((tiempoRestante / (1000*60)) % 60);
        int hours   = (int) ((tiempoRestante / (1000*60*60)) % 24);
        tvContador.setText(String.format("%02d", hours)+":"+String.format("%02d", minutes)+":"+String.format("%02d", seconds));
    }

    public void actualizarContadorIni(){
        int seconds = (int) (tiempoRestante / 1000) % 60 ;
        int minutes = (int) ((tiempoRestante / (1000*60)) % 60);
        int hours   = (int) ((tiempoRestante / (1000*60*60)) % 24);
        tvContadorIni.setText(String.format("%02d", hours)+":"+String.format("%02d", minutes)+":"+String.format("%02d", seconds));
    }




    public void obtenerPuntuaciones(){

        baseDatos.getReference("usuarios/" + user.getUid()+"/competiciones/"+identificadorCompe+"/puntos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Almacenarlos puntos registrados
                int contadorPuntos = 0, contNivel1=0, contNivel2=0, contNivel3=0;

                //Si tenemos registrado el ese punto lo eliminamos del listado para no mostrarlo
                for (DataSnapshot punto : dataSnapshot.getChildren()) {
                    switch (Integer.parseInt(punto.getValue().toString())){
                        case 1:
                            contNivel1++;
                            break;
                        case 2:
                            contNivel2++;
                            break;
                        case 3:
                            contNivel3++;
                            break;
                    }
                    //Sumar los puntos en funcion del nivel
                    contadorPuntos+=Integer.parseInt(punto.getValue().toString());
                }

                //Establecer los puntos en los marcadores
                TextView tvPuntuacion = findViewById(R.id.tvPuntos);
                tvPuntuacion.setText(contadorPuntos+"");
                TextView tvPuntosNivel1 = findViewById(R.id.tvPuntosN1);
                tvPuntosNivel1.setText("x "+contNivel1);

                TextView tvPuntosNivel2 = findViewById(R.id.tvPuntosN2);
                tvPuntosNivel2.setText("x "+contNivel2);

                TextView tvPuntosNivel3 = findViewById(R.id.tvPuntosN3);
                tvPuntosNivel3.setText("x "+contNivel3);
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * METODO PARA ABRIR EL ACTIVITY DE MAPA AL PULSAR EL BOTON
     */
    public void abrirMapa(View view){
        Intent i = new Intent(this, MapaActivity.class);
        i.putExtra("id", getIntent().getIntExtra("id", -1));
        i.putExtra("fechaFin", fechFin.getTime());
        startActivity(i);
    }

    public void abrirClasificacion(View view){
        Intent i = new Intent(context, ClasificacionActivity.class);
        i.putExtra("id", identificadorCompe);
        context.startActivity(i);
    }

    //----------------------------------------------------------------------------------------------

    public void leerCodigo(View view){
        Intent i = new Intent(this, ScanerActivity.class);
        i.putExtra("registrados", puntosAnadidos);
        i.putExtra("fechaFin", fechFin.getTime());
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
        if(reqCode== CODIGO_RESPUESTA && resCode==RESULT_OK){
            final TextView tvPuntuacion = findViewById(R.id.tvPuntos);

            //Establecer codigo en el cuadro de texto
            if(datos.getBooleanExtra("yaLeido", true)){
                //CODIGO YA LEIDO
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
                String punto = datos.getStringExtra("codigo");
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
                    tvPuntuacion.setVisibility(View.INVISIBLE);
                    completado=false;
                    LayoutInflater inflater = PrincipalActivity.this.getLayoutInflater();
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
                                tvPuntuacion.setVisibility(View.VISIBLE);
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


                    AlertDialog dialogoLeido = new AlertDialog.Builder(this)
                            .setView(R.layout.dialogo_codigo_no_existe) //Layout personalizado
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

                }
            }
        }
        super.onActivityResult(reqCode, resCode, datos);
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


            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}