package com.rutillastoby.zoria.ui.principal;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.rutillastoby.zoria.GeneralActivity;
import com.rutillastoby.zoria.MapFragment;
import com.rutillastoby.zoria.QuestionsFragment;
import com.rutillastoby.zoria.R;
import com.rutillastoby.zoria.dao.CompeticionDao;
import com.rutillastoby.zoria.dao.UsuarioDao;
import com.rutillastoby.zoria.dao.competicion.Jugador;

import java.util.Map;

public class PrincipalFragment extends Fragment {
    //Referencias
    private TextView tvTitlePrincipalCompe, tvSecPrin, tvHourMinPrin, tvHourMinToStart, tvSecToStart,
                     tvL1PointsPrin, tvL2PointsPrin, tvL3PointsPrin, tvL4PointsPrin, tvTotalPointsPrin,
                     tvFinishCompetitionPrin, tvNameToFinishCompe;
    private ConstraintLayout lyInProgress, lyToStart, bMapPrin, bQuestionPrin, lyRankingPrin, lyFinishPrin,
                             lyLoadPrin, lyNotRegisPrin, lyClockCurrent;
    private LinearLayout lyShowRankingPrin;
    private Button bAllCompetitions;
    private View dividerRanking;
    private ProgressBar pbToStart;
    private GeneralActivity ga;

    //Variables
    private CompeticionDao currentCompetition;
    private boolean userFinish;
    private final Handler handler = new Handler();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_principal, container, false);

        //Llamada al metodo para inicializar varibales
        initVar(view);
        //Llamada al metodo para verificar continuamente la hora actual y actuar sobre la competicion segun esta
        timeUpdate();

        return view;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View v){
        View view = v;

        //Referencias
        ga =  ((GeneralActivity)getActivity());
        tvTitlePrincipalCompe = view.findViewById(R.id.tvTitlePrincipalCompe);
        tvHourMinPrin = view.findViewById(R.id.tvHourMinPrin);
        tvSecPrin = view.findViewById(R.id.tvSecPrin);
        lyInProgress = view.findViewById(R.id.lyInProgress);
        lyToStart = view.findViewById(R.id.lyToStart);
        lyFinishPrin = view.findViewById(R.id.lyFinishPrin);
        lyLoadPrin = view.findViewById(R.id.lyLoadPrin);
        tvHourMinToStart = view.findViewById(R.id.tvHourMinToStart);
        tvSecToStart = view.findViewById(R.id.tvSecToStart);
        pbToStart = view.findViewById(R.id.pbToStart);
        bMapPrin = view.findViewById(R.id.bMapPrin);
        bQuestionPrin = view.findViewById(R.id.bQuestionPrin);
        lyRankingPrin = view.findViewById(R.id.lyRankingPrin);
        tvL1PointsPrin = view.findViewById(R.id.tvL1PointsPrin);
        tvL2PointsPrin = view.findViewById(R.id.tvL2PointsPrin);
        tvL3PointsPrin = view.findViewById(R.id.tvL3PointsPrin);
        tvL4PointsPrin = view.findViewById(R.id.tvL4PointsPrin);
        tvTotalPointsPrin = view.findViewById(R.id.tvTotalPointsPrin);
        lyNotRegisPrin = view.findViewById(R.id.lyNotRegisPrin);
        tvFinishCompetitionPrin = view.findViewById(R.id.tvFinishCompetitionPrin);
        lyClockCurrent = view.findViewById(R.id.lyClockCurrent);
        lyShowRankingPrin = view.findViewById(R.id.lyShowRankingPrin);
        dividerRanking = view.findViewById(R.id.dividerRanking);
        bAllCompetitions = view.findViewById(R.id.bAllCompetitions);
        tvNameToFinishCompe = view.findViewById(R.id.tvNameToFinishCompe);

        //Estado inicial
        lyLoadPrin.setVisibility(View.VISIBLE);

        //Clicks de botones

        //Boton mapa en fragmento principal
        bMapPrin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ga.showMapFragment();
            }
        });
        //Boton preguntas en fragmento principal
        bQuestionPrin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ga.showQuestionsFragment();
            }
        });
        //Boton ranking en fragmento principal
        lyRankingPrin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ga.showRankingFragment();
                ga.getRankF().autoScroll(ga.getPosMyUserRanking()); //Auto-scroll hasta mi posicion
            }
        });
        //Boton ver todas las competiciones (fragmento principal sin registrarse en competicion)
        bAllCompetitions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ga.showCompetitionsFragment();
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MOSTRAR EL PANEL INICIAL INDICANDO QUE NO SE ESTA REGISTRADO EN NINGUNA COMPETICION
     */
    public void setViewNoneCompetition() {
        //Mostrar layout correspondiente
        lyInProgress.setVisibility(View.GONE);
        lyToStart.setVisibility(View.GONE);
        lyFinishPrin.setVisibility(View.GONE);
        lyLoadPrin.setVisibility(View.GONE);
        lyNotRegisPrin.setVisibility(View.VISIBLE);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER LOS DATOS DE LA COMPETICION EN EL FRAGMENTO
     */
    public void setDataCompetition(CompeticionDao competition, QuestionsFragment questF, MapFragment mapF,
                                   UsuarioDao myUser){
        currentCompetition = competition;

        //Inicializar layouts
        lyInProgress.setVisibility(View.GONE);
        lyToStart.setVisibility(View.GONE);
        lyFinishPrin.setVisibility(View.GONE);
        lyNotRegisPrin.setVisibility(View.GONE);
        //Estado inicial vista
        tvFinishCompetitionPrin.setVisibility(View.GONE);
        lyClockCurrent.setVisibility(View.VISIBLE);
        ga.getMapF().setActiveFloatButtons(true);

        //Establecer datos
        tvTitlePrincipalCompe.setText(competition.getNombre());
        tvNameToFinishCompe.setText("COMPETICIÓN "+competition.getNombre()+" FINALIZADA.");

        //Establecer datos al fragmento de preguntas para crear listado con las preguntas de la competicion
        //(Indicando si el boton de enviar respuesta estará habilitado)
        System.out.println(competition.getJugadores().get(myUser.getUid()));

        questF.loadQuestions(competition.getPreguntas(), competition.getJugadores().get(myUser.getUid()).getPreguntas(),
                competition.getRes()==1);

        //Establecer datos al fragmento del mapa
        mapF.loadPoints(competition.getPuntos(), competition.getJugadores().get(myUser.getUid()).getPuntos());
        mapF.changeProperties(competition.getMapa());

        //Comprobar si el jugador ha atrapado la bandera (Competicion finalizada para el)
        userFinish = false;
        for (Map.Entry<String, Jugador> player : competition.getJugadores().entrySet()) {
            if(myUser.getUid().equals(player.getKey()) && player.getValue().getFin()==1){
                userFinish=true;
            }
        }

        //Actualizar la vista
        updateView();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * VERIFICAR CONTINUAMENTE A TRAVES DE UN HILO LA HORA ACTUAL PARA APLICAR CAMBIOS EN LA VISTA
     */
    private void timeUpdate(){

        //Creacion del hilo
        final Runnable r = new Runnable() {
            public void run() {

                //Actualizar la vista
                updateView();

                //Actualizacion cada 500ms
                handler.postDelayed(this, 500);
            }
        };

        handler.postDelayed(r, 500);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * APLICAR CAMBIOS EN LAS VISTAS TENIENDO EN CUENTA EL ESTADO DE LA COMPETICION Y LA HORA ACTUAL
     */
    public void updateView(){
        //Hora actual
        long currentTime =System.currentTimeMillis();

        if(currentCompetition!=null){

            //Estado inicial
            lyInProgress.setVisibility(View.GONE);
            lyFinishPrin.setVisibility(View.GONE);
            lyToStart.setVisibility(View.GONE);

            //COMPETICION FINALIZADA
            if (currentTime >= currentCompetition.getHora().getFin() || userFinish) {

                //Competicion en estado de visualizacion de resultados (FINALIZADA y ENTREGADOS PREMIOS)
                if(currentCompetition.getRes()==1){
                    tvFinishCompetitionPrin.setVisibility(View.VISIBLE); //Mostrar mensaje
                    lyClockCurrent.setVisibility(View.GONE); //Ocultar marcador de cuenta atras
                    ga.getMapF().setActiveFloatButtons(false); //Deshabilitar boton de escaneo
                    lyInProgress.setVisibility(View.VISIBLE); //Reutilizamos la vista

                //Competicion esperando resultados
                }else{
                    lyFinishPrin.setVisibility(View.VISIBLE);
                }

                //Cerrar el fragmento si alguno de la competicion esta activo
                if(ga.getActive() == ga.getQuestF() || ga.getActive() == ga.getRankF()
                    || ga.getActive() == ga.getMapF() ||ga.getActive() == ga.getScanF() ){
                    ga.returnToPrincFrag();
                }

            //COMPETICION SIN COMENZAR
            } else if (currentTime < currentCompetition.getHora().getInicio()) {
                System.out.println("sin comenzar");
                //Calcular tiempo restante para inicio
                long remainingTime = currentCompetition.getHora().getInicio() - currentTime;
                updateCountToStart(remainingTime);
                lyToStart.setVisibility(View.VISIBLE);

            //COMPETICION EN CURSO
            } else {
                System.out.println("en curso");
                //Tiempo restante para final
                long remainingTime = currentCompetition.getHora().getFin() - currentTime;
                updateCount(remainingTime);
                lyInProgress.setVisibility(View.VISIBLE);
                tvFinishCompetitionPrin.setVisibility(View.GONE); //Mostrar mensaje
                lyClockCurrent.setVisibility(View.VISIBLE); //Ocultar marcador de cuenta atras

                //Cerrar la clasificación 30 minutos antes de que finalice la competicion
                if (remainingTime < 4000) {
                    lyRankingPrin.setEnabled(false);
                    dividerRanking.setVisibility(View.GONE);
                    lyShowRankingPrin.setVisibility(View.GONE);
                    if (ga.getActive()== ga.getRankF()) { //Si esta mostrandose el fragmento lo ocultamos
                        ga.returnToPrincFrag();
                    }
                } else {
                    lyRankingPrin.setEnabled(true);
                    dividerRanking.setVisibility(View.VISIBLE);
                    lyShowRankingPrin.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ACTUALIZAR EL CONTADOR REGRESIVO DE TIEMPO DE LA VISTA DE COMPETICION EN CURSO
     * @param time
     */
    public void updateCount(long time){
        int seconds = (int) (time / 1000) % 60 ;
        int minutes = (int) ((time / (1000*60)) % 60);
        int hours   = (int) ((time / (1000*60*60)) % 24);
        tvHourMinPrin.setText(String.format("%02d", hours)+":"+String.format("%02d", minutes));
        tvSecPrin.setText(String.format("%02d", seconds));
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ACTUALIZAR EL CONTADOR REGRESIVO DE TIEMPO DE LA VISTA DE COMPETICION ANTES DE EMPEZAR
     * @param time
     */
    public void updateCountToStart(long time){
        int seconds = (int) (time / 1000) % 60 ;
        int minutes = (int) ((time / (1000*60)) % 60);
        int hours   = (int) ((time / (1000*60*60)) % 24);
        //Actualizar texto
        tvHourMinToStart.setText(String.format("%02d", hours)+":"+String.format("%02d", minutes));
        tvSecToStart.setText(String.format("%02d", seconds));
        //Actualizar progresbar
        pbToStart.setProgress(seconds);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER EL MARCADOR DE PUNTOS PROPIO DE LA VISTA PRINCIPAL
     */
    public void setPointMarker(int total, int l1, int l2, int l3, int l4){
        tvL1PointsPrin.setText("x "+l1);
        tvL2PointsPrin.setText("x "+l2);
        tvL3PointsPrin.setText("x "+l3);
        tvL4PointsPrin.setText("x "+l4);
        tvTotalPointsPrin.setText(total+"");
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER VISIBILIDAD DEL PANEL DE CARGA DEL FRAGMENTO
     */
    public void visibilityLyLoad(boolean status) {
        if(status){
            lyLoadPrin.setVisibility(View.VISIBLE);
        }else {
            lyLoadPrin.setVisibility(View.GONE);
        }
    }
}
