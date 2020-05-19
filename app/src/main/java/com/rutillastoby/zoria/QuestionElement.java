package com.rutillastoby.zoria;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rutillastoby.zoria.dao.competicion.Pregunta;

import java.util.ArrayList;

public class QuestionElement extends RecyclerView.Adapter<QuestionElement.QuestionInstance>{

    private ArrayList<Pregunta> questionList;
    private QuestionsFragment context;
    private boolean lockButtonSend;

    /**
     * CONSTRUCTOR POR DEFECTO
     */
    public QuestionElement(ArrayList<Pregunta> questionList, QuestionsFragment context, boolean lockButtonSend){
        this.questionList = questionList;
        this.context = context;
        this.lockButtonSend = lockButtonSend;
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
     * @param position Posicion del elemento dentro del listado
     */
    @Override
    public void onBindViewHolder(@NonNull final QuestionInstance instance, int position) {
        final int i = position;
        //0. Estado inicial, ninguna respuesta seleccionada
        questionList.get(i).setRespSelect(-1);
        resetResponse(instance);

        //1. Obtener los datos del ArrayList y establecerlos al elemento
        instance.tvQuestionText.setText(questionList.get(i).getTexto());
        instance.tvResp1.setText(questionList.get(i).getResp().get(1));
        instance.tvResp2.setText(questionList.get(i).getResp().get(2));
        //Agregar respuestas
        int size = questionList.get(i).getResp().size()-1;
        //El -1 es debebido a que las respuestas se recuperan con el primer valor a null ya que el primer id es 1 y no 0
        switch (size){
            case 4:
                instance.divider4.setVisibility(View.VISIBLE);
                instance.tvResp4.setVisibility(View.VISIBLE);
                instance.tvResp4.setText(questionList.get(i).getResp().get(4));
            case 3:
                instance.divider3.setVisibility(View.VISIBLE);
                instance.tvResp3.setVisibility(View.VISIBLE);
                instance.tvResp3.setText(questionList.get(i).getResp().get(3));
                break;
        }

        //2. Comprobar si el elemento de pregunta se encuentra abierto (las opciones de respuesta se muestran)
        if(questionList.get(i).isViewRespOpen())
            instance.lyResponseQuestion.setVisibility(View.VISIBLE);
        else
            instance.lyResponseQuestion.setVisibility(View.GONE);

        //3. Establecer funcionalidad al hacer clic en el propio elemento
        instance.lyTitleQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Comprobar si la respuestas están visibles
                if(questionList.get(i).isViewRespOpen()){
                    //CERRAR RESPUESTA

                    //Ocultar todas las respuestas de la preguntas
                    for(int j=0; j<questionList.size();j++){
                        questionList.get(j).setViewRespOpen(false);
                    }
                }else{
                    //ABRIR RESPUESTA

                    //Ocultar todas las respuestas de la preguntas
                    for(int j=0; j<questionList.size();j++){
                        questionList.get(j).setViewRespOpen(false);
                    }
                    //Marcar el elemento para mostrar respuestas de la pregunta pulsada
                    questionList.get(i).setViewRespOpen(true);
                }

                //Indicar que se han producido cambios en los elemetos para recargar la vista
                notifyDataSetChanged();
            }
        });

        //4. Establecer funcionalidad al hacer click en una respuesta
        View.OnClickListener clickResp = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Desmarcar todos
                resetResponse(instance);
                //Llamada al metodo
                clickResponse((TextView) v, questionList.get(i));
            }
        };
        //Establecer los escuchadores
        instance.tvResp1.setOnClickListener(clickResp);
        instance.tvResp2.setOnClickListener(clickResp);
        instance.tvResp3.setOnClickListener(clickResp);
        instance.tvResp4.setOnClickListener(clickResp);

        //5. Dar funcionalidad al boton de aceptar para enviar la respuesta
        instance.bAceptResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("aaa", "enviarrrrrr");
                //Comprobar que se ha seleccionado una respuesta
                if(questionList.get(i).getRespSelect()!=-1) {
                    //Obtener el texto de la respuesta seleccionada
                    final int idResp = questionList.get(i).getRespSelect();
                    String txtResp = questionList.get(i).getResp().get(idResp);
                    //Mostrar ventana de confirmacion
                    AlertDialog.Builder builder = new AlertDialog.Builder(context.getContext());
                    builder.setCancelable(true);
                    builder.setTitle(context.getResources().getText(R.string.sendResponse));
                    builder.setMessage("Se enviará '" + txtResp + "' como respuesta.");
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean correct; //Variable para indicar si la respuesta es correcta
                            //Mostrar mensaje segun si la respuesta es correcta o no
                            if(questionList.get(i).getSolu()==idResp) {
                                GenericFuntions.snack(context.getView(), "Respuesta Correcta :)");
                                correct=true;
                            }else{
                                GenericFuntions.errorSnack(context.getView(), "Respuesta Incorrecta", context.getContext());
                                correct=false;
                            }

                            //Enviar respuesta
                            ((GeneralActivity)context.getActivity()).sendResponseQuestion(questionList.get(i).getId(),idResp, correct);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    GenericFuntions.errorSnack(context.getView(), "No se ha seleccionado ninguna respuesta", context.getContext());
                }
            }
        });

        //6. En funcion del estado de respuesta de la pregunta actuaremos
        hideAllStatus(instance);
        if(questionList.get(i).getResponseSend()==0){
            //SIN CONTESTAR
            instance.bAceptResponse.setVisibility(View.VISIBLE);
            instance.ivEmptyResponse.setVisibility(View.VISIBLE);
        }else {
            if(questionList.get(i).getSolu()==questionList.get(i).getResponseSend()){
                //CORRECTA
                instance.ivCorrectResponse.setVisibility(View.VISIBLE);
            }else{
                //INCORRECTA
                instance.ivErrorResponse.setVisibility(View.VISIBLE);
            }
            //Ocultar boton aceptar enviar respuesta
            instance.bAceptResponse.setVisibility(View.GONE);
            //Desactivar escucha clic opciones de respuesta
            instance.tvResp1.setOnClickListener(null); instance.tvResp2.setOnClickListener(null);
            instance.tvResp3.setOnClickListener(null); instance.tvResp4.setOnClickListener(null);
            //Cambiar colores de lasrespuestas segun si es correcta o incorrecta
            checkResponses(instance, questionList.get(i).getSolu(), questionList.get(i).getResponseSend());
        }

        //7. Deshabilitar el boton de enviar respuesta si así se ha indicado en el constructor
        if(lockButtonSend){
            instance.bAceptResponse.setEnabled(false);
            instance.bAceptResponse.setBackground(context.getResources().getDrawable(R.drawable.button_b));
            //Desactivar seleccion de opciones
            instance.tvResp1.setOnClickListener(null); instance.tvResp2.setOnClickListener(null);
            instance.tvResp3.setOnClickListener(null); instance.tvResp4.setOnClickListener(null);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OCULTAR LOS ICONOS DE ESTADO DE LAS APLICACIONES
     */
    public void hideAllStatus(QuestionInstance instance){
        instance.ivCorrectResponse.setVisibility(View.INVISIBLE);
        instance.ivEmptyResponse.setVisibility(View.INVISIBLE);
        instance.ivErrorResponse.setVisibility(View.INVISIBLE);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MARCAR LAS OPCIONES DE RESPUESTA SEGUN SI ES CORRECTA O INCORRECTA
     */
    public void checkResponses(QuestionInstance instance, int correct, int marked){
        //Marcar primero el error para sobreescribir con la opcion correcta si coinciden
        int colorError = Color.rgb(216,37,37);
        switch (marked){
            case 1: instance.tvResp1.setBackgroundColor(colorError);
                    instance.tvResp1.setTextColor(Color.WHITE);
                    break;
            case 2: instance.tvResp2.setBackgroundColor(colorError);
                    instance.tvResp2.setTextColor(Color.WHITE);
                    break;
            case 3: instance.tvResp3.setBackgroundColor(colorError);
                    instance.tvResp3.setTextColor(Color.WHITE);
                    break;
            case 4: instance.tvResp4.setBackgroundColor(colorError);
                    instance.tvResp4.setTextColor(Color.WHITE);
                    break;
        }
        int colorCorrect = Color.rgb(119,170,23);
        switch (correct){
            case 1: instance.tvResp1.setBackgroundColor(colorCorrect);
                    instance.tvResp1.setTextColor(Color.WHITE);
                    break;
            case 2: instance.tvResp2.setBackgroundColor(colorCorrect);
                    instance.tvResp2.setTextColor(Color.WHITE);
                    break;
            case 3: instance.tvResp3.setBackgroundColor(colorCorrect);
                    instance.tvResp3.setTextColor(Color.WHITE);
                    break;
            case 4: instance.tvResp4.setBackgroundColor(colorCorrect);
                    instance.tvResp4.setTextColor(Color.WHITE);
                    break;
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA RESETEAR EL INDICADOR DE SELECCION DE LAS RESPUESTAS
     * @param instance
     */
    public void resetResponse(QuestionInstance instance){
        instance.tvResp1.setTextColor(Color.GRAY); instance.tvResp1.setBackgroundColor(Color.TRANSPARENT);
        instance.tvResp2.setTextColor(Color.GRAY); instance.tvResp2.setBackgroundColor(Color.TRANSPARENT);
        instance.tvResp3.setTextColor(Color.GRAY); instance.tvResp3.setBackgroundColor(Color.TRANSPARENT);
        instance.tvResp4.setTextColor(Color.GRAY); instance.tvResp4.setBackgroundColor(Color.TRANSPARENT);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA MARCAR LA OPCION DE RESPUESTA A LA PREGUNTA AL PULSAR SOBRE ELLA
     * @param tvResp
     * @param p
     */
    public void clickResponse(TextView tvResp, Pregunta p){
        //Obtener el id de la respuesta pulsada
        int id=-1;
        switch (tvResp.getId()){
            case R.id.tvResp1: id=1; break;
            case R.id.tvResp2: id=2; break;
            case R.id.tvResp3: id=3; break;
            case R.id.tvResp4: id=4; break;
        }

        //Marcar o desmarcar la opcion
        if(id==p.getRespSelect()){
            //DESMARCAMOS LA OPCION
            p.setRespSelect(-1);
        }else{
            //MARCAMOS LA OPCION
            tvResp.setTextColor(Color.BLACK);
            tvResp.setBackgroundColor(Color.rgb(205, 205, 205));
            //Marcar la respuesta seleccionada
            p.setRespSelect(id);
        }
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
        protected TextView tvQuestionText, tvResp1, tvResp2, tvResp3, tvResp4;
        protected ConstraintLayout lyResponseQuestion, lyQuestionElement, lyTitleQuestion;
        protected View divider3, divider4;
        protected Button bAceptResponse;
        protected ImageView ivEmptyResponse, ivCorrectResponse, ivErrorResponse;

        public QuestionInstance(@NonNull View itemView) {
            super(itemView);
            //Referencias
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            lyResponseQuestion = itemView.findViewById(R.id.lyResponseQuestion);
            lyQuestionElement = itemView.findViewById(R.id.lyQuestionElement);
            lyTitleQuestion = itemView.findViewById(R.id.lyTitleQuestion);
            tvResp1 = itemView.findViewById(R.id.tvResp1);
            tvResp2 = itemView.findViewById(R.id.tvResp2);
            tvResp3 = itemView.findViewById(R.id.tvResp3);
            tvResp4 = itemView.findViewById(R.id.tvResp4);
            divider3 = itemView.findViewById(R.id.divider3);
            divider4 = itemView.findViewById(R.id.divider4);
            bAceptResponse = itemView.findViewById(R.id.bAceptResponse);
            ivErrorResponse = itemView.findViewById(R.id.ivErrorResponse);
            ivCorrectResponse = itemView.findViewById(R.id.ivCorrectResponse);
            ivEmptyResponse = itemView.findViewById(R.id.ivEmptyResponse);

            //Estado inicial
            divider3.setVisibility(View.GONE);
            divider4.setVisibility(View.GONE);
            tvResp3.setVisibility(View.GONE);
            tvResp4.setVisibility(View.GONE);
        }
    }
}