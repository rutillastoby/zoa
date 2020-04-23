package com.rutillastoby.zoria.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.rutillastoby.zoria.dao.UsuarioDao;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    //Referencias
    private ImageView ivProfile, ivLogout;
    private EditText etNickNameProfile, etEmailProfile;
    private ProgressBar pbNickProfile;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;
    //Variables
    private UsuarioDao myUser;
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

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View v){
        final View view = v;
        //Obtener usuario y base de datos
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        //Referencias
        ivProfile = view.findViewById(R.id.ivPhotoProfile);
        etNickNameProfile = view.findViewById(R.id.etNickName);
        etEmailProfile = view.findViewById(R.id.etEmailProfile);
        pbNickProfile = view.findViewById(R.id.pbNickProfile);
        ivLogout = getActivity().findViewById(R.id.ivLogout);

        //Onclick boton cerrar sesion
        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(view);
            }
        });
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

        ////// Funcionalidad para cambiar nombre de usuario al pulsar done del teclado
        etNickNameProfile.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //Si es diferente que el actual lo cambiamos
                    final String newNick = etNickNameProfile.getText().toString().trim();

                    if (!newNick.equals(nickUser)) {
                        pbNickProfile.setVisibility(View.VISIBLE);//Activar la barra de progreso
                        //Obtener los nombre de usuario
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
                                    updateName(view, newNick);
                                }else{
                                    GenericFuntions.errorSnack(view,GenericFuntions.checkNick(newNick, nombresUsados),getContext());
                                    //Ocultar barra de progreso
                                    pbNickProfile.setVisibility(View.GONE);
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

    /**
     * METODO PARA ALMACENAR EN LA BASE DE DATOS EL NUEVO NOMBRE DE USUARIO
     */
    private void updateName(View v, final String newNick){
        final View view = v;
        //Guardar el nuevo valor
        db.getReference("usuarios/"+user.getUid()+"/nombre")
                .setValue(newNick, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    //Error al guardar
                    GenericFuntions.errorSnack(view,"Error al guardar", getContext());
                } else {
                    //Cambios guardados
                    GenericFuntions.snack(view, "Nombre actualizado");
                    //Cerrar teclado
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    nickUser = newNick;
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    etNickNameProfile.clearFocus();
                }

                //Ocultar barra de progreso
                pbNickProfile.setVisibility(View.GONE);
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CERRAR SESIÓN Y LA APLICACIÓN
     * @param view
     */
    private void logout(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle(R.string.logoutText);
        //builder.setMessage("Message");
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                getActivity().finishAffinity();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER LA INFORMACION ALMACENADA EN LA BASE DE DATOS SOBRE MI USUARIO
     * @param myUser
     */
    public void setMyUser(UsuarioDao myUser) {
        this.myUser = myUser;
        //Establecer el nombre en el campo de texto
        nickUser = myUser.getNombre();
        etNickNameProfile.setText(nickUser);
    }
}
