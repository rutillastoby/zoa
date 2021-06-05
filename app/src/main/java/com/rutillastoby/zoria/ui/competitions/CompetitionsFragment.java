package com.rutillastoby.zoria.ui.competitions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private ConstraintLayout lyLoadCompe;

    //Firebase
    private FirebaseDatabase db;

    //Variables
    private ArrayList<CompeticionDao> competitionsList;
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
        //Firebase
        db = FirebaseDatabase.getInstance();
        //Referencias
        rvCompetitions = view.findViewById(R.id.rvCompetitions);
        lyLoadCompe = view.findViewById(R.id.lyLoadCompe);

        //Estado inicial
        lyLoadCompe.setVisibility(View.VISIBLE);
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
        adapter = new CompetitionElement(competitionsList, thisClass, System.currentTimeMillis());
        rvCompetitions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCompetitions.setAdapter(adapter);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA COMPROBAR SI SE ESTA REGISTRADO EN UNA COMPETICION
     * @param id
     */
    public void checkAccess(int id){
        ArrayList<Integer> competitionsRegisteredList = ((GeneralActivity)getActivity()).getMyUser().getCompetitionsRegistered();

        //Seguridad para evitar problemas de inicialización
        if(competitionsRegisteredList!=null) {
            GeneralActivity ga = ((GeneralActivity)getActivity());
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

                // Si la competicion presionada es la que esta activa mostramos directamente el panel
                if(id==ga.getCurrentCompeId()){
                    ga.showFragmentCurrent();

                // Si es diferente al tutorial y la competicion no ha finalizado
                // la marcamos como competicion activa y la abrimos en seccion current
                } else if((id!=1 && !ga.competitionFinish(id))) {
                    String myUid = ((GeneralActivity) getActivity()).getMyUser().getUid();
                    db.getReference("usuarios/" + myUid + "/compeActiva").setValue(id);
                    ga.showFragmentCurrent();

                }else{
                    //En otro caso mostramos directamente la competicion en el fragmento actual sin cambiar de menu
                    ga.showMainViewCompetition(id);
                }

            } else if(!ga.competitionFinish(id)){
                ///////////// SOLICITAR ACCESO SI NO ES UNA COMPETICION FINALIZADA /////////////////

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
        final int compeId = id;
        //Creación de la vantana modal con layout personalizado
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialog = inflater.inflate(R.layout.dialog_register_compe, null);
        final TextInputEditText input = dialog.findViewById(R.id.etPwdDCC);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setView(dialog)
                .setTitle(compeId==1? getString(R.string.titleDialogRegister)+" (1234)" : getString(R.string.titleDialogRegister))
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
                            //Registrarse en competicion
                            final String myUid = ((GeneralActivity)getActivity()).getMyUser().getUid();

                            //Mostrar panel de carga
                            lyLoadCompe.setVisibility(View.VISIBLE);

                            //Entrada jugador en la competicion
                            db.getReference("competiciones/" + compeId + "/jugadores/" + myUid + "/fin").setValue(0, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        //Llamada al metodo de cargar competicion que se ejecutará al guardar todos los datos
                                        openCompetitionRegister(compeId, myUid);
                                    }
                                }
                            });

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
     * METODO PARA ABRIR LA COMPETICION EN LA QUE NOS HEMOS REGISTRADO Y MARCARLA COMO ACTIVA
     * @param id
     * @param myUid
     */
    public void openCompetitionRegister(final int id, String myUid){
        final GeneralActivity ga = ((GeneralActivity)getActivity());

        //Marcar como competicion activa si no nos estamos registrando en el tutorial de forma posterior al registro ya de una
        //competicion cualquiera (evitamos sobreescribir la competicion actual con el tutorial si el registro en este es posterior)
        if(id!=1 || ga.getCurrentCompeId()==-1) {
            db.getReference("usuarios/" + myUid + "/compeActiva").setValue(id, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        //Abrir la competicion en la ventana de competicion activa (current)
                        ga.setCurrentCompeId(id);
                        ga.showFragmentCurrent();
                        //Ocultar panel de carga al completar el registro en la nueva competicion
                        lyLoadCompe.setVisibility(View.GONE);
                    }
                }
            });
        }else {
            //En caso contrario directamente mostramos la competicion en el fragmento en el que nos encontramos
            ga.showMainViewCompetition(id);
            //Ocultar panel de carga al completar el registro en la nueva competicion
            lyLoadCompe.setVisibility(View.GONE);
        }
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
     * METODO PARA ESTABLECER VISIBILIDAD DEL PANEL DE CARGA DEL FRAGMENTO
     */
    public void visibilityLyLoad(boolean status) {
        if(status){
            lyLoadCompe.setVisibility(View.VISIBLE);
        }else {
            lyLoadCompe.setVisibility(View.GONE);
        }
    }


}
