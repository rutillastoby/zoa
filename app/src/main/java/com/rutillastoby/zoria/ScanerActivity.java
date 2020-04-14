package com.rutillastoby.zoria;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;
import com.instacart.library.truetime.TrueTime;

import java.util.ArrayList;
import java.util.Date;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView scanner;
    private ArrayList<String> puntosRegistrados;
    Button button;
    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    private CountDownTimer contador=null;
    Camera.Parameters params;
    private boolean encendida =false;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaner);
        context = this;
        setTitle("Registrar Punto AZ");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));


        //Crear objeto escanner
        scanner = (ZXingScannerView) findViewById(R.id.zxscan);
        scanner.setResultHandler(this);
        scanner.setAutoFocus(true);
        scanner.setAspectTolerance(0.5f);
        //Iniciar la camara
        scanner.startCamera();

        puntosRegistrados = getIntent().getStringArrayListExtra("registrados");
        //Cuenta atras
        tiempo(getIntent().getLongExtra("fechaFin",0 ));
    }

    @Override
    protected void onPause() {
        if(contador!=null)
            contador.cancel();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(contador!=null)
            contador.cancel();
        super.onBackPressed();
    }

    //----------------------------------------------------------------------------------------------

    public void tiempo(long fecha){
        Date fechFin = new Date();
        fechFin.setTime(fecha);
        //Calcular el tiempo restante
        long tiempoRestante = fechFin.getTime() - TrueTime.now().getTime();


        //Toast.makeText(this,""+tiempoRestante,Toast.LENGTH_SHORT).show();
        //Comenzar contador de cuenta atras
        contador = new CountDownTimer(tiempoRestante,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Toast.makeText(context, ""+millisUntilFinished, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                //Al acabar llamarse de forma recursiva para ocultar la pantalla principal
                //actuarHoras();
                finish();
            }
        }.start();
    }

    /**
     * METODO QUE SE EJECUTA AL ESCANEAR UN CODIGO DE BARRAS PARA OBTENER EL RESULTADO
     * @param resultado
     */
    @Override
    public void handleResult(Result resultado) {
        //Obtenemos el codigo leido
        String res = resultado.getText();

        //comprobar si esta ya leido
        boolean yaLeido=false;
        for(int i=0; i<puntosRegistrados.size();i++){
            if(res.equals(puntosRegistrados.get(i))){
                yaLeido=true;
            }
        }

        //Detener el scanner
        scanner.resumeCameraPreview(this);
        scanner.stopCamera();
        //Detener contador
        if(contador!=null)
            contador.cancel();

        //Devolver el codigo leido
        Intent i = new Intent();
        i.putExtra("yaLeido", yaLeido);
        i.putExtra("codigo",res);
        setResult(RESULT_OK,i);
        finish();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * SOBRESCRITURA DEL METODO ON STOP
     */
    @Override
    protected void onStop() {
        //Detener el scanner
        scanner.resumeCameraPreview(this);
        scanner.stopCamera();
        finish();
        super.onStop();
    }



    public void botonLinterna(View view){
        if(encendida){
            encendida=false;
            scanner.setFlash(false);
        }else{
            encendida=true;
            scanner.setFlash(true);
        }
    }

}
