package com.rutillastoby.zoria;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    final Fragment mapFragment = new MapFragment();
    final Fragment scannerFragment = new ScannerFragment();
    final Fragment questionsFragment = new QuestionsFragment();
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
    ScannerFragment scanF;
    QuestionsFragment questF;
    MapFragment mapF;

    //Variables
    private static ArrayList<CompeticionDao> competitionsList;
    private static ArrayList<UsuarioDao> usersList;
    private int currentCompeId=-1; //Id de la competicion que esta como activa para el usuario (Accesible desde el boton current del menu inferior)
    private int showingCompeId=-1; //Id de la competicion que se esta mostrando en el fragmento principal y para la que hay que recargar al recibir nuevos datos
    private long currentMilliseconds;
    private CompeticionDao competitionShow = new CompeticionDao();//Datos de la competicion que se esta visualizando en este momento
    private UsuarioDao myUser;

    //----------------------------------------------------------------------------------------------

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
        fm.beginTransaction().add(R.id.container_fragment, questionsFragment, "6").hide(questionsFragment).commit();
        fm.beginTransaction().add(R.id.container_fragment, scannerFragment, "5").hide(scannerFragment).commit();
        fm.beginTransaction().add(R.id.container_fragment, mapFragment, "4").hide(mapFragment).commit();

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
        scanF = (ScannerFragment) scannerFragment;
        questF = (QuestionsFragment) questionsFragment;
        mapF = (MapFragment) mapFragment;

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
            //Detener el scanner al cambiar de fragmento por si estaba iniciado
            scanF.stopScanner();

            switch (item.getItemId()) {
                case R.id.navigation_competitions:
                    fm.beginTransaction().hide(active).show(competitionsFrag).commit();
                    active = competitionsFrag;
                    //Ocultar boton cerrar sesion
                    ivLogout.setVisibility(View.GONE);
                    return true;

                case R.id.navigation_current:
                    //Ocultar boton cerrar sesison
                    ivLogout.setVisibility(View.GONE);
                    //Llamada al metodo que mostrará el fragmento current
                    checkFragmentCurrent();
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
     * METODO PARA MOSTRAR EL FRAGMENTO DE TIPO MAPA
     */
    public void showMapFragment(){
        fm.beginTransaction().hide(active).show(mapFragment).commit();
        active = mapFragment;
        //Detener el scanner al cambiar de fragmento por si estaba iniciado
        scanF.stopScanner();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR EL FRAGMENTO DE TIPO SCANNER
     */
    public void showScannerFragment(){
        fm.beginTransaction().hide(active).show(scannerFragment).commit();
        active = scannerFragment;
        //Iniciar la camara para el scanner
        scanF.startScanner();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR EL FRAGMENTO DE TIPO PREGUNTAS
     */
    public void showQuestionsFragment(){
        fm.beginTransaction().hide(active).show(questionsFragment).commit();
        active = questionsFragment;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA APLICAR UNA TRANSICION ENTRE FRAGMENTOS Y MOSTRAR EL PRINCIPAL SIN CAMBIAR DE COMPETICION
     */
    public void showPrincActivityNotChange(){
        fm.beginTransaction().hide(active).show(principalFrag).commit();
        active = principalFrag;
    }

    //----------------------------------------------------------------------------------------------

    /**
     *  METODO PARA MOSTRAR LA COMPETICIÓN QUE SE ESTÁ DISPUTANDO
     *  SE EJECUTA AL HACER CLIC EN LA OPCION CURRENT DEL MENU INFERIOR
     */
    public void checkFragmentCurrent(){
        //Marcar opcion del menu como activa
        navigation.getMenu().findItem(R.id.navigation_current).setChecked(true);
        fm.beginTransaction().hide(active).show(principalFrag).commit();
        active = principalFrag;

        //INSERTAR AQUI CODIGO PARA MOSTRAR PANEL DE NO REGISTRADO EN NINGUNA COMPETICION
        ////// AQUI

        //Mostrar la vista de la competicion activa
        if(currentCompeId!=-1) showMainViewCompetition(currentCompeId);
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
        //Detener el scanner al cambiar de fragmento por si estaba iniciado
        scanF.stopScanner();
        //Enviar los datos de la competicion
        for(int i=0; i<competitionsList.size();i++){
            if(competitionsList.get(i).getId() == id) {
                prinF.setDataCompetition(competitionsList.get(i), questF, mapF, myUser);
                competitionShow = competitionsList.get(i); //Establecer los datos de la competicion que se está visualizando
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * SOBREESCRITURA DEL METODO QUE SE ACCIONA AL PULSAR EN EL BOTON DE VOLVER.
     * CIERRA LA APLICACIÓN EN LUGAR DE VOLVER A CARGAR LA ACTIVITY DE LOGIN
     */
    @Override
    public void onBackPressed() {
        //En funcion del fragmento activo actuaremos
        switch (active.getTag()){
            //MAP FRAGMENT, QUESTIONS FRAGMENT, RANKING FRAGMENT
            case "6":
            case "7":
            case "4":
                //Volver al fragmento principal sin hacer cambios
                showPrincActivityNotChange();
                break;
            //SCANNER FRAGMENT
            case "5":
                showMapFragment();
                break;
            //OTRO FRAGMENT
            default:
                finishAffinity(); //Cerrar aplicacion directamente
                break;
        }
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

                    //Agregamos a la lista de competiciones
                    competitionsList.add(c);

                    //Comprobar si la competicion que ha cambiado es la que se esta mostrando en fragment para actualizar los cambios
                    if(c.getId()==showingCompeId) {
                        prinF.setDataCompetition(c, questF, mapF,myUser); //Establecemos los datos al fragmento principal de la competicion
                        competitionShow = c; //Establecer los datos de la competicion que se está visualizando
                    }
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
                        //Guardar los datos de mi usuario
                        myUser = u;
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
    //                         GUARDAR INFORMACION EN LA BASE DE DATOS                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void sendResponseQuestion(String idQuestion, int idResponse){
        db.getReference("competiciones/"+showingCompeId+"/jugadores/"+user.getUid()+"/preguntas/"+idQuestion)
                .setValue(idResponse);
    }

    //----------------------------------------------------------------------------------------------

    public void sendPointScann(String idPoint){
        db.getReference("competiciones/"+showingCompeId+"/jugadores/"+user.getUid()+"/puntos/"+idPoint)
                .setValue(currentMilliseconds);
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

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA RECUPERAR LOS DATOS DE MI USUARIO
     * @return
     */
    public UsuarioDao getMyUser() {
        return myUser;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER EL VALOR DE LA COMPETICION MARCADA COMO ACTIVA
     * @param currentCompeId
     */
    public void setCurrentCompeId(int currentCompeId) {
        this.currentCompeId = currentCompeId;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER EL VALOR DE LA COMPETICION MARCADA COMO ACTIVA
     * @return
     */
    public int getCurrentCompeId() {
        return currentCompeId;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER LOS DATOS DE LA COMPETICION QUE SE ESTÁ VISUALIZANDO EN ESTE INSTANTE
     */
    public CompeticionDao getCompetitionShow() {
        return competitionShow;
    }
}
