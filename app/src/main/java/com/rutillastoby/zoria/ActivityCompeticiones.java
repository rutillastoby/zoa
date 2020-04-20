package com.rutillastoby.zoria;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
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

public class ActivityCompeticiones extends AppCompatActivity {

    // RecyclerView =============================================
    private RecyclerView rvCompeticiones;
    private RecyclerView.Adapter adaptador;
    private RecyclerView.LayoutManager layoutCompeticiones;

    private Vibrator v;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase baseDatos;
    private static Context context;
    private ValueEventListener escuchador1=null, escuchador2=null;
    private ProgressBar pbCompeticiones;

    private static ArrayList<Integer> competicionesConAcceso=new ArrayList<Integer>();
    private static ArrayList<Competicion> listCompeticiones =new ArrayList<Competicion>();

    @Override
    public void onBackPressed() {
        finishAffinity(); //Cerrar activity
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competiciones);
        context = this;
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        pbCompeticiones = findViewById(R.id.pbCompeticiones);

        //Iniciar variables del listado
        rvCompeticiones = (RecyclerView) findViewById(R.id.rvCompeticiones);
        rvCompeticiones.setHasFixedSize(true); //Indicar que el tamaño del listado no depende de los elemntos que tenga
        layoutCompeticiones = new LinearLayoutManager(this);
        rvCompeticiones.setVisibility(View.INVISIBLE);
        pbCompeticiones.setVisibility(View.VISIBLE);

        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        baseDatos = FirebaseDatabase.getInstance();
        //Obtener competiciones
        if(TrueTime.isInitialized()){
            Date fechTelefono = new Date();
            if(Math.abs(TrueTime.now().getTime()- fechTelefono.getTime())>1800000){
                Toast.makeText(this, "Fecha del dispositivo es incorrecta.", Toast.LENGTH_SHORT).show();
            }else{
                obtenerCompeticiones();
            }
        }

        //Guardar imagen Url
        baseDatos.getReference("usuarios/"+user.getUid()+"/foto").setValue(user.getPhotoUrl()+"");
    }

    public void obtenerCompeticiones(){
        DatabaseReference competiciones = baseDatos.getReference("competiciones");

        // Poner a la escucha el nodo de usuarios
        escuchador1 = competiciones.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Reiniciar listado
                listCompeticiones = new ArrayList<Competicion>();

                //Obtener datos de nombres
                for (DataSnapshot compe : dataSnapshot.getChildren()) {
                    String nombre = "", urlImagen = "";
                    int id = -1, pwd = -1;

                    id = Integer.parseInt(compe.getKey());

                    for (DataSnapshot dato : compe.getChildren()) {
                        //Segun cada dato leido almacenar en la variable adecuada
                        switch (dato.getKey()) {
                            case "nombre":
                                nombre = dato.getValue().toString();
                                break;
                            case "foto":
                                urlImagen = dato.getValue().toString();
                                break;
                            case "pwd":
                                pwd = Integer.parseInt(dato.getValue().toString());
                                break;
                        }
                    }
                    //Crear objeto datos competicion
                    Competicion c = new Competicion(nombre, urlImagen, id, pwd);
                    listCompeticiones.add(c);
                }

                //Asignar listado al recyclerview
                //////////////adaptador = new CompetitionElement(listCompeticiones);
                rvCompeticiones.setLayoutManager(layoutCompeticiones);
                rvCompeticiones.setAdapter(adaptador);

                //Llamada al metodo que obtiene cuales son las competiciones a las que el usuario tiene acceso
                obtenerCompeticionesAcceso();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void obtenerCompeticionesAcceso(){
        DatabaseReference misCompeticiones = baseDatos.getReference("usuarios/"+user.getUid()+"/competiciones");

        // Poner a la escucha el nodo de usuarios
        escuchador2 = misCompeticiones.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Reiniciar listado
                competicionesConAcceso = new ArrayList<Integer>();

                //Obtener datos de nombres
                for(DataSnapshot compe : dataSnapshot.getChildren()) {
                    competicionesConAcceso.add(Integer.parseInt(compe.getKey()));
                }

                //Activar elementos Graficos y desactivar los de carga

                new CountDownTimer(2000,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        //Al acabar llamarse de forma recursiva para ocultar la pantalla principal
                        pbCompeticiones.setVisibility(View.INVISIBLE);
                        rvCompeticiones.setVisibility(View.VISIBLE);
                    }
                }.start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //---------------------------------------

    public static void comprobarAccesoCompeticion(int id){
        boolean conAcceso = false;
        Log.d("JAJAJA", "HOLA");

        for(int i=0;i<competicionesConAcceso.size();i++){
            if(competicionesConAcceso.get(i)==id){
                conAcceso = true;

            }
            Log.d("JAJAJA", competicionesConAcceso.get(i)+"");
        }



        //Comprobar si tiene un acceso a la competicion
        if(conAcceso){
            String nombre = "";
            for(int i=0;i< listCompeticiones.size();i++){
                if(listCompeticiones.get(i).getIdentificador()==id){
                    nombre=listCompeticiones.get(i).getNombre();
                }
            }

            /////////////// ENTRAR EN COMPETICION //////////////////////
            Intent i = new Intent(context, PrincipalActivity.class);
            i.putExtra("id", id);
            i.putExtra("nombre", nombre);
            context.startActivity(i);

        }else{
            /////////////// PEDIR CONTRASEÑA //////////////////////////
            //Solicitar Contraseña con una ventana emergente
           // FragmentManager ft = ((FragmentActivity)context).getSupportFragmentManager();
           // DialogFragment newFragment = RegisterDialogCompe.newInstance(id);
           // newFragment.show(ft, "DCC");
        }
    }


    public void onAceptarDialogo(int pwd, int id) {
        for(int i=0; i<listCompeticiones.size();i++){
            if(listCompeticiones.get(i).getIdentificador() == id){
                if(listCompeticiones.get(i).getPwd()==(pwd)){
                    /////// CONTRASEÑA CORRECTA ///////
                    //Toast.makeText(context, "Aceptado", Toast.LENGTH_SHORT).show();
                    //Añadir la competicion a la que se ha accedido
                    baseDatos.getReference("usuarios/"+user.getUid()+"/competiciones/"+id).setValue("registrado");
                    //Establecer jugador dentro de la competicion
                    baseDatos.getReference("competiciones/"+id+"/jugadores/"+user.getUid()).setValue("uid");

                    //PASAR AL SIGUIENTE ACTIVITY
                    String nombre = "";
                    for(int j=0;j< listCompeticiones.size();j++){
                        if(listCompeticiones.get(j).getIdentificador()==id){
                            nombre=listCompeticiones.get(j).getNombre();
                        }
                    }

                    Intent intent = new Intent(context, PrincipalActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("nombre", nombre);
                    context.startActivity(intent);

                }else{
                    View contextView = findViewById(R.id.layoutCompe);

                    Snackbar snackbar;
                    snackbar = Snackbar.make(contextView, "CONTRASEÑA INCORRECTA", Snackbar.LENGTH_LONG);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(Color.parseColor("#000000"));
                    TextView textView = (TextView) snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextColor(Color.parseColor("#ffffff"));
                    textView.setTypeface(null, Typeface.BOLD);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snackbar.show();
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
    }

    public void cerrarSesion(View view){
        firebaseAuth.signOut();
        finishAffinity();
    }


}

