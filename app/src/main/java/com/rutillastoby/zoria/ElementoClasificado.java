package com.rutillastoby.zoria;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ElementoClasificado extends RecyclerView.Adapter<ElementoClasificado.InstanciaClafificado>{
    private ArrayList<Clasificado> listUsuarios;

    /**
     * CONSTRUCTOR PARAMETRIZADO
     */
    public ElementoClasificado(ArrayList<Clasificado> l){
        listUsuarios=l;
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
    public InstanciaClafificado onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Crear la vista con el layout correspondiente a la plantilla de paquete
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.elemento_clasificado, viewGroup, false);
        //Crear objeto de tipo viewHolder de la clase interna con la vista creada anteriormente
        InstanciaClafificado ejemplo = new InstanciaClafificado(view);
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
    public void onBindViewHolder(@NonNull final InstanciaClafificado instancia, final int i) {
        //Obtener los datos del array y agregarlos a los elementos de la fila que se mostrará
        //instancia.ivCompe.setText(listadoPaquetes.get(i).getCodigo());
        instancia.tvNombre.setText(listUsuarios.get(i).getNombre());
        instancia.tvPosicion.setText(""+(i+1));
        instancia.tvN1.setText(listUsuarios.get(i).getPuntosN1()+"");
        instancia.tvN2.setText(listUsuarios.get(i).getPuntosN2()+"");
        instancia.tvN3.setText(listUsuarios.get(i).getPuntosN3()+"");
        instancia.tvTotal.setText(listUsuarios.get(i).getTotal()+"");
        Picasso.get().load(listUsuarios.get(i).getFotoPerfil()).into(instancia.ivFoto);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER EL NUMERO DE ELEMENTOS DEL LISTADO
     * @return
     */
    @Override
    public int getItemCount() {
        return listUsuarios.size();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * CLASE INTERNA CON EJEMPLO DE UNA FILA DE UN PAQUETE
     */
    public static class InstanciaClafificado extends RecyclerView.ViewHolder{
        public CircularImageView ivFoto;
        public TextView tvNombre, tvPosicion, tvN1, tvN2, tvN3, tvTotal;

        public InstanciaClafificado(@NonNull View itemView) {
            super(itemView);
            //Referencias
            tvNombre = (TextView) itemView.findViewById(R.id.tvNombreClas);
            tvN1 = (TextView) itemView.findViewById(R.id.tvN1Clas);
            tvN2 = (TextView) itemView.findViewById(R.id.tvN2Clas);
            tvN3 = (TextView) itemView.findViewById(R.id.tvN3Clas);
            tvPosicion = (TextView) itemView.findViewById(R.id.tvPosicionClas);
            tvTotal = (TextView) itemView.findViewById(R.id.tvTotalClas);
            ivFoto = (CircularImageView) itemView.findViewById(R.id.ivFotoClas);

// Set Circle color for transparent image
            //ivFoto.setCircleColor(Color.WHITE);
// Set Border
            ivFoto.setBorderColor(Color.YELLOW);
            ivFoto.setBorderWidth(0);
// Add Shadow with default param

        }
    }
}
