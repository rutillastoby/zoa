package com.rutillastoby.zoria.ui.principal;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.rutillastoby.zoria.GeneralActivity;
import com.rutillastoby.zoria.R;
import com.rutillastoby.zoria.dao.CompeticionDao;

public class PrincipalFragment extends Fragment {
    //Referencias
    private TextView tvTitlePrincipalCompe, tvSecPrin, tvHourMinPrin;

    //Variables
    CountDownTimer countCompe=null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_principal, container, false);

        //Llamada al metodo para
        initVar(view);

        return view;
    }

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View v){
        View view = v;

        //Referencias
        tvTitlePrincipalCompe = view.findViewById(R.id.tvTitlePrincipalCompe);
        tvHourMinPrin = view.findViewById(R.id.tvHourMinPrin);
        tvSecPrin = view.findViewById(R.id.tvSecPrin);
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
        checkTime(competition.getHora().getInicio(),competition.getHora().getFin());
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA COMPARAR LA HORA ACTUAL CON LA DE INICIO/FIN DE LA
     * COMPETICION Y ACTUAR EN FUNCION DE ELLO
     */
    private void checkTime(final long startTime, final long finishTime){
        long currentTime = ((GeneralActivity)getActivity()).getCurrentMilliseconds();

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
                remainingTime = startTime - currentTime;//Tiempo restante
            }else {
                remainingTime = finishTime - currentTime;//Tiempo restante
                updateCount(remainingTime); //Llamada inicial para establecer valores
            }
            //3.Iniciar contador regresivo
            countCompe = new CountDownTimer(remainingTime,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(statusCompe==0) {
                        //Competicion no iniciada

                    }else{
                        //Competicion en curso
                        updateCount(millisUntilFinished);
                    }
                }
                @Override
                public void onFinish() {
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
}
