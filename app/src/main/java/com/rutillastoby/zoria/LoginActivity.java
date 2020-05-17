package com.rutillastoby.zoria;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    // Autenticacion ============================================
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;
    public static final int SIGN_IN_CODE = 777;
    public static Activity inicio;
    private boolean permisos=true;

    // Base de datos ============================================
    private FirebaseDatabase baseDatos;

    // Otros ====================================================
    private boolean adminObtenidos, transpObtenidos, iniciado, registrado;
    public static Context context;
    private ProgressBar pbCargando;
    private Button bAcceder;

    //private MediaPlayer mpLogin;

    //----------------------------------------------------------------------------------------------

    /**
     * SOBRESCRITURA DEL METODO ONCREATE
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inicio = this;

        //Comprobar permisos
        int PERMISSION_ALL = 333;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA
        };

        if(!hasPermissions(this, PERMISSIONS)){
            permisos=false;
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        //Iniciar variables
        iniciado=false;
        //mpLogin = MediaPlayer.create(this, R.raw.login);
        pbCargando = (ProgressBar) findViewById(R.id.pbcarga2);
        pbCargando.setVisibility(View.VISIBLE);
        bAcceder = (Button) findViewById(R.id.bLogin);
        bAcceder.setVisibility(View.INVISIBLE);

        context = this;

        //Referencia a la base de datos
        baseDatos = FirebaseDatabase.getInstance();
        //Referencia al objeto de autenticacion de firebase
        firebaseAuth = FirebaseAuth.getInstance();
        comprobarVersion();
    }

    /**
     * COMPROBAR LA VERSION DE LA APLICACION
     */
    public void comprobarVersion(){
        baseDatos.getReference("version").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null && dataSnapshot.getValue().toString().equals("1")){
                    //ACCEDER AL SISTEMA
                    //Se intenta obtener el usuario si esta logueado anteriormente
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if(user!=null&&permisos){
                        logueado(user);
                    }else{
                        bAcceder.setVisibility(View.VISIBLE);
                        pbCargando.setVisibility(View.INVISIBLE);
                        //Inicio Login
                        inicioLogin();
                    }
                }else{
                    Toast.makeText(context, "Version antigua, actualiza la aplicacion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }



    //----------------------------------------------------------------------------------------------

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
     *
     */
    public void inicioLogin(){
        //Ajustes de la ventana de inicio de sesion propia de google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Boton Iniciar sesion, programar accion al pulsarlo
        bAcceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbCargando.setVisibility(View.VISIBLE);
                bAcceder.setVisibility(View.INVISIBLE);
                //Abrir el intent correspondiente con la ventana de inicio de sesion para seleccionar correo
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, SIGN_IN_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pbCargando.setVisibility(View.VISIBLE);
        bAcceder.setVisibility(View.INVISIBLE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == SIGN_IN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("ERROR", "Google sign in failed", e);
                // ...
                //Toast.makeText(this, "d", Toast.LENGTH_SHORT).show();
                pbCargando.setVisibility(View.INVISIBLE);
                bAcceder.setVisibility(View.VISIBLE);

            }
        }
    }





    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            logueado(user);
                            Snackbar.make(findViewById(R.id.lyNewUser), "Entrando en el sistema...", Snackbar.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.lyNewUser), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }




    //----------------------------------------------------------------------------------------------

    private void logueado(final FirebaseUser user) {
        registrado=false;
        DatabaseReference usuarios = baseDatos.getReference("usuarios");

        // Poner a la escucha el nodo de usuarios
        usuarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot usu : dataSnapshot.getChildren()) {
                    if(usu.getKey().equals(user.getUid())){
                        registrado=true;
                    }
                }

                baseDatos.getReference("usuarios").removeEventListener(this);
                //Cargamos el intent segun si es un usuario nuevo o no
                if(registrado){

                    //Obtener la hora de la base de datos para pasarla a la activity general
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

                }else{
                    //Abrir activity nuevo usuario
                        //View contextView = findViewById(R.id.LayoutEleCompe);
                        //Snackbar.make(contextView, "nuevo", Snackbar.LENGTH_SHORT).show();
                    Intent i = new Intent(context, NewUserActivity.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    //----------------------------------------------------------------------------------------------



    //----------------------------------------------------------------------------------------------

    /**
     * SOBRESCRITURA DEL METODO onConnectionFailed
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
        if(hasAllPermissionsGranted(grantResults)){
            //Toast.makeText(this, "d", Toast.LENGTH_SHORT).show();
        }else {
            finishAffinity();
        }

    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected void onResume() {
        Log.d("ppp", "loginResume");

        super.onResume();
    }
}
