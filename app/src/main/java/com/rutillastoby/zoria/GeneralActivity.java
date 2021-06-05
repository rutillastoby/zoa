package com.rutillastoby.zoria;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.rutillastoby.zoria.dao.competicion.Hora;
import com.rutillastoby.zoria.dao.competicion.Jugador;
import com.rutillastoby.zoria.dao.competicion.Pregunta;
import com.rutillastoby.zoria.ui.competitions.CompetitionsFragment;
import com.rutillastoby.zoria.ui.principal.PrincipalFragment;
import com.rutillastoby.zoria.ui.profile.ProfileFragment;

import java.util.ArrayList;
import java.util.Map;

public class GeneralActivity extends AppCompatActivity {

    //Fragmentos para mostrar con el menu
    final Fragment competitionsFrag = new CompetitionsFragment();
    final Fragment principalFrag = new PrincipalFragment();
    final Fragment profileFrag = new ProfileFragment();
    final Fragment mapFragment = new MapFragment();
    final Fragment scannerFragment = new ScannerFragment();
    final Fragment questionsFragment = new QuestionsFragment();
    final Fragment rankingFragment = new RankingFragment();
    final FragmentManager fm = getSupportFragmentManager();
    private Fragment active;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;

    //Referencias
    private BottomNavigationView navigation;
    private Toolbar toolbar;
    private ImageView ivLogout, ivInfoRanking, ivInfoGeneral;
    private CompetitionsFragment compF;
    private ProfileFragment profF;
    private PrincipalFragment prinF;
    private ScannerFragment scanF;
    private QuestionsFragment questF;
    private MapFragment mapF;
    private RankingFragment rankF;

    //Variables
    private static ArrayList<CompeticionDao> competitionsList;
    private static ArrayList<UsuarioDao> usersList;
    private int currentCompeId=-1; //Id de la competicion que esta como activa para el usuario (Accesible desde el boton current del menu inferior)
    private int showingCompeId=-1; //Id de la competicion que se esta mostrando en el fragmento principal y para la que hay que recargar al recibir nuevos datos
    private long currentMilliseconds;
    private CompeticionDao competitionShow = new CompeticionDao(); //Datos de la competicion que se esta visualizando en este momento
    private UsuarioDao myUser;
    private boolean getCompetitions=false, getUsers=false, initExecute=true; //Variables para determinar cuando se han recuperado los datos
    private int posMyUserRanking=0; //Variable para indicar en que posicion del recyclerview del rankig esta mi usuario
    private CountDownTimer countOffApp=null; //Variable para contabilizar un tiempo máximo de funcionamiento de la aplicacion en suspensión
    private boolean isInitLoad=false; //Variable para establecer cuando se carga la informacion de las vistas inicialmente

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); //Soporte SVG api 19

        //Establecer vita
        setContentView(R.layout.activity_general);

        //Obtener menu inferior
        navigation = findViewById(R.id.nav_view);
        //Escucha para los botones del menu
        navigation.setOnNavigationItemSelectedListener(onClickMenuItem);

        //Ocultar fragmentos inicialmente
        fm.beginTransaction().add(R.id.container_fragment, rankingFragment, "7").hide(rankingFragment).commit();
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
        ivInfoRanking = findViewById(R.id.ivInfoRanking);
        ivInfoGeneral = findViewById(R.id.ivInfoGeneral);
        compF = (CompetitionsFragment) competitionsFrag;
        profF = (ProfileFragment) profileFrag;
        prinF = (PrincipalFragment) principalFrag;
        scanF = (ScannerFragment) scannerFragment;
        questF = (QuestionsFragment) questionsFragment;
        mapF = (MapFragment) mapFragment;
        rankF = (RankingFragment) rankingFragment;

        //Obtener hora actual del intent
        currentMilliseconds = getIntent().getLongExtra("currentTime", 0);
        if(currentMilliseconds==0) onBackPressed(); //Cerrar si no se obtiene correctamente

        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();

        //Estado inicial
        ivLogout.setVisibility(View.GONE);
        ivInfoRanking.setVisibility(View.GONE);
        ivInfoGeneral.setVisibility(View.GONE);

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
                    //Ocultar botones toolbar
                    hideToolbarButtons();
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
                    //Mostrar botones correspondientes toolbar
                    ivLogout.setVisibility(View.VISIBLE);
                    ivInfoGeneral.setVisibility(View.VISIBLE);
                    ivInfoRanking.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }
    };

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR EL FRAGMENTO DE COMPETICIONES
     */
    public void showCompetitionsFragment(){
        fm.beginTransaction().hide(active).show(competitionsFrag).commit();
        navigation.getMenu().findItem(R.id.navigation_competitions).setChecked(true);
        active = competitionsFrag;
        //Ocultar botones toolbar
        hideToolbarButtons();
    }


    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR EL FRAGMENTO DE TIPO RANKING
     */
    public void showRankingFragment(){
        fm.beginTransaction().hide(active).show(rankingFragment).commit();
        active = rankingFragment;
        //Mostrar boton de informacion
        hideToolbarButtons();
        ivInfoRanking.setVisibility(View.VISIBLE);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR EL FRAGMENTO DE TIPO MAPA
     */
    public void showMapFragment(){
        fm.beginTransaction().hide(active).show(mapFragment).commit();
        active = mapFragment;
        //Ocultar botones toolbar
        hideToolbarButtons();
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
        //Ocultar botones toolbar
        hideToolbarButtons();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA APLICAR UNA TRANSICION ENTRE FRAGMENTOS Y MOSTRAR EL PRINCIPAL SIN CAMBIAR DE COMPETICION
     */
    public void showPrincActivityNotChange(){
        fm.beginTransaction().hide(active).show(principalFrag).commit();
        active = principalFrag;
        //Ocultar botones toolbar
        hideToolbarButtons();
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
        //Ocultar botones toolbar
        hideToolbarButtons();

        //Mostrar la vista de la competicion activa, si no hay ninguna mostramos panel de no registrado
        //Comprobando si se han cargado los datos iniciales
        if(isInitLoad){
            if(currentCompeId!=-1){
                showMainViewCompetition(currentCompeId);
            }else{
                prinF.setViewNotRegister();
            }
        }

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
            if(competitionsList.get(i).getId() == id) {
                competitionShow = competitionsList.get(i); //Establecer los datos de la competicion que se está visualizando
                prinF.setDataCompetition(competitionsList.get(i), questF, mapF, myUser);
                rankF.loadRanking(competitionsList.get(i), usersList);
            }
        }
        //Ocultar iconos toolbar
        hideToolbarButtons();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CARGAR LOS PUNTOS EN EL MAPA POR PRIMERA VEZ CUANDO ESTE SE INICIA POSTERIORMENTE
     * A LA RECUPERACION DE LOS DATOS (PUNTOS + USUARIOS)
     */
    public void initLoadPointsMap(){
        //Cargar siempre y cuando el mapa se inicie despues de cargar todos los datos, si no es así
        // el metodo @chargeAll se encargará de cargarlos de forma explicita a traves del metodo @checkFragmentCurrent
        if(!initExecute && currentCompeId!=-1){
            //A traves de este metodo se cargan los puntos
            prinF.setDataCompetition(competitionShow, questF, mapF, myUser);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO QUE SE EJECUTA AL RECUPERAR TODOS LOS DATOS, SE ENCARGA DEL RESPALDO DE DATOS GUARDADOS EN LOCAL
     */
    public void chargeAll(){
        initExecute=false; //Marcar como ejecutado

        //Reproducir sonido de acceso
        GenericFuntions.playSound(this, R.raw.login);

        //Comprobar si quedan elementos en local por subir a la base de datos
        if(countRowsData()>0){

            //Declarar base de datos y tabla con la que utilizar
            AdminSQLiteOpenHelper asoh = new AdminSQLiteOpenHelper(this, "localdata", null, 1);
            SQLiteDatabase dbLocal = asoh.getReadableDatabase();
            Cursor cursor = dbLocal.rawQuery("select * from data",null);

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    final int id = cursor.getInt(cursor.getColumnIndex("id"));
                    String path = cursor.getString(cursor.getColumnIndex("path"));
                    String value = cursor.getString(cursor.getColumnIndex("value"));
                    //Obtener el tipo de dato 0->texto, 1->int.
                    int typeValue  = cursor.getInt(cursor.getColumnIndex("typevalue"));

                    //Resubir datos como texto o numero en funcion del tipo de dato guardado en local
                    db.getReference(path)
                        .setValue(typeValue==1?Integer.parseInt(value):value, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                //Si se ha guardado correctamente el valor, eliminamos el dato del fichero
                                if (databaseError == null) {
                                    removeALine(id);
                                    //En el momento en el que no quede ningun dato por respaldar cargamos la informacion inicial
                                    if(countRowsData()==0){
                                        initLoad();
                                    }
                                }
                            }
                        });
                    cursor.moveToNext();
                }
            }

            //Cerrar conexiones
            dbLocal.close();
            cursor.close();

        }else{
           //Si no hay datos que reespaldar directamente cargamos la informacion en la vistas
           initLoad();
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CARGAR LA INFORMACION INICIAL EN LAS VISTAS
     */
    public void initLoad(){
        isInitLoad = true;
        //Ocultar paneles de carga
        changeVisibilityLoad(false);
        //Cargar el fragment con la competición marcada como activa
        checkFragmentCurrent();
        //Cargar el historial de competiciones en el fragmento del perfil
        profF.loadRecordCompetition(competitionsList);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ACTIVAR / DESACTIVAR LA VISTA DE CARGA DE LOS 3 FRAGMENTOS DE LA APLICACION
     * @param status
     */
    public void changeVisibilityLoad(boolean status){
        prinF.visibilityLyLoad(status);
        compF.visibilityLyLoad(status);
        profF.visibilityLyLoad(status);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA COMPROBAR SI UNA COMPETICION HA FINALIZADO Y SE MUESTRAN LOS RESULTADOS
     * @param id
     * @return
     */
    public boolean competitionFinish(int id){
        for(int i=0; i<competitionsList.size();i++){
            if(competitionsList.get(i).getId() == id){
                if(competitionsList.get(i).getRes()==1){
                   return true;
                }
                return false;
            }
        }
        return false;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OCULTAR LOS BOTONES DE TOOLBAR
     */
    public void hideToolbarButtons(){
        ivInfoGeneral.setVisibility(View.GONE);
        ivLogout.setVisibility(View.GONE);
        ivInfoRanking.setVisibility(View.GONE);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO QUE SE EJECUTA AL PASAR APP A SEGUNDO PLANO (APAGAR O CAMBIAR DE APLICACION)
     * INICIAMOS CONTADOR QUE CERRARÁ LA APLICACIÓN SI PASA MAS DE 20 MINUTOS
     */
    @Override
    protected void onPause() {
        countOffApp = new CountDownTimer(1200000,1000) {
            @Override
            public void onTick(long millisUntilFinished) { }
            @Override
            public void onFinish() {
                //Cerrar aplicación pasados 20 min de inactividad
                finish();
            }
        }.start();

        super.onPause();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO QUE SE EJECUTA AL VOLVER DEL ESTADO DE PAUSE
     * DESACTIVAREMOS EL CONTADOR DE CIERRE POR SUSPENSION
     */
    @Override
    protected void onResume() {
        if(countOffApp!=null){
            countOffApp.cancel();
            countOffApp=null;
        }
        super.onResume();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * SOBREESCRITURA DEL METODO QUE SE ACCIONA AL PULSAR EN EL BOTON DE VOLVER.
     * CIERRA LA APLICACIÓN EN LUGAR DE VOLVER A CARGAR LA ACTIVITY DE LOGIN
     */
    @Override
    public void onBackPressed() {
        //Enviar ubicación nula
        sendLocation(null);
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
                //finish(); //Cerrar aplicacion directamente
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

                    //Si es el tutorial, cambiamos las fechas
                    if(c.getId()==1){
                        Hora hour = new Hora();
                        hour.setInicio(currentMilliseconds-1200000); //Hora actual menos 20 minutos
                        hour.setFin(currentMilliseconds+28800000); //Hora actual mas 8 horas
                        c.setHora(hour);
                    }

                    //Agregamos a la lista de competiciones
                    competitionsList.add(c);

                    //Comprobar si la competicion que ha cambiado es la que se esta mostrando en fragment para actualizar los cambios
                    //Si es la primera ejecucion no actuamos
                    if(c.getId()==showingCompeId && !initExecute) {
                        competitionShow = c; //Establecer los datos de la competicion que se está visualizando
                        prinF.setDataCompetition(c, questF, mapF,myUser); //Establecemos los datos al fragmento principal de la competicion
                        rankF.loadRanking(c, usersList); //Actualizar datos del ranking
                    }
                }

                //Establecer los nuevos valores para el fragmento del listado de competiciones
                compF.setCompetitionsList(competitionsList);
                //Cargar el historial de competiciones en el fragmento del perfil
                if(getUsers) {
                    profF.loadRecordCompetition(competitionsList);
                }

                //Obtener el ranking tras cargar estos datos y los de usuario ya que necesita ambos
                getCompetitions=true;
                if(getUsers && initExecute) {
                    chargeAll();
                }
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

                        //Si cambia el current compe id, mostrar la nueva competicion en el fragmento principal
                        if(currentCompeId!=u.getCompeActiva()){
                            currentCompeId = u.getCompeActiva();
                            checkFragmentCurrent();
                        }

                        //Obtener las competiciones en las que el usuario esta registrado
                        ArrayList<Integer> competitionsRegistered = new ArrayList<Integer>();
                        for (int i =0 ; i<competitionsList.size(); i++){
                            for (Map.Entry<String, Jugador> player : competitionsList.get(i).getJugadores().entrySet()) {
                                if(player.getKey().equals(u.getUid())){
                                    competitionsRegistered.add(competitionsList.get(i).getId());
                                }
                            }
                        }
                        u.setCompetitionsRegistered(competitionsRegistered);

                        //Guardar usuario en la variable del fragmento profile
                        profF.setMyUser(u);
                    }
                }

                //Cargar datos del ranking al recuperar los usuarios y competiciones ya que necesita ambos
                getUsers=true;
                if(getCompetitions && initExecute) {
                    chargeAll();
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

    /**
     * METODO PARA ALMACENAR LA RESPUESTA A UNA PREGUNTA EN LA BASE DE DATOS
     */
    public void sendResponseQuestion(String idQuestion, int idResponse, boolean correct){
        final String path = "competiciones/"+showingCompeId+"/jugadores/"+user.getUid()+"/preguntas/"+idQuestion;

        //Guardar datos en fichero local indicado el tipo de value 0->texto, 1->int
        final int idInserted = saveDataOnLocal(path, idResponse+"", 1);

        //Guardar datos en base de datos local
        db.getReference(path)
            .setValue(idResponse, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    //Si se ha guardado correctamente el valor, eliminamos el dato del fichero
                    if (databaseError == null) {
                        //Llamada al metodo para eliminar la linea previamente introducida
                        removeALine(idInserted);
                    }
                }
            });

        //Establecer la puntuación obtenida al resolver la pregunta
        if(correct){
            String idPointAssociated = competitionShow.getPreguntas().get(idQuestion).getIdPunto();
            sendPointScann(idPointAssociated, -1, 6); //El nivel lo establecemos en -1 para no resetear de nuevo la pregunta
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ALMACENAR EL REGISTRO DE UN CODIGO EN LA BASE DE DATOS
     */
    public void sendPointScann(String idPoint, int level, int points) {
        final String path = "competiciones/" + showingCompeId + "/jugadores/" + user.getUid() + "/puntos/" + idPoint;
        final String value = points + "-" + currentMilliseconds;

        //Guardar datos en fichero local indicado el tipo de value 0->texto, 1->int
        final int idInserted = saveDataOnLocal(path, value, 0);

        //Guardar datos en base de datos
        db.getReference(path)
            .setValue(value, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    //Si se ha guardado correctamente el valor, eliminamos el dato del fichero
                    if (databaseError == null) {
                        //Llamada al metodo para eliminar la linea previamente introducida
                        boolean correct = removeALine(idInserted);
                    }
                }
            });

        //Si es un codigo de pregunta buscar la pregunta para desbloquearla
        if(level==4){
            for (Map.Entry<String, Pregunta> quest : competitionShow.getPreguntas().entrySet()) {
                //Al encontrar la pregunta asociada al punto la desbloqueamos
                if(quest.getValue().getIdPunto().equals(idPoint)){
                    sendResponseQuestion(quest.getValue().getId(),0,false); //El 0 indica que no esta contestada
                }
            }
        //Si es la bandera final marcar la competicion como finalizada para el usuario
        }else if(level == 5){
            sendGetFlag();
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INDICAR EN BASE DE DATOS QUE SE HA ATRAPADO LA BANDERA
     * COMPETICION FINALIZADA PARA EL USUARIO
     */
    public void sendGetFlag(){
        final String path = "competiciones/" + showingCompeId + "/jugadores/" + user.getUid() + "/fin";

        //Guardar datos en fichero local para respaldar problemas de conexion indicado el tipo de value 0->texto, 1->int
        final int idInserted = saveDataOnLocal(path, "1", 1);

        //Almacenar datos en la base de datos
        db.getReference(path)
            .setValue(1, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    //Si se ha guardado correctamente el valor, eliminamos el dato del fichero local de seguridad
                    if (databaseError == null) {
                        removeALine(idInserted);
                    }
                }
            });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ENVIAR MI UBICACIÓN A LA BASE DE DATOS
     */
    public void sendLocation(Location location){
        //Comprobar si esta seleccionado el envio de ubicacion
        if(competitionShow.getUbi()==1 && location!=null){
            db.getReference("usuarios/" + user.getUid() + "/ubi/lat").setValue(location.getLatitude());
            db.getReference("usuarios/" + user.getUid() + "/ubi/lon").setValue(location.getLongitude());
        }else{
            db.getReference("usuarios/" + user.getUid() + "/ubi/lat").setValue(0);
            db.getReference("usuarios/" + user.getUid() + "/ubi/lon").setValue(0);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //              GUARDAR INFORMACION DE FORMA LOCAL COMO RESPALDO ANTE DESCONEXION             //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * METODO PARA ALMACENAR INFORMACIÓN EN FICHERO LOCAL Y NO PERDER DATOS EN CASO DE DESCONEXIÓN
     * @return Identificador del dato insertado en la tabla de datos local
     */
    public int saveDataOnLocal(String path, String value, int typeValue){

        //Declarar la base de datos que se utilizara
        AdminSQLiteOpenHelper asoh = new AdminSQLiteOpenHelper(this, "localdata", null, 1);
        //Declarar objeto para escribir/leer en la base de datos
        SQLiteDatabase dbLocal = asoh.getWritableDatabase();
        //Crear la fila de datos que vamos a agregar a la tabla de la base de datos
        ContentValues rowData = new ContentValues();
        rowData.put("path", path);
        rowData.put("value", value);
        rowData.put("typevalue", typeValue); //texto:0, int:1
        //Insertar el dato
        long id = dbLocal.insert("data", null, rowData);
        dbLocal.close();//Cerrar transaccion

        //Devolver valor id del elemento insertado
        return (int) id;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ELIMINAR UNA LINEA DEL FICHERO DE RESPALDO DE DATOS
     * @param id
     * @return
     */
    public boolean removeALine(int id){
        //Declarar la base de datos que se utilizara
        AdminSQLiteOpenHelper asoh = new AdminSQLiteOpenHelper(this, "localdata", null, 1);
        //Declarar objeto para escribir/leer en la base de datos
        SQLiteDatabase dbLocal = asoh.getWritableDatabase();

        //Eliminar dato utilizando el id, devolver booleano indicando si se ha eliminado
        if(dbLocal.delete("data","id=?",new String[]{id+""})>0){
            return true;
        }else{
            return false;
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER EL NUMERO DE ELEMENTOS RESTANTES POR RESPALDAR EN LA BASE DE DATOS
     * (ENTRADAS EXISTENTES EN LA TABLA DATA)
     */
    public int countRowsData(){
        //Obtener el numero de elementos pendientes por subir a la base de datos
        AdminSQLiteOpenHelper asoh = new AdminSQLiteOpenHelper(this, "localdata", null, 1);
        SQLiteDatabase dbLocal = asoh.getReadableDatabase();
        Cursor cursor = dbLocal.rawQuery("select  * from data", null);
        int count = cursor.getCount();
        //Cerrar conexiones
        dbLocal.close();
        cursor.close();
        //Devolver numero de elementos en la tabla data
        return count;
    }

    //----------------------------------------------------------------------------------------------

    /*
    //DEPURACION: METODO PARA VISUALIZAR DATOS DE LA TABLA PARA
    public void testFile(){
        //Declarar la base de datos que se utilizara
        AdminSQLiteOpenHelper asoh = new AdminSQLiteOpenHelper(this, "localdata", null, 1);
        //Declarar objeto para escribir/leer en la base de datos
        SQLiteDatabase bdLocal = asoh.getWritableDatabase();
        //Recorrer el contenido
        Cursor cursor = bdLocal.rawQuery("select * from data",null);
        Log.d("txt", "-------------------------");
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String path = cursor.getString(cursor.getColumnIndex("path"));
                String value = cursor.getString(cursor.getColumnIndex("value"));
                Log.d("txt", id+": "+path+" "+value);
                cursor.moveToNext();
            }
        }
        Log.d("txt", "-------------------------");
    }

    //DEPURACION: METODO PARA ELIMINAR EL CONTENIDO POR COMPLETO DE LA TABLA QUE ALMACENA DATOS SIN GUARDAR ONLINE
    public void deleteAll(){
        //Declarar la base de datos que se utilizara
        AdminSQLiteOpenHelper asoh = new AdminSQLiteOpenHelper(this, "localdata", null, 1);
        //Declarar objeto para escribir/leer en la base de datos
        SQLiteDatabase dbLocal = asoh.getWritableDatabase();
        dbLocal.execSQL("delete from data");
    }
    */

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

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER LA CLASE CORRESPONDIENTE CON EL FRAGMENTO DEL MAPA
     * @return
     */
    public MapFragment getMapF() {
        return mapF;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER LA CLASE CORRESPONDIENTE CON EL FRAGMENTO PRINCIPAL
     * @return
     */
    public PrincipalFragment getPrinF() {
        return prinF;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER LA CLASE CORRESPONDIENTE CON EL FRAGMENTO PRINCIPAL
     * @return
     */
    public RankingFragment getRankF() {
        return rankF;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER LA POSICION EN LA QUE SE ENCUENTRA EL ELEMENTO DE MI USUARIO EN EL
     * RECYCLER VIEW DEL RANKING
     * @param posMyUserRanking
     */
    public void setPosMyUserRanking(int posMyUserRanking) {
        this.posMyUserRanking = posMyUserRanking;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER LA POSICION EN LA QUE SE ENCUENTRA EL ELEMENTO DE MI USUARIO EN EL
     * RECYCLER VIEW DEL RANKING
     * @return
     */
    public int getPosMyUserRanking() {
        return posMyUserRanking;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA RECUPERAR EL FRAGMENTO ACTIVO
     * @return
     */
    public Fragment getActive() {
        return active;
    }

    //----------------------------------------------------------------------------------------------

    public int getTypeMapCompe(){
        return competitionShow.getMapa();
    }
}