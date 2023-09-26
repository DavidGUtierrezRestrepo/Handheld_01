package com.example.handheld;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.handheld.ClasesOperativas.CaptureActivityPortraint;
import com.example.handheld.ClasesOperativas.Gestion_alambronLn;
import com.example.handheld.ClasesOperativas.Ing_prod_ad;
import com.example.handheld.ClasesOperativas.ObjTraslado_bodLn;
import com.example.handheld.ClasesOperativas.Obj_ordenprodLn;
import com.example.handheld.ClasesOperativas.objOperacionesDb;
import com.example.handheld.atv.holder.adapters.listescanerAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.databinding.ActivityEscanerBinding;
import com.example.handheld.modelos.DetalleTranModelo;
import com.example.handheld.modelos.PersonaModelo;
import com.example.handheld.modelos.TipotransModelo;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Escaner extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ActivityEscanerBinding binding;

    //Se declaran los elementos del layout
    EditText etCodigo;
    TextView txtTransaccion, txtKilosRollo, txtIngMovimientos, lblCodigo, lblDescripcion;
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
    List<Object> listTransaccion_prod;

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

    //Se inicializa variables necesarias en la clase
    boolean yaentre = false;
    String consecutivo, nit_proveedor,num_importacion,id_detalle,numero_rollo, error;
    Integer numero_transaccion;

    PersonaModelo personaEntrega;
    PersonaModelo personaRecibe;

    //Metodo que activa el escaner por medio de la camara del movil
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
        nit_usuario = getIntent().getStringExtra("nit_usuario");
        bod_origen = getIntent().getIntExtra("bod_origen", 0);
        bod_destino = getIntent().getIntExtra("bod_destino", 0);
        modelo = getIntent().getStringExtra("modelo");

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
            }else{
                if (barras.equals("")){
                    escanear();
                }else{
                    toastError("Borrar el texto escrito para abrir el escaner de la camara");
                    //closeTecladoMovil();
                    //codigoIngresado();
                }
            }
        });

        //Se programa el boton de transacción
        btnTransaccion.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Escaner.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_cargando,null);
            builder.setView(mView);
            AlertDialog alertDialogCargando = builder.create();
            alertDialogCargando.setCancelable(false);
            alertDialogCargando.show();

            if (validarFrm()){
                try {
                    guardar();
                    alertDialogCargando.dismiss();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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
                    leer_nuevo();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(Escaner.this);
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
                personaEntrega = conexion.obtenerPersona(Escaner.this,CeEntrega );
                personaRecibe = conexion.obtenerPersona(Escaner.this,CeRecibe );
                String cargEntrega = personaEntrega.getCargo().trim();
                String cargRecibe = personaRecibe.getCargo().trim();
                if(cargEntrega.equals("AUXILIAR ALMACEN") || cargEntrega.equals("AUXILIAR DE TREFILACION") || cargEntrega.equals("COORDINADOR TREFILACION") || cargEntrega.equals("COORDINADOR ALAMBRE GALVANIZADO Y PUAS")){
                    if(cargRecibe.equals("OPERARIO MONTACARGA")){
                        alertDialog.dismiss();
                    }else{
                        toastError("La cedula de la persona que recibe no corresponde a un montacarguista");
                    }
                }else{
                    toastError("La cedula de la persona que entrega no corresponde a una de las permitidas");
                }
            }
        });
        btnCancelar.setOnClickListener(v -> {
            Intent i = new Intent(Escaner.this,MainActivity.class);
            startActivity(i);
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void consultarTipos(){
        conexion = new Conexion();

        tiposLista = conexion.obtenerTipos(getApplication());
        listaTp = obtenerLista(tiposLista);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(Escaner.this, android.R.layout.simple_spinner_item, listaTp);
        spinner.setEnabled(false);
        spinner.setClickable(false);
        spinner.setAdapter(adapter);
    }

    //Metodoque recibe una lista tipo transmodelo, la recorre y añade a otra lista tipo String
    private ArrayList<String> obtenerLista(ArrayList<TipotransModelo> tiposLista ){
        listaTipos = new ArrayList<>();
        //listaTipos.add("Seleccione");

        for(int i = 0; i < tiposLista.size(); i++){
            listaTipos.add(tiposLista.get(i).getTipo());
        }
        return listaTipos;
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


    //Metodo para ocultar el teclado virtual
    private void closeTecladoMovil() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    //METODO PARA CERRAR LA APLICACION
    @SuppressLint("")
    public void salir(View view){
        finishAffinity();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////INICIAN LOS METODOS Y FUNCIONES DE LOS PROCESOS DE TRANSACCION//////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void codigoIngresado(){
        consecutivo = etCodigo.getText().toString();
        //consecutivo = "444444218-4-3-168";
        if (validarCodigoBarras(consecutivo)){
            nit_proveedor = obj_gestion_alambronLn.extraerDatoCodigoBarras("proveedor", consecutivo);
            num_importacion = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_importacion", consecutivo);
            id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarras("detalle", consecutivo);
            numero_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_rollo", consecutivo);
            if(validarRolloRegistrado(num_importacion,numero_rollo,nit_proveedor,id_detalle)) {
                String sql_codigo = "SELECT d.codigo FROM J_alambron_solicitud_det d WHERE d.num_importacion =" + num_importacion + " AND d.nit_proveedor =" + nit_proveedor + "  AND d.id_det =" + id_detalle;
                String sql_peso = "SELECT peso FROM J_alambron_importacion_det_rollos WHERE peso IS NOT NULL AND num_importacion =" + num_importacion + " AND numero_rollo = " + numero_rollo + " AND nit_proveedor=" + nit_proveedor + " AND id_solicitud_det =" + id_detalle;
                String peso = conexion.obtenerPesoAlamImport(getApplicationContext(), sql_peso);
                String codigo = conexion.obtenerCodigoAlamImport(getApplicationContext(), sql_codigo);
                if(codigo.equals(pcodigo)){
                    Boolean valid;
                    valid = validarRolloConTransaccion(num_importacion,numero_rollo,nit_proveedor,id_detalle);
                    if(valid.equals(true)){
                        lblCodigo.setText(codigo);
                        String sql_descripcion = "SELECT descripcion FROM referencias WHERE  codigo = '" + codigo + "'";
                        lblDescripcion.setText(conexion.obtenerDescripcionCodigo(Escaner.this,sql_descripcion));
                        txtKilosRollo.setText(peso);
                        yaentre=true;
                        //Se bloquea el EditText ya que el tiquete fue leido correctamente
                        etCodigo.setEnabled(false);
                        toastAcierto("Rollo validado");
                    }else{
                        //nit_proveedor= "999999999";
                        if (nit_proveedor.equals("999999999")){
                            //Creamos el mensaje que se mostrara con la pregunta
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setIcon(R.mipmap.ic_error_mimap).
                                    setTitle("¿Desactivar?").
                                    setMessage("¿Desea desactivar este tiquete único?").
                                    setPositiveButton("Aceptar", (dialogInterface, i) -> {
                                        boolean resp;
                                        resp = conexion.eliminarTiqueteUnico(Escaner.this, num_importacion, numero_rollo, nit_proveedor, id_detalle);
                                        if (resp){
                                            toastAcierto("El rollo se desactivo en forma correcta!");
                                        }else{
                                            toastError("!Error al desactivar el rollo");
                                        }
                                    }).
                                    setNegativeButton("Cancelar", (dialogInterface, i) -> toastError("Se cancelo la eliminacion"));
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }else{
                            toastError("Ya se le hizo una salida al rollo");
                            leer_nuevo();
                        }
                    }
                }else{
                    toastError("El código de alambrón no pertenece al pedido");
                    leer_nuevo();
                }
            }else{
                toastError("El codigo de barras no se encuentra asignado");
                leer_nuevo();
            }
        }
    }

    //Metodo que valida que el codigo de barras exista y este bien
    private boolean validarCodigoBarras(String consecutivo){
        boolean resp = false;

        String nit_proveedor = obj_gestion_alambronLn.extraerDatoCodigoBarras("proveedor", consecutivo);
        String num_importacion = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_importacion", consecutivo);
        String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarras("detalle", consecutivo);
        String numero_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_rollo", consecutivo);

        if (!num_importacion.isEmpty() && !numero_rollo.isEmpty() && !id_detalle.isEmpty() && !nit_proveedor.isEmpty()) {
            String sql = "SELECT id FROM J_alambron_importacion_det_rollos WHERE num_importacion =" + num_importacion + " AND numero_rollo = " + numero_rollo + " AND nit_proveedor = " + nit_proveedor + " AND id_solicitud_det = " + id_detalle;
            String id = conexion.obtenerIdAlamImport(Escaner.this, sql);
            if (id.isEmpty()){
                toastError("Intente leerlo nuevamente,Problemas con el tiquete");
                leer_nuevo();

            }else{
                resp = true;
            }
        }else{
            toastError("Intente leerlo nuevamente,Problemas con el tiquete");
            leer_nuevo();
        }
        return resp;
    }

    //Metodo que consulta y obtiene el peso del rollo registrado
    private boolean validarRolloRegistrado(String num_importacion, String num_rollo, String nit_proveedor, String id_detalle){
        boolean resp = false;
        String sql = "SELECT peso FROM J_alambron_importacion_det_rollos WHERE peso IS NOT NULL AND num_importacion =" + num_importacion + " AND numero_rollo = " + num_rollo + " AND nit_proveedor=" + nit_proveedor + " AND id_solicitud_det =" + id_detalle;
        String peso = conexion.obtenerPesoAlamImport(getApplicationContext(), sql);
        if (!peso.isEmpty()){
            resp = true;
        }
        return resp;
    }

    //Metodo que valida que el rollo no tenga ya una salida
    private boolean validarRolloConTransaccion(String num_importacion, String num_rollo, String nit_proveedor, String id_detalle){
        boolean respuesta = false;
        try {
            String sql = "SELECT num_importacion FROM J_alambron_importacion_det_rollos WHERE num_transaccion_salida IS NOT NULL AND num_importacion =" + num_importacion + " AND numero_rollo = " + num_rollo + " AND nit_proveedor=" + nit_proveedor + " AND id_solicitud_det =" + id_detalle;
            String id = conexion.obtenerNumTranAlamImport(getApplicationContext(), sql);
            if (id.isEmpty()){
                respuesta = true;
            }
        }catch (Exception e){
            Toast.makeText(Escaner.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        //Trabajo para el translator de bodega 2 a 1
        /*if (bod_origen.equals("2") && bod_destino.equals("1")){
            if (resp == true){
                resp == false;
            }else{
                resp == true;
            }
        }*/
        return respuesta;
    }

    //Se verifica que todos los datos del codigo de barras
    private boolean validarFrm(){
        String sql_cantidad = "SELECT (D.cantidad - (SELECT COUNT(numero) FROM J_salida_alambron_transaccion  WHERE numero = D.numero AND id_detalle = D.id_detalle))As pendiente FROM J_salida_alambron_enc E ,J_salida_alambron_det D, CORSAN.dbo.referencias R WHERE E.anulado is null AND  R.codigo = D.codigo AND D.numero = E.numero  and e.numero=" + pNumero + "";
        String cantidad = conexion.obtenerCantidadPedido(Escaner.this, sql_cantidad);
        if (!lblCodigo.getText().toString().isEmpty() && !lblCodigo.getText().toString().equals("LEA CODIGO")){
            if (!txtKilosRollo.getText().toString().isEmpty()){
                if (!spinner.getSelectedItem().equals("Seleccione")){
                    if (conexion.existeCodigo(Escaner.this, lblCodigo.getText().toString())){
                        if (Double.parseDouble(txtKilosRollo.getText().toString()) > 0) {
                            if (!cantidad.equals("0")){
                                if (conexion.existeTipoTransaccion(Escaner.this,spinner.getSelectedItem().toString())){
                                    if (!etCodigo.getText().equals("")){
                                        if (validarCodigoBarras(consecutivo)){
                                            return true;
                                        }else{
                                            toastError("Verifique, El código de barras no se encuentra asignado!");
                                        }
                                    }else{
                                        toastError("Verifique, No se leyo ningun código de barras!");
                                    }
                                }else{
                                    toastError("Verifique, No existe el tipo de transacción!");
                                }
                            }else{
                                toastError("Verifique, No se puede leer más alambron en este pedido!");
                            }
                        }else{
                            toastError("Verifique, Los kilos no pueden ser negativos ó iguales a (0)");
                        }
                    }else{
                        toastError("Verifique, falta el CODIGO no existe");
                    }
                }else{
                    toastError("Verifique, falta el TIPO de transacción");
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
        String gTipo = spinner.getSelectedItem().toString();
        Double gPeso = Double.parseDouble(txtKilosRollo.getText().toString());
        String gCodigo = lblCodigo.getText().toString().trim();
        String gBodega = objTraslado_bodLn.obtenerBodegaXcodigo(gCodigo);
        String gStock = conexion.consultarStock(Escaner.this,gCodigo,gBodega);
        Double gNit_prov = Double.parseDouble(obj_gestion_alambronLn.extraerDatoCodigoBarras("proveedor", consecutivo));
        Double gNum_importa = Double.parseDouble(obj_gestion_alambronLn.extraerDatoCodigoBarras("num_importacion", consecutivo));
        Double gDeta = Double.parseDouble(obj_gestion_alambronLn.extraerDatoCodigoBarras("detalle", consecutivo));
        Double gNum_rollo = Double.parseDouble(obj_gestion_alambronLn.extraerDatoCodigoBarras("num_rollo", consecutivo));
        String sql_costo_unit = "SELECT d.costo_kilo FROM J_alambron_solicitud_det d WHERE d.num_importacion =" + gNum_importa + " AND d.nit_proveedor =" + gNit_prov + "  AND d.id_det =" + gDeta;
        Double gCosto_unit = Double.parseDouble(conexion.obtenerCostoUnit(Escaner.this,sql_costo_unit));

        try {
            realizar_transaccion(gCodigo, gPeso, gNit_prov, gNum_importa, gTipo, gDeta, gNum_rollo, gCosto_unit);
            etCodigo.requestFocus();
        }catch (Exception e){
            leer_nuevo();
            toastError(e.getMessage());
        }
    }

    //Metodo donde se agregan las consultas sql a una lista y se envian a otro metodo para ejecutarlas
    public Boolean realizar_transaccion(String gCodigo, Double gPeso, Double gNit_prov, Double gNum_importa, String gTipo, Double gDeta, Double gNum_rollo, Double gCosto_unit) throws SQLException {
        boolean resp = true;
        listTransaccion_prod = new ArrayList<>();
        String sql_rollo;
        String consecutivo = etCodigo.getText().toString();
        String sql_solicitud;
        String sql_detalle_salida;
        String sql_devuelto;
        listTransaccion_corsan = traslado_bodega(gCodigo, gPeso, gTipo, gCosto_unit);
        sql_solicitud = "INSERT INTO J_salida_alambron_transaccion (numero,id_detalle,tipo,num_transaccion) " +
                "VALUES (" + pNumero + "," + pIdDetalle + ",'" + gTipo + "'," + numero_transaccion + ") ";


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
            listTransaccion_prod.add(sql_solicitud);
        }catch (Exception e){
            Toast.makeText(Escaner.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        try {
            //Se añade el sql a la lista
            listTransaccion_prod.add(sql_detalle_salida);
        }catch (Exception e){
            Toast.makeText(Escaner.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (bod_origen.equals(1)  && bod_destino.equals(2)){
            sql_rollo = "UPDATE J_alambron_importacion_det_rollos SET " +
                    "num_transaccion_salida =" + numero_transaccion + " ,tipo_salida = '" + gTipo + "' " +
                    "WHERE num_importacion=" + num_importacion + " AND  id_solicitud_det =" + gDeta + " " +
                    "AND numero_rollo =" + gNum_rollo + " AND nit_proveedor =" + gNit_prov;
        }else{
            sql_rollo = "UPDATE J_alambron_importacion_det_rollos  SET num_transaccion_salida = NULL " +
                    ",tipo_salida = NULL WHERE num_importacion=" + num_importacion + " " +
                    "AND  id_solicitud_det =" + gDeta + " AND numero_rollo =" + gNum_rollo + " " +
                    "AND nit_proveedor =" + gNit_prov;

            sql_devuelto = "UPDATE J_alambron_importacion_det_rollos " +
                    "SET num_transaccion_dev =" + numero_transaccion + "" +
                    "WHERE num_importacion=" + num_importacion + " AND  id_solicitud_det =" + gDeta + " " +
                    "AND numero_rollo =" + gNum_rollo + " AND nit_proveedor =" + gNit_prov;

            objOperacionesDb.ejecutarUpdateProduccion(sql_devuelto,Escaner.this);
        }

        try {
            //Se añade el sql a la lista
            listTransaccion_prod.add(sql_rollo);
        }catch (Exception e){
            Toast.makeText(Escaner.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        repeticiones = 0;
        error = transaccion();
        if (error.equals("")){
            repeticiones = 0;
            error = tabla_produccion();
            if (error.equals("")){
                addRollo(num_importacion, consecutivo, gPeso, gNum_rollo, gDeta, gNit_prov, gTipo);
                etCodigo.setEnabled(true);
                leer_nuevo();
                contar_movimientos();

                toastAcierto("Transaccion Realizada con Exito! - "+ gTipo +": " + numero_transaccion);
            }else{
                toastError(error);
                etCodigo.setEnabled(true);
                leer_nuevo();
                resp = false;
            }

        }else{
            toastError(error);
            etCodigo.setEnabled(true);
            leer_nuevo();
            resp = false;
        }
        return  resp;
    }

    private String transaccion() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listTransaccion_corsan, "CORSAN", Escaner.this);
            if(error.equals("")){
                return error;
            }else{
                transaccion();
            }
        }else{
            return error;
        }
        return error;
    }

    private String tabla_produccion() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listTransaccion_prod, "PRGPRODUCCION", Escaner.this);
            if(error.equals("")){
                return error;
            }else{
                transaccion();
            }
        }else{
            return error;
        }
        return error;
    }

    //Solo para 'TRB1' modelo 08 traslado de la 1 a la 2
    // Solo para 'TRB1' modelo 12 traslado de la 2 a la 1
    private List<Object> traslado_bodega(String codigo, Double cantidad, String tipo, Double costo_unit){
        List<Object> listSql;

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String fecha = dateFormat.format(calendar.getTime());

        String usuario = nit_usuario;
        String notas = "MOVIL fecha:" + fecha + " usuario:" + usuario;
        numero_transaccion = Integer.valueOf(Obj_ordenprodLn.mover_consecutivo(tipo, Escaner.this));
        listSql = objTraslado_bodLn.listaTransaccionDatable_traslado_bodega(numero_transaccion, codigo, bod_origen, bod_destino, calendar, notas, usuario, cantidad, tipo, modelo, costo_unit,Escaner.this);
        return listSql;
    }

    //Se añaden los datos del rollo en la lista "ListaEscaner"
    public void addRollo(String num_importacion, String consecutivo, Double peso, Double num_rollo, Double id_detalle, Double nit_prov, String tipo){
        DetalleTranModelo escanerModelo;

        String sql_codigo = "SELECT codigo FROM  J_alambron_solicitud_det WHERE num_importacion = " + num_importacion + " AND nit_proveedor =" + nit_prov + " AND id_det =" + id_detalle;
        String codigo = conexion.obtenerCodigo(Escaner.this, sql_codigo);
        //No se usa en ningun momento
        //String sql_descripcion = "SELECT descripcion  FROM  referencias WHERE codigo = '" + codigo + "'";
        //String descripcion = conexion.obtenerDescripcionCodigo(Escaner.this, sql_descripcion);

        escanerModelo = new DetalleTranModelo();
        escanerModelo.setNumero(consecutivo);
        escanerModelo.setTipo(tipo);
        escanerModelo.setNum_trans(numero_transaccion.toString());
        escanerModelo.setCodigo(codigo);
        escanerModelo.setPeso(peso.toString());
        escanerModelo.setNum_imp(num_importacion);
        escanerModelo.setDetalle(id_detalle.toString());
        escanerModelo.setNum_rollo(num_rollo.toString());
        escanerModelo.setEstado_muestra("0");
        escanerModelo.setNit_prov(nit_prov.toString());
        escanerModelo.setCosto_unit("0");
        ListaEscaner.add(escanerModelo);

        addrollotrans();
    }

    //Se envia la lista de transaccion de rollos al EscanerAdapter y despues a la listview
    public void addrollotrans(){

        EscanerAdapter = new listescanerAdapter(Escaner.this,R.layout.item_row_escaner,ListaEscaner);
        listviewEscaner.setAdapter(EscanerAdapter);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que borra el codigo del EditText y cambia la variable "yaentre"
    private void cargarNuevo() {
        if (!yaentre){
            yaentre = true;
        }else{
            yaentre = false;
            etCodigo.requestFocus();
        }
    }

    @SuppressLint("SetTextI18n")
    private void leer_nuevo(){
        etCodigo.setText("");
        lblCodigo.setText("LEA CODIGO");
        lblDescripcion.setText("LEA CODIGO");
        txtKilosRollo.setText("");
        etCodigo.requestFocus();
        yaentre = false;
    }

    //Metodo que cuenta la cantidad de elemento que hay en la lista y cuenta cada uno como un movimiento
    private void contar_movimientos() {
        int size = ListaEscaner.size();
        String sizeString = Integer.toString(size);
        txtIngMovimientos.setText(sizeString);
    }


    //METODO DE TOAST PERSONALIZADO : ERROR
    public void toastError(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_per_no_encon, findViewById(R.id.ll_custom_toast_per_no_encon));
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
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM,0,200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }
}