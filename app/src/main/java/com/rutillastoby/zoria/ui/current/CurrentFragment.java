package com.rutillastoby.zoria.ui.current;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.rutillastoby.zoria.R;

public class CurrentFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_current, container, false);


        return root;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER LA COMPETICION ACTIVA SOBRE EL FRAGMENTO
     */
    public void setCompetition(){

    }
}
