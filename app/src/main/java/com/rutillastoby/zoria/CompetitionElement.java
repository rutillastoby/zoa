package com.rutillastoby.zoria;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rutillastoby.zoria.dao.CompeticionDao;
import com.rutillastoby.zoria.ui.competitions.CompetitionsFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CompetitionElement extends RecyclerView.Adapter<CompetitionElement.CompetitionInstance>{
    private ArrayList<CompeticionDao> competitionsList;
    CompetitionsFragment context;

    /**
     * CONSTRUCTOR PARAMETRIZADO
     */
    public CompetitionElement(ArrayList<CompeticionDao> c, CompetitionsFragment cc){
        competitionsList =c;
        context =cc;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CREAR UN OBJETO O FILA QUE ASIGNAREMOS AL RECYCLER VIEW
     * @param viewGroup
     * @param i
     * @return
     */
    @NonNull
    @Override
    public CompetitionInstance onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Crear la vista con el layout correspondiente a la plantilla de la competicion
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_competition, viewGroup, false);
        //Crear objeto de tipo viewHolder de la clase interna con la vista creada anteriormente
        CompetitionInstance example = new CompetitionInstance(view);
        //Devolver el objeto de la fila creado
        return example;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO QUE SE EJECUTA PARA CREAR LAS FILAS QUE SE VAN A MOSTRAR
     * Ya que recyclerView Elimina las filas al hacer scroll y las crea de nuevo para ahorrar memoria y así no tener
     * que crear y cargar todos los registros o filas de una vez
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull final CompetitionInstance instance, final int i) {
        //1. Obtener los datos del array y agregarlos a los elementos del elemento de competicion
        instance.tvNombre.setText(competitionsList.get(i).getNombre());
        Picasso.get().load(competitionsList.get(i).getFoto().replace("http:", "https:"))
                .error(R.drawable.portada)
                .into(instance.ivCompe);

        //2. Segun si la competicion ha finalizado o no mostramos un elemento u otro
        if(System.currentTimeMillis()<competitionsList.get(i).getHora().getFin()){
            instance.lyDateFinish.setVisibility(View.GONE); //Ocultar finalizado
            instance.lyDateAvailable.setVisibility(View.VISIBLE);

            //Establecer la fecha de la competicion
            Date now = new Date(competitionsList.get(i).getHora().getInicio());
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
            cal.setTime(now);
            instance.tvDateCompe.setText(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))+"/"+
                    (String.format("%02d", cal.get(Calendar.MONTH)+1)) +"/"+cal.get(Calendar.YEAR));


        }
        //3. Comprobar si es el tutorial para modificarlo
        if(competitionsList.get(i).getId() == 1){
            instance.clGeneralDate.setVisibility(View.GONE);
        }
        //4. Accion al presionar la competicion
        instance.layoutElemCompe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               context.checkAccess(competitionsList.get(i).getId());
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER EL NUMERO DE ELEMENTOS DEL LISTADO
     * @return
     */
    @Override
    public int getItemCount() {
        return competitionsList.size();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * CLASE INTERNA CON EJEMPLO DE UNA FILA DE UNA COMPETICION
     */
    public static class CompetitionInstance extends RecyclerView.ViewHolder{
        public ImageView ivCompe;
        public TextView tvNombre;
        public ConstraintLayout layoutElemCompe, clGeneralDate;
        public TextView tvDateCompe;
        public LinearLayout lyDateAvailable, lyDateFinish;

        public CompetitionInstance(@NonNull View itemView) {
            super(itemView);
            //Referencias
            tvNombre = (TextView) itemView.findViewById(R.id.tvTituloComp);
            ivCompe = (ImageView) itemView.findViewById(R.id.ivBackCompeElement);
            layoutElemCompe = (ConstraintLayout) itemView.findViewById(R.id.lyNewUser);
            lyDateAvailable = itemView.findViewById(R.id.lyDateAvailable);
            lyDateFinish = itemView.findViewById(R.id.lyDateFinish);
            tvDateCompe = itemView.findViewById(R.id.tvDateCompe);
            clGeneralDate = itemView.findViewById(R.id.clDateCompe);
        }
    }
}
