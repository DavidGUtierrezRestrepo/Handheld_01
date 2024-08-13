package com.example.handheld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld.ClasesOperativas.MyHolder;
import com.example.handheld.atv.model.TreeNode;
import com.example.handheld.atv.view.AndroidTreeView;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.conexionDB.ConfiguracionBD;
import com.example.handheld.modelos.PermisoPersonaModelo;
import com.example.handheld.modelos.PersonaModelo;


public class MainActivity extends AppCompatActivity {

    //Se declaran los elementos del layout
    EditText cedula;
    ImageButton consultar;

    Button btnCambiarBD;
    TextView mensaje, txtBD;

    //Se declara un objeto conexion
    Conexion conexion;

    //Se declaran variables necesarias
    PersonaModelo persona;
    PermisoPersonaModelo personaPermiso;
    String nombre_usuario;
    String cd, permiso = "";

    ProgressBar progressBar;

    //Se inicializa los varibles para el sonido de error
    SoundPool sp;
    int sonido_de_Reproduccion;

    //Se inicializa una instancia para hacer vibrar el celular
    Vibrator vibrator;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Se inicializan los elementos del layout
        cedula = findViewById(R.id.txtcedula);
        consultar = findViewById(R.id.btnBuscarPersona);
        mensaje = findViewById(R.id.txtMensaje);
        progressBar = findViewById(R.id.progress_bar);
        btnCambiarBD = findViewById(R.id.btnCambiarBD);
        txtBD = findViewById(R.id.txtBD);

        //Se inicializa el objeto conexión
        conexion = new Conexion();

        //Se Define los varibles para el sonido de error y vibracion
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC,1);
        sonido_de_Reproduccion = sp.load(this, R.raw.sonido_error_2,1);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if(ConfiguracionBD.isModoPrueba()){
            txtBD.setText("BD_PRUEBA");
        }else{
            txtBD.setText("BD_REAL");
        }

        //Se programa el boton consultar
        consultar.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            if(validar()){
                cd = cedula.getText().toString();

                if (isNetworkAvailable()) {
                    persona = conexion.obtenerPersona(MainActivity.this,cd );
                    nombre_usuario = persona.getNombres();

                    if(nombre_usuario.equals("")){
                        toastError("Persona no encontrada");
                        progressBar.setVisibility(View.GONE);
                        cedula.setText("");
                    }else{
                        progressBar.setVisibility(View.GONE);
                        mensaje.setText("Bienvenido " + nombre_usuario);
                        agregarTreeview();
                        consultar.setEnabled(false);
                        cedula.setEnabled(false);
                    }
                } else {
                    toastError("Problemas de conexión a Internet");
                    progressBar.setVisibility(View.GONE);
                }
            }else{
                toastEscribir("Por favor escribir tu cedula");
                progressBar.setVisibility(View.GONE);
            }
        });

        btnCambiarBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.alertdialog_cedularecepciona,null);
                //Obtenemos los elementos del alertDialog
                EditText CedulaIngresada = mView.findViewById(R.id.txtCedulaLogistica);
                TextView txtMrollos = mView.findViewById(R.id.txtMrollos);
                TextView txtCedula = mView.findViewById(R.id.textView6);
                Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                Button btnCancelar = mView.findViewById(R.id.btnCancelar);
                //Enviamos valores a los elementos del alert
                txtMrollos.setText("¡Se cambiaran las bases de datos!");
                txtCedula.setText("Ingrese cedula autorizada");
                CedulaIngresada.setHint("Cedula Autorizada");
                builder.setView(mView);
                AlertDialog alertDialog = builder.create();
                btnAceptar.setOnClickListener(v12 -> {
                    //Verificamos que haya internet
                    if (isNetworkAvailable()) {
                        //Obtenemos la cedula del EditText
                        String CedulaAutorizada = CedulaIngresada.getText().toString().trim();
                        //Verificamos que el campo de cedula no este vacio
                        if (CedulaAutorizada.equals("")){
                            AudioError();
                            toastError("Ingresar cedula autorizada");
                        }else{
                            personaPermiso = conexion.obtenerPermisoPersona(MainActivity.this,CedulaAutorizada,"cambiar_bd" );
                            permiso = personaPermiso.getNit();
                            if(!permiso.equals("")){
                                // Cambiar el modo al hacer clic en el botón
                                ConfiguracionBD.cambiarModo();
                                Boolean tipo = ConfiguracionBD.isModoPrueba();
                                if (tipo.equals(true)){
                                    toastAcierto("Base de datos cambiada correctamente a prueba");
                                    txtBD.setText("BD_PRUEBA");
                                }else{
                                    toastAcierto("Base de datos cambiada correctamente a real");
                                    txtBD.setText("BD_REAL");
                                }
                                alertDialog.dismiss();
                            }else{
                                CedulaIngresada.setText("");
                                AudioError();
                                toastError("La cedula ingresada no esta autorizada!");
                            }
                        }
                    } else {
                        toastError("Problemas de conexión a Internet");
                    }
                });
                btnCancelar.setOnClickListener(v1 -> alertDialog.dismiss());
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        });
    }

    //Metodo para validar la conexion de internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Metodo que valida que el campo EditText "cedula" no este vacia
    public boolean validar(){
        boolean retorno = true;

        String text = cedula.getText().toString();
        if(text.isEmpty()){
            retorno = false;
        }
        return retorno;
    }

    //METODO DE TOAST PERSONALIZADO : PERSONA NO ENCONTRADA
    public void toastError(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_per_no_encon,  findViewById(R.id.ll_custom_toast_per_no_encon));
        TextView txtMensaje = view.findViewById(R.id.txtMensajeToast1);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM,0,200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    //METODO DE TOAST PERSONALIZADO : ACIERTO
    public void toastAcierto(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_acierto, findViewById(R.id.ll_custom_toast_acierto));
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView txtMens = view.findViewById(R.id.txtMensa);
        txtMens.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER,0,200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    //METODO DE TOAST PERSONALIZADO : ESCRIBIR CEDULA
    public void toastEscribir(String msg) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_escribir_cedula, findViewById(R.id.ll_custom_toast_escribir_cedula));
        TextView txtMensaje = view.findViewById(R.id.txtMensajeToast);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    //Este codigo es del arbol
    public void agregarTreeview(){
        //Root
        TreeNode root = TreeNode.root();

        //Parent
        //MyHolder.IconTreeItem nodeItem = new MyHolder.IconTreeItem(R.drawable.ic_arrow_drop_down, "Parent");
        //TreeNode parent = new TreeNode(nodeItem).setViewHolder(new MyHolder(getApplicationContext(), true, MyHolder.DEFAULT, MyHolder.DEFAULT));

        //Grupo"Gestion de Alambron"
        MyHolder.IconTreeItem childItem1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Gestion de Alambron");
        TreeNode child1 = new TreeNode(childItem1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Gestion de Alambron"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem1_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Traslado de Bodega (1-2)");
        TreeNode subChild1_1 = new TreeNode(subChildItem1_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild1_1.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent i = new Intent(MainActivity.this,Pedido.class);
                i.putExtra("nit_usuario",cd);
                i.putExtra("bod_origen",1);
                i.putExtra("bod_destino",2);
                i.putExtra("modelo","08");

                startActivity(i);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //Subgrupo2"Gestion de Alambron"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem1_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Traslado de Bodega (2-1)");
        TreeNode subChild1_2 = new TreeNode(subChildItem1_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild1_2.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent i = new Intent(MainActivity.this,PedidoTrasladoBod2_Bod1.class);
                i.putExtra("nit_usuario",cd);
                i.putExtra("bod_origen",2);
                i.putExtra("bod_destino",1);
                i.putExtra("modelo","12");

                startActivity(i);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //SubGrupo3 "Gestion de Alambron"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem1_3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Descargue de Alambron");
        TreeNode subChild1_3 = new TreeNode(subChildItem1_3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild1_3.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent i = new Intent(MainActivity.this,Lector_Cod_Alambron.class);
                i.putExtra("nit_usuario",cd);
                startActivity(i);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //Agregamos subgrupo1"Traslado de Bodega (1-2)".
        child1.addChild(subChild1_1);

        //Agregamos subgrupo2"Traslado de Bodega (2-1)".
        child1.addChild(subChild1_2);

        //Agregamos subgrupo2"Descargue de Alambron".
        child1.addChild(subChild1_3);

        //Agregamos Grupo"Gestion de Alambron".
        //parent.addChildren(child1);
        root.addChild(child1);

        //Grupo"Gestion de Gestion Galvanizado"
        MyHolder.IconTreeItem childItem2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Gestion Galvanizado");
        TreeNode child2 = new TreeNode(childItem2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Gestion Galvanizado"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem2_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Traslado de Bodega (2-11)");
        TreeNode subChild2_1 = new TreeNode(subChildItem2_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild2_1.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent i = new Intent(MainActivity.this, Pedido_MP_Galvanizado.class);
                i.putExtra("nit_usuario", cd);
                i.putExtra("bod_origen", 2);
                i.putExtra("bod_destino", 11);
                i.putExtra("modelo", "21");

                startActivity(i);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });



        //SubGrupo2"Gestion Galvanizado"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem2_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Traslado de Bodega (11-2)");
        TreeNode subChild2_2 = new TreeNode(subChildItem2_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild2_2.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent i = new Intent(MainActivity.this, Pedido_MP_Galvanizado.class);
                i.putExtra("nit_usuario", cd);
                i.putExtra("bod_origen", 11);
                i.putExtra("bod_destino", 2);
                i.putExtra("modelo", "24");

                startActivity(i);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //Agregamos subgrupo1"Gestion Galvanizado".
        child2.addChild(subChild2_1);

        //Agregamos subgrupo2"Gestion Galvanizado".
        child2.addChild(subChild2_2);

        //Agregamos Grupo"Gestion Galvanizado".
        //parent.addChildren(child1);
        root.addChild(child2);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Gestion de Gestion Puas"
        MyHolder.IconTreeItem childItem3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Gestion Puas");
        TreeNode child3 = new TreeNode(childItem3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Gestion Puas"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem3_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Entrega de Materia Prima Puas");
        TreeNode subChild3_1 = new TreeNode(subChildItem3_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        subChild3_1.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, PedidoMateriaPrimaPuas.class);
            intent.putExtra("nit_usuario",cd);
            intent.putExtra("bod_origen",2);
            intent.putExtra("bod_destino",2);
            intent.putExtra("modelo","01");
            startActivity(intent);
        });

        //SubGrupo2"Gestion Puas"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem3_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Devolver materia prima");
        TreeNode subChild3_2 = new TreeNode(subChildItem3_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Agregamos subgrupo1"Gestion Puas".
        child3.addChild(subChild3_1);

        //Agregamos subgrupo2"Gestion Puas".
        //child3.addChild(subChild3_2);

        //Agregamos Grupo"Gestion Puas".
        //parent.addChildren(child1);
        root.addChild(child3);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Gestion Puntilleria"
        MyHolder.IconTreeItem childItem4 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Gestion Puntilleria");
        TreeNode child4 = new TreeNode(childItem4).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Gestion Puntilleria"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem4_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Mesas de Empaque");
        TreeNode subChild4_1 = new TreeNode(subChildItem4_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        subChild4_1.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, IngreProTerPunti.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            startActivity(intent);
        });

        //SubGrupo2"Gestion Puntilleria"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem4_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Entrega Materia Prima");
        TreeNode subChild4_2 = new TreeNode(subChildItem4_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        subChild4_2.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, PedidoMateriaPrimaPuntilleria.class);
            intent.putExtra("nit_usuario",cd);
            intent.putExtra("bod_origen",2);
            intent.putExtra("bod_destino",12);
            intent.putExtra("modelo","22");
            startActivity(intent);
        });

        //SubGrupo3"Gestion Puntilleria"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem4_3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Devoluciones Puntilleria");
        TreeNode subChild4_3 = new TreeNode(subChildItem4_3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        subChild4_3.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, PedidoDevolucionMateriaPrimaPuntilleria.class);
            intent.putExtra("nit_usuario",cd);
            intent.putExtra("bod_origen",12);
            intent.putExtra("bod_destino",2);
            intent.putExtra("modelo","14");
            startActivity(intent);
        });

        //Agregamos subgrupo1"Gestion Puntilleria".
        //child4.addChild(subChild4_1);

        //Agregamos subgrupo2"Gestion Puntilleria".
        child4.addChild(subChild4_2);

        //Agregamos subgrupo2"Gestion Puntilleria".
        child4.addChild(subChild4_3);

        //Agregamos Grupo"Gestion Puntilleria".
        //parent.addChildren(child1);
        root.addChild(child4);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Revisión - Calidad"
        MyHolder.IconTreeItem childItem5 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Revisión - Calidad");
        TreeNode child5 = new TreeNode(childItem5).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Revisión - Calidad"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem5_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Galvanizado");
        TreeNode subChild5_1 = new TreeNode(subChildItem5_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild5_1.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(MainActivity.this, RevisionTerminadoGalvanizado.class);
                intent.putExtra("nit_usuario", cd);
                startActivity(intent);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });


        //SubGrupo2"Revisión - Calidad"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem5_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Trefilación (Brillante y Especial)");
        TreeNode subChild5_2 = new TreeNode(subChildItem5_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild5_2.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(MainActivity.this, RevisionTerminadoTrefilacion.class);
                intent.putExtra("nit_usuario", cd);
                startActivity(intent);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //SubGrupo3"Mesas Empaque"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem5_3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Recocido Industrial");
        TreeNode subChild5_3 = new TreeNode(subChildItem5_3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild5_3.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, RevisionTerminadoRecocido.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            intent.putExtra("tipo","industrial");
            startActivity(intent);
        });

        //SubGrupo3"Mesas Empaque"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem5_4 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Recocido Construcción");
        TreeNode subChild5_4 = new TreeNode(subChildItem5_4).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild5_4.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, RevisionTerminadoRecocido.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            intent.putExtra("tipo","construccion");
            startActivity(intent);
        });

        //SubGrupo3"Mesas Empaque"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem5_5 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Puntilleria");
        TreeNode subChild5_5 = new TreeNode(subChildItem5_5).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild5_5.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, ResumenPunti.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            startActivity(intent);
        });

        //SubGrupo1"Revisión - Calidad"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem5_6 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Muestreo Galvanizado");
        TreeNode subChild5_6 = new TreeNode(subChildItem5_6).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild5_6.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(MainActivity.this, Muestreo_galvanizado.class);
                intent.putExtra("nit_usuario", cd);
                startActivity(intent);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //Agregamos subgrupo1"Revisión - Calidad: Galvanizado".
        //child5.addChild(subChild5_1);

        //Agregamos subgrupo2"Revisión - Calidad: Trefilación".
        child5.addChild(subChild5_2);

        //Agregamos subgrupo2"Revisión - Calidad: Recocido Industrial".
        child5.addChild(subChild5_3);

        //Agregamos subgrupo2"Revisión - Calidad: Recocido de Construcción".
        child5.addChild(subChild5_4);

        //Agregamos subgrupo1"Revisión - Calidad: Puntilleria".
        //child5.addChild(subChild5_5);

        //Agregamos subgrupo1"Revisión - Calidad: Muestreo Galvanizado".
        //child5.addChild(subChild5_6);


        //Agregamos subgrupo2"Revisión - Calidad: Mesas Empaque".
        //child5.addChild(subChild5_3); Todavia no hay desarrollado un modulo de calidad para empaque

        //Agregamos Grupo"Revisión - Calidad".
        //parent.addChildren(child1);
        root.addChild(child5);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Logistica - Recepción"
        MyHolder.IconTreeItem childItem6 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Logistica - Recepción");
        TreeNode child6 = new TreeNode(childItem6).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Logistica - Recepción"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Galvanizado");
        TreeNode subChild6_1 = new TreeNode(subChildItem6_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_1.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(MainActivity.this, EscanerInventario.class);
                intent.putExtra("nit_usuario", cd);
                startActivity(intent);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //SubGrupo2"Logistica - Recepción"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Trefilación (Brillante y Especial)");
        TreeNode subChild6_2 = new TreeNode(subChildItem6_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_2.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(MainActivity.this, RecepcionTerminadoTrefilacion.class);
                intent.putExtra("nit_usuario", cd);
                startActivity(intent);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //SubGrupo3"Logistica - Recepción "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Recocido Industrial");
        TreeNode subChild6_3 = new TreeNode(subChildItem6_3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_3.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(MainActivity.this, RecepcionTerminadoRecocido.class);
                intent.putExtra("nit_usuario", cd);
                intent.putExtra("tipo","industrial");
                startActivity(intent);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //SubGrupo3"Logistica - Recepción "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_4 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Recocido Construcción");
        TreeNode subChild6_4 = new TreeNode(subChildItem6_4).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_4.setClickListener((node, value) -> {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(MainActivity.this, RecepcionTerminadoRecocido.class);
                intent.putExtra("nit_usuario", cd);
                intent.putExtra("tipo","construccion");
                startActivity(intent);
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });

        //SubGrupo4"Logistica - Recepción "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_5 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Púas");
        TreeNode subChild6_5 = new TreeNode(subChildItem6_5).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_5.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, RecepcionTerminadoPuasOperarios.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            startActivity(intent);
        });

        //SubGrupo4"Logistica - Recepción "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_6 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Puntilleria");
        TreeNode subChild6_6 = new TreeNode(subChildItem6_6).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_6.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, ResumenPunti.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            startActivity(intent);
        });

        //SubGrupo6"Logistica - Recepción"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_7 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Auditoria Logistica");
        TreeNode subChild6_7 = new TreeNode(subChildItem6_7).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_7.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, AuditoriaLogistica.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            startActivity(intent);
        });

        //Agregamos subgrupo1"Logistica - Recepción: Galvanizado".
        child6.addChild(subChild6_1);

        //Agregamos subgrupo2"Logistica - Recepción: Trefilación".
        child6.addChild(subChild6_2);

        //Agregamos subgrupo3"Logistica - Recepción: Recocido Industrial".
        child6.addChild(subChild6_3);

        //Agregamos subgrupo4"Logistica - Recepción: Recocido Construcción".
        child6.addChild(subChild6_4);

        //Agregamos subgrupo4"Logistica - Recepción: Puas".
        child6.addChild(subChild6_5);

        //Agregamos subgrupo4"Logistica - Recepción: Puntilleria".
        //child6.addChild(subChild6_6);

        //Agregamos subgrupo4"Logistica - Recepción: Auditoria Logistica".
        child6.addChild(subChild6_7);

        //Agregamos Grupo"Logistica - Recepción".
        //parent.addChildren(child1);
        root.addChild(child6);


        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Otros"
        MyHolder.IconTreeItem childItem7 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Otros");
        TreeNode child7 = new TreeNode(childItem7).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Otros "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem7_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Consumo scal");
        TreeNode subChild7_1 = new TreeNode(subChildItem7_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild7_1.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, PedidoMateriaPrimaScal.class);
            intent.putExtra("nit_usuario",cd);
            intent.putExtra("bod_origen",2);
            intent.putExtra("bod_destino",2);
            intent.putExtra("modelo","01");
            startActivity(intent);
        });

        //SubGrupo2"Otros "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem7_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Consumo scae");
        TreeNode subChild7_2 = new TreeNode(subChildItem7_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild7_2.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, PedidoMateriaPrimaScae.class);
            intent.putExtra("nit_usuario",cd);
            intent.putExtra("bod_origen",2);
            intent.putExtra("bod_destino",2);
            intent.putExtra("modelo","01");
            startActivity(intent);
        });

        //SubGrupo3"Otros "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem7_3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Consumo sar");
        TreeNode subChild7_3 = new TreeNode(subChildItem7_3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild7_3.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, PedidoMateriaPrimaSar.class);
            intent.putExtra("nit_usuario",cd);
            intent.putExtra("bod_origen",2);
            intent.putExtra("bod_destino",2);
            intent.putExtra("modelo","01");
            startActivity(intent);
        });

        //SubGrupo4"Otros "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem7_4 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Consumo sav");
        TreeNode subChild7_4 = new TreeNode(subChildItem7_4).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild7_4.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, PedidoMateriaPrimaSav.class);
            intent.putExtra("nit_usuario",cd);
            intent.putExtra("bod_origen",2);
            intent.putExtra("bod_destino",2);
            intent.putExtra("modelo","01");
            startActivity(intent);
        });

        /////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////// SE CREA SUBGRUPO PARA INVENTARIOS //////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////
        //SubGrupo5"Otros "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem7_5 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Control de inventario");
        TreeNode subChild7_5 = new TreeNode(subChildItem7_5).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild7_5.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, Control_Inventarios.class);
            intent.putExtra("nit_usuario", cd);
            startActivity(intent);
        });

        //SubGrupo5" Inventario "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItemInventario = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Inventario");
        TreeNode subChildInventario = new TreeNode(subChildItemInventario).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChildInventario.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this,  Inventario_proceso_alambre.class);
            intent.putExtra("nit_usuario", cd);
            startActivity(intent);
        });


        //Agregamos subgrupo4"Otros".
        child7.addChild(subChildInventario);


        //Agregamos subgrupo1"Otros".
        child7.addChild(subChild7_1);

        //Agregamos subgrupo2"Otros".
        child7.addChild(subChild7_2);

        //Agregamos subgrupo3"Otros".
        child7.addChild(subChild7_3);

        //Agregamos subgrupo4"Otros".
        child7.addChild(subChild7_4);

        //Agregamos subgrupo5"Otros".
        //child7.addChild(subChild7_5); Se comenta porque es un modulo que se empezo, pero nunca se termino

        //Agregamos Grupo"Otros".
        //parent.addChildren(child1);
        root.addChild(child7);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Agregamos AndroidTreeView en la vista.
        AndroidTreeView tView = new AndroidTreeView(getApplicationContext(), root);
        ((LinearLayout) findViewById(R.id.ll_menu)).addView(tView.getView());
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que reproduce sonido y hace vibrar el dispositivo
    public void AudioError(){
        sp.play(sonido_de_Reproduccion,100,100,1,0,0);
        vibrator.vibrate(2000);
    }
}