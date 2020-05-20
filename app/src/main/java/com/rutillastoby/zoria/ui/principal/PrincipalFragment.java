package com.rutillastoby.zoria.ui.principal;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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
    CountDownTimer countCompe=null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_principal, container, false);

        //Llamada al metodo para
        initVar(view);

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
    public void setViewNotRegister() {
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
        questF.loadQuestions(competition.getPreguntas(), competition.getJugadores().get(myUser.getUid()).getPreguntas(),
                competition.getRes()==1);

        //Establecer datos al fragmento del mapa
        mapF.loadPoints(competition.getPuntos(), competition.getJugadores().get(myUser.getUid()).getPuntos());

        //Comprobar si el jugador ha atrapado la bandera
        boolean userFinish=false;
        for (Map.Entry<String, Jugador> player : competition.getJugadores().entrySet()) {
            if(myUser.getUid().equals(player.getKey()) && player.getValue().getFin()==1){
                userFinish=true;
            }
        }
        Log.d("nnn","pues aqui0");
        //Comprobar si la partida ha finalizado para el usuario (Atrapada la bandera)
        if(userFinish && competition.getRes()==0) {
            //Mostrar panel de final de competicion
            lyFinishPrin.setVisibility(View.VISIBLE);

        //Comprobar si la competición está en estado de visualización de resultados (FINALIZADA y ENTREGADOS PREMIOS)
        }else if(competition.getRes()==1){
            //Visualizar resultados competición sin poder hacer cambios (registrar puntos/Contestar preguntas)
            tvFinishCompetitionPrin.setVisibility(View.VISIBLE); //Mostrar mensaje
            lyClockCurrent.setVisibility(View.GONE); //Ocultar marcador
            ga.getMapF().setActiveFloatButtons(false); //Deshabilitar boton de escaneo
            lyInProgress.setVisibility(View.VISIBLE); //Reutilizamos la vista

        }else {
            //Llamada al metodo para actuar en funcion de la hora actual y la de la competicion
            checkTime(competition.getHora().getInicio(), competition.getHora().getFin());
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA COMPARAR LA HORA ACTUAL CON LA DE INICIO/FIN DE LA
     * COMPETICION Y ACTUAR EN FUNCION DE ELLO
     */
    private void checkTime(final long startTime, final long finishTime){
        long currentTime = ga.getCurrentMilliseconds();
        Log.d("aaa", "curr "+currentTime);
        Log.d("aaa", "fin "+finishTime);
        //Comprobar si no ha comenzado, si ha finalizado o si esta en curso
        if(currentTime>=finishTime){
            //COMPETICION POR TIEMPO FINALIZADA
            Log.d("aaa", "aaaaquiiiiii1");
            //Mostrar panel de finalizacion
            lyFinishPrin.setVisibility(View.VISIBLE);
            //Mostrar fragment principal
            ga.showPrincActivityNotChange();

        }else{
            //COMPETICION NO INICIADA O EN CURSO
            Log.d("aaa", "alliii");
            //0. Saber si ha comenzado o si esta en curso
            final int statusCompe = currentTime<startTime? 0:1; //0->No iniciada | 1->Iniciada
            //1. Resetear valores
            if(countCompe!=null) countCompe.cancel();
            //2. Calcular tiempo restante
            final long remainingTime;
            if(statusCompe==0) {
                Log.d("aaa", "aqui");
                //Competicion no iniciada
                remainingTime = startTime - currentTime;//Tiempo restante
                updateCountToStart(remainingTime); //Llamada inicial para establecer valores
                lyToStart.setVisibility(View.VISIBLE);
            }else {
                //Competicion en curso
                remainingTime = finishTime - currentTime;//Tiempo restante
                updateCount(remainingTime); //Llamada inicial para establecer valores
                lyInProgress.setVisibility(View.VISIBLE);
            }
            //3.Iniciar contador regresivo
            countCompe = new CountDownTimer(remainingTime,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(statusCompe==0) {
                        //Competicion no iniciada
                        updateCountToStart(millisUntilFinished);
                    }else{
                        //Competicion en curso
                        updateCount(millisUntilFinished);

                        //Cerrar la clasificación 30 minutos antes de que finalice la competicion
                        if (millisUntilFinished<1800000){
                            lyRankingPrin.setEnabled(false);
                            dividerRanking.setVisibility(View.GONE);
                            lyShowRankingPrin.setVisibility(View.GONE);
                            if(ga.getActive().getTag().equals("7")){ //Si esta mostrandose el fragmento lo ocultamos
                                ga.showPrincActivityNotChange();
                            }
                        }else{
                            lyRankingPrin.setEnabled(true);
                            dividerRanking.setVisibility(View.VISIBLE);
                            lyShowRankingPrin.setVisibility(View.VISIBLE);
                        }
                    }
                }
                @Override
                public void onFinish() {
                    //Resetear layout
                    lyInProgress.setVisibility(View.GONE);
                    lyToStart.setVisibility(View.GONE);
                    //Rellamar a la funcion para actuar
                    checkTime(startTime, finishTime);
                }
            }.start();
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
