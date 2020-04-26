package com.rutillastoby.zoria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    //Variables
    private RecyclerView.Adapter adapter; //Crear un contenedor de vistas de cada competicion
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
        rvQuestions = view.findViewById(R.id.rvQuestions);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CARGAR EL LISTADO DE PREGUNTAS EN LA VISTA DEL FRAGMENTO
     */
    public void loadQuestions(ArrayList<Pregunta> questionsList, HashMap<String, Integer> myQuestions){
        //Establecer los valores del listado

        ArrayList<Pregunta> questionsListAvailable = new ArrayList<Pregunta>(); //Listado de preguntas disponibles para el usuario

        //Recorrer el listado completo de preguntas y comprobar cuales de ellas estan disponibles para el usuario
        for(int i=0; i<questionsList.size();i++){
            for (Map.Entry<String, Integer> entry : myQuestions.entrySet()) {
                //Si la pregunta está entre las desbloqueadas la agregamos al listado
                if(entry.getKey().equals(questionsList.get(i).getId())){
                    //Establecemos la contestacion a la pregunta, 0 si no se ha contestado aun.
                    questionsList.get(i).setResponseSend(entry.getValue());
                    questionsListAvailable.add(questionsList.get(i));
                }
            }
        }

        //Asignar listado al recyclerview
        adapter = new QuestionElement(questionsListAvailable, thisClass);
        rvQuestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvQuestions.setAdapter(adapter);
    }

    //----------------------------------------------------------------------------------------------
}
