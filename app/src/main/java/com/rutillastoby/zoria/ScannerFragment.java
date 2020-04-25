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
        Log.d("aaa", rawResult.getText());
        stopScanner();
    }
}