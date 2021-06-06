package com.rutillastoby.zoria.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rutillastoby.zoria.GeneralActivity;
import com.rutillastoby.zoria.GenericFuntions;
import com.rutillastoby.zoria.R;
import com.rutillastoby.zoria.RecordElement;
import com.rutillastoby.zoria.dao.CompeticionDao;
import com.rutillastoby.zoria.dao.UsuarioDao;

import java.util.ArrayList;
import java.util.Map;

public class ProfileFragment extends Fragment {
    //Referencias
    private ImageView ivProfile, ivLogout, ivInfoGeneral;
    private EditText etNickNameProfile, etEmailProfile;
    private ProgressBar pbNickProfile;
    private RecyclerView rvRecordList;
    private ConstraintLayout lyEmptyRecord, lyLoadProfile;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;

    //Variables
    private UsuarioDao myUser;
    private String nickUser;
    private RecyclerView.Adapter adapter; //Crear un contenedor de vistas de cada competicion
    private ProfileFragment thisClass;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Iniciar variables
        initVar(view);
        //Carga inicial
        lyLoadProfile.setVisibility(View.VISIBLE);

        return view;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA INICIALIZAR VARIABLES Y REFERENCIAS
     */
    private void initVar(View v){
        final View view = v;
        final Context context = getContext();
        thisClass = this;
        //Obtener usuario y base de datoS
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        //Referencias
        ivProfile = view.findViewById(R.id.ivPhotoProfile);
        etNickNameProfile = view.findViewById(R.id.etNickName);
        etEmailProfile = view.findViewById(R.id.etEmailProfile);
        pbNickProfile = view.findViewById(R.id.pbNickProfile);
        rvRecordList = view.findViewById(R.id.rvRecordList);
        lyEmptyRecord = view.findViewById(R.id.lyEmptyRecord);
        ivLogout = getActivity().findViewById(R.id.ivLogout);
        ivInfoGeneral = getActivity().findViewById(R.id.ivInfoGeneral);
        lyLoadProfile = view.findViewById(R.id.lyLoadProfile);

        //Onclick boton cerrar sesion
        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(view);
            }
        });

        //Ejecucion al perder el foco del cuadro de texto
        etNickNameProfile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //Actualizar el nick
                    checkUpdateNick();
                }
            }
        });

        //Ejecucion al pulsar el boton DONE del teclado con el cuadro de texto de nick activo
        etNickNameProfile.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    etNickNameProfile.clearFocus();
                }
                return false; //Cerrar teclado
            }
        });

        //Ejecucion al pulsar fuera del cuadro de texto, limpiar foco para actualizar nick
        getActivity().findViewById(R.id.container_fragment).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Cerrar teclado
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                etNickNameProfile.clearFocus();
                return true;
            }
        });

        ///// Funcionalidad al hacer click sobre boton de info general
        ivInfoGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creación de la ventana modal con la informacion
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialog = inflater.inflate(R.layout.dialog_info_general, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                //Funcionalidad boton valorar del dialogo
                Button bRate = dialog.findViewById(R.id.bRate);
                bRate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GenericFuntions.openPlayStore(context);
                    }
                });

                builder.setView(dialog)
                    .show();
            }
        });
    }


    //----------------------------------------------------------------------------------------------

    /**
     * VERIFICAR SI LOS CAMBION EN EL NICK SON VALIDOS
     */
    public void checkUpdateNick(){
        final String newNick = etNickNameProfile.getText().toString();

        //Si es diferente que el actual lo cambiamos
        if (!newNick.equals(nickUser)) {
            pbNickProfile.setVisibility(View.VISIBLE);//Activar la barra de progreso

            //Obtener los nombre de usuario
            db.getReference("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    ArrayList<String> nombresUsados = new ArrayList<String>();

                    //Obtener datos de nombres
                    for(DataSnapshot usu : dataSnapshot.getChildren()) {
                        for(DataSnapshot dato : usu.getChildren()){
                            if(dato.getKey().equalsIgnoreCase("nombre")){
                                nombresUsados.add(dato.getValue().toString().toLowerCase());
                            }
                        }
                    }

                    //El usuario es valido
                    if(GenericFuntions.checkNick(newNick.toLowerCase(), nombresUsados).equals("true")){
                        updateNick(getView(), newNick);

                    //Nombre de usuario no valido
                    }else{
                        GenericFuntions.errorSnack(getView(),GenericFuntions.checkNick(newNick.toLowerCase(), nombresUsados),getContext());
                        etNickNameProfile.setText(nickUser);
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



    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ALMACENAR EN LA BASE DE DATOS EL NUEVO NOMBRE DE USUARIO
     */
    private void updateNick(View v, final String newNick){
        final View view = v;
        //Guardar el nuevo valor
        db.getReference("usuarios/"+user.getUid()+"/nombre")
                .setValue(newNick, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    //Error al guardar
                    GenericFuntions.errorSnack(view,"Error al guardar", getContext());
                    etNickNameProfile.setText(nickUser);
                } else {
                    //Cambios guardados
                    GenericFuntions.snack(view, "Nombre actualizado");
                }

                //Ocultar barra de progreso
                pbNickProfile.setVisibility(View.GONE);
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CARGAR EL HISTORIAL DE COMPETICIONES
     */
    public void loadRecordCompetition(ArrayList<CompeticionDao> allCompe){

        ArrayList<CompeticionDao> competitionsList=new ArrayList<CompeticionDao>();

        for(int i=0; i<allCompe.size();i++){
            //Comprobar si la competicion esta marcada como historial y se pueden ver los resultados
            if(allCompe.get(i).getRes()==1) {
                for (int j=0; j< myUser.getCompetitionsRegistered().size() ; j++) {
                    //Comprobar si el usuario esta registrado en la competicion
                    if (allCompe.get(i).getId() == myUser.getCompetitionsRegistered().get(j)) {
                        competitionsList.add(allCompe.get(i));
                    }
                }
            }
        }

        //Comprobar si existen competiciones
        if(competitionsList.size()>0) {
            //Ocultar panel sin competiciones
            lyEmptyRecord.setVisibility(View.GONE);
            rvRecordList.setVisibility(View.VISIBLE);
            //Asignar listado al recyclerview de historial de competiciones
            adapter = new RecordElement(competitionsList, thisClass, ((GeneralActivity)getActivity()));
            rvRecordList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvRecordList.setAdapter(adapter);
        }else{
            //Si no existen competiciones, mostrar panel
            lyEmptyRecord.setVisibility(View.VISIBLE);
            rvRecordList.setVisibility(View.GONE);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA CERRAR SESIÓN Y LA APLICACIÓN
     * @param view
     */
    private void logout(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle(R.string.titleLogout);
        builder.setMessage(R.string.logoutText);
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Cerrar sesion
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(getContext(),gso);
                googleSignInClient.signOut();

                FirebaseAuth.getInstance().signOut();

                //Cerrar aplicacion
                getActivity().finishAffinity();
                //El codigo comentado es para abrir de nuevo la ventana de login, preferible cerrar app
                //para evitar problemas al quedarse activity funcionando en segundo plano o similar
                /*Intent i = new Intent(getContext(), LoginActivity.class);
                startActivity(i);
                getActivity().finish();*/
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * METODO PARA ESTABLECER LA INFORMACION ALMACENADA EN LA BASE DE DATOS SOBRE MI USUARIO
     * + CARGAR DATOS DEL MISMO SOBRE LA VISTA
     * @param myUser
     */
    public void setMyUser(UsuarioDao myUser) {
        this.myUser = myUser;
        //Establecer el nombre en el campo de texto
        nickUser = myUser.getNombre();
        etNickNameProfile.setText(nickUser);

        ////// Cargar foto y email
        GenericFuntions.chargeImageRound(getContext(), myUser.getFoto(), ivProfile);
        etEmailProfile.setText(user.getEmail());

        //Si mi imagen ha cambiado actualizar datos en base de datos
        String bigPhoto = user.getPhotoUrl().toString().replace("s96-c", "s320-c");
        if(!myUser.getFoto().equals(bigPhoto)){
            db.getReference("/usuarios/"+user.getUid()+"/foto").setValue(bigPhoto);
        }
    }

    //----------------------------------------------------------------------------------------------
    // GETs + Sets
    //----------------------------------------------------------------------------------------------

    /**
     * Obtener el layout con icono de carga del fragmento
     * @return
     */
    public ConstraintLayout getLyLoadProfile(){
        return lyLoadProfile;
    }
}
