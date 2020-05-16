package com.rutillastoby.zoria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rutillastoby.zoria.dao.CompeticionDao;
import com.rutillastoby.zoria.ui.profile.ProfileFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class RecordElement extends RecyclerView.Adapter<RecordElement.CompetitionRecordInstance>{
    private ArrayList<CompeticionDao> competitionsList;
    ProfileFragment context;

    /**
     * CONSTRUCTOR PARAMETRIZADO
     */
    public RecordElement(ArrayList<CompeticionDao> c, ProfileFragment cc){
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
    public CompetitionRecordInstance onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Crear la vista con el layout correspondiente a la plantilla de la competicion
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_competition, viewGroup, false);
        //Crear objeto de tipo viewHolder de la clase interna con la vista creada anteriormente
        CompetitionRecordInstance example = new CompetitionRecordInstance(view);
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
    public void onBindViewHolder(@NonNull final CompetitionRecordInstance instance, final int i) {
        //1. Obtener nombre de la competicion
        instance.tvNameRecord.setText(competitionsList.get(i).getNombre());
        //2. Establecer imagen de fondo
        Picasso.get().load(competitionsList.get(i).getFoto()).error(R.color.colorPrimaryDark).into(instance.ivBackRecord);
        //3. Establecer fecha de competicion
        Date now = new Date(competitionsList.get(i).getHora().getInicio());
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
        cal.setTime(now);
        instance.tvDateRecord.setText(cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.MONTH)
                +"/"+cal.get(Calendar.YEAR));

        //4. Accion al presionar la competicion
        instance.lyCompeRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //Abrir competicion en modo historial
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
    public static class CompetitionRecordInstance extends RecyclerView.ViewHolder{
        public ImageView ivBackRecord;
        public TextView tvNameRecord, tvDateRecord;
        public ConstraintLayout lyCompeRecord;

        public CompetitionRecordInstance(@NonNull View itemView) {
            super(itemView);
            //Referencias
            tvNameRecord = itemView.findViewById(R.id.tvNameRecord);
            ivBackRecord = itemView.findViewById(R.id.ivBackRecord);
            tvDateRecord = itemView.findViewById(R.id.tvDateRecord);
            lyCompeRecord = itemView.findViewById(R.id.lyCompeRecord);
        }
    }
}
