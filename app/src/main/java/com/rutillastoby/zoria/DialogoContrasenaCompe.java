package com.rutillastoby.zoria;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

public class DialogoContrasenaCompe extends DialogFragment implements DialogInterface.OnClickListener {
    //Variables
    protected TextInputEditText etContrasena;
    private static int identificador;
    onDialogoRecord miListener;


    /**
     * CONSTRUCTOR NEWINSTANCE
     * @return
     */
    public static DialogoContrasenaCompe newInstance(int id){
        identificador = id;
        return new DialogoContrasenaCompe();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * SOBRESCRITURA DEL METODO OnCreateDialog
     * @param saveInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState){
        //Declarar el cuadro de dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Obtener inflador del layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogo_contrasena_compe, null);

        //Crear el cuadro de dialogo
        builder.setView(view) //establecer el layout personalizado
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(true)
                .setTitle("Indica la contraseña para acceder a la competicion");
        return builder.create();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * INTERFAZ PARA OBLIGAR A IMPLEMENTAR EL METODO
     */
    public interface onDialogoRecord{
        public void onAceptarDialogo(int pwd, int id);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * SOBRESCITRURA DEL METODO ONATTACH
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity=null;
        if(context instanceof Activity) {
            activity =(Activity)context;
        }
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            miListener = (onDialogoRecord) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement onDialogoRecord");
        }
    }


    //----------------------------------------------------------------------------------------------

    /**
     * SOBRESCRITURA DEL METODO ONCLICK
     * DECIDIR QUE HACER AL PULSAR ACEPTAR
     * @param dialog
     * @param which
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        int respuesta=-1;
        //Referencias
        etContrasena= (TextInputEditText)((Dialog)dialog).findViewById(R.id.etPwdDCC);
        if(etContrasena.getText().length()!=0){
            respuesta = Integer.parseInt(etContrasena.getText().toString());
        }
        //Lanzar el metodo para el oyente
        miListener.onAceptarDialogo(respuesta, identificador);
    }
}