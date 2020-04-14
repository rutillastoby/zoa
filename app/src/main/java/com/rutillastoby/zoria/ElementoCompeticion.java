package com.rutillastoby.zoria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ElementoCompeticion extends RecyclerView.Adapter<ElementoCompeticion.InstanciaCompeticion>{
    private ArrayList<Competicion> listCompeticiones;

    /**
     * CONSTRUCTOR PARAMETRIZADO
     */
    public ElementoCompeticion(ArrayList<Competicion> c){
        listCompeticiones=c;
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
    public InstanciaCompeticion onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Crear la vista con el layout correspondiente a la plantilla de paquete
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.elemento_competicion, viewGroup, false);
        //Crear objeto de tipo viewHolder de la clase interna con la vista creada anteriormente
        InstanciaCompeticion ejemplo = new InstanciaCompeticion(view);
        //Devolver el objeto de la fila creado
        return ejemplo;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO QUE SE EJECUTA PARA CREAR LAS FILAS QUE SE VAN A MOSTRAR
     * Ya que recyclerView Elimina las filas al hacer scroll y las crea de nuevo para ahorrar memoria y así no tener
     * que crear todos los registros o filas de una vez
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull final InstanciaCompeticion instancia, final int i) {
        //Obtener los datos del array y agregarlos a los elementos de la fila que se mostrará
        //instancia.ivCompe.setText(listadoPaquetes.get(i).getCodigo());
        instancia.tvNombre.setText(listCompeticiones.get(i).getNombre());
        Picasso.get().load(listCompeticiones.get(i).getUrlImage()).error(R.color.colorPrimaryDark).into(instancia.ivCompe);

            //Accion al presionar la competicion
            instancia.layoutElemCompe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   ActivityCompeticiones.comprobarAccesoCompeticion(listCompeticiones.get(i).getIdentificador());
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
        return listCompeticiones.size();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * CLASE INTERNA CON EJEMPLO DE UNA FILA DE UN PAQUETE
     */
    public static class InstanciaCompeticion extends RecyclerView.ViewHolder{
        public ImageView ivCompe;
        public TextView tvNombre;
        public ConstraintLayout layoutElemCompe;

        public InstanciaCompeticion(@NonNull View itemView) {
            super(itemView);
            //Referencias
            tvNombre = (TextView) itemView.findViewById(R.id.tvTituloComp);
            ivCompe = (ImageView) itemView.findViewById(R.id.ivCompe);
            layoutElemCompe = (ConstraintLayout) itemView.findViewById(R.id.LayoutEleCompe);
        }
    }
}
