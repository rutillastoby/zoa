package com.rutillastoby.zoria.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rutillastoby.zoria.GenericFuntions;
import com.rutillastoby.zoria.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    //Referencias
    private ImageView ivProfile;
    private EditText etNickNameProfile, etEmailProfile;
    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;
    //Variables
    private String nickUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Iniciar variables
        initVar(view);
        //Cargar informacion del usuario
        loadInformation(view);


        return view;
    }




    /**
     * METODO PARA INICIALIZAR VARIABLES
     */
    private void initVar(View view){
        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        
        ivProfile = view.findViewById(R.id.ivPhotoProfile);
        etNickNameProfile = view.findViewById(R.id.etNickName);
        etEmailProfile = view.findViewById(R.id.etEmailProfile);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CARGAR LA INFORMACION DEL USUARIO EN LA VISTA
     */
    private void loadInformation(View v){
        final View view = v;

        ////// Cargar foto y email
        Picasso.get().load(user.getPhotoUrl()).into(ivProfile);
        etEmailProfile.setText(user.getEmail());

        ////// Obtener el nombre usuario
        db.getReference("usuarios/"+user.getUid()+"/nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nickUser = dataSnapshot.getValue().toString();
                etNickNameProfile.setText(nickUser);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        ////// Funcionalidad para cambiar nombre de usuario al pulsar done del teclado
        etNickNameProfile.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    //Si es diferente que el actual lo cambiamos
                    final String newNick = etNickNameProfile.getText().toString().replaceAll("\\s+", "");
                    if (!newNick.equals(nickUser)) {
                        db.getReference("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Listado
                                ArrayList<String> nombresUsados = new ArrayList<String>();

                                //Obtener datos de nombres
                                for(DataSnapshot usu : dataSnapshot.getChildren()) {
                                    for(DataSnapshot dato : usu.getChildren()){
                                        if(dato.getKey().equalsIgnoreCase("nombre")){
                                            nombresUsados.add(dato.getValue().toString());
                                        }
                                    }
                                }

                                //Comprobar si el usuario es valido
                                if(GenericFuntions.checkNick(newNick, nombresUsados)=="true"){
                                    updateName(view);
                                }else{
                                    GenericFuntions.errorSnack(view,GenericFuntions.checkNick(newNick, nombresUsados));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
                return true; //El true deja el teclado abierto
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    private void updateName(View v){
        final View view = v;
        //Guardar el nuevo valor
        db.getReference("usuarios/"+user.getUid()+"/nombre")
                .setValue(etNickNameProfile.getText().toString(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    //Error al guardar
                    GenericFuntions.errorSnack(view,"Error al guardar");
                } else {
                    //Cambios guardados
                    GenericFuntions.snack(view, "Nombre actualizado");
                    //Cerrar teclado
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    etNickNameProfile.clearFocus();

                }
            }
        });
    }
}
