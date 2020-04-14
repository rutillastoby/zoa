package com.rutillastoby.zoria;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ActivityNuevoUsu extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase baseDatos;
    private ArrayList<String> nombresUsados = new ArrayList<String>();

    private TextInputEditText etNombre;
    private Button bAceptar;
    private CircularImageView ivFoto;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_usu);

        etNombre = findViewById(R.id.etNombre);
        bAceptar = findViewById(R.id.bAceptar);
        bAceptar.setActivated(false);
        tvError = findViewById(R.id.tvError);
        tvError.setVisibility(View.INVISIBLE);
        ivFoto = findViewById(R.id.ivFotoNuevoUsu);
        ivFoto.setBorderWidth(0);

        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        baseDatos = FirebaseDatabase.getInstance();

        if(user==null){
            finish();
        }
        //Establecer imagen
        Picasso.get().load(user.getPhotoUrl()).into(ivFoto);


        setTitle("Información Inicial");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));

        obtenerNombresUsuario();


    }

    @Override
    public void onBackPressed() {
        finishAffinity(); //Cerrar activity
        super.onBackPressed();
    }

    public void bAceptar(View view){
        boolean disponible = true;
        for(int i=0; i<nombresUsados.size();i++){
            if(etNombre.getText().toString().equals(nombresUsados.get(i))){
                disponible=false;
            }
        }

        if(disponible){
            if(etNombre.getText().length()>0){
                if(etNombre.getText().length()>=5 && etNombre.getText().length()<=15){

                    //Eliminar espacios en blanco
                    etNombre.setText(etNombre.getText().toString().replaceAll("\\s+",""));
                    //Guardar en base de datos
                    baseDatos.getReference("/usuarios/"+user.getUid()+"/nombre").setValue(etNombre.getText().toString());
                    baseDatos.getReference("/usuarios/"+user.getUid()+"/foto").setValue(user.getPhotoUrl()+"");
                    //Pasar al siguiente activity
                    Intent i = new Intent(this, ActivityCompeticiones.class);
                    startActivity(i);
                }else{
                    tvError.setText("Tu nombre debe tener entre 5 Y 15 caracteres");
                    tvError.setVisibility(View.VISIBLE);
                }
            }else{
                tvError.setText("Debes introducir un nombre.");
                tvError.setVisibility(View.VISIBLE);
            }
        }else{
            tvError.setText("Ese nombre ya esta registrado.");
            tvError.setVisibility(View.VISIBLE);
        }
    }


    public void obtenerNombresUsuario(){
        DatabaseReference usuarios = baseDatos.getReference("usuarios");

        // Poner a la escucha el nodo de usuarios
        usuarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Reiniciar listado
                nombresUsados = new ArrayList<String>();

                //Obtener datos de nombres
                for(DataSnapshot usu : dataSnapshot.getChildren()) {
                    for(DataSnapshot dato : usu.getChildren()){
                        if(dato.getKey().equalsIgnoreCase("nombre")){
                            Log.d("Nombre", dato.getValue().toString());
                            nombresUsados.add(dato.getValue().toString());
                        }
                    }
                }

                bAceptar.setActivated(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
