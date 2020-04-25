package com.rutillastoby.zoria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rutillastoby.zoria.dao.competicion.Pregunta;

import java.util.ArrayList;

public class QuestionElement extends RecyclerView.Adapter<QuestionElement.QuestionInstance>{

    private ArrayList<Pregunta> questionList;
    private QuestionsFragment context;

    /**
     * CONSTRUCTOR POR DEFECTO
     */
    public QuestionElement(ArrayList<Pregunta> questionList, QuestionsFragment context){
        this.questionList = questionList;
        this.context = context;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CREAR LA VISTA DE CADA OBJETO PARA AGREGAR AL RECYCLERVIEW
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public QuestionInstance onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Crear la vista con el layout correspondiente a la plantilla de la pregunta
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_question, parent, false);
        //Crear objeto de tipo viewHolder de la clase interna con la vista creada anteriormente
        QuestionElement.QuestionInstance example = new QuestionElement.QuestionInstance(view);
        //Devolver el objeto de la fila creado
        return example;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CREAR LA PROPIA VISTA DEL OBJETO PARA AGREGAR AL RECYCLERVIEW
     * @param instance Vista del elemento
     * @param i Posicion del elemento dentro del listado
     */
    @Override
    public void onBindViewHolder(@NonNull QuestionInstance instance, int i) {
        //1. Obtener los datos del ArrayList y establecerlos al elemento
        instance.tvQuestionText.setText(questionList.get(i).getTexto());
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER EL NUMERO DE ELEMENTOS DEL LISTADO
     * @return
     */
    @Override
    public int getItemCount() {
        return questionList.size();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * CLASE INTERNA CON EJEMPLO DE UNA FILA DE UNA PREGUNTA
     */
    public static class QuestionInstance extends RecyclerView.ViewHolder{
        //Referencias
        public TextView tvQuestionText;

        public QuestionInstance(@NonNull View itemView) {
            super(itemView);
            //Referencias
            tvQuestionText = (TextView) itemView.findViewById(R.id.tvQuestionText);
        }
    }
}