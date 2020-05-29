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
    private MapFragment mapF;
    private ImageView ivBackScanner;

    //Variables
    private ZXingScannerView scanner;
    private boolean visible;



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
        mapF = ga.getMapF();
        ivBackScanner = view.findViewById(R.id.ivBackScanner);
        //Variables
        visible = false;

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
        //Si la competicion es nocturna encendemos el falsh
        if(ga.getCompetitionShow().getTipo()==1){
            scanner.setFlash(true);
        }else{
            scanner.setFlash(false);
        }
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
     * METODO QUE SE EJECUTA CUANDO SE DEJA DE MOSTRAR EL FRAGMENTO
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            visible=false;
            stopScanner(); //Detener camara
        }else{
            visible=true;
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * SOBREESCRITURA DEL METODO QUE SE EJECUTA AL SALIR BLOQUEAR DISPOSITIVO O MINIMIZAR APP
     */
    @Override
    public void onStop() {
        super.onStop();
        stopScanner(); //Detener camara
    }

    //----------------------------------------------------------------------------------------------

    /**
     * SOBREESCRITURA DEL METODO QUE SE EJECUTA AL VOLVER A MOSTRAR LA APP DESPUES DE UN ONSTOP
     */
    @Override
    public void onResume() {
        super.onResume();
        //Si el escaner estaba visible lo reabrimos
        if(visible)
            startScanner();
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
        HashMap<String, String> myPoints = ga.getCompetitionShow().getJugadores().get(ga.getMyUser().getUid()).getPuntos();
        boolean exist=false, escaned=false;
        int level=-1;
        String namePoint="";

        //Recorrer todos los puntos para comprobar si el punto escaneado existe
        for (Map.Entry<String, Punto> point : ga.getCompetitionShow().getPuntos().entrySet()) {
            Log.d("aaa", "vuelta "+point.getKey());
            if(point.getKey().equals(code)){
                exist = true;
                level= point.getValue().getNivel();
                namePoint = point.getValue().getNombre();
                //Recorrer mis puntos para comprobar que el codigo no haya sido ya escaneado
                for (Map.Entry<String, String> mPoint : myPoints.entrySet()) {
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
            int points=0;
            //Obtener puntos en funcion del nivel
            if(level==5)
                points=10;
            else if(level!=4)
                points=level;

            //Llamada al metodo para almacenarlo en la base de datos
            ga.sendPointScann(code, level, points);
            mapF.alertDialogScannedPoint(namePoint, level, false); //Mostrar dialog de confirmacion de escaneo
            //Volver al mapa
            ga.showMapFragment();

        }else if(exist && escaned) {
            //EL PUNTO YA ESTA REGISTRADO
            mapF.alertDialogScannedPoint(namePoint, level, true); //Mostrar dialog indicando que esta escaneado
            //Volver al mapa
            ga.showMapFragment();

        }else{
            //EL PUNTO NO EXISTE
            startScanner();
        }


    }
}