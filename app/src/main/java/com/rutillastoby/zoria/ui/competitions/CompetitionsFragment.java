package com.rutillastoby.zoria.ui.competitions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rutillastoby.zoria.CompetitionElement;
import com.rutillastoby.zoria.R;
import com.rutillastoby.zoria.dao.CompeticionDao;

import java.util.ArrayList;

public class CompetitionsFragment extends Fragment {

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;

    //Referencias
    private RecyclerView rvCompetitions;

    //Variables
    private static ArrayList<CompeticionDao> competitionsList =new ArrayList<CompeticionDao>();
    private RecyclerView.Adapter adapter; //Crear un contenedor de vistas de cada competicion



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_competitions, container, false);

        //Iniciar variables
        initVar(view);

        loadCompetitions();
        return view;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View view){
        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        //Referencias
        rvCompetitions = view.findViewById(R.id.rvCompetitions);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CARGAR TODAS LAS COMPETICIONES DE LA BASE DE DATOS
     */
    private void loadCompetitions(){
        DatabaseReference competitions = db.getReference("competiciones");

        competitions.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Reiniciar listado
                competitionsList = new ArrayList<CompeticionDao>();

                //Obtener los datos de las competiciones
                for (DataSnapshot compe : dataSnapshot.getChildren()) {
                    CompeticionDao c = compe.getValue(CompeticionDao.class);
                    c.setId(Integer.parseInt(compe.getKey()));
                    competitionsList.add(c); //Agregamos a la lista de competiciones
                }

                //Asignar listado al recyclerview
                adapter = new CompetitionElement(competitionsList);
                rvCompetitions.setLayoutManager(new LinearLayoutManager(getContext()));
                rvCompetitions.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
