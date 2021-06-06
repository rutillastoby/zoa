package com.rutillastoby.zoria;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;

public class NewUserActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;

    //Referencias
    private EditText etNickNewUser;
    private Button bOkNewUser, bCancelNewUser;
    private ImageView ivPhotoProfileNewUser;
    private ProgressBar pbNickNewUser;
    private ConstraintLayout lyNewUser;

    //Variables
    String nick;
    boolean saveName, savePhoto, saveCompeEnab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        //Si el usuario no esta registrado cerrar aplicacion (mecanismo de seguridad)
        if(user==null){
            finish();
        }

        //Inicializar variables y obtener referencias
        initVar();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(){
        final Context context = this;

        //Obtener referencias
        etNickNewUser = findViewById(R.id.etNickNewUser);
        bOkNewUser = findViewById(R.id.bOkNewUser);
        bCancelNewUser = findViewById(R.id.bCancelNewUser);
        ivPhotoProfileNewUser = findViewById(R.id.ivPhotoProfileNewUser);
        pbNickNewUser = findViewById(R.id.pbNickNewUser);
        lyNewUser = findViewById(R.id.lyNewUser);

        //Ocultar botones toolbar
        findViewById(R.id.ivLogout).setVisibility(View.GONE);
        findViewById(R.id.ivInfoRanking).setVisibility(View.GONE);
        findViewById(R.id.ivInfoGeneral).setVisibility(View.GONE);

        //Cargar la imagen de perfil del usuario
        String bigPhoto = user.getPhotoUrl().toString().replace("s96-c", "s320-c");
        GenericFuntions.chargeImageRound(context, bigPhoto, ivPhotoProfileNewUser);

        //Funcionalidad al pulsar el boton de aceptar
        bOkNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nick = etNickNewUser.getText().toString();
                correctUsername(nick.toLowerCase());

                //Cerrar teclado
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(R.id.lyNewUser).getWindowToken(), 0);
                etNickNewUser.clearFocus();
            }
        });

        //Funcionalidad al pulsar el boton de cancelar
        bCancelNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cerrar sesion
                FirebaseAuth.getInstance().signOut();
                //Cerrar aplicacion
                finishAffinity();
            }
        });

        //Ejecucion al pulsar fuera del cuadro de texto cerrar teclado y limpiar foco
        lyNewUser.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Cerrar teclado
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                etNickNewUser.clearFocus();
                return true;
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA COMPROBAR SI EL NOMBRE DE USUARIO INTRODUCIDO ES VALIDO Y ESTA DISPONIBLE
     */
    public void correctUsername(final String username){
        final Context context = this;

        //Activar barra de progreso
        pbNickNewUser.setVisibility(View.VISIBLE);

        //Obtener los nombre de usuario ocupados
        db.getReference("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Listado
                ArrayList<String> nombresUsados = new ArrayList<String>();

                //Obtener datos de nombres
                for(DataSnapshot usu : dataSnapshot.getChildren()) {
                    for(DataSnapshot dato : usu.getChildren()){
                        if(dato.getKey().equalsIgnoreCase("nombre")){
                            nombresUsados.add(dato.getValue().toString().toLowerCase());
                        }
                    }
                }

                //Al obtener todos los nombres de usuarios, comprobar si el usuario es valido
                if(GenericFuntions.checkNick(username, nombresUsados)=="true"){
                    //Registrar usuario en la base de datos
                    registerUser();
                }else{
                    GenericFuntions.errorSnack(findViewById(R.id.lyNewUser), GenericFuntions.checkNick(username, nombresUsados), context);
                    //Ocultar barra de progreso
                    pbNickNewUser.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA REGISTRAR EL USUARIO EN LA BASE DE DATOS
     */
    public void registerUser(){
        saveName=false;
        savePhoto=false;
        saveCompeEnab=false;

        //Nombre de usuario
        db.getReference("/usuarios/"+user.getUid()+"/nombre").setValue(nick, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                //Si se ha guardado correctamente
                if (databaseError == null) {
                    saveName=true; //Marcar que el dato se ha guardado
                    loadGeneralActivity(); //Comprobar si se han almacenado todos los datos para pasar a la siguiente activity
                }
            }
        });
        //Imagen de perfil
        String bigPhoto = user.getPhotoUrl().toString().replace("s96-c", "s320-c");
        db.getReference("/usuarios/"+user.getUid()+"/foto").setValue(bigPhoto, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                //Si se ha guardado correctamente
                if (databaseError == null) {
                    savePhoto=true; //Marcar que el dato se ha guardado
                    loadGeneralActivity(); //Comprobar si se han almacenado todos los datos para pasar a la siguiente activity
                }
            }
        });
        //Competicion activa
        db.getReference("/usuarios/"+user.getUid()+"/compeActiva").setValue(-1, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                //Si se ha guardado correctamente
                if (databaseError == null) {
                    saveCompeEnab=true; //Marcar que el dato se ha guardado
                    loadGeneralActivity(); //Comprobar si se han almacenado todos los datos para pasar a la siguiente activity
                }
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO QUE SE EJECUTARA AL COMPLETAR EL GUARDADO DE DATOS DEL USUARIO EN LA BASE DE DATOS
     * (REGISTRO INICIAL), ESTE ABRIRA LA SIGUIENTE ACTIVITY
     */
    public void loadGeneralActivity(){
        if(saveName && savePhoto && saveCompeEnab){
            //Cargar activity general tras el registro
            final Context context = this;

            //Obtener la hora de la base de datos para la General Activity
            FirebaseFunctions.getInstance("europe-west1").getHttpsCallable("getTime")
                    .call().addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                @Override
                public void onSuccess(HttpsCallableResult httpsCallableResult) {
                    long currentMilliseconds = (long) httpsCallableResult.getData();

                    //Abrir activity general
                    Intent i = new Intent(context, GeneralActivity.class);
                    i.putExtra("currentTime", currentMilliseconds);
                    startActivity(i);
                    finish();
                }
            });
        }
    }
}