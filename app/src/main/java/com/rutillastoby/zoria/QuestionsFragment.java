package com.rutillastoby.zoria;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rutillastoby.zoria.dao.competicion.Pregunta;

import java.util.ArrayList;

public class QuestionsFragment extends Fragment{
    //Referencias
    private RecyclerView rvQuestions;
    //Variables
    private ArrayList<Pregunta> questionsList;
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
    public void loadQuestions(){
        //Asignar listado al recyclerview
        adapter = new QuestionElement(questionsList, thisClass);
        rvQuestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvQuestions.setAdapter(adapter);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER LOS VALORES DEL LISTADO DE PREGUNTAS ASOCIADO A LA COMPETICION
     * @param questionsList
     */
    public void setQuestionsList(ArrayList<Pregunta> questionsList) {
        this.questionsList = questionsList;
        loadQuestions();
        Log.d("aaa", questionsList+" ss");
    }
}
