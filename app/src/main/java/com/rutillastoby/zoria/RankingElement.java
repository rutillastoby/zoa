package com.rutillastoby.zoria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rutillastoby.zoria.dao.RankingUserData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Formatter;

public class RankingElement extends RecyclerView.Adapter<RankingElement.RankingInstance>{
    private ArrayList<RankingUserData> rankingList;
    private RankingFragment context;


    /**
     * CONSTRUCTOR PARAMETRIZADO
     */
    public RankingElement(ArrayList<RankingUserData> rl, RankingFragment c){
        rankingList=rl;
        context=c;
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
    public RankingInstance onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Crear la vista con el layout correspondiente a la plantilla del ranking
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_ranking, viewGroup, false);
        //Crear objeto de tipo viewHolder de la clase interna con la vista creada anteriormente
        RankingInstance example = new RankingInstance(view);
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
    public void onBindViewHolder(@NonNull final RankingInstance instance, final int i) {
        //Obtener los datos del array y agregarlos a los elementos del elemento de ranking
        instance.tvNameUserRanking.setText(rankingList.get(i).getNameProfile());
        instance.tvPointsRanking.setText(rankingList.get(i).getPoints()+" puntos.");
        instance.tvPLevel1Ranking.setText("x"+rankingList.get(i).getLevel1P());
        instance.tvPLevel2Ranking.setText("x"+rankingList.get(i).getLevel2P());
        instance.tvPLevel3Ranking.setText("x"+rankingList.get(i).getLevel3P());
        instance.tvPLevel4Ranking.setText("x"+rankingList.get(i).getLevel4P());
        //Comprobar si ha cogido la bandera para mostrar el icono correspondiente
        if(rankingList.get(i).isFlag()){
            instance.ivFlagRanking.setImageResource(R.drawable.ic_flag);
        }
        //Cargar imagen de perfil
        Picasso.get().load(rankingList.get(i).getImageProfile()).into(instance.ivProfileImageRanking);
        //Indicar posicion en el ranking
        instance.tvPositionRanking.setText(new Formatter().format("%02d",i+1)+"");

        //Si es mi usuario mostramos los detalles de puntuacion
        if(rankingList.get(i).isMyUser()){
            instance.dividerRanking.setVisibility(View.VISIBLE);
            instance.lyPointsRanking.setVisibility(View.VISIBLE);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA OBTENER EL NUMERO DE ELEMENTOS DEL LISTADO
     * @return
     */
    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * CLASE INTERNA CON EJEMPLO DE UNA FILA DE UNA COMPETICION
     */
    public static class RankingInstance extends RecyclerView.ViewHolder{
        public ImageView ivProfileImageRanking, ivFlagRanking;
        public TextView tvPLevel1Ranking, tvPLevel2Ranking, tvPLevel3Ranking, tvPLevel4Ranking,
                        tvPointsRanking, tvPositionRanking, tvNameUserRanking;
        public View dividerRanking;
        public ConstraintLayout lyPointsRanking, lyRanking;

        public RankingInstance(@NonNull View itemView) {
            super(itemView);
            //Referencias
            ivProfileImageRanking = itemView.findViewById(R.id.ivProfileImageRanking);
            ivFlagRanking = itemView.findViewById(R.id.ivFlagRanking);
            tvPLevel1Ranking = itemView.findViewById(R.id.tvPLevel1Ranking);
            tvPLevel2Ranking = itemView.findViewById(R.id.tvPLevel2Ranking);
            tvPLevel3Ranking = itemView.findViewById(R.id.tvPLevel3Ranking);
            tvPLevel4Ranking = itemView.findViewById(R.id.tvPLevel4Ranking);
            tvPointsRanking = itemView.findViewById(R.id.tvPointsRanking);
            tvPositionRanking = itemView.findViewById(R.id.tvPositionRanking);
            tvNameUserRanking = itemView.findViewById(R.id.tvNameUserRanking);
            lyPointsRanking = itemView.findViewById(R.id.lyPointsRanking);
            dividerRanking = itemView.findViewById(R.id.dividerRanking);
            lyRanking = itemView.findViewById(R.id.lyRanking);

            //Estado inicial
            dividerRanking.setVisibility(View.GONE);
            lyPointsRanking.setVisibility(View.GONE);
        }
    }
}
