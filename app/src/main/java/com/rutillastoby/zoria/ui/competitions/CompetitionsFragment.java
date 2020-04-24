package com.rutillastoby.zoria.ui.competitions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.rutillastoby.zoria.CompetitionElement;
import com.rutillastoby.zoria.GeneralActivity;
import com.rutillastoby.zoria.GenericFuntions;
import com.rutillastoby.zoria.R;
import com.rutillastoby.zoria.dao.CompeticionDao;

import java.util.ArrayList;
import java.util.Collections;

public class CompetitionsFragment extends Fragment{

    //Referencias
    private RecyclerView rvCompetitions;

    //Variables
    private ArrayList<CompeticionDao> competitionsList;
    private ArrayList<Integer> competitionsRegisteredList=null;
    private RecyclerView.Adapter adapter; //Crear un contenedor de vistas de cada competicion
    private CompetitionsFragment thisClass;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_competitions, container, false);
        //Iniciar variables
        initVar(view);
        return view;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View view){
        thisClass = this;

        //Referencias
        rvCompetitions = view.findViewById(R.id.rvCompetitions);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CARGAR INICIALMENTE TODAS LAS COMPETICIONES DE LA BASE DE DATOS
     */
    private void loadCompetitions(){
        //Ordenar listado poniendo el tutorial en la primera posicion
        Collections.reverse(competitionsList);
        competitionsList.add(0, competitionsList.get(competitionsList.size()-1));
        competitionsList.remove(competitionsList.size()-1);

        //Asignar listado al recyclerview
        adapter = new CompetitionElement(competitionsList, thisClass, ((GeneralActivity)getActivity()).getCurrentMilliseconds());
        rvCompetitions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCompetitions.setAdapter(adapter);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA COMPROBAR SI SE ESTA REGISTRADO EN UNA COMPETICION
     * @param id
     */
    public void checkAccess(int id){
        //Seguridad para evitar problemas de inicialización
        if(competitionsRegisteredList!=null) {
            boolean access = false;
            //Comprobar si estamos registrados en esa competicion
            for (int i = 0; i < competitionsRegisteredList.size(); i++) {
                if (competitionsRegisteredList.get(i) == id) {
                    access = true;
                }
            }

            //Segun si tenemos accesso o no
            if (access) {
                ///////////// ACCEDER A COMPETICION ////////////////
                ((GeneralActivity)getActivity()).showMainViewCompetition(id);
            } else {
                ///////////// SOLICITAR ACCESO /////////////////
                //Solicitar Contraseña con una ventana emergente
                for (int i = 0; i < competitionsList.size(); i++) {
                    if (competitionsList.get(i).getId() == id) {
                        inputPwd(competitionsList.get(i).getPwd(), id);
                    }
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA SOLICITAR LA CONTRASENNA DE ACCESO A UNA COMPETICION
     * @param pwd
     */
    public void inputPwd(final int pwd, int id){
        //Creación de la vantana modal con layout personalizado
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialog = inflater.inflate(R.layout.dialog_register_compe, null);
        final TextInputEditText input = dialog.findViewById(R.id.etPwdDCC);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setView(dialog)
                .setTitle(id==1? getString(R.string.titleDialogRegister)+" (0000)" : getString(R.string.titleDialogRegister))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    //Al hacer clic en aceptar
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int answer = -1;
                        if(input.getText().length()!=0){
                            answer = Integer.parseInt(input.getText().toString());
                        }

                        //Comprobar si la contrasenna introducida es correcta
                        if(answer==pwd){
                            //Marcar como competicion activa en base de datos

                            //Abrir la competicion
                            //openCompetition();
                        }else{
                            //Contrasenna incorrecta
                            GenericFuntions.errorSnack(getView(), getString(R.string.pwdFail), getContext());
                        }
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        //Mostrar ventana
        builder.show();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER EL VALOR DE LA LISTA DE COMPETICIONES
     * @param competitionsList
     */
    public void setCompetitionsList(ArrayList<CompeticionDao> competitionsList) {
        this.competitionsList = competitionsList;
        loadCompetitions();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER EL LISTADO DE COMPETICIONES A LOS QUE SE ESTÁ REGISTRADO
     * @param competitionsRegisteredList
     */
    public void setCompetitionsRegisteredList(ArrayList<Integer> competitionsRegisteredList) {
        this.competitionsRegisteredList = competitionsRegisteredList;
    }
}
