package com.example.handheld;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.handheld.atv.holder.adapters.listTrasMateriaPrimaScaeAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.conexionDB.ConfiguracionBD;
import com.example.handheld.databinding.ActivityEscanerBinding;
import com.example.handheld.modelos.DetalleTranMateriaPrimaScaeModelo;
import com.example.handheld.modelos.PermisoPersonaModelo;
import com.example.handheld.modelos.TipotransModelo;
import com.example.handheld.modelos.ValidarTrasladoScaeModelo;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EscanerTrasladoMateriaPrimaScae extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ActivityEscanerBinding binding;

    //Se declaran los elementos del layout
    EditText etCodigo;
    TextView txtTransaccion, txtKilosRollo, txtIngMovimientos, lblCodigo, lblDescripcion, mensajeCargando;
    Button btnLeerCodigo, btnSalida, btnCancelar, btnTransaccion;
    Spinner spinner;

    //Se declaran las Herramientas para el listview
    ListView listviewEscaner;
    ListAdapter EscanerAdapter;
    List<DetalleTranMateriaPrimaScaeModelo> ListaEscaner = new ArrayList<>();

    //Se inicializan las listas para el comboBox
    ArrayList<String> listaTipos;
    ArrayList<String> listaTp;
    ArrayList<TipotransModelo> tiposLista = new ArrayList<>();
    List<Object> listTransaccion_corsan;
    ;
    //Se inicializa un objeto conexion
    Conexion conexion;

    //Se declaran los objetos de otras clases necesarias
    Gestion_alambronLn obj_gestion_alambronLn = new Gestion_alambronLn();
    ObjTraslado_bodLn objTraslado_bodLn = new ObjTraslado_bodLn();
    Ing_prod_ad ing_prod_ad = new Ing_prod_ad();

    //se declaran las variables donde estaran los datos que vienen de la anterior clase
    Integer pNumero, pIdDetalle, bod_origen, bod_destino;
    String pfecha, pcodigo, pPendiente, pDescripcion, nit_usuario, modelo, db_produccion;

    //Se inicializa variables necesarias en la clase
    boolean yaentre = false;
    String consecutivo, error;
    Integer numero_transaccion;

    PermisoPersonaModelo personaEntrega, personaRecibe;

    ProgressBar cargando;

    //Se inicializa los varibles para el sonido de error
    SoundPool sp;
    int sonido_de_Reproduccion;

    //Se inicializa una instancia para hacer vibrar el celular
    Vibrator vibrator;

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
        setContentView(R.layout.activity_escaner_traslado_materia_prima_scae);

        //Se Define los varibles para el sonido de error
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC,1);
        sonido_de_Reproduccion = sp.load(this, R.raw.sonido_error_2,1);

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

        db_produccion = ConfiguracionBD.obtenerNombreBD(2) + ".dbo.";

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
            cargando.setVisibility(View.VISIBLE);
            if (validarFrm()){
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

    //Funcion donde se validan las cedulas al ingresar al modulo
    private void ingresarCedulas() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EscanerTrasladoMateriaPrimaScae.this);
        View mView = getLayoutInflater().inflate(R.layout.alertdialog_cedulastranslado,null);
        final EditText txtCedulaEntrega = mView.findViewById(R.id.txtCedulaEntrega);
        txtCedulaEntrega.setHint("Montacarguista");
        final EditText txtCedulaRecibe = mView.findViewById(R.id.txtCedulaRecibe);
        txtCedulaRecibe.setHint("Operario");

        //Se inhabilita la cedula de la persona que recibe porque aun no se ha realizado un procedimiento para llevar a cabo la entrega correctamente
        txtCedulaRecibe.setEnabled(false);

        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
        Button btnCancelar = mView.findViewById(R.id.btnCancelar);
        builder.setView(mView);
        AlertDialog alertDialog = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            String CeEntrega = txtCedulaEntrega.getText().toString().trim();

            //Se inabilita el recibir el valor escrito en el Edit Text porque aun no se ha realizado un procedimiento para llevar a cabo la entrega correctamente
            //String CeRecibe= txtCedulaRecibe.getText().toString().trim();
            //Y la cedula de la persona que recibe la dejamos en cero para no modificar demasiado el codigo y pueda hacer la verificación de que no son iguales ambas cedulas
            String CeRecibe="0";

            if (CeEntrega.equals("") || CeRecibe.equals("")){
                toastError("Por favor ingresar ambas cedulas");
            }else{
                if (CeEntrega.equals(CeRecibe)){
                    toastError("Ambas cedulas no pueden ser iguales");
                }else{
                    personaEntrega = conexion.obtenerPermisoPersonaTrasladoMateriaScae(EscanerTrasladoMateriaPrimaScae.this,CeEntrega,"entrega" );

                    //Se inhabilita la consulta que busca la información de la persona que recibe porque aun no se ha realizado un procedimiento para llevar a cabo la entrega correctamente
                    //personaRecibe = conexion.obtenerPermisoPersonaTrasladoMateriaScae(EscanerTrasladoMateriaPrimaScae.this,CeRecibe,"recibe" );
                    String permisoEntrega = personaEntrega.getPermiso();

                    //Se inhabilita tomar el permiso del modelo de la persona que recibe porque aun no se ha realizado un procedimiento para llevar a cabo la entrega correctamente
                    //String permisoRecibe = personaRecibe.getPermiso();
                    if(permisoEntrega.equals("E")){
                        alertDialog.dismiss();

                        //Se inhabilita  verificación de la cedula de la persona que recibe porque aun no se ha realizado un procedimiento para llevar a cabo la entrega correctamente
                        //if(permisoRecibe.equals("R")){
                        //    alertDialog.dismiss();
                        //}else{
                        //    toastError("La cedula de la persona que recibe no corresponde a un operario de púas");
                        //    txtCedulaRecibe.setText("");
                        //}

                    }else{
                        toastError("La cedula de la persona que entrega no corresponde a un montacarguista");
                        txtCedulaEntrega.setText("");
                    }
                }
            }
        });
        btnCancelar.setOnClickListener(v -> {
            alertDialog.dismiss();
            finish();
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void consultarTipos(){
        conexion = new Conexion();

        tiposLista = conexion.obtenerTiposScae(getApplication());
        listaTp = obtenerLista(tiposLista);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(EscanerTrasladoMateriaPrimaScae.this, android.R.layout.simple_spinner_item, listaTp);
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
        if (validarCodigoBarrasRec(consecutivo)) {
            String consecutivo_materia_prima = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden", consecutivo);
            String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle", consecutivo);
            String id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo", consecutivo);
            if (validarTrasladoRec(consecutivo)){
                String sql_codigo = "SELECT s.prod_final \n" +
                        "FROM JB_rollos_rec r, JB_orden_prod_rec_refs s \n" +
                        "WHERE (r.id_prof_final = s.num AND r.cod_orden_rec = s.cod_orden) AND r.cod_orden_rec =" + consecutivo_materia_prima + " AND r.id_rollo_rec = " + id_rollo + " AND r.id_detalle_rec = " + id_detalle;
                String sql_peso = "SELECT r.peso \n" +
                        "FROM JB_rollos_rec r, JB_orden_prod_rec_refs s \n" +
                        "WHERE (r.id_prof_final = s.num AND r.cod_orden_rec = s.cod_orden) AND r.cod_orden_rec =" + consecutivo_materia_prima + " AND r.id_rollo_rec = " + id_rollo + " AND r.id_detalle_rec = " + id_detalle;
                String peso = conexion.valorTodo(EscanerTrasladoMateriaPrimaScae.this,sql_peso);
                String codigo = conexion.valorTodo(EscanerTrasladoMateriaPrimaScae.this,sql_codigo);
                codigo = codigo.toUpperCase();
                pcodigo = pcodigo.toUpperCase();
                if (validar_guardado_rollo(peso)){
                    if (codigo.equals(pcodigo)){
                        lblCodigo.setText(codigo);
                        String sql_descripcion = "SELECT descripcion FROM referencias WHERE  codigo = '" + codigo + "'";
                        lblDescripcion.setText(conexion.obtenerDescripcionCodigo(EscanerTrasladoMateriaPrimaScae.this,sql_descripcion));
                        txtKilosRollo.setText(peso);
                        etCodigo.setEnabled(false);
                        toastAcierto("Rollo validado");
                    }else{
                        toastError("El código del tiquete no pertenece al pedido");
                        AudioError();
                        leer_nuevo();
                    }
                }else{
                    toastError("El tiquete excede el peso del pedido");
                    AudioError();
                    leer_nuevo();
                }
            }else{
                leer_nuevo();
            }
        }
    }

    private boolean validar_guardado_rollo(String peso) {
        boolean resp;
        resp = false;
        double peso_conver;
        double peso_solicitud;
        String sql_rollo = "SELECT (SELECT CASE WHEN (SELECT sum(peso) FROM J_salida_materia_prima_Tscae_transaccion  WHERE numero = D.numero AND id_detalle = D.id_detalle ) is null THEN D.cantidad  ELSE (D.cantidad -(SELECT sum(peso) FROM J_salida_materia_prima_Tscae_transaccion  WHERE numero = D.numero AND id_detalle = D.id_detalle )) END )As pendiente FROM J_salida_materia_prima_Tscae_enc E ,J_salida_materia_prima_Tscae_det D, CORSAN.dbo.referencias R where E.numero=" + pNumero + " AND E.anulado is null AND  R.codigo = D.codigo AND (e.devolver = 'N' OR e.devolver IS NULL ) AND E.numero = D.numero AND id_detalle = D.id_detalle";
        peso_solicitud = Double.parseDouble(conexion.valorTodo(EscanerTrasladoMateriaPrimaScae.this,sql_rollo));
        peso_conver = Double.parseDouble(peso);
        if (peso_solicitud >= peso_conver){
            resp = true;
        }
        return resp;
    }

    private boolean validarTrasladoRec(String consecutivo) {
        boolean resp = false;
        String consecutivo_materia_prima = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden", consecutivo);
        String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle", consecutivo);
        String id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo", consecutivo);
        ValidarTrasladoScaeModelo validacion = conexion.validarTrasladoScae(EscanerTrasladoMateriaPrimaScae.this,consecutivo_materia_prima,id_detalle,id_rollo);
        if(!(validacion.getScae() == null)){
            toastError("El rollo ya a sido consumido");
            AudioError();
            leer_nuevo();
        }else{
            if (!(validacion.getTipo_trans().equals("EPPP"))){
                toastError("El rollo no es de bodega 2");
                AudioError();
                leer_nuevo();
            }else{
                resp = true;
            }
        }
        return resp;
    }

    private boolean validarCodigoBarrasRec(String consecutivo) {
        Boolean resp = false;
        String consecutivo_materia_prima = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden", consecutivo);
        String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle", consecutivo);
        String id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo", consecutivo);
        if (!consecutivo_materia_prima.equals("") && !id_detalle.equals("") && !id_rollo.equals("")){
            String sql = "SELECT s.prod_final FROM JB_rollos_rec r, JB_orden_prod_rec_refs s \n" +
                    "WHERE (r.id_prof_final = s.num AND r.cod_orden_rec = s.cod_orden) \n" +
                    "AND r.cod_orden_rec =" + consecutivo_materia_prima + " AND r.id_rollo_rec = " + id_rollo + " AND r.id_detalle_rec = " + id_detalle;
            String id = conexion.valorTodo(EscanerTrasladoMateriaPrimaScae.this,sql);
            if (!id.equals("")){
                resp = true;
            }
        }
        if (resp.equals(false)){
            toastError("Problemas con el tiquete, Intente leerlo nuevamente");
            AudioError();
            leer_nuevo();
        }
        return resp;
    }


    //Se verifica que todos los datos del codigo de barras
    private boolean validarFrm(){
        if (!lblCodigo.getText().toString().isEmpty() && !lblCodigo.getText().toString().equals("LEA CODIGO")){
            if(!txtKilosRollo.getText().toString().isEmpty() && !txtKilosRollo.getText().equals("")){
                if (!spinner.getSelectedItem().equals("Seleccione")){
                    if (conexion.existeCodigo(EscanerTrasladoMateriaPrimaScae.this,lblCodigo.getText().toString())){
                        if (Double.parseDouble(txtKilosRollo.getText().toString()) > 0){
                            if(conexion.existeTipoTransaccion(EscanerTrasladoMateriaPrimaScae.this,spinner.getSelectedItem().toString())){
                                if (!etCodigo.getText().equals("")){
                                    if (validarCodigoBarrasRec(etCodigo.getText().toString())){
                                        return true;
                                    }else{
                                        toastError("El código de barras no se encuentra asignado");
                                        AudioError();
                                    }
                                }else{
                                    toastError("Verifique, No se leyo ningun código de barras!");
                                }
                            }else{
                                toastError("Verifique, No existe el tipo de transacción!");
                            }
                        }else{
                            toastError("Verifique, Los kilos no pueden ser negativos ó iguales a (0)");
                        }
                    }else{
                        toastError("El código ingresado no existe");
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
        cargando.setVisibility(View.VISIBLE);
        String gTipo = spinner.getSelectedItem().toString();
        double gPeso = Double.parseDouble(txtKilosRollo.getText().toString());
        String gCodigo = lblCodigo.getText().toString().trim();
        String gBodega = bod_origen.toString();
        double gStock = Double.parseDouble(conexion.consultarStock(EscanerTrasladoMateriaPrimaScae.this,gCodigo,gBodega));
        String sql_costo_unit = "select costo_unitario from referencias where codigo = '" + gCodigo + "'";
        Double gCosto_unit = Double.parseDouble(conexion.valorTodoCorsan(EscanerTrasladoMateriaPrimaScae.this,sql_costo_unit));
        String consecutivo_materia_prima = "";
        String id_detalle = "";
        String id_rollo = "";
        String sql_solicitud = "";

        if (gPeso <= gStock){
            listTransaccion_corsan = traslado_bodega(gCodigo, gPeso, gTipo, gCosto_unit);

            // Obtén la fecha y hora actual
            Date fechaActual = new Date();
            // Define el formato de la fecha y hora que deseas obtener
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // Convierte la fecha actual en un String con el formato definido
            String fechaActualString = formatoFecha.format(fechaActual);

            ////Se inhabilita el registro de la cedula de la persona que recibe porque aun no se ha realizado un procedimiento para llevar a cabo la entrega correctamente
            //String sql_detalle_translado = "INSERT INTO " + db_produccion + "jd_detalle_traslado_materia_prima_scae (nit_entrega,nit_recibe,fecha_transaccion,trb1) " +
            //        "VALUES (" + personaEntrega.getNit() + "," + personaRecibe.getNit() + ",'" + fechaActualString + "'," + numero_transaccion + ") ";

            String sql_detalle_translado = "INSERT INTO " + db_produccion + "jd_detalle_traslado_materia_prima_scae (nit_entrega,fecha_transaccion,trb1) " +
                    "VALUES (" + personaEntrega.getNit() + ",'" + fechaActualString + "'," + numero_transaccion + ") ";

            try {
                //Se añade el sql a la lista
                listTransaccion_corsan.add(sql_detalle_translado);
            }catch (Exception e){
                Toast.makeText(EscanerTrasladoMateriaPrimaScae.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            consecutivo_materia_prima = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden", consecutivo);
            id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle", consecutivo);
            id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo", consecutivo);
            String sql = "UPDATE " + db_produccion + "JB_rollos_rec SET scae=" + numero_transaccion + " where  id_detalle_rec =" + id_detalle + "  AND id_rollo_rec =" + id_rollo + " AND cod_orden_rec=" + consecutivo_materia_prima;
            try {
                //Se añade el sql a la lista
                listTransaccion_corsan.add(sql);
            }catch (Exception e){
                Toast.makeText(EscanerTrasladoMateriaPrimaScae.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }


            sql_solicitud = "INSERT INTO " + db_produccion + "J_salida_materia_prima_Tscae_transaccion \n" +
                    "(numero,id_detalle,tipo,num_transaccion,peso) \n" +
                    "VALUES (" + pNumero + "," + pIdDetalle + ",'" + gTipo + "'," + numero_transaccion + "," + gPeso + ") ";
            try {
                //Se añade el sql a la lista
                listTransaccion_corsan.add(sql_solicitud);
            }catch (Exception e){
                Toast.makeText(EscanerTrasladoMateriaPrimaScae.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            error = ing_prod_ad.ExecuteSqlTransaction(listTransaccion_corsan, ConfiguracionBD.obtenerNombreBD(1), EscanerTrasladoMateriaPrimaScae.this);
            if(error.equals("")){
                addRollo(consecutivo_materia_prima,id_detalle, id_rollo, gCodigo, Double.toString(gPeso), gTipo, numero_transaccion.toString());
                etCodigo.setEnabled(true);
                leer_nuevo();
                contar_movimientos();
                cargando.setVisibility(View.INVISIBLE);
                toastAcierto("Transaccion Realizada con Exito! - "+ gTipo +": " + numero_transaccion);
            }else{
                AudioError();
                AlertDialog.Builder builder = new AlertDialog.Builder(EscanerTrasladoMateriaPrimaScae.this);
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
            }
        }else{
            toastError("El pedido es más grande que el stock!");
            AudioError();
        }
    }

    //Lista con las consultas de translado de bodega
    private List<Object> traslado_bodega(String codigo, Double cantidad, String tipo, Double costo_unit){
        List<Object> listSql;

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        String fecha = dateFormat.format(calendar.getTime());

        String usuario = personaEntrega.getNit();
        String notas = "MOVIL fecha:" + fecha + " usuario:" + usuario;
        numero_transaccion = Integer.valueOf(Obj_ordenprodLn.mover_consecutivo(tipo, EscanerTrasladoMateriaPrimaScae.this));
        listSql = objTraslado_bodLn.listaTransaccionDatable_traslado_scae(numero_transaccion, codigo, bod_origen, calendar, notas, usuario, cantidad, tipo, costo_unit,EscanerTrasladoMateriaPrimaScae.this);
        return listSql;
    }

    //Se añaden los datos del rollo en la lista "ListaEscaner"
    public void addRollo(String Nro_orden,String Id_detalle,String Id_rollo, String gCodigo, String gPeso, String gTipo, String numero_transaccion){
        DetalleTranMateriaPrimaScaeModelo detalleTranMateriaPrimaScaeModelo;

        detalleTranMateriaPrimaScaeModelo = new DetalleTranMateriaPrimaScaeModelo();
        detalleTranMateriaPrimaScaeModelo.setNro_orden(Nro_orden);
        detalleTranMateriaPrimaScaeModelo.setId_detalle(Id_detalle);
        detalleTranMateriaPrimaScaeModelo.setNumRollo(Id_rollo);
        detalleTranMateriaPrimaScaeModelo.setCodigo(gCodigo);
        detalleTranMateriaPrimaScaeModelo.setPeso(gPeso);
        detalleTranMateriaPrimaScaeModelo.setTipoTransa(gTipo);
        detalleTranMateriaPrimaScaeModelo.setNumTransa(numero_transaccion);
        ListaEscaner.add(detalleTranMateriaPrimaScaeModelo);

        addrollotrans();
    }

    //Se envia la lista de transaccion de rollos al EscanerAdapter y despues a la listview
    public void addrollotrans(){

        EscanerAdapter = new listTrasMateriaPrimaScaeAdapter(EscanerTrasladoMateriaPrimaScae.this,R.layout.item_row_traslado_materiaprima_scae,ListaEscaner);
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

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que reproduce sonido y hace vibrar el dispositivo
    public void AudioError(){
        sp.play(sonido_de_Reproduccion,100,100,1,0,0);
        vibrator.vibrate(2000);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

}