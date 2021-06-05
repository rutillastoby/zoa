package com.rutillastoby.zoria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rutillastoby.zoria.dao.competicion.Pregunta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestionsFragment extends Fragment{
    //Referencias
    private RecyclerView rvQuestions;
    private GeneralActivity ga;
    private ImageView ivBackQuestions;
    private ConstraintLayout lyNotQuestions;

    //Variables
    private RecyclerView.Adapter adapter; //Crear un contenedor de vistas de cada pregunta
    private QuestionsFragment thisClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questions, container, false);

        //Inicializar variables
        initVar(view);

        return view;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES DEL FRAGMENTO
     * @param view
     */
    private void initVar(View view){
        thisClass = this;
        //Referencias
        ga =  ((GeneralActivity)getActivity());
        rvQuestions = view.findViewById(R.id.rvQuestions);
        ivBackQuestions = view.findViewById(R.id.ivBackQuestions);
        lyNotQuestions = view.findViewById(R.id.lyNotQuestions);

        //Boton para volver a la vista de la competicion
        ivBackQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ga.returnToPrincFrag();
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CARGAR EL LISTADO DE PREGUNTAS EN LA VISTA DEL FRAGMENTO
     */
    public void loadQuestions(HashMap<String, Pregunta> questionsList, HashMap<String, Integer> myQuestions, boolean lockSendButton){
        //Establecer el listado de preguntas disponibles
        ArrayList<Pregunta> questionsListAvailable = new ArrayList<Pregunta>(); //Listado de preguntas disponibles para el usuario

        //Recorrer el listado completo de preguntas y comprobar cuales de ellas estan disponibles para el usuario
        for (Map.Entry<String, Pregunta> quest : questionsList.entrySet()) {
            //Agregar id al objeto de tipo pregunta para usarlo en el recyclerView
            quest.getValue().setId(quest.getKey());
            for (Map.Entry<String, Integer> myQuest : myQuestions.entrySet()) {
                //Si la pregunta está entre las desbloqueadas la agregamos al listado
                if(myQuest.getKey().equals(quest.getKey())){
                    //Establecemos la contestacion a la pregunta, 0 si no se ha contestado aun.
                    quest.getValue().setResponseSend(myQuest.getValue());
                    questionsListAvailable.add(quest.getValue());
                }
            }
        }

        //Mostrar panel sin respuestas si no hay ninguna disponible
        if(questionsListAvailable.size()==0){
            lyNotQuestions.setVisibility(View.VISIBLE);
            rvQuestions.setVisibility(View.GONE);
        }else{
            lyNotQuestions.setVisibility(View.GONE);
            rvQuestions.setVisibility(View.VISIBLE);
        }

        //Asignar listado al recyclerview
        adapter = new QuestionElement(questionsListAvailable, thisClass, lockSendButton);
        rvQuestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvQuestions.setAdapter(adapter);
    }
}