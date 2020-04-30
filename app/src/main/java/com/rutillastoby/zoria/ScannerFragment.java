package com.rutillastoby.zoria;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.zxing.Result;
import com.rutillastoby.zoria.dao.competicion.Punto;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {
    //Referencias
    private GeneralActivity ga;
    private ImageView ivBackScanner;

    //Variables
    private ZXingScannerView scanner;



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        //Iniciar variables
        initVar(view);

        //Inicializar scanner
        scanner = (ZXingScannerView) view.findViewById(R.id.zxscanner);
        scanner.setResultHandler(this);
        scanner.setAutoFocus(true);
        scanner.setAspectTolerance(0.5f);

        return view;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View v) {
        View view = v;
        //Referencias
        ga = ((GeneralActivity) getActivity());
        ivBackScanner = view.findViewById(R.id.ivBackScanner);

        //On clicks
        ivBackScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ga.showMapFragment();
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIAR EL SCANNER
     */
    public void startScanner(){
        scanner.setResultHandler(this);
        scanner.startCamera();
        Log.d("aaa", "iniciado Scanner");
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA DETENER EL SCANNER
     */
    public void stopScanner(){
        scanner.stopCamera();
        scanner.removeAllViews();
        Log.d("aaa", "finalizado scanner");
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO QUE SE EJECUTA TRAS ESCANEAR UN CÓDIGO
     * @param rawResult
     */
    @Override
    public void handleResult(Result rawResult) {
        //Detener el scanner
        stopScanner();

        //Interpretar respuesta
        String code = rawResult.getText();
        HashMap<String, Long> myPoints = ga.getCompetitionShow().getJugadores().get(ga.getMyUser().getUid()).getPuntos();
        boolean exist=false, escaned=false;

        //Recorrer todos los puntos para comprobar si el punto escaneado existe
        for (Map.Entry<String, Punto> point : ga.getCompetitionShow().getPuntos().entrySet()) {
            Log.d("aaa", "vuelta "+point.getKey());
            if(point.getKey().equals(code)){
                exist = true;
                //Recorrer mis puntos para comprobar que el codigo no haya sido ya escaneado
                for (Map.Entry<String, Long> mPoint : myPoints.entrySet()) {
                    if(mPoint.getKey().equals(code)){
                        escaned = true;
                        break;
                    }
                }
                break;
            }
        }

        //En funcion del estado actuamos
        if(exist && !escaned){
            //REGISTRAR PUNTO
            ga.sendPointScann(code);
            Log.d("aaa", "codigo guardado");
        }else if(exist && escaned) {
            //EL PUNTO YA ESTA REGISTRADO
            Log.d("aaa", "codigo ya registrado");
        }else{
            //EL PUNTO NO EXISTE
            Log.d("aaa", "eii ese codigo no existe: "+code);
        }
    }
}