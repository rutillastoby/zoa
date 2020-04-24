package com.rutillastoby.zoria;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rutillastoby.zoria.dao.CompeticionDao;
import com.rutillastoby.zoria.dao.UsuarioDao;
import com.rutillastoby.zoria.ui.competitions.CompetitionsFragment;
import com.rutillastoby.zoria.ui.principal.PrincipalFragment;
import com.rutillastoby.zoria.ui.profile.ProfileFragment;

import java.util.ArrayList;

public class GeneralActivity extends AppCompatActivity {

    //Fragmentos para mostrar con el menu
    final Fragment competitionsFrag = new CompetitionsFragment();
    final Fragment principalFrag = new PrincipalFragment();
    final Fragment profileFrag = new ProfileFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;

    //Referencias
    BottomNavigationView navigation;
    Toolbar toolbar;
    ImageView ivLogout;
    CompetitionsFragment compF;
    ProfileFragment profF;
    PrincipalFragment prinF;

    //Variables
    private static ArrayList<CompeticionDao> competitionsList;
    private static ArrayList<UsuarioDao> usersList;
    private int currentCompeId=-1; //Id de la competicion que esta como activa para el usuario (Accesible desde el boton current del menu inferior)
    private int showingCompeId=-1; //Id de la competicion que se esta mostrando en el fragmento principal y para la que hay que recargar al recibir nuevos datos
    private long currentMilliseconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Establecer vita
        setContentView(R.layout.activity_general);

        //Obtener menu inferior
        navigation = findViewById(R.id.nav_view);
        //Escucha para los botones del menu
        navigation.setOnNavigationItemSelectedListener(onClickMenuItem);

        //Ocultar fragmentos inicialmente
        fm.beginTransaction().add(R.id.container_fragment, profileFrag, "3").hide(profileFrag).commit();
        fm.beginTransaction().add(R.id.container_fragment, competitionsFrag, "2").hide(competitionsFrag).commit();
        fm.beginTransaction().add(R.id.container_fragment, principalFrag, "1").commit();
        active= principalFrag;
        navigation.getMenu().findItem(R.id.navigation_current).setChecked(true);

        //Establecer barra personalizada
        toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);

        //Referencias
        ivLogout = findViewById(R.id.ivLogout);
        compF = (CompetitionsFragment) competitionsFrag;
        profF = (ProfileFragment) profileFrag;
        prinF = (PrincipalFragment) principalFrag;

        //Obtener hora actual del intent
        currentMilliseconds = getIntent().getLongExtra("currentTime", 0);
        if(currentMilliseconds==0) onBackPressed(); //Cerrar si no se obtiene correctamente

        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();

        //Inicializar escucha de datos
        getCompetitions();
        getUsers();
        currentTimer();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ACTUAR EN FUNCION DE LA OPCIÓN DEL MENU PULSADA
     */
    private BottomNavigationView.OnNavigationItemSelectedListener onClickMenuItem
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_competitions:
                    fm.beginTransaction().hide(active).show(competitionsFrag).commit();
                    active = competitionsFrag;
                    //Ocultar boton cerrar sesion
                    ivLogout.setVisibility(View.GONE);
                    Log.d("aaa" ,"competitions");
                    return true;

                case R.id.navigation_current:
                    //Ocultar boton cerrar sesison
                    ivLogout.setVisibility(View.GONE);
                    //Llamada al metodo que mostrará el fragmento current
                    setFragmentCurrent();
                    return true;

                case R.id.navigation_profile:
                    fm.beginTransaction().hide(active).show(profileFrag).commit();
                    active = profileFrag;
                    //Mostrar boton cerrar sesion
                    ivLogout.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    //----------------------------------------------------------------------------------------------

    /**
     *  METODO PARA MOSTRAR LA COMPETICIÓN QUE SE ESTÁ DISPUTANDO
     *  SE EJECUTA AL HACER CLIC EN LA OPCION CURRENT DEL MENU INFERIOR
     */
    public void setFragmentCurrent(){
        //Marcar opcion del menu como activa
        navigation.getMenu().findItem(R.id.navigation_current).setChecked(true);
        //Mostrar la vista de la competicion activa
        showMainViewCompetition(currentCompeId);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR LA VISTA PRINCIPAL DE UNA COMPETICION DETERMINADA
     */
    public void showMainViewCompetition(int id){
        //Actualizar la variable de marca de la competicion para la que se deben enviar actualizaciones
        showingCompeId=id;
        //Mostrar el fragmento principal de la competicion
        fm.beginTransaction().hide(active).show(principalFrag).commit();
        active = principalFrag;
        //Enviar los datos de la competicion
        for(int i=0; i<competitionsList.size();i++){
            if(competitionsList.get(i).getId() == id)
                prinF.setDataCompetition(competitionsList.get(i));
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * SOBREESCRITURA DEL METODO QUE SE ACCIONA AL PULSAR EN EL BOTON DE VOLVER.
     * CIERRA LA APLICACIÓN EN LUGAR DE VOLVER A CARGAR LA ACTIVITY DE LOGIN
     */
    @Override
    public void onBackPressed() {
        finishAffinity(); //Cerrar aplicacion directamente
        super.onBackPressed();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                              OBTENER HORA DEL SERVIDOR                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * METODO PARA MANTENER LA HORA ACTUAL ACTUALIZADA
     */
    private void currentTimer(){
        new CountDownTimer(Long.MAX_VALUE,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentMilliseconds +=1000;
            }
            @Override
            public void onFinish() {}
        }.start();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                         OBTENER INFORMACION DE LA BASE DE DATOS                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * METODO PARA OBTENER DATOS DE LAS COMPETICIONES
     */
    private void getCompetitions(){
        final DatabaseReference competitions = db.getReference("competiciones");

        competitions.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Reiniciar listado
                competitionsList = new ArrayList<CompeticionDao>();

                //Obtener los datos de las competiciones
                for (DataSnapshot compe : dataSnapshot.getChildren()) {
                    CompeticionDao c = compe.getValue(CompeticionDao.class); //Rellenar objeto de tipo competicion
                    c.setId(Integer.parseInt(compe.getKey()));
                    competitionsList.add(c); //Agregamos a la lista de competiciones

                    //Comprobar si la competicion que ha cambiado es la que se esta mostrando en fragment para actualizar los cambios
                    if(c.getId()==showingCompeId)
                        prinF.setDataCompetition(c);
                }

                //Establecer el nuevos valores para el fragmento del listado de competiciones
                compF.setCompetitionsList(competitionsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER LOS DATOS DE LOS USUARIOS
     */
    public void getUsers(){
        final DatabaseReference users = db.getReference("usuarios");

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Reiniciar listado
                usersList = new ArrayList<UsuarioDao>();

                //Obtener los datos de las competiciones
                for (DataSnapshot userRead : dataSnapshot.getChildren()) {
                    UsuarioDao u = userRead.getValue(UsuarioDao.class); //Rellenar objeto de tipo usuario
                    u.setUid(userRead.getKey());
                    usersList.add(u); //Agregamos a la lista de usuarios

                    //Si es mi propio usuario, actumamos
                    if(u.getUid().equals(user.getUid())){
                        //Obtener compecion marcada como activa
                        currentCompeId = u.getCompeActiva();

                        //Llamada al metodo para establecer las competiciones en las que el usuario esta registrado.
                        // Dentro del fragmento de competicions
                        if(u.getCompeticiones()!=null) {
                            compF.setCompetitionsRegisteredList(new ArrayList<>(u.getCompeticiones().values()));
                        }
                        //Guardar usuario en la variable del fragmento profile
                        profF.setMyUser(u);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      GETS + SETS                                           //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * METODO PARA OBTENER LOS MILISEGUNDOS DE LA HORA ACTUAL
     * @return
     */
    public long getCurrentMilliseconds() {
        return currentMilliseconds;
    }

}
