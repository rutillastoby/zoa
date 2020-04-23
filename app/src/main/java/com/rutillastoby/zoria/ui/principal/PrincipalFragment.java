package com.rutillastoby.zoria.ui.principal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.rutillastoby.zoria.R;
import com.rutillastoby.zoria.dao.CompeticionDao;

public class PrincipalFragment extends Fragment {
    //Referencias
    private TextView tvTitlePrincipalCompe;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_principal, container, false);

        //Llamada al metodo para
        initVar(view);

        return view;
    }

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View v){
        View view = v;

        //Referencias
        tvTitlePrincipalCompe = view.findViewById(R.id.tvTitlePrincipalCompe);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER LOS DATOS DE LA COMPETICION EN EL FRAGMENTO
     */
    public void setDataCompetition(CompeticionDao competition){
        Log.d("aaa", "fragmento");
    }
}
