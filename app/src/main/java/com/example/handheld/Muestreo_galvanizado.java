package com.example.handheld;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.handheld.ClasesOperativas.objOperacionesDb;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.CodigoGalvModelo;
import com.example.handheld.modelos.Galvanizado_muestras_modelo;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Muestreo_galvanizado extends AppCompatActivity {
    objOperacionesDb obj_db;
    Conexion conexion;
    String nit_usuario;
    Button iniciar;
    Button finalizar;
    Button agregar;
    Button editar;
    EditText nro_bobina;
    EditText calibre;
    RadioButton puas;
    RadioButton externo;
    RadioButton grapas;
    EditText diametro_ini;
    //EditText diametro_fin;
    EditText vr_max;
    EditText vr_min;
    EditText vr_diferencia;
    EditText codigo;
    EditText velocidad_bobina;
    EditText traccion;
    RadioButton acabado_ok;
    RadioButton acabado_no;
    RadioButton nivel_cuba_ok;
    RadioButton nivel_cuba_no;
    EditText acabado_desc;
    EditText temp_horno;
    EditText temp_cuba;
    EditText concentracion_hci_decapado;
    EditText concentracion_fe_decapado;
    EditText grados_baume;
    EditText ph_decapado;
    String acabado = "";
    String id= "";
    Float longitud ;
    Galvanizado_muestras_modelo modelo;

    FragmentManager fragmentManager;

    FragmentTransaction fragmentTransaction;

    Spinner spinner;
    ArrayList<CodigoGalvModelo> tiposLista = new ArrayList<>();
    ArrayList<String> listaTp;
    ArrayList<String> listaTipos;

    private String codigoSeleccionado;

    // variables para controlar radiobutton
    RadioButton rbLeft, rbRight;
    RadioButton rbLeftp, rbRightp;
    String cd;

    CardView datos_proces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muestreo_galvanizado);
        conexion = new Conexion();
        obj_db = new objOperacionesDb();
        //Se inicializan los elementos del layout

        iniciar = findViewById(R.id.btn_iniciar);
        finalizar = findViewById(R.id.btn_terminar);
        agregar = findViewById(R.id.btn_registrar);
        editar = findViewById(R.id.editar_muestreo);
        nro_bobina = findViewById(R.id.txt_nro_bobina);
        calibre = findViewById(R.id.txt_calibre);
        puas = findViewById(R.id.radiobuton_puas);
        externo = findViewById(R.id.radiobuton_externo);
        grapas = findViewById(R.id.radiobutton_grapas);
        diametro_ini = findViewById(R.id.txt_diametro_ini);
        //diametro_fin = findViewById(R.id.txt_diametro_fin);
      ///////  vr_max = findViewById(R.id.txt_vr_max);
    ///////    vr_min = findViewById(R.id.txt_vr_min);
        //vr_diferencia = findViewById(R.id.txt_vr_diferencia);
        codigo = findViewById(R.id.txt_codigo);
        velocidad_bobina = findViewById(R.id.txt_velocidad_bobina);
        //traccion = findViewById(R.id.txt_traccion);
        nit_usuario = getIntent().getStringExtra("nit_usuario");
         acabado_desc = findViewById(R.id.txt_observaciones_acabado);
         temp_horno = findViewById(R.id.txt_temp_horno);
         temp_cuba = findViewById(R.id.txt_temp_cuba_zinc);
         concentracion_hci_decapado = findViewById(R.id.txt_concen_hci_decapado);
         concentracion_fe_decapado = findViewById(R.id.txt_concen_fe_decapado);
         grados_baume = findViewById(R.id.txt_grados_beume);
         ph_decapado = findViewById(R.id.txt_ph_decapado);
         acabado_ok= findViewById(R.id.rbLeft);
         acabado_no = findViewById(R.id.rbRight);
         nivel_cuba_ok = findViewById(R.id.radiobutton_ok_nivel_cuba);
         nivel_cuba_no = findViewById(R.id.radiobutton_no_nivel_cuba);
        spinner = findViewById(R.id.spinner);
        rbLeft = findViewById(R.id.rbLeft);
        rbRight = findViewById(R.id.rbRight);
        rbLeftp = findViewById(R.id.rbsi_proceso);
        rbRightp = findViewById(R.id.rbno_proceso);
        datos_proces = findViewById(R.id.card_proceso);
        ocultarCardView();
        //acabado_no.setChecked(true);
        //iniciar todo deshabilitado, y validar que no hay proceso iniciado
        //Activamos el metodo para consultar los tipos
        consultarCodigos();
        validar_sin_cerrar();



        //Se programa el boton consultar
        iniciar.setOnClickListener(view -> {
                //logica
            try {
                desbloquear();
            } catch (SQLException e) {
                toastError(e.getMessage());
            }
        });
        //metodo para finalizar muestreos abiertos
        finalizar.setOnClickListener(view -> {
            //logica
            try {
                finalizar_muestreo();
            } catch (SQLException e) {
                toastError(e.getMessage());
            }
        });
        //metodo para agregar informacion a la base de datos
        agregar.setOnClickListener(view -> {
            //logica
            try {
                ingresar_informacion();
            } catch (SQLException e) {
                toastError(e.getMessage());
            }
        });

        editar.setOnClickListener(view -> {
            //logica
            Intent intent = new Intent(Muestreo_galvanizado.this, ListPersonaActivity.class);
            intent.putExtra("nit_usuario",nit_usuario);
            startActivity(intent);
        });

//        acabado_ok.setOnClickListener(view -> {
//            //logica
//            if (acabado_ok.isChecked()){
//                acabado_desc.setEnabled(false);
//            }else{
//                acabado_desc.setEnabled(true);
//            }
//        });

//        acabado_no.setOnClickListener(view -> {
//            //logica
//            if (acabado_no.isChecked()){
//                acabado_desc.setEnabled(true);
//            }else{
//                acabado_desc.setEnabled(false);
//            }
//        });
    }

    public void onRadioButtonClicked(View view){
        boolean isSelect = ((RadioButton)view).isChecked();

        switch (view.getId()){
            case R.id.rbLeft:
                if(isSelect){
                    rbLeft.setTextColor(Color.WHITE);
                    rbRight.setTextColor(Color.RED);

                }
                break;
            case R.id.rbRight:
                if(isSelect){
                    rbRight.setTextColor(Color.WHITE);
                    rbLeft.setTextColor(Color.BLACK);

                }
                break;

        }
    }

    public void onRadioButton2Clicked(View view){
        boolean isSelect = ((RadioButton)view).isChecked();

        switch (view.getId()){
            case R.id.radiobuton_puas:
                if(isSelect){
                    puas.setTextColor(Color.BLACK);
                    externo.setTextColor(Color.BLACK);
                    grapas.setTextColor(Color.BLACK);
                }
                break;
            case R.id.radiobuton_externo:
                if(isSelect){
                    puas.setTextColor(Color.BLACK);
                    externo.setTextColor(Color.BLACK);
                    grapas.setTextColor(Color.BLACK);
                }
                break;
            case R.id.radiobutton_grapas:
                if(isSelect){
                    puas.setTextColor(Color.BLACK);
                    externo.setTextColor(Color.BLACK);
                    grapas.setTextColor(Color.BLACK);
                }
                break;
        }
    }

    public void onRadioButton3Clicked(View view){
        boolean isSelect = ((RadioButton)view).isChecked();

        switch (view.getId()){
            case R.id.rbsi_proceso:
                if(isSelect){
                    rbLeftp.setTextColor(Color.WHITE);
                    rbRightp.setTextColor(Color.RED);
                    mostrarCardView();
                }
                break;
            case R.id.rbno_proceso:
                if(isSelect){
                    rbRightp.setTextColor(Color.WHITE);
                    rbLeftp.setTextColor(Color.BLACK);
                    ocultarCardView();
                }
                break;

        }
    }
    // Método para mostrar el CardView
    private void mostrarCardView() {
        CardView cardView = findViewById(R.id.card_proceso); // Reemplaza 'tuCardView' con el ID de tu CardView
        cardView.setVisibility(View.VISIBLE);
    }

    // Método para ocultar el CardView
    private void ocultarCardView() {
        CardView cardView = findViewById(R.id.card_proceso); // Reemplaza 'tuCardView' con el ID de tu CardView
        cardView.setVisibility(View.GONE);
    }

    public  void ocultar_datos_proceso(){
        //desabilitar layout de datos procesos
    }

    public void consultarCodigos(){
        conexion = new Conexion();

        tiposLista = conexion.obtenerCodigos(getApplication());
        listaTp = obtenerLista(tiposLista);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(Muestreo_galvanizado.this, android.R.layout.simple_spinner_item, listaTp);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtiene el CodigoGalvModelo seleccionado
                CodigoGalvModelo codigoSeleccionadoModelo = tiposLista.get(position);

                // Establece el código en el campo correspondiente
                codigoSeleccionado = codigoSeleccionadoModelo.getCodigo();
                longitud = Float.parseFloat(codigoSeleccionadoModelo.getLongitud());
                codigo.setText(codigoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Aquí puedes manejar la lógica si no se selecciona nada
            }
        });

    }

    //Metodoque recibe una lista tipo transmodelo, la recorre y añade a otra lista tipo String
    private ArrayList<String> obtenerLista(ArrayList<CodigoGalvModelo> tiposLista ){
        listaTipos = new ArrayList<>();
        //listaTipos.add("Seleccione");

        for(int i = 0; i < tiposLista.size(); i++){
            listaTipos.add(tiposLista.get(i).getDescripcion());
        }
        return listaTipos;
    }

    public void ingresar_informacion() throws SQLException {
        if (codigo.getText().length() > 0) {
            //validar codigo
            if (conexion.valorTodo(this,"Select codigo from F_galv_longitud_codigo where codigo = '"+codigo.getText()+"'").length() > 0){
                //realizar ingreso
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String currentDateandTime = simpleDateFormat.format(new Date());
                String destino = "";
                String nivel_Cuba = "";
                double cubrimiento_zaba = 0.f;
                Float diferencia = 0.f;
                Float longitud_d = 0.f;
                Double diametro_d = 0.0;
                if(puas.isChecked()){
                    destino = "P";
                } else if (externo.isChecked()) {
                    destino = "E";
                }else if(grapas.isChecked()){
                    destino = "G";
                }
                if(nivel_cuba_ok.isChecked()){
                    nivel_Cuba = "ok";
                } else if (nivel_cuba_no.isChecked()) {
                    nivel_Cuba = "mal";
                }

                if(acabado_ok.isChecked()){
                    acabado = "ok";
                    acabado_desc.setText("Bien");
                } else if (acabado_no.isChecked()) {
                    acabado = "mal";
                 //   acabado_desc.setEnabled(true);
                }
                //validar diferencia
//                if(vr_max.getText().length() > 0 && vr_min.getText().length() >0){
//                    diferencia = Float.parseFloat(String.valueOf(vr_max.getText())) -  Float.parseFloat(String.valueOf(vr_min.getText()));
//                }else{
//                    diferencia = Float.valueOf(0);
//                }
                //este metodo se crea para evaluar la diferencia con 1 se debe quitar para su proceso
                diferencia = Float.valueOf(10);

                //metodo para verificar si lo ingresado cumple con el modelo
                Double diametro_g = Double.valueOf(conexion.valorTodo(this,"Select diametro from F_galv_longitud_codigo where codigo = '"+codigo.getText()+"'"));
                Double longitud_g = Double.valueOf(conexion.valorTodo(this,"Select longitud from F_galv_longitud_codigo where codigo = '"+codigo.getText()+"'"));
                Double calibre_g = Double.valueOf(conexion.valorTodo(this,"Select calibre from F_galv_longitud_codigo where codigo = '"+codigo.getText()+"'"));
                //longitud = Float.valueOf(modelo.getLongitud());
                //longitud_d = Float.parseFloat(String.valueOf(longitud));
                if (diametro_ini.getText().length() > 0){
                    diametro_d = Double.parseDouble(String.valueOf(diametro_ini.getText()));
                }else{
                    diametro_d = Double.valueOf(0);
                }
                // calcular longitud para recubrimiento zaba  CAMBIAR A CAPTURAR SEGUN CALIBRE Y CODIGO
                //longitud = 100;
                //calcular cubrimiento zaba
                cubrimiento_zaba = (787.f * diferencia * 0.000853 * 1000000.f )/ (3.1416 * diametro_d * longitud_g * 297.f);
                //cubrimiento_zaba = (787.f * diferencia * 0.000853 * 1000000.f )/ (3.1416 * diametro_d * longitud * 297.f);
                int nro_bobina_i ;
                if (nro_bobina.getText().length() > 0){
                    nro_bobina_i = Integer.parseInt(nro_bobina.getText().toString());
                }else{
                    nro_bobina_i = 0;
                }
                Double calibre_i ;
                if (calibre.getText().length() >0){
                    calibre_i = Double.parseDouble(calibre.getText().toString());
                }else{
                    calibre_i = Double.valueOf(0);
                }
                Double diametro_ini_i;
                if(diametro_ini.getText().length() > 0){
                    diametro_ini_i = Double.parseDouble(diametro_ini.getText().toString());
                }else{
                    diametro_ini_i= Double.valueOf(0);
                }
                Double vr_max_i;
//                if(vr_max.getText().length() > 0){
//                    vr_max_i = Double.parseDouble(vr_max.getText().toString());
//                }else{
//                    vr_max_i= Double.valueOf(0);
//                }
//                Double vr_min_i;
//                if(vr_min.getText().length() > 0){
//                    vr_min_i = Double.parseDouble(vr_min.getText().toString());
//                }else{
//                    vr_min_i= Double.valueOf(0);
//                }
                Double velocidad_bobina_i;
                if(velocidad_bobina.getText().length() > 0){
                    velocidad_bobina_i = Double.parseDouble(velocidad_bobina.getText().toString());
                }else{
                    velocidad_bobina_i= Double.valueOf(0);
                }
                Double traccion_i;
//                if(traccion.getText().length() > 0){
//                    traccion_i = Double.parseDouble(traccion.getText().toString());
//                }else{
//                    traccion_i= Double.valueOf(0);
//                }
                Double temp_horno_i;
                if(temp_horno.getText().length() > 0){
                    temp_horno_i = Double.parseDouble(temp_horno.getText().toString());
                }else{
                    temp_horno_i= Double.valueOf(0);
                }
                Double temp_cuba_i;
                if(temp_cuba.getText().length() > 0){
                    temp_cuba_i = Double.parseDouble(temp_cuba.getText().toString());
                }else{
                    temp_cuba_i= Double.valueOf(0);
                }
                Double concentracion_hci_decapado_i;
                if(concentracion_hci_decapado.getText().length() > 0){
                    concentracion_hci_decapado_i = Double.parseDouble(concentracion_hci_decapado.getText().toString());
                }else{
                    concentracion_hci_decapado_i= Double.valueOf(0);
                }
                Double concentracion_fe_decapado_i;
                if(concentracion_fe_decapado.getText().length() > 0){
                    concentracion_fe_decapado_i = Double.parseDouble(concentracion_fe_decapado.getText().toString());
                }else{
                    concentracion_fe_decapado_i= Double.valueOf(0);
                }
                Double grados_baume_i;
                if(grados_baume.getText().length() > 0){
                    grados_baume_i = Double.parseDouble(grados_baume.getText().toString());
                }else{
                    grados_baume_i= Double.valueOf(0);
                }
                Double ph_decapado_i;
                if(ph_decapado.getText().length() > 0){
                    ph_decapado_i = Double.parseDouble(ph_decapado.getText().toString());
                }else{
                    ph_decapado_i= Double.valueOf(0);
                }

                //validar si diametro y diferencia son numericos entonces procede ,sino no continua

                if(diametro_d >= 0 && diferencia >= 0 && calibre.getText().length() >0){
                    String sql = "insert into F_det_muestreo_galvanizado " +
                            "(Id_muestreo,Nit,Nro_bobina,Destino,Fecha_hora,Calibre,Diametro_inicial,Diferencia,Velocidad_bobina," +
                            "acabado_des,temp_horno,temp_cuba,hci_decapado,fe_decapado,grados_baume,ph_decapado,acabado,nivel_cuba,longitud,codigo) " +
                            "values ("+id+",'"+nit_usuario+"',"+nro_bobina_i+",'"+destino+"','"+currentDateandTime+"',"+calibre_i+"," +
                            ""+diametro_ini_i+","+diferencia+","+velocidad_bobina_i+"," +
                            "'"+acabado_desc.getText()+"',"+temp_horno_i+","+temp_cuba_i+","+concentracion_hci_decapado_i+","+concentracion_fe_decapado_i+","+grados_baume_i+"," +
                            ""+ph_decapado_i+",'"+acabado+"','"+nivel_Cuba+"',"+longitud+",'"+codigo.getText()+"') ";
                    if (obj_db.ejecutarInsertJjprgproduccion(sql,Muestreo_galvanizado.this) > 0){
                        toastSuccesfull("Registro ingresado de forma correcta ");
                            if(calibre_g == Double.parseDouble(String.valueOf(calibre.getText()))){
                            }else{
                                toastinfo("El calibre no corresponde al estandar del codigo ingresado, aun asi se ingresa el registro");
                            }
                        if(diametro_g == Double.parseDouble(String.valueOf(diametro_ini.getText()))){
                        }else{
                            toastinfo("El diametro no corresponde al codigo ingresado, aun asi se ingresa el registro");
                        }
                        nuevo();
                    }else{
                        toastError("No se pudo ingresar el registro, diametro o o diferencia de VR deben esta completadas");
                    }
                }else{
                    toastError("NO SE HA INGRESADO LA DIFERENCIA Ó EL DIAMETRO PARA CALCULAR EL RECUBRIMIENTO DE ZINC");
                }
            }else{
                //codigo no existe
                toastError("El codigo ingresado no esta registrado,verificar con calidad");
            }
        }else{
            toastError("El campo codigo. no puede estar vacio");
        }
    }

    public  void finalizar_muestreo() throws SQLException {
        //consultar id para insertar
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentDateandTime = simpleDateFormat.format(new Date());
        String sql = "update F_enc_muestreo_galvanizado set Fecha_fin = '" + currentDateandTime + "' where Id_muestreo = "+id+" and nit = "+nit_usuario+" and Fecha_fin is null";
        if (obj_db.ejecutarUpdateDbProduccion(sql,Muestreo_galvanizado.this) > 0){
            toastSuccesfull("Se finalizó el muestreo Nro: " + id );
            inicio();
        }else{
            toastError("No se pudo finalizar el muestreo");
        }
    }
    public void nuevo(){
        //Se inicializan los elementos del layout
        iniciar.setEnabled(false);
        finalizar.setEnabled(true);
        agregar.setEnabled(true);
        nro_bobina.setText("");
        calibre.setText("");
        diametro_ini.setText("");
        //diametro_fin.setText("");
    //    vr_max.setText("");
   //     vr_min.setText("");
        //vr_diferencia.setText("");
        codigo.setText("");
        velocidad_bobina.setText("");
   //     traccion.setText("");
        acabado_desc.setText("");
        temp_horno.setText("");
        temp_cuba.setText("");
        concentracion_hci_decapado.setText("");
        concentracion_fe_decapado.setText("");
        grados_baume.setText("");
        ph_decapado.setText("");
    }
    public void inicio(){
        //Se inicializan los elementos del layout
        iniciar.setEnabled(true);
        finalizar.setEnabled(false);
        agregar.setEnabled(false);
        nro_bobina.setEnabled(false);
        calibre.setEnabled(false);
        puas.setEnabled(false);
        externo.setEnabled(false);
        diametro_ini.setEnabled(false);
        //diametro_fin.setEnabled(false);
      //  vr_max.setEnabled(false);
       // vr_min.setEnabled(false);
        //vr_diferencia.setEnabled(false);
        codigo.setEnabled(false);
        velocidad_bobina.setEnabled(false);
     //   traccion.setEnabled(false);
    }
    public void desbloquear() throws SQLException {
        //consultar id para insertar
        String sql = "select max(Id_muestreo)+1 from F_enc_muestreo_galvanizado";
        id = conexion.valorTodo(this,sql);
        //insertar id y nit y fecha en la bd para empezar el proceso
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentDateandTime = simpleDateFormat.format(new Date());
        sql = "insert into F_enc_muestreo_galvanizado (Id_muestreo,Nit,Fecha_inicio) values ("+id+","+nit_usuario+",'"+currentDateandTime+"')";
        if (obj_db.ejecutarInsertJjprgproduccion(sql,Muestreo_galvanizado.this) >= 0){
            toastSuccesfull("Se inicio el muestreo Nro: " + id);
        }else{
            toastError("No se pudo iniciar el muestreo");
        }
        //Se inicializan los elementos del layout
        iniciar.setEnabled(false);
        finalizar.setEnabled(true);
        agregar.setEnabled(true);
        nro_bobina.setEnabled(true);
        calibre.setEnabled(true);
        puas.setEnabled(true);
        externo.setEnabled(true);
        diametro_ini.setEnabled(true);
        //diametro_fin.setEnabled(true);
    //    vr_max.setEnabled(true);
     //   vr_min.setEnabled(true);
//        vr_diferencia.setEnabled(true);
        codigo.setEnabled(true);
        velocidad_bobina.setEnabled(true);
      //  traccion.setEnabled(true);
        acabado_desc.setEnabled(true);
        temp_horno.setEnabled(true);
        temp_cuba.setEnabled(true);
        concentracion_hci_decapado.setEnabled(true);
        concentracion_fe_decapado.setEnabled(true);
        grados_baume.setEnabled(true);
        ph_decapado.setEnabled(true);
        acabado_ok.setEnabled(true);
        acabado_no.setEnabled(true);
        nivel_cuba_ok.setEnabled(true);
        nivel_cuba_no.setEnabled(true);
    }
    public String validar_sin_cerrar(){
        String estado = "";
        try {
            String sql = "Select Id_muestreo from F_enc_muestreo_galvanizado where nit = " + nit_usuario + " and Fecha_fin is null";
            estado = conexion.valorTodo(Muestreo_galvanizado.this,sql);
            if (estado.isEmpty()) {
                inicio();
            }else{
                toastError("Hay ordenes pendientes");
                id = estado;
                //bloquear todo para cerrar
                bloquear();
            }
        }catch (Exception e){
            toastError(e.getMessage());
        }
        return estado;
    }

    //METODO DE TOAST PERSONALIZADO : PERSONA NO ENCONTRADA
    public void toastError(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_per_no_encon,  findViewById(R.id.ll_custom_toast_per_no_encon));
        TextView txtMensaje = view.findViewById(R.id.txtMensajeToast1);
        txtMensaje.setText(msg);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM,0,200);
        toast.setDuration(10 * 1000);
        toast.setView(view);
        toast.show();
    }

    public void toastinfo(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_actualizado,  findViewById(R.id.ll_custom_toast_actualizado));
        TextView txtMensaje = view.findViewById(R.id.txtMsgToast);
        txtMensaje.setText(msg);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM,0,200);
        toast.setDuration(10 * 1000);
        toast.setView(view);
        toast.show();
    }

    public void toastSuccesfull(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_acierto,  findViewById(R.id.ll_custom_toast_acierto));
        TextView txtMensaje = view.findViewById(R.id.txtMensa);
        txtMensaje.setText(msg);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM,0,200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    public void bloquear(){
        //Se inicializan los elementos del layout
        iniciar.setEnabled(false);
        finalizar.setEnabled(true);
        agregar.setEnabled(false);
        nro_bobina.setEnabled(false);
        calibre.setEnabled(false);
        puas.setEnabled(false);
        externo.setEnabled(false);
        diametro_ini.setEnabled(false);
        //diametro_fin.setEnabled(false);
    //    vr_max.setEnabled(false);
     //   vr_min.setEnabled(false);
        //vr_diferencia.setEnabled(false);
        codigo.setEnabled(false);
        velocidad_bobina.setEnabled(false);
     //   traccion.setEnabled(false);
        acabado_desc.setEnabled(true);
        temp_horno.setEnabled(false);
        temp_cuba.setEnabled(false);
        concentracion_hci_decapado.setEnabled(false);
        concentracion_fe_decapado.setEnabled(false);
        grados_baume.setEnabled(false);
        ph_decapado.setEnabled(false);
        acabado_ok.setEnabled(false);
        acabado_no.setEnabled(false);
        nivel_cuba_ok.setEnabled(false);
        nivel_cuba_no.setEnabled(false);
    }
}