package com.rutillastoby.zoria.ui.principal;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.rutillastoby.zoria.GeneralActivity;
import com.rutillastoby.zoria.R;
import com.rutillastoby.zoria.dao.CompeticionDao;

public class PrincipalFragment extends Fragment {
    //Referencias
    private TextView tvTitlePrincipalCompe, tvSecPrin, tvHourMinPrin, tvHourMinToStart, tvSecToStart;
    private ConstraintLayout lyInProgress, lyToStart, bMapPrin;
    private ProgressBar pbToStart;

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
        tvTitlePrincipalCompe = view.findViewById(R.id.tvTitlePrincipalCompe);
        tvHourMinPrin = view.findViewById(R.id.tvHourMinPrin);
        tvSecPrin = view.findViewById(R.id.tvSecPrin);
        lyInProgress = view.findViewById(R.id.lyInProgress);
        lyToStart = view.findViewById(R.id.lyToStart);
        tvHourMinToStart = view.findViewById(R.id.tvHourMinToStart);
        tvSecToStart = view.findViewById(R.id.tvSecToStart);
        pbToStart = view.findViewById(R.id.pbToStart);
        bMapPrin = view.findViewById(R.id.bMapPrin);

        //Clicks de botones
        bMapPrin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GeneralActivity)getActivity()).showMapFragment();
            }
        });

    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER LOS DATOS DE LA COMPETICION EN EL FRAGMENTO
     */
    public void setDataCompetition(CompeticionDao competition){
        //Obtener tiempo del servidor
        Log.d("aaa", ""+competition.getHora().getInicio());

        tvTitlePrincipalCompe.setText(competition.getNombre());

        //Llamada al metodo para actuar en funcion de la hora actual y la de la competicion
        checkTime(competition.getHora().getInicio(), competition.getHora().getFin());
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA COMPARAR LA HORA ACTUAL CON LA DE INICIO/FIN DE LA
     * COMPETICION Y ACTUAR EN FUNCION DE ELLO
     */
    private void checkTime(final long startTime, final long finishTime){
        long currentTime = ((GeneralActivity)getActivity()).getCurrentMilliseconds();
        //Inicializar layouts
        lyInProgress.setVisibility(View.GONE);
        lyToStart.setVisibility(View.GONE);


        //Comprobar si no ha comenzado, si ha finalizado o si esta en curso
        if(currentTime>finishTime){
            //COMPETICION FINALIZADA

        }else{
            //COMPETICION NO INICIADA O EN CURSO

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
        tvHourMinPrin.setText(hours+":"+String.format("%02d", minutes));
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


}
