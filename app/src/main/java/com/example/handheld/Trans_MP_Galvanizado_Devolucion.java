package com.example.handheld;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld.ClasesOperativas.CaptureActivityPortraint;
import com.example.handheld.ClasesOperativas.Gestion_alambronLn;
import com.example.handheld.ClasesOperativas.Ing_prod_ad;
import com.example.handheld.ClasesOperativas.ObjTraslado_bodLn;
import com.example.handheld.ClasesOperativas.Obj_ordenprodLn;
import com.example.handheld.ClasesOperativas.objOperacionesDb;
import com.example.handheld.atv.holder.adapters.listescanerAdapter;
import com.example.handheld.conexionDB.Conexion;


import com.example.handheld.conexionDB.ConfiguracionBD;
import com.example.handheld.databinding.ActivityEscanerBinding;
import com.example.handheld.modelos.DetalleTranModelo;
import com.example.handheld.modelos.PermisoPersonaModelo;
import com.example.handheld.modelos.TipotransModelo;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Trans_MP_Galvanizado_Devolucion extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ActivityEscanerBinding binding;

    //Se declaran los elementos del layout
    EditText etCodigo;
    TextView txtTransaccion, txtKilosRollo, txtIngMovimientos, lblCodigo, lblDescripcion, mensajeCargando;
    Button btnLeerCodigo, btnSalida, btnCancelar, btnTransaccion;
    Spinner spinner;

    //Se declaran las Herramientas para el listview
    ListView listviewEscaner;
    ListAdapter EscanerAdapter;
    List<DetalleTranModelo> ListaEscaner = new ArrayList<>();


    //Se inicializan las listas para el comboBox
    ArrayList<String> listaTipos;
    ArrayList<String> listaTp;
    ArrayList<TipotransModelo> tiposLista = new ArrayList<>();
    List<Object> listTransaccion_corsan;
    List<Object> listTransaccion_prodGalv;

    //Se inicializa un objeto conexion
    Conexion conexion;
    //Se declaran los objetos de otras clases necesarias

    Gestion_alambronLn obj_gestion_alambronLn = new Gestion_alambronLn();

    ObjTraslado_bodLn objTraslado_bodLn = new ObjTraslado_bodLn();

    com.example.handheld.ClasesOperativas.objOperacionesDb objOperacionesDb = new objOperacionesDb();

    Ing_prod_ad ing_prod_ad = new Ing_prod_ad();

    //se declaran las variables donde estaran los datos que vienen de la anterior clase

    Integer pNumero, pIdDetalle, bod_origen, bod_destino, repeticiones;
    String pfecha, pcodigo, pPendiente, pDescripcion, nit_usuario, modelo;

    String consecutivo,cod_orden, detalle,id_rollo,nit_proveedor,error;


    //Se inicializa variables necesarias en la clase

    boolean yaentre = false;



    Integer numero_transaccion;


    PermisoPersonaModelo personaEntrega, personaRecibe;

    ProgressBar cargando;

    //Se inicializa los varibles para el sonido de error
    SoundPool sp;
    int sonido_de_Reproduccion;

    //Se inicializa una instancia para hacer vibrar el celular
    Vibrator vibrator;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() == null){
            toastError("CANCELADO");
        }else{
            binding.etCodigo.setText(result.getContents());
            codigoIngresado();
            closeTecladoMovil();
        }
    });

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEscanerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Se Define los varibles para el sonido de error
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 1);
        sonido_de_Reproduccion = sp.load(this, R.raw.sonido_error_2, 1);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        //Se Definen los elementos de layout
        etCodigo = findViewById(R.id.etCodigo);
        txtTransaccion = findViewById(R.id.txtTransaccion);
        txtKilosRollo = findViewById(R.id.txtKilosRollo);
        txtIngMovimientos = findViewById(R.id.txtIngMovimientos);
        lblCodigo = findViewById(R.id.lblCodigo);
        lblDescripcion = findViewById(R.id.lblDescripcion);
        btnLeerCodigo = findViewById(R.id.btnLeerCodigo);
        btnSalida = findViewById(R.id.btnSalida);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnTransaccion = findViewById(R.id.btnTransaccion);
        spinner = findViewById(R.id.spinner);
        cargando = findViewById(R.id.cargando);
        mensajeCargando = findViewById(R.id.mensajeCargando);

        //Se definen las herramientas para el listView
        listviewEscaner = findViewById(R.id.listviewEscaner);
        listviewEscaner.setOnItemClickListener(this);

        //Recibimos los datos del pedido desde el anterior Activity
        pNumero = getIntent().getIntExtra("numero", 0);
        pIdDetalle = getIntent().getIntExtra("idDetalle", 0);
        pfecha = getIntent().getStringExtra("fecha");
        pcodigo = getIntent().getStringExtra("codigo");
        pPendiente = getIntent().getStringExtra("pendiente");
        pDescripcion = getIntent().getStringExtra("descripcion");
        //Recibimos los datos traidos desed el primer activity
        bod_origen = getIntent().getIntExtra("bod_origen", 0);
        bod_destino = getIntent().getIntExtra("bod_destino", 0);
        modelo = getIntent().getStringExtra("modelo");
        nit_usuario = getIntent().getStringExtra("nit_usuario");

        //Colocamos el titulo con la informacion
        txtTransaccion.setText(pcodigo + " - movimiento: bodega " + bod_origen + " - " + bod_destino);

        //Activamos el metodo para consultar los tipos
        consultarTipos();

        //Se establece el foco en el edit text
        etCodigo.requestFocus();

        //Se programa el boton de salida de la apicación
        btnSalida.setOnClickListener(this::salir);

        //Se programa el boton de lectura de codigo
        btnLeerCodigo.setOnClickListener(view -> {
            String barras = etCodigo.getText().toString();
            if (yaentre) {
                toastError("Rollo en proceso, Terminar la transferencia");
            }else {
                if (barras.equals("")) {
                    escanear();
                } else {
                    toastError("Borrar el texto escrito para abrir el escaner de la camara");
                    closeTecladoMovil();
                    codigoIngresado();
                }
            }
        });

        //Se programa el boton de transacción
        btnTransaccion.setOnClickListener(view -> {
            cargando.setVisibility(View.VISIBLE);
            if(validarFrm()){
                try {
                    guardar();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                cargando.setVisibility(View.INVISIBLE);
            }
            cargando.setVisibility(View.INVISIBLE);

        });

        //Se programa el boton cancelar
        btnCancelar.setOnClickListener(v -> {
            String proceso = (String) lblCodigo.getText();
            if(proceso.equals("LEA CODIGO")){
                toastError("No hay ningun rollo en proceso");
            }else{
                if(proceso.equals("")){
                    toastError("No hay ningun rollo en proceso");
                }else{
                    etCodigo.setEnabled(true);
                    //leer_nuevo();
                    toastError("Proceso cancelado, Lea codigo de nuevo!");
                }
            }

        });

        //Se programa para que al presionar enter en el edit text haga el proceso
        etCodigo.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if(!yaentre){
                    if(etCodigo.getText().toString().equals("")){
                        toastError("Por favor escribir o escanear el codigo de barras");
                    }else{
                        //Ocultamos el teclado de la pantalla
                        codigoIngresado();
                        yaentre = true;
                        closeTecladoMovil();
                    }
                    return true;
                }else{
                    //Cargamos de nuevo las varibles y cambiamos "yaentre" a 1 ó 0
                    cargarNuevo();
                }
                return false;
            }
            return false;
        });

        ingresarCedulas();
    }

    private void ingresarCedulas() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Trans_MP_Galvanizado_Devolucion.this);
        View mView = getLayoutInflater().inflate(R.layout.alertdialog_cedulastranslado,null);
        final EditText txtCedulaEntrega = mView.findViewById(R.id.txtCedulaEntrega);
        final EditText txtCedulaRecibe = mView.findViewById(R.id.txtCedulaRecibe);
        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
        Button btnCancelar = mView.findViewById(R.id.btnCancelar);
        builder.setView(mView);
        AlertDialog alertDialog = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            String CeEntrega = txtCedulaEntrega.getText().toString().trim();
            String CeRecibe= txtCedulaRecibe.getText().toString().trim();

            if (CeEntrega.equals("") || CeRecibe.equals("")){
                toastError("Por favor ingresar ambas cedulas");
            }else{
                if (CeEntrega.equals(CeRecibe)){
                    toastError("Ambas cedulas no pueden ser iguales");
                }else{
                    personaEntrega = conexion.obtenerPermisoPersonaAlambron(Trans_MP_Galvanizado_Devolucion.this,CeEntrega,"entrega" );

                    personaRecibe = conexion.obtenerPermisoPersonaAlambron(Trans_MP_Galvanizado_Devolucion.this,CeRecibe,"recibe" );
                    String permisoEntrega = personaEntrega.getPermiso();
                    String permisoRecibe = personaRecibe.getPermiso();
                    if(permisoEntrega.equals("E")){
                        if(permisoRecibe.equals("R")){
                            alertDialog.dismiss();
                        }else{
                            toastError("La cedula de la persona que recibe no corresponde a un montacarguista");
                        }
                    }else{
                        toastError("La cedula de la persona que entrega no corresponde a una de las permitidas");
                    }
                }
            }
        });
        btnCancelar.setOnClickListener(v -> {
            Intent i = new Intent(Trans_MP_Galvanizado_Devolucion.this,Pedido_MP_Galvanizado.class);
            startActivity(i);
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }




    public void escanear() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setPrompt("ESCANEAR CODIGO");
        options.setCameraId(0);
        options.setOrientationLocked(false);
        options.setBeepEnabled(true);
        options.setCaptureActivity(CaptureActivityPortraint.class);
        options.setBarcodeImageEnabled(false);

        barcodeLauncher.launch(options);
    }


    //METODO PARA CERRAR LA APLICACION
    @SuppressLint("")
    public void salir(View view){
        finishAffinity();
    }
    public void consultarTipos(){
        conexion = new Conexion();

        tiposLista = conexion.obtenerTipos(getApplication());
        listaTp = obtenerLista(tiposLista);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(Trans_MP_Galvanizado_Devolucion.this, android.R.layout.simple_spinner_item, listaTp);
        spinner.setEnabled(false);
        spinner.setClickable(false);
        spinner.setAdapter(adapter);
    }

    private void codigoIngresado() {
        consecutivo = etCodigo.getText().toString();
        // consecutivo = "444444218-4-3-168";
        if (validarCodigoBarras(consecutivo)) {
            cod_orden = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", consecutivo);
            detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("detalle", consecutivo);
            id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", consecutivo);

            if (validarRolloRegistrado(cod_orden, detalle, id_rollo)) {
                String sql_codigo = "SELECT O.prod_final FROM J_rollos_tref R, J_orden_prod_tef O WHERE O.consecutivo =" + cod_orden + " AND R.id_detalle =" + detalle + "  AND id_rollo =" + id_rollo + " and R.cod_orden=O.consecutivo";
                String sql_peso = "SELECT R.peso FROM J_orden_prod_tef O, J_rollos_tref R WHERE peso IS NOT NULL AND O.consecutivo =" + cod_orden + " AND R.id_detalle = " + detalle + " AND R.id_rollo=" + id_rollo + " and R.cod_orden=O.consecutivo";
                String peso = conexion.obtenerPesoTrefImport(getApplicationContext(), sql_peso);
                String codigo = conexion.obtenerCodigoTrefImport(getApplicationContext(), sql_codigo);

                if (codigo.equals(pcodigo)) {
                    Boolean valid = validarRolloConTransaccion(cod_orden, detalle, id_rollo);

                    if (valid) {
                        lblCodigo.setText(codigo);
                        String sql_descripcion = "SELECT descripcion FROM referencias WHERE codigo = '" + codigo + "'";
                        lblDescripcion.setText(conexion.obtenerDescripcionCodigo(Trans_MP_Galvanizado_Devolucion.this, sql_descripcion));
                        txtKilosRollo.setText(peso);
                        yaentre = true;
                        // Se bloquea el EditText ya que el tiquete fue leido correctamente
                        etCodigo.setEnabled(false);
                        toastAcierto("Rollo validado");
                    } else {
                        toastError("Este rollo ya tiene registrado consumos");
                        AudioError();
                        leer_nuevo();
                    }
                } else {
                    toastError("Este rollo no se encuentra en bodega 2");
                    AudioError();
                    leer_nuevo();
                }
            } else {
                toastError("El código de alambrón no pertenece al pedido");
                AudioError();
                leer_nuevo();
            }
        } else {
            toastError("El código de barras no se encuentra asignado");
            AudioError();
            leer_nuevo();
        }
    }






    private boolean validarCodigoBarras(String consecutivo) {
        boolean resp = false;

        String num_consecutivo = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", consecutivo);
        String detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("detalle", consecutivo);
        String id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", consecutivo);

        if (!num_consecutivo.isEmpty() && !detalle.isEmpty() && !id_rollo.isEmpty()) {
            String sql = "SELECT destino FROM J_rollos_tref WHERE cod_orden = " + num_consecutivo + " AND id_detalle = " + detalle + " AND id_rollo = " + id_rollo;
            String id = conexion.obtenerDestino(Trans_MP_Galvanizado_Devolucion.this, sql);
            if (id.equals("G")) {
                resp = true;
            } else {
                resp = false;
            }
        } else {
            toastError("Intente leerlo nuevamente, problemas con el tiquete");
            leer_nuevo();
        }
        return resp;
    }


    private boolean validarRolloRegistrado(String cod_orden, String detalle, String id_rollo){
        boolean resp = false;
        String sql = "SELECT (SELECT CASE WHEN (SELECT sum(peso) FROM J_salida_materia_prima_G_transaccion  WHERE numero = D.numero AND id_detalle = D.id_detalle ) is null THEN D.cantidad  ELSE (D.cantidad -(SELECT sum(peso) FROM J_salida_materia_prima_G_transaccion  WHERE numero = D.numero AND id_detalle = D.id_detalle )) END )As pendiente FROM J_salida_materia_prima_G_enc E ,J_salida_materia_prima_G_det D, CORSAN.dbo.referencias R where E.numero=" + pNumero + " AND E.anulado is null AND  R.codigo = D.codigo AND (e.devolver = 'S' OR e.devolver IS NULL ) AND E.numero = D.numero AND id_detalle = D.id_detalle";
        String peso = conexion.obtenerPesoTrefImport(getApplicationContext(), sql);
        if (!peso.isEmpty()){
            resp = true;
        }
        return resp;
    }


    private boolean validarRolloConTransaccion(String cod_orden, String detalle, String id_rollo){
        boolean respuesta = false;
        try {
            String sql = "SELECT traslado FROM J_rollos_tref WHERE cod_orden =" + cod_orden + " AND id_detalle = " + detalle + " AND id_rollo = " + id_rollo;
            String id = conexion.obtenerNumTranTrefImport(getApplicationContext(), sql);
            if (id.isEmpty()){
                respuesta = true;
            }
        }catch (Exception e){
            Toast.makeText(Trans_MP_Galvanizado_Devolucion.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        //Trabajo para el translator de bodega 11 a 2
        if (bod_origen.equals(11) && bod_destino.equals(2)){
            if (respuesta){
                respuesta = false;
            }else{
                respuesta = true;
            }
        }
        return respuesta;
    }



    private boolean validarFrm(){
        if (!lblCodigo.getText().toString().isEmpty() && !lblCodigo.getText().toString().equals("LEA CODIGO")){
            if (!txtKilosRollo.getText().toString().isEmpty()){
                if (!spinner.getSelectedItem().equals("Seleccione")){
                    if (conexion.existeCodigo(Trans_MP_Galvanizado_Devolucion.this, lblCodigo.getText().toString())){
                        if (Double.parseDouble(txtKilosRollo.getText().toString()) > 0) {
                            if (conexion.existeTipoTransaccion(Trans_MP_Galvanizado_Devolucion.this,spinner.getSelectedItem().toString())){
                                if (!etCodigo.getText().equals("")){
                                    if (validarCodigoBarras(consecutivo)){
                                        return true;
                                    }else{
                                        toastError("Verifique, El código de barras no se encuentra asignado!");
                                    }
                                }else{
                                    toastError("Verifique,No se leyo ningun código de barras!");
                                }
                            }else{
                                toastError("Verifique, No existe el tipo de transacción!");
                            }
                        }else{
                            toastError("Verifique, Los kilos no pueden ser negativos ó iguales a (0)");
                        }
                    }else{
                        toastError("El código ingresado no existe,verifique");
                    }
                }else{
                    toastError("Verifique, Seleccione un tipo de transacción");
                }
            }else{
                toastError("Verifique, faltan los KILOS");
            }
        }else{
            toastError("Verifique, falta el CÓDIGO");
        }
        return false;
    }


    //Metodo donde se obtienen todos los datos del rollo obtenidos por su codigo de barras
    //Y se envian al metodo realizar_transacción
    private void guardar() throws SQLException {
        cargando.setVisibility(View.VISIBLE);
        String tipo = spinner.getSelectedItem().toString();
        Double peso = Double.parseDouble(txtKilosRollo.getText().toString());
        String codigo = lblCodigo.getText().toString().trim();
        String bodega = objTraslado_bodLn.obtenerBodegaXcodigo(codigo);
        String stock = conexion.consultarStock(Trans_MP_Galvanizado_Devolucion.this,codigo,bodega);
        String nit_prov = obj_gestion_alambronLn.extraerDatoCodigoBarras("proveedor", consecutivo);
        String consecutivo_materia_prima = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("num_consecutivo", consecutivo);
        String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("detalle", consecutivo);
        String id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", consecutivo);
        String sql_costo_unit = "SELECT costo_unitario FROM referencias WHERE codigo ='" + codigo + "'";

        Double costo_unit = Double.parseDouble(conexion.obtenerCostoUnitTref(Trans_MP_Galvanizado_Devolucion.this,sql_costo_unit));

        try {
            realizar_transaccion(codigo, peso,consecutivo_materia_prima, id_detalle, id_rollo, tipo, costo_unit,nit_prov);
            etCodigo.requestFocus();
        }catch (Exception e){
            leer_nuevo();
            toastError(e.getMessage());
        }
    }

    @SuppressLint("SetTextI18n")
    public Boolean realizar_transaccion(String codigo, Double peso, String consecutivo_materia_prima, String id_detalle, String id_rollo,String tipo,Double costo_unit,String nit_prov )  throws SQLException {
        cargando.setVisibility(View.VISIBLE);
        boolean resp = true;
        listTransaccion_prodGalv = new ArrayList<>();
        String consecutivo = etCodigo.getText().toString();
        String sql_solicitud;
        String sql_detalle_salida;
        String sql_rollo;
        String sql_anulado;
        String sql_devuelto;
        listTransaccion_corsan = traslado_bodega(codigo, peso, tipo, costo_unit);
        sql_solicitud = "UPDATE J_rollos_tref SET destino = 'G', traslado=" + pNumero + " where id_detalle = " + detalle + " AND id_rollo= " + id_rollo +" and cod_orden ="+ cod_orden;;

        // Obtén la fecha y hora actual
        Date fechaActual = new Date();
        // Define el formato de la fecha y hora que deseas obtener
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Convierte la fecha actual en un String con el formato definido
        String fechaActualString = formatoFecha.format(fechaActual);

        sql_detalle_salida = "INSERT INTO jd_detalle_salida_alambron (nit_entrega,nit_recibe,fecha_transaccion,trb1) " +
                "VALUES (" + personaEntrega.getNit() + "," + personaRecibe.getNit() + ",'" + fechaActualString + "'," + numero_transaccion + ") ";

        try {
            //Se añade el sql a la lista
            listTransaccion_prodGalv.add(sql_solicitud);
        }catch (Exception e){
            Toast.makeText(Trans_MP_Galvanizado_Devolucion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        try {
            //Se añade el sql a la lista
            listTransaccion_prodGalv.add(sql_detalle_salida);
        }catch (Exception e){
            Toast.makeText(Trans_MP_Galvanizado_Devolucion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        repeticiones = 0;
        error = transaccion();
        if (error.equals("")){
            repeticiones = 0;
            error = tabla_produccion();
            if (error.equals("")){
                addRollo(consecutivo_materia_prima, id_detalle, id_rollo, tipo, peso,costo_unit,nit_prov);
                etCodigo.setEnabled(true);
                leer_nuevo();
                contar_movimientos();
                cargando.setVisibility(View.INVISIBLE);
                toastAcierto("Transaccion Realizada con Exito! - "+ tipo +": " + numero_transaccion);
            }else{
                AudioError();
                AlertDialog.Builder builder = new AlertDialog.Builder(Trans_MP_Galvanizado_Devolucion.this);
                View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                alertMensaje.setText(error);
                Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                btnAceptar.setText("Aceptar");
                builder.setView(mView);
                AlertDialog alertDialog = builder.create();
                btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
                alertDialog.setCancelable(false);
                alertDialog.show();
                etCodigo.setEnabled(true);
                leer_nuevo();
                resp = false;
            }

        }else{
            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(Trans_MP_Galvanizado_Devolucion.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText(error);
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
            alertDialog.setCancelable(false);
            alertDialog.show();
            etCodigo.setEnabled(true);
            leer_nuevo();
            resp = false;
        }
        return  resp;
    }

    public void addRollo(String consecutivo_materia_prima, String id_detalle, String id_rollo, String tipo, Double peso, Double costo_unit,String nit_prov){
        DetalleTranModelo escanerModelo;

        String sql_codigo = "SELECT O.prod_final FROM  J_rollos_tref R,J_orden_prod_tef O WHERE cod_orden = " + consecutivo_materia_prima + " AND id_detalle =" + id_detalle + " AND id_rollo =" + id_rollo;
        String codigo = conexion.obtenerCodigo(Trans_MP_Galvanizado_Devolucion.this, sql_codigo);
        //No se usa en ningun momento
        String sql_descripcion = "SELECT descripcion  FROM  referencias WHERE codigo = '" + codigo + "'";
        String descripcion = conexion.obtenerDescripcionCodigo(Trans_MP_Galvanizado_Devolucion.this, sql_descripcion);

        escanerModelo = new DetalleTranModelo();
        escanerModelo.setNumero(consecutivo);
        escanerModelo.setTipo(tipo);
        escanerModelo.setNum_trans(numero_transaccion.toString());
        escanerModelo.setCodigo(codigo);
        escanerModelo.setPeso(peso.toString());
        escanerModelo.setDetalle(id_detalle.toString());
        escanerModelo.setNum_rollo(id_rollo.toString());
        escanerModelo.setEstado_muestra("0");
        escanerModelo.setNit_prov(nit_prov.toString());
        escanerModelo.setCosto_unit(costo_unit.toString());
        ListaEscaner.add(escanerModelo);

        addrollotrans();
    }

    public void addrollotrans(){

        EscanerAdapter = new listescanerAdapter(Trans_MP_Galvanizado_Devolucion.this,R.layout.item_row_escaner,ListaEscaner);
        listviewEscaner.setAdapter(EscanerAdapter);
    }

    private List<Object> traslado_bodega(String codigo, Double cantidad, String tipo, Double costo_unit){
        List<Object> listSql;

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        String fecha = dateFormat.format(calendar.getTime());

        String usuario = personaEntrega.getNit();
        String notas = "MOVIL fecha:" + fecha + " usuario:" + usuario;
        numero_transaccion = Integer.valueOf(Obj_ordenprodLn.mover_consecutivoTref(tipo, Trans_MP_Galvanizado_Devolucion.this));
        listSql = objTraslado_bodLn.listaTransaccionDatable_traslado_bodega(numero_transaccion, codigo, bod_origen, bod_destino, calendar, notas, usuario, cantidad, tipo, modelo, costo_unit,Trans_MP_Galvanizado_Devolucion.this);
        return listSql;
    }



    private String tabla_produccion() {
        cargando.setVisibility(View.VISIBLE);
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            //Quitar este mensaje ya que no funciono muy bien en el programa
            //mensajeCargando.setText("Intento producción " + repeticiones + "/5");
            error = ing_prod_ad.ExecuteSqlTransaction(listTransaccion_prodGalv, ConfiguracionBD.obtenerNombreBD(2), Trans_MP_Galvanizado_Devolucion.this);
            if(error.equals("")){
                mensajeCargando.setText("");
                return error;
            }else{
                transaccion();
            }
        }else{
            cargando.setVisibility(View.INVISIBLE);
            mensajeCargando.setText("");
            return error;
        }
        return error;
    }
    private String transaccion() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            cargando.setVisibility(View.VISIBLE);
            // quitar mensaje guardado ya que esto no funciono muy bien en el programa
            //mensajeCargando.setText("Intento transacción " + repeticiones + "/5");
            error = ing_prod_ad.ExecuteSqlTransaction(listTransaccion_corsan, ConfiguracionBD.obtenerNombreBD(1), Trans_MP_Galvanizado_Devolucion.this);
            if(error.equals("")){
                mensajeCargando.setText("");
                return error;
            }else{
                transaccion();
            }
        }else{
            cargando.setVisibility(View.INVISIBLE);
            return error;
        }
        return error;
    }
    private ArrayList<String> obtenerLista(ArrayList<TipotransModelo> tiposLista ){
        listaTipos = new ArrayList<>();
        //listaTipos.add("Seleccione");

        for(int i = 0; i < tiposLista.size(); i++){
            listaTipos.add(tiposLista.get(i).getTipo());
        }
        return listaTipos;
    }

    private void cargarNuevo() {
        if (!yaentre){
            yaentre = true;
        }else{
            yaentre = false;
            etCodigo.requestFocus();
        }
    }

    //Metodo para ocultar el teclado virtual
    private void closeTecladoMovil() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    @SuppressLint("SetTextI18n")
    private void leer_nuevo() {
        etCodigo.setText("");
        lblCodigo.setText("LEA CODIGO");
        lblDescripcion.setText("LEA CODIGO");
        txtKilosRollo.setText("");
        etCodigo.requestFocus();
        yaentre = false;
    }

    private void contar_movimientos() {
        int size = ListaEscaner.size();
        String sizeString = Integer.toString(size);
        txtIngMovimientos.setText(sizeString);
    }

    //METODO DE TOAST PERSONALIZADO : ERROR
    public void toastError (String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_per_no_encon, findViewById(R.id.ll_custom_toast_per_no_encon));
        TextView txtMensaje = view.findViewById(R.id.txtMensajeToast1);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 200);
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
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM,0,200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    public void AudioError(){
        sp.play(sonido_de_Reproduccion,100,100,1,0,0);
        vibrator.vibrate(2000);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}