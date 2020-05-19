package com.rutillastoby.zoria;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

public class LoginActivity extends AppCompatActivity {

    // Autenticacion ============================================
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    // Permisos =================================================
    private boolean hasPermissions=true;
    int PERMISSION_ALL = 333;

    // Base de datos ============================================
    private FirebaseDatabase db;

    //Referencias ===============================================
    public static Context context;
    private ProgressBar pbLoadingLogin;
    private Button bLogin;
    private ConstraintLayout lyOldVersion, lyUpdateApp;

    // Variables ================================================
    private boolean registerInDB; //Variable para determinar si el usuario esta ya registrado en la base de datos o no


    //----------------------------------------------------------------------------------------------

    /**
     * SOBRESCRITURA DEL METODO ONCREATE
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Referencias
        context=this;
        pbLoadingLogin = findViewById(R.id.pbLoadingLogin);
        bLogin = findViewById(R.id.bLogin);
        lyOldVersion = findViewById(R.id.lyOldVersion);
        lyUpdateApp = findViewById(R.id.lyUpdateApp);

        //Estado inicial
        changeVisibilityButton(1); //Mostrar cargando
        lyOldVersion.setVisibility(View.GONE);

        //Firebase
        db = FirebaseDatabase.getInstance();


        //////////////////////////// Logueo /////////////////////////////////
        //Objeto para declarar que datos se obtendrán del usuario de google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Referencia al objeto de autenticacion de firebase
        firebaseAuth = FirebaseAuth.getInstance();


        /////////////////////////// Permisos ///////////////////////////////
        //Declarar permisos necesarios
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA
        };
        //Comprobar si se tienen los permisos adecuados
        if(!hasPermissions(this, PERMISSIONS)){
            //Si no se tienen los permisos, los solicitamos de nuevo
            hasPermissions=false;
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }else{
            //Comprobar version de la base de datos
            checkVersionDB();
        }

        //////////////////////////// Botones //////////////////////////////
        lyUpdateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenericFuntions.openPlayStore(context);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                VERSION DE BASE DE DATOS                                    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * METODO PARA COMPROBAR LA VERSION DE BASE DE DATOS DE LA APLICACION Y ASI PODER FORAZAR UNA ACTUALIZACION
     */
    public void checkVersionDB(){
        db.getReference("version").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Comprobar coincidencia entre version base de datos e indicada en fichero strings
                if(dataSnapshot.getValue()!=null && dataSnapshot.getValue().toString().equals(getString(R.string.versionDB))){
                    //Comprobar si el usuario tiene una sesión de login previa guardada
                    checkOldLogin();
                }else{
                    lyOldVersion.setVisibility(View.VISIBLE);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          LOGIN                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Metodo para comprobar si el usuario ya se ha registrado de forma previa
    private void checkOldLogin() {
        //Obtener usuario previamente logueado
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        // Comprobar si el usuario ya esta logueado
        if(currentUser!=null){
            //Acceder al sistema
            login(currentUser);
        }else{
            //Activamos el boton de login
            changeVisibilityButton(2);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ABRIR VENTA DE SELECCION DE CUENTA DE USUARIO DE GOOGLE
     */
    private void signIn() {
        //Mostrar barra de carga
        changeVisibilityButton(1);
        //Abrir intent con cuentas de google del telefono
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //----------------------------------------------------------------------------------------------

    /*
    * METODO RESULTANTE DEL CIERRE/FINALIZACION DEL INTENT DE SELECCION DE USUARIO DE GOOGLE
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Comprobar si el codigo numerico enviado al intent de seleccion de cuenta es el mismo que el recibido
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OPERAR CON LOS RESULTADOS OBTENIDOS (USUARIO SELECCIONADO)
     * @param completedTask
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            //Completar logueo a traves de firebase a partir de la cuenta de google obtenida
            firebaseAuthWithGoogle(account);
            //Mensaje inicial
            GenericFuntions.snack(findViewById(R.id.lyLogin), "Estamos preparando todo!");
        } catch (ApiException e) {
            //Error al iniciar sesion
            GenericFuntions.errorSnack(findViewById(R.id.lyLogin), "Error al iniciar sesión. x001", this);
            changeVisibilityButton(2); //Mostrar boton de login
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA UTILIZAR LA CUENTA DE GOOGLE OBTENIDA Y LOGUEARSE A PARTIR DE ESTA EN FIREBASE
     * @param acct cuenta de google obtenida
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //Comprobar si se ha obtenido correctamente el usuario de firebase
                    if (task.isSuccessful()) {
                        // Obtener el usuario final de firebase
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        //Llamada al metodo para acceder al sistema con el usuario obtenido
                        login(user);
                    } else {
                        //Error al iniciar sesion
                        GenericFuntions.errorSnack(findViewById(R.id.lyLogin), "Error al iniciar sesión. x002", context);
                        changeVisibilityButton(2); //Mostrar boton de login
                    }

                }
            });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ACCEDER AL SISTEMA
     */
    public void login(final FirebaseUser user){
        registerInDB=false;

        // Poner a la escucha el nodo de usuarios
        db.getReference("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Establecer si el usuario esta registrado en la base de datos
                for(DataSnapshot usu : dataSnapshot.getChildren()) {
                    if(usu.getKey().equals(user.getUid())){
                        registerInDB=true;
                    }
                }

                //Cargamos la activity correspondiente segun si es un usuario nuevo o no
                if(registerInDB){
                    //Obtener la hora de la base de datos para pasarla a la activity general
                    FirebaseFunctions.getInstance("europe-west1").getHttpsCallable("getTime")
                            .call().addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                        @Override
                        public void onSuccess(HttpsCallableResult httpsCallableResult) {
                            //Datos obtenidos
                            long currentMilliseconds = (long) httpsCallableResult.getData();

                            //Abrir activity general
                            Intent i = new Intent(context, GeneralActivity.class);
                            i.putExtra("currentTime", currentMilliseconds);
                            startActivity(i);
                            finish();
                        }
                    });

                }else{
                    //Abrir activity nuevo usuario
                    Intent i = new Intent(context, NewUserActivity.class);
                    startActivity(i);
                    finish();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                         PERMISOS                                           //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * METODO PARA COMPROBAR SI SE TIENEN PERMISOS ACTIVADOS PASANDO EL LISTADO DE LOS MISMOS
     * @param context
     * @param permissions
     * @return
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER LA RESPUESTA A LA SOLICITUD DE PERMISOS
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Comprobar si se han concedido los permisos
        boolean grant = true;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                grant = false;
            }
        }

        if(grant){
            //Comprobar version de la aplicacion
            checkVersionDB();
        }else {
            //Si se deniegan los permisos, se cierra la aplicacion
            finishAffinity();
        }

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          OTROS                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * METODO PARA CAMBIAR LA VISIBILIDAD DEL BOTON DE LOGIN Y CARGA
     * @param status 0-> no mostrar nada, 1 -> Mostrar boton login, 2-> mostrar barra progreso
     */
    public void changeVisibilityButton(int status){
        switch (status){
            case 0:
                pbLoadingLogin.setVisibility(View.INVISIBLE);
                bLogin.setVisibility(View.INVISIBLE);
                break;
            case 1:
                pbLoadingLogin.setVisibility(View.VISIBLE);
                bLogin.setVisibility(View.INVISIBLE);
                break;
            case 2:
                pbLoadingLogin.setVisibility(View.INVISIBLE);
                bLogin.setVisibility(View.VISIBLE);
        }
    }
}