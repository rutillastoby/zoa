package com.rutillastoby.zoria;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ClasificacionActivity extends AppCompatActivity {

    private int identificadorCompe; //Identificador de la competicion
    private ArrayList<String> usuariosCompeticion;
    private ArrayList<Clasificado> usuariosClasificacion;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase baseDatos;


    private RecyclerView rvClasificacion;
    private RecyclerView.Adapter adaptador;
    private RecyclerView.LayoutManager layoutCompeticiones;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clasificacion);

        setTitle("Clasificación Provisional");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));

        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        baseDatos = FirebaseDatabase.getInstance();
        //Obtener el identificador de la competicion
        identificadorCompe = getIntent().getIntExtra("id", -1);


        //Iniciar variables del listado
        rvClasificacion = (RecyclerView) findViewById(R.id.rvClasificacion);
        rvClasificacion.setHasFixedSize(true); //Indicar que el tamaño del listado no depende de los elemntos que tenga
        layoutCompeticiones = new LinearLayoutManager(this);

        obteneterUsuarios();
    }

    public void obteneterUsuarios(){
        baseDatos.getReference("competiciones/"+identificadorCompe+"/jugadores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usuariosCompeticion = new ArrayList<String>();
                for (DataSnapshot usuario: dataSnapshot.getChildren()) {
                    usuariosCompeticion.add(usuario.getKey());
                }
                Log.d("CLAS", "Jugadores"+usuariosCompeticion.size());
                obtenerDatosJugadores();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void obtenerDatosJugadores(){
        baseDatos.getReference("usuarios/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usuariosClasificacion= new ArrayList<Clasificado>();

                for (DataSnapshot usuario: dataSnapshot.getChildren()) {
                    //Comprobamos si el usuario esta registrado en la competicion
                    for(String usu : usuariosCompeticion){
                        if(usuario.getKey().equals(usu)){

                            //Crear objeto para la clasificacion
                            Clasificado clas = new Clasificado();

                            //Leer los datos del usuario
                            for (DataSnapshot dato: usuario.getChildren()) {
                                switch (dato.getKey()){
                                    case "nombre":
                                        clas.setNombre(dato.getValue().toString());
                                        break;
                                    case "foto":
                                        clas.setFotoPerfil(dato.getValue().toString());
                                        break;
                                    case "competiciones":
                                        int n1=0, n2=0, n3=0;
                                        //Buscar la competicion activa
                                        for (DataSnapshot competicion: dato.child(identificadorCompe+"/puntos").getChildren()) {
                                            switch (competicion.getValue().toString()){
                                                case "1":
                                                    n1++;
                                                    break;
                                                case "2":
                                                    n2++;
                                                    break;
                                                case "3":
                                                    n3++;
                                                    break;
                                            }
                                        }
                                        clas.setPuntosN1(n1);
                                        clas.setPuntosN2(n2);
                                        clas.setPuntosN3(n3);
                                        break;
                                }
                            }

                            //Añadir el usuario al listado de usuarios clasificados
                            usuariosClasificacion.add(clas);

                        }
                    }
                }
                //OrdenarListado
                for(int i=0; i<usuariosClasificacion.size(); i++){
                    for(int j = i+1; j<usuariosClasificacion.size(); j++){
                        if(usuariosClasificacion.get(i).getTotal()<usuariosClasificacion.get(j).getTotal()){
                            Clasificado aux = usuariosClasificacion.get(i);
                            usuariosClasificacion.set(i, usuariosClasificacion.get(j));
                            usuariosClasificacion.set(j, aux);
                        }
                    }
                }


                //RECARGAR EL LISTADO
                adaptador = new ElementoClasificado(usuariosClasificacion);
                rvClasificacion.setLayoutManager(layoutCompeticiones);
                rvClasificacion.setAdapter(adaptador);
                Log.d("CLAS", "Lista"+usuariosClasificacion.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
