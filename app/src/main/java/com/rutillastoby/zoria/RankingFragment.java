package com.rutillastoby.zoria;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rutillastoby.zoria.dao.CompeticionDao;
import com.rutillastoby.zoria.dao.RankingUserData;
import com.rutillastoby.zoria.dao.UsuarioDao;
import com.rutillastoby.zoria.dao.competicion.Jugador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class RankingFragment extends Fragment {
    //Referencias
    private RecyclerView rvRanking;
    private GeneralActivity ga;
    private ImageView ivBackRanking, ivInfoRanking;

    //Variables
    private RecyclerView.Adapter adapter; //Crear un contenedor de vistas de cada competicion
    private RankingFragment thisClass;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

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
        rvRanking = view.findViewById(R.id.rvRanking);
        ivBackRanking = view.findViewById(R.id.ivBackRanking);
        ivInfoRanking = getActivity().findViewById(R.id.ivInfoRanking);

        //Boton para volver a la vista de la competicion
        ivBackRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ga.showPrincActivityNotChange();
            }
        });

        //Accion al pulsar el boton de la toolbar info ranking (Mostrar valor de los codigos)
        ivInfoRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creación de la ventana modal con la informacion
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialog = inflater.inflate(R.layout.dialog_info_ranking, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setView(dialog)
                    .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {}
                    })
                    .show();
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA RECARGAR LOS DATOS DEL RANKING
     */
    public void loadRanking(CompeticionDao showCompetition, ArrayList<UsuarioDao> usersList){
        ArrayList<RankingUserData> rankingList = new ArrayList<RankingUserData>();

        //1. Recorrer todos los jugadores inscritos en la competición
        for (Map.Entry<String, Jugador> player : showCompetition.getJugadores().entrySet()) {
            int uL1=0, uL2=0, uL3=0, uL4=0, total=0;
            RankingUserData u = new RankingUserData();

            Log.d("aaa", "id: "+player.getKey());
            //Obtener nombre + foto
            for(UsuarioDao user : usersList){
                if(user.getUid().equals(player.getKey())){
                    u.setImageProfile(user.getFoto());
                    u.setNameProfile(user.getNombre());
                    Log.d("aaa", "nombre: "+user.getNombre());
                }
            }

            //Obtener puntos
            for (Map.Entry<String, String> pointScanned : player.getValue().getPuntos().entrySet()) {
                //Optener el valor del punto
                int value = Integer.parseInt(pointScanned.getValue().split("-")[0]);
                //En funcion del valor del punto obtenemos el tipo
                switch (value){
                    case 1: uL1++; break; //TIPO 1
                    case 2: uL2++; break; //TIPO 2
                    case 3: uL3++; break; //TIPO 3
                    case 6: uL4++; break; //TIPO 4 (Pregunta)
                    case 10: u.setFlag(true); break; //TIPO 5 (bandera)
                }
                //Sumar puntos al total
                total+=value;
            }

            u.setMyUser(false);
            //Comprobar si el usuario en cuestion es nuestro usuario
            Log.d("aaa", "yo: "+ga.getMyUser().getUid());
            if(player.getKey().equals(ga.getMyUser().getUid())){
                u.setMyUser(true);
                //Actualizar datos del marcador principal del usuario
                ga.getPrinF().setPointMarker(total,uL1,uL2,uL3,uL4);
                Log.d("aaa", "mi usuario");
            }

            //Establecer datos de puntos
            u.setLevel1P(uL1);
            u.setLevel2P(uL2);
            u.setLevel3P(uL3);
            u.setLevel4P(uL4);
            u.setPoints(total);

            //Agregar los datos del usuario al listado
            rankingList.add(u);
        }

        //2. Ordenar arraylist por puntuacion
        Collections.sort(rankingList);

        //2.1 Indicar la posicion de mi usuario dentro del listado del recycler view
        for(int i=0; i<rankingList.size();i++){
            if(rankingList.get(i).isMyUser()){
                ga.setPosMyUserRanking(i);
            }
        }

        //3. Asignar listado al recyclerview
        adapter = new RankingElement(rankingList, thisClass.getContext());
        rvRanking.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRanking.setAdapter(adapter);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA DESPLAZAR EL RANKING AUTOMÁTICAMENTE HASTA UNA POSICION
     * @param position
     */
    public void autoScroll(final int position){
        final int speedScroll = 250;
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                rvRanking.smoothScrollToPosition(position);
            }
        };

        handler.postDelayed(runnable,speedScroll);
    }
}