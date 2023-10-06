package com.example.handheld;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.handheld.ClasesOperativas.Gestion_alambronLn;
import com.example.handheld.ClasesOperativas.Ing_prod_ad;
import com.example.handheld.ClasesOperativas.ObjTraslado_bodLn;
import com.example.handheld.ClasesOperativas.Obj_ordenprodLn;
import com.example.handheld.ClasesOperativas.objOperacionesDb;
import com.example.handheld.atv.holder.adapters.listLectorCodAlambronCargueAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.LectorCodAlambronModelo;
import com.example.handheld.modelos.LectorCodCargueModelo;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class lector_cod_alambron_cargue extends AppCompatActivity implements AdapterView.OnItemClickListener{

    //Se declara un objeto conexion
    Conexion conexion;


    //Se declaran los objetos de otras clases necesarias
    Gestion_alambronLn obj_gestion_alambronLn = new Gestion_alambronLn();
    ObjTraslado_bodLn objTraslado_bodLn = new ObjTraslado_bodLn();
    objOperacionesDb objOperacionesDb = new objOperacionesDb();
    Ing_prod_ad ing_prod_ad = new Ing_prod_ad();
    Obj_ordenprodLn Obj_ordenprodLn = new Obj_ordenprodLn();


    //Se declaran las variables de los elementos para el ListView
    private  ListView mListView;
    ListAdapter mAdapterCodAlambron;

    private final List<LectorCodAlambronModelo> ListaCodAlambron = new ArrayList<>();
    public final   ArrayList<String> listaConsecutivos = new ArrayList<>();
    List<LectorCodCargueModelo> requisicionesPendientes = new ArrayList<>();

    //se declaran las variables de los elementos del Layout
    Button btntransaccion,btncargar,btnmuestreo;

    EditText ePesoAlambron,eCodBarrasCargue;
    TextView lblDescripcion,lblCodigo,TxtCountMovi;

    //Variables globales

    String consecutivo,nit_usuario, nit_proveedor,num_importacion,id_detalle,numero_rollo;
    Integer numero_transaccion;
    String id_Alambronrequision; //este es el id de requisicion con el cual
    String numero_transacc;//Este se asignada al consecutivo el cual luego asignada este numero como transaccion
    int eCantRollos;
    int cant;
    boolean yaentre = false;
    String error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lector_cod_alambron_cargue);

        //Definimos los elemetos del layout en la clase
        eCodBarrasCargue=findViewById(R.id.eCodBarrasCargue);
        ePesoAlambron =findViewById(R.id.ePesoAlambron);
        lblDescripcion = findViewById(R.id.lblDescripcion);
        lblCodigo = findViewById(R.id.lblCodigo);
        TxtCountMovi=findViewById(R.id.TxtCountMovi);
        btntransaccion = findViewById(R.id.btntransaccion);
        btncargar=findViewById(R.id.btncargar);
        btnmuestreo=findViewById(R.id.btnmuestreo);

        //Se inicializa la variable de conexion

        conexion = new Conexion();

        //Definimos la variables necesarias recibiendo los datos enviados por la anterior clase

        Intent intent = getIntent();
        id_Alambronrequision = intent.getStringExtra("id_Alambronrequision"); //Recibimos el Id_requision enviado de la clase Lector_Cod_Alambron
        eCantRollos= Integer.parseInt(intent.getStringExtra("eCantRollos"));  //Recibimos la cantidad de rollos de la clase Lector_Cod_Alambron
        nit_usuario = getIntent().getStringExtra("nit_usuario");  //Recibimos el nit_usuario enviado de la clase Lector_Cod_Alambron


        //Se establece el foco en el edit text del codigo de barras
        eCodBarrasCargue.requestFocus();

        //Se crea el ListView para los codigos de Alambron que son leidos

        mListView = findViewById(R.id.listviewLectorAlambron);//Se relaciona la variable mListView a traves del Id asignado en el diseño
        mListView.setOnItemClickListener(this); // obtenemos el elemento cliqueado de la lista

        //Se agrega el primer elemento al listado de los muestreos, se inicializa
        listaConsecutivos.add("Seleccionar");

        ///////////////////////////////////////////////////////////////////////////////////////////////
        //////////////SE PROGRAMAN LAS ACCIONES ONCLICKLISTENER DE LOS BOTONES ////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////

        //se programa el boton Cargar

        btncargar.setOnClickListener(view ->
        {

            if (validarPesoAlambron()) {
                String consecutivo = eCodBarrasCargue.getText().toString();
                String nit_proveedor = obj_gestion_alambronLn.extraerDatoCodigoBarras("proveedor", consecutivo);
                String num_importacion = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_importacion", consecutivo);
                String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarras("detalle", consecutivo);
                String numero_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_rollo", consecutivo);
                double peso;
                peso = Double.parseDouble(ePesoAlambron.getText().toString());
               /*toastAcierto("el numero de importacion es :" + num_importacion +"el consecutivo es:"+consecutivo+"el peso:"+peso+"el numero de rollo:"+numero_rollo+
                        "el detalle es :" + id_detalle +"el proveedor es: "+nit_proveedor);*/
                try {
                    addRollo(num_importacion, consecutivo, ePesoAlambron.getText().toString(), numero_rollo, id_detalle, nit_proveedor);
                    btnmuestreo.setEnabled(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
            eCodBarrasCargue.setEnabled(true);
            eCodBarrasCargue.requestFocus();
        }) ;


        //se programa el boton Transaccion
        btntransaccion.setOnClickListener(v -> transaccion());

        //se programa el boton para las acciones del muestreo
        btnmuestreo.setEnabled(false);
        btnmuestreo.setOnClickListener(v ->  datosmuestreo() );

        //Se realiza la validacion para cuando se oprime la tecla enter sobre el codigo de barras si este se ingresa manualmente
        eCodBarrasCargue.setOnKeyListener((v, keyCode, event) -> {

            if (!yaentre && keyCode == KeyEvent.KEYCODE_ENTER) {
                if(eCodBarrasCargue.getText().equals("")){
                    toastError("Por favor escribir o escanear el codigo de barras");
                }else{
                    closeTecladoMovil();
                    codigoIngresado();

                }
                return true;
            }
            return false;
        });




       // verificarTransaccionesPendientes(nit_usuario,id_Alambronrequision);
        ValidarRequisicionesIniciadas();


    }

    private void datosmuestreo() {

        for (String consecutivo : listaConsecutivos) {
            System.out.println(consecutivo);
            Log.d("TAG", consecutivo);
        }

        Intent intent = new Intent(this, lector_cod_alambron_muestreo.class);
        intent.putStringArrayListExtra("listaConsecutivos", listaConsecutivos);
        startActivity(intent);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///SE AGREGAN LOS METODOS PARA VALIDAR LAS REQUISICIONES PENDIENTES Y CARGAR LOS ROLLOS LEIDOS///
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public void ValidarRequisicionesIniciadas(){
        verificarTransaccionesPendientes(nit_usuario,id_Alambronrequision);
        if(requisicionesPendientes.isEmpty()){
            toastAcierto("Se habilita el sistema para la lectura de los rollos a descargar");
        }
        else{
            toastAlert("Existe un descargue de Alambron incompleto, se cargaran los datos para terminar el proceso");

        }
    }





    public void verificarTransaccionesPendientes(String nit, String id_resicionpendiente) {

        String sql= "SELECT CAST (e.nit_proveedor  AS varchar(25) ) + '-' + CAST (e.numero_importacion AS varchar(25) ) \n" +
                "+'-'+ CAST (d.id_det AS varchar(25) )+'-'+ CAST (r.numero_rollo  AS varchar(25) ) As consecutivo,\n" +
                "e.nit_proveedor ,e.numero_importacion,e.fecha,d.codigo,r.numero_rollo ,r.peso,d.costo_kilo,d.id_det ,\n" +
                "r.numero_rollo , a.id As id_requisicion, a.placa , a.peso_cargado,a.peso_descargado,a.num_rollos  \n" +
                "FROM J_alambron_importacion_det_rollos r , J_alambron_solicitud_det d , J_alambron_solicitud_enc e ,J_alambron_requisicion a\n" +
                "WHERE r.num_transaccion is null AND r.nit_proveedor <> 999999999 AND   d.nit_proveedor = e.nit_proveedor AND\n" +
                "r.nit_proveedor = d.nit_proveedor AND  d.num_importacion = e.numero_importacion  AND\n" +
                "r.num_importacion = d.num_importacion  AND r.id_solicitud_det  = d.id_det \n" +
                "AND a.id =' " + id_resicionpendiente + "' \n " + "AND r.id_requisicion =' " + id_resicionpendiente + "' \n" +
                "AND a.nit = '" + nit + "'";

        requisicionesPendientes=conexion.lista_pendientes_requisicion(lector_cod_alambron_cargue.this,sql);

        if(!requisicionesPendientes.isEmpty())
        {
        for (LectorCodCargueModelo consecutivo : requisicionesPendientes) {
            System.out.println(consecutivo);//imprimimos todos los datos de la lista
            Log.d("TAG", String.valueOf(consecutivo));
            LectorCodAlambronModelo modelo = new LectorCodAlambronModelo();
            modelo.setCosto_unitario_alambron(consecutivo.getCosto_unitario_alambron());
            modelo.setPesoAlambron(consecutivo.getPesoAlambron());
            modelo.setCodigoalambron(consecutivo.getCodigoalambron());
            modelo.setConsecutivo(consecutivo.getConsecutivo());
            modelo.setNit_proveedor(consecutivo.getNit_proveedor());
            modelo.setNumero_transaccion(consecutivo.getNumero_transaccion());
            modelo.setNum_imp(consecutivo.getNum_imp());
            modelo.setDetalle(consecutivo.getDetalle());
            modelo.setNum_rolloAlambron(consecutivo.getNum_rolloAlambron());
            modelo.setEstado_muestra("0");
            ListaCodAlambron.add(modelo);

            mAdapterCodAlambron = new listLectorCodAlambronCargueAdapter(lector_cod_alambron_cargue.this, R.layout.item_row_lectorcodalambron, ListaCodAlambron);
            mListView.setAdapter(mAdapterCodAlambron);
        }
            toastAcierto("Se cargaran los datos de la requisicion pendiente");
            contar_movimientos();

        }else {
            toastAcierto("Por favor inicie la lectura de los rollos  descagados");
        }


    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////SE CREAN LOS METODOS Y FUNCIONES PARA EL LLENADO Y CONSULTA DE DATOS//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        view.setBackgroundColor(Color.GREEN);
        toastAcierto("Elemento Seleccionado: "+position);
    }


    //Metodo para ocultar el teclado virtual
    private void closeTecladoMovil() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    // Se crea el metodo para validar el peso del alambron ingresado
    private boolean validarPesoAlambron() {
        boolean resp = true;
        String pesoText = ePesoAlambron.getText().toString();
        double peso;
        try {
            peso = Double.parseDouble(pesoText);
        } catch (NumberFormatException e) {
            toastError("Ingrese un peso correcto");
            return false;
        }

        if (peso < 200 || peso > 2700 || pesoText.isEmpty()) {
            toastError("El peso ingresado es inválido, el peso debe ser igual o mayor a 200 y menor a 2700");
            //Se establece el foco en el edit text
            ePesoAlambron.requestFocus();
            resp = false;
        }
        //Inicializamos las variables globales
        consecutivo = eCodBarrasCargue.getText().toString();
        //consecutivo = "444444218-4-3-168";
        toastAcierto("el consecutivo es:"+consecutivo);

        if (!validarCodigoBarras(consecutivo)) {
            eCodBarrasCargue.setEnabled(true);
            resp = false;
        }
        return resp;
    }


    private void codigoIngresado(){
        consecutivo = eCodBarrasCargue.getText().toString();
        //consecutivo = "444444218-4-3-168";
        if (validarCodigoBarras(consecutivo)){
            nit_proveedor = obj_gestion_alambronLn.extraerDatoCodigoBarras("proveedor", consecutivo);
            num_importacion = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_importacion", consecutivo);
            id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarras("detalle", consecutivo);
            numero_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_rollo", consecutivo);
            String sql_codigo="SELECT codigo FROM J_alambron_solicitud_det WHERE num_importacion = " + num_importacion + " AND nit_proveedor =" + nit_proveedor + " AND id_det =" + id_detalle;
            String codigo = conexion.obtenerCodigo(lector_cod_alambron_cargue.this ,sql_codigo);
            if(validarRolloRegistrado(num_importacion,numero_rollo,nit_proveedor,id_detalle)) {
                lblCodigo.setText(codigo); //Asigna el valor del codigo de la base de datos al campo TextView del Activity
                String sql_descripcion="SELECT descripcion  FROM  referencias WHERE codigo = '" + codigo + "'";
                String descripcion = conexion.obtenerDescripcionCodigo(lector_cod_alambron_cargue.this,sql_descripcion);
                lblDescripcion.setText(descripcion);
                eCodBarrasCargue.setEnabled(false);
                ePesoAlambron.setText("");
                ePesoAlambron.requestFocus();
                //nit_proveedor= "999999999";
                if (nit_proveedor.equals("999999999")){
                    //Creamos el mensaje que se mostrara con la pregunta
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setIcon(R.mipmap.ic_error_mimap).
                            setTitle("¿Desactivar?").
                            setMessage("¿Desea desactivar este tiquete único?").
                            setPositiveButton("Aceptar", (dialogInterface, i) -> {
                                boolean resp;
                                resp = conexion.eliminarTiqueteUnico(lector_cod_alambron_cargue.this, num_importacion, numero_rollo, nit_proveedor, id_detalle);
                                if (resp){
                                    toastAcierto("El rollo se desactivo en forma correcta!");
                                }else{
                                    toastError("!Error al desactivar el rollo");
                                }
                            }).
                            setNegativeButton("Cancelar", (dialogInterface, i) -> toastError("Se cancelo la eliminacion"));
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            }else{
                toastError("El rollo ya se registro! /n" + "Rollo registrado");
                leer_nuevo();
            }
        }
    }



    //Se crea el metodo para validar que el codigo de barras ingresado existe
    private boolean validarCodigoBarras(String consecutivo){
        boolean resp = false;

        String nit_proveedor = obj_gestion_alambronLn.extraerDatoCodigoBarras("proveedor", consecutivo);
        String num_importacion = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_importacion", consecutivo);
        String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarras("detalle", consecutivo);
        String numero_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarras("num_rollo", consecutivo);

        if (!num_importacion.isEmpty() && !numero_rollo.isEmpty() && !id_detalle.isEmpty() && !nit_proveedor.isEmpty()) {
            String sql = "SELECT id as id  FROM J_alambron_importacion_det_rollos WHERE num_importacion =" + num_importacion +
                    " AND numero_rollo = " + numero_rollo + " AND nit_proveedor = " + nit_proveedor + " AND id_solicitud_det = " + id_detalle;
            String id = conexion.obtenerIdAlamImport(lector_cod_alambron_cargue.this, sql);
            //if (id.isEmpty()){
            if (!id.isEmpty()){
                //toastError("Intente leerlo nuevamente,Problemas con el tiquete");
                //leer_nuevo();
                resp=true;

            }//else{
            // resp = true;
            //}
        }else{
            toastError("Intente leerlo nuevamente,Problemas con el tiquete");
            leer_nuevo();
        }
        return resp;
    }


    //Se crea el metodo para leer los nuevos tiquetes o cuando se ingresa un tiquete erroneo
    private void leer_nuevo(){
        eCodBarrasCargue.setText("");
        ePesoAlambron.setText("PESO");
        ePesoAlambron.setEnabled(true);
        //Se establece el foco en el edit text
        eCodBarrasCargue.requestFocus();
        eCodBarrasCargue.setEnabled(true);
    }


    //Metodo que consulta y obtiene el peso del rollo registrado

    /*private boolean validarRolloRegistrado(String num_importacion, String num_rollo, String nit_proveedor, String id_detalle){
        boolean resp = false;
        String sql = "SELECT peso FROM J_alambron_importacion_det_rollos WHERE peso IS NOT NULL AND num_importacion =" + num_importacion + " AND numero_rollo = " + num_rollo + " AND nit_proveedor=" + nit_proveedor + " AND id_solicitud_det =" + id_detalle;
        String peso = conexion.obtenerPesoAlamImport(getApplicationContext(), sql);
        if (!peso.isEmpty()){
            resp = true;
        }
        return resp;
    }*/


    private boolean validarRolloRegistrado(String num_importacion, String num_rollo, String nit_proveedor, String id_detalle){
        boolean resp = false;
        String sql = "SELECT id_requisicion as id FROM J_alambron_importacion_det_rollos WHERE peso IS NOT NULL AND num_importacion =" + num_importacion + " AND numero_rollo = " + num_rollo + " AND nit_proveedor=" + nit_proveedor + " AND id_solicitud_det =" + id_detalle;
        String id = conexion.obtenerIdAlamImport(getApplicationContext(), sql);
        if (id.isEmpty()){
            resp = true;
        }
        return resp;
    }


    //Se crea el metodo para agregar y registrar los rollos leidos (tickets)
    public void addRollo(String num_importacion, String consecutivo, String peso, String num_rollo, String id_detalle, String nit_prov) throws SQLException {

        String sql_costo_kilo="SELECT costo_kilo FROM  J_alambron_solicitud_det WHERE num_importacion = " + num_importacion + " AND nit_proveedor =" + nit_prov + " AND id_det =" + id_detalle;
        String costo_kilo = conexion.obtenerCostoUnit(lector_cod_alambron_cargue.this,sql_costo_kilo); //Asigna el valor del costo unitario de la base de datos al campo del ListView del Activity
        String sql_codigo="SELECT codigo FROM J_alambron_solicitud_det WHERE num_importacion = " + num_importacion + " AND nit_proveedor =" + nit_prov + " AND id_det =" + id_detalle;
        String codigo = conexion.obtenerCodigo(lector_cod_alambron_cargue.this ,sql_codigo);
        lblCodigo.setText(codigo); //Asigna el valor del codigo de la base de datos al campo TextView del Activity
        String sql_descripcion="SELECT descripcion  FROM  referencias WHERE codigo = '" + codigo + "'";
        String descripcion = conexion.obtenerDescripcionCodigo(lector_cod_alambron_cargue.this,sql_descripcion);
        lblDescripcion.setText(descripcion);

        LectorCodAlambronModelo modelo = new LectorCodAlambronModelo();
        modelo.setCosto_unitario_alambron(costo_kilo.toString());
        modelo.setPesoAlambron(peso.toString());
        modelo.setCodigoalambron(codigo.toString());
        modelo.setConsecutivo(consecutivo.toString());
        modelo.setNit_proveedor(nit_prov.toString());
        modelo.setNumero_transaccion(id_Alambronrequision.toString());
        modelo.setNum_imp(num_importacion.toString());
        modelo.setDetalle(id_detalle.toString());
        modelo.setNum_rolloAlambron(num_rollo.toString());
        modelo.setEstado_muestra("0");
        ListaCodAlambron.add(modelo);

        mAdapterCodAlambron=new listLectorCodAlambronCargueAdapter(lector_cod_alambron_cargue.this,R.layout.item_row_lectorcodalambron,ListaCodAlambron);
        mListView.setAdapter(mAdapterCodAlambron);


        //Se agrega los consecutivos leidos correctamente que luego se usaran en el Activity del muestreo
        listaConsecutivos.add(consecutivo);


        //Se realiza el registro del peso ingresado para el rolllo leido a la base de datos
        String sql_peso_rollo = "UPDATE J_alambron_importacion_det_rollos " +
                " SET peso =" + peso + " ,id_requisicion = " + id_Alambronrequision + " " +
                "WHERE num_importacion=" + num_importacion + " AND numero_rollo =" + num_rollo + " AND nit_proveedor =" + nit_prov + " AND id_solicitud_det =" + id_detalle;
        int pesoConsulta= objOperacionesDb.ejecutarUpdateDbProduccion(sql_peso_rollo, lector_cod_alambron_cargue.this);
        toastAcierto("El peso se registro correctamente");

        contar_movimientos();
        leer_nuevo();


        //mAdapterCodAlambron = mListView.getAdapter();
        for (int i = 0; i < mAdapterCodAlambron.getCount(); i++) {
            Object item = mAdapterCodAlambron.getItem(i);
            if (item instanceof View) {
                View view = (View) item;
                TextView nitProveedorTextView = view.findViewById(R.id.txtNit);
                String nitProveedor = nitProveedorTextView.getText().toString();
                //if (nitProveedor.equals("999999999")) {
                if (nitProveedor.equals("999999999")){
                    view.setBackgroundColor(Color.YELLOW);
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
            }
        }


    }

    //Funcion para contar los movimientos es decir numero de rollos leidos en la lista

        private void contar_movimientos() {
            cant = mAdapterCodAlambron.getCount();
            TxtCountMovi.setText(String.valueOf(cant));
        }

   /* private void contar_movimientos() {
        try {
            if (mAdapterCodAlambron == null) {
                throw new NullPointerException("Se iniciara el proceso de lectura de tiquetes de alambron");
            }

            if (mAdapterCodAlambron.getCount() == 0) {
                cant = 0; // Asignar 0 cuando la lista esté vacía
            } else {
                cant = mAdapterCodAlambron.getCount();
            }

            TxtCountMovi.setText(String.valueOf(cant));
        } catch (NullPointerException e) {
            // Manejar la excepción aquí
            e.printStackTrace(); // Opcionalmente, imprimir el rastro de la excepción para depuración
            toastAcierto("El sistema esta listo para iniciar el descargue");
            // Realiza alguna acción adecuada para manejar la excepción, como mostrar un mensaje de error al usuario
        }
    }*/


    //Metodo para llamar los procesos para la transaccion
    public void transaccion () {

        int resp;
        if (mAdapterCodAlambron.getCount() > 0) {
            resp = new AlertDialog.Builder(lector_cod_alambron_cargue.this)
                    .setTitle("Terminar?")
                    .setMessage("Seguro que terminó de descargar el camión?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (eCantRollos < 0 ) {
                                Toast.makeText(lector_cod_alambron_cargue.this, "Ingrese la cantidad de rollos en la planilla de descargue", Toast.LENGTH_SHORT).show();
                            } else if (mAdapterCodAlambron.getCount() == eCantRollos) {
                                try {
                                  //  toastAlert("Estamos ingresando al proceso de verificar transacciones");
                                    if (verificar_proveedores_transacciones()) {
                                        try {
                                            if (terminar_cargue()) {
                                                nuevo_requisicion();
                                                toastAcierto("Requisición terminada con éxito");
                                            } else {
                                                toastError( "Error al cerrar la requisición");
                                            }
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        toastError("Primero La cantidad de rollos leídos no coincide con la ingresada en la planilla: "+eCantRollos+ "En la lista hay: "+ mAdapterCodAlambron.getCount());

                                    }
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                toastError( "Segundo La cantidad de rollos registrados no coincide con la planilla"+eCantRollos + "En la lista hay "+ mAdapterCodAlambron.getCount());
                            }
                        }
                    })
                    .setNegativeButton("No", null)
                    .setNeutralButton("Cancelar", null)
                    .show()
                    .getButton(DialogInterface.BUTTON_POSITIVE).getId();
        } else {
            toastError( "No se encontraron rollos por cargar");
        }

    }


    //**************************************************************************************************//
    // *********** SE CREAN LOS METODOS Y LAS FUNCIONES PARA VALIDAR EL NIT DEL PROVEEDOR **************//
    //**************************************************************************************************//
    private boolean verificar_proveedores_transacciones() throws SQLException {
       // toastAcierto("Entramos en verificar proveedores");
        boolean resp = false;
        TableLayout tl_codigos_valores = new TableLayout(this);
        Collections.sort(ListaCodAlambron, new Comparator<LectorCodAlambronModelo>() {
            @Override
            public int compare(LectorCodAlambronModelo o1, LectorCodAlambronModelo o2) {
                return Double.compare(Double.parseDouble(o1.getNit_proveedor()), Double.parseDouble(o2.getNit_proveedor()));
            }
        });
        double prov_ant = 0;
        double nit_proveedor = 0;
        double num_importacion = 0;
        for (int i = 0; i < ListaCodAlambron.size(); i++) {
            nit_proveedor = Double.parseDouble(ListaCodAlambron.get(i).getNit_proveedor());
            if (nit_proveedor != 999999999) {
                num_importacion = Double.parseDouble(ListaCodAlambron.get(i).getNum_imp());
                if (prov_ant != nit_proveedor) {
                    prov_ant = nit_proveedor;
                    tl_codigos_valores.removeAllViews();
                  //  toastAlert("Se inicia creacion del TableLayout");
                    tl_codigos_valores = get_datos(nit_proveedor);
                      toastAcierto("Se construyo el TableLayout");
                    // printTableLayout(tl_codigos_valores);
                  //  toastAcierto("Se llama y se crea el TableLayout del metodo get_datos correctamente");


                    if (realizar_transaccion(tl_codigos_valores, nit_proveedor, num_importacion)) {
                        resp = true;
                    }

                }
            }
        }
        //Prueba para imprimir TABLELAYOUT
        // Imprimir el TableLayout en la consola
       // toastAlert("Estamos saliendo de verificar proovedores");

        return resp;
    }


    private TableLayout get_datos(double nit_proveedor) {
        TableLayout tl = new TableLayout(this);

        TableRow headerRow = new TableRow(this);

        TextView codigoHeader = new TextView(this);

        codigoHeader.setText("codigo");
        headerRow.addView(codigoHeader);
        TextView pesoHeader = new TextView(this);
        pesoHeader.setText("peso");
        headerRow.addView(pesoHeader);
        TextView cantidadHeader = new TextView(this);
        cantidadHeader.setText("cantidad");
        headerRow.addView(cantidadHeader);
        TextView nitProveedorHeader = new TextView(this);
        nitProveedorHeader.setText("nit_proveedor");
        headerRow.addView(nitProveedorHeader);
        TextView costoKiloHeader = new TextView(this);
        costoKiloHeader.setText("costo_kilo");
        headerRow.addView(costoKiloHeader);
        TextView numImportacionHeader = new TextView(this);
        numImportacionHeader.setText("num_importacion");
        headerRow.addView(numImportacionHeader);
        TextView detalleHeader = new TextView(this);
        detalleHeader.setText("detalle");
        headerRow.addView(detalleHeader);
        TextView numRolloHeader = new TextView(this);
        numRolloHeader.setText("num_rollo");
        headerRow.addView(numRolloHeader);
        tl.addView(headerRow);

        for(int i=0; i < mAdapterCodAlambron.getCount(); i++) {

            LectorCodAlambronModelo modelo= ListaCodAlambron.get(i);

            double comparanit_proveedor=Double.parseDouble(modelo.getNit_proveedor());
            if(modelo.getCodigoalambron() != null)
            {
                if (modelo.getPesoAlambron()!= null)
                {
                    if(modelo.getCosto_unitario_alambron() != null)
                    {
                        if(nit_proveedor==comparanit_proveedor) {

                            TableRow tableRow = new TableRow(this);
                            TextView codigoTV = new TextView(lector_cod_alambron_cargue.this);
                            codigoTV.setText(modelo.getCodigoalambron());
                            tableRow.addView(codigoTV);

                            TextView pesoTV = new TextView(lector_cod_alambron_cargue.this);
                            pesoTV.setText(modelo.getPesoAlambron());
                            tableRow.addView(pesoTV);

                            TextView cantidadTV = new TextView(lector_cod_alambron_cargue.this);
                            cantidadTV.setText(TxtCountMovi.getText().toString());
                            tableRow.addView(cantidadTV);

                            TextView nitProveedorTV = new TextView(lector_cod_alambron_cargue.this);
                            nitProveedorTV.setText(modelo.getNit_proveedor());
                            tableRow.addView(nitProveedorTV);

                            TextView costoKiloTV = new TextView(lector_cod_alambron_cargue.this);
                            costoKiloTV.setText(modelo.getCosto_unitario_alambron());
                            tableRow.addView(costoKiloTV);

                            TextView numImportacionTV = new TextView(lector_cod_alambron_cargue.this);
                            numImportacionTV.setText(modelo.getNum_imp());
                            tableRow.addView(numImportacionTV);

                            TextView detalleTV = new TextView(lector_cod_alambron_cargue.this);
                            detalleTV.setText(modelo.getDetalle());
                            tableRow.addView(detalleTV);

                            TextView numRolloTV = new TextView(lector_cod_alambron_cargue.this);
                            numRolloTV.setText(modelo.getNum_rolloAlambron());
                            tableRow.addView(numRolloTV);
                            tl.addView(tableRow);
                        }
                    }
                }
            }
        }

        return tl;
    }

    //--------------------------------METODO DE PRUEBA PARA IMPRIMIR EL TABLELAYOU----------//

    private void printTableLayout(TableLayout tableLayout) {
        int count = tableLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                int columnCount = row.getChildCount();
                for (int j = 0; j < columnCount; j++) {
                    View columnView = row.getChildAt(j);
                    if (columnView instanceof TextView) {
                        TextView textView = (TextView) columnView;
                        Log.d("TableLayout", textView.getText().toString());
                    }
                }
            }
        }
    }



    //Metodo que realiza la transacción
    public boolean realizar_transaccion(TableLayout dtValores, double nitProveedor, double numImportacion) throws SQLException {
        boolean resp = true;
        String tipo = "";
        String modelo = "";
        ArrayList<Object> listTransaccionProd = new ArrayList<Object>();
        ArrayList<Object> listTransaccionCorsan = new ArrayList<Object>();
        String sql_proveedor="SELECT nombres FROM terceros WHERE nit = " + nitProveedor;
        String nombreProveedor = conexion.obtenerNombreProveedor(lector_cod_alambron_cargue.this,sql_proveedor);
        toastAcierto("Entramos en realizar transaccion y obtenemos el nombre del proveedor: "+nombreProveedor);
        String sqlEnc = "SELECT numero_importacion, nit_proveedor, tipo, modelo " +
                "FROM J_alambron_solicitud_enc " +
                "WHERE numero_importacion = " + numImportacion + " AND nit_proveedor = " + nitProveedor;
        ArrayList<HashMap<String, Object>> dtEnc = objOperacionesDb.listadoDatosProduccionHasp(sqlEnc, lector_cod_alambron_cargue.this);
        for (int i = 0; i < dtEnc.size(); i++) {
            HashMap<String, Object> row = dtEnc.get(i);
            tipo = Objects.requireNonNull(row.get("tipo")).toString();
            modelo = Objects.requireNonNull(row.get("modelo")).toString();
            //nitProveedor = Double.parseDouble(row.get("nit_proveedor").toString());
        }
        String sqlPesoRollo = "";
        //toastAlert("Entramos al metodo ingProdDms");
        listTransaccionCorsan.addAll(ingProdDms(dtValores, tipo, nitProveedor, modelo, numImportacion));
        //toastAcierto("Salimos del metodo ingProdDms");
        for (int i = 1; i < dtValores.getChildCount(); i++) {
            TableRow row = (TableRow) dtValores.getChildAt(i);
            TextView codigo = (TextView) row.getChildAt(0);
            TextView peso = (TextView) row.getChildAt(1);
            TextView num_importacion = (TextView) row.getChildAt(5);
            TextView num_rollo = (TextView) row.getChildAt(7);
            TextView costo_kilo = (TextView) row.getChildAt(4);
            TextView detalle = (TextView) row.getChildAt(6);
            if (codigo != null && peso != null && num_importacion != null &&
                    num_rollo != null && costo_kilo != null && detalle != null) {
                sqlPesoRollo = "UPDATE J_alambron_importacion_det_rollos " +
                        "SET num_transaccion = " + numero_transacc + " " +
                        "WHERE num_importacion = " + num_importacion.getText().toString() +
                        " AND id_solicitud_det = " + detalle.getText().toString() +
                        " AND numero_rollo = " + num_rollo.getText().toString() +
                        " AND nit_proveedor = " + nitProveedor;
                listTransaccionProd.add(sqlPesoRollo);
            }
        }
        //if (ing_prod_ad.ExecuteSqlTransaction(listTransaccionCorsan, "CORSAN",lector_cod_alambron_cargue.this).equals("")) {
           error=ing_prod_ad.ExecuteSqlTransaction(listTransaccionCorsan, "CORSAN",lector_cod_alambron_cargue.this);
            if (error.equals("")) {
            Toast.makeText(this, "Transacción realizada con éxito!\n" + nombreProveedor + "\n" + tipo + ": " + numero_transaccion, Toast.LENGTH_LONG).show();
            if (!ing_prod_ad.ExecuteSqlTransaction(listTransaccionProd, "PRGPRODUCCION",lector_cod_alambron_cargue.this).equals("")) {
                toastError("Error al actualizar los Códigos de barra, comuníquese con sistemas!");
                //Toast.makeText(this, "Error al actualizar los Códigos de barra, comuníquese con sistemas!", Toast.LENGTH_LONG).show();
                resp = false;
            }
        } else {
            //toastError("Error al realizar la transacción!");
            //Toast.makeText(this, "Error al realizar la transacción!", Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(lector_cod_alambron_cargue.this);
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

            resp = false;
        }
        return resp;
    }


    //Metodo para registrar la transacción en DMS
    public List<Object> ingProdDms(TableLayout tl, String tipo, double nit_proveedor, String modelo, double num_imp) throws SQLException {
        String bodega = "1";
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String Fec = dateFormat.format(calendar.getTime());
        Date dFec = new Date();
        String usuario = nit_usuario;
        String notas = "MV No." + num_imp + ". " + Fec + " usr:" + usuario;
        //String notas = "SP No." + num_imp + ". " + dFec.getYear() + "-" + dFec.getMonth() + "-" + dFec.getDay() + " usr:" + usuario;
        numero_transacc = Obj_ordenprodLn.mover_consecutivo(tipo, lector_cod_alambron_cargue.this);
        //toastAcierto("Pasamos el metodo mover consecutivo y entramos al metodo listaTransaccionTableLayout_importaciones"+ numero_transacc);
        toastAcierto("Se asigna el consecutivo y se asigno el siguiente numero de transaccion: " + numero_transacc);
        List<Object> transacciones = objTraslado_bodLn.listaTransaccionTableLayout_importaciones(Integer.parseInt(numero_transacc), tl,  bodega,calendar, notas, usuario, tipo, nit_proveedor, modelo,lector_cod_alambron_cargue.this);
        //toastAcierto("se termina la transaccion de listaTransaccionTableLayout_importaciones valida las bases de datos");

        return transacciones;
    }



    //Metodo para finalizar la carga
    private boolean terminar_cargue() throws SQLException {
        boolean resp = true;
        String sql_terminar_cargue = "UPDATE J_alambron_requisicion SET fecha_final = GETDATE(), num_rollos = " + TxtCountMovi.getText() + " " + "WHERE id = " + id_Alambronrequision;
        if (objOperacionesDb.ejecutarUpdateDbProduccion(sql_terminar_cargue, lector_cod_alambron_cargue.this) > 0) {
            toastAcierto("La planilla se cerró en forma correcta!");
        } else {
            toastError( "Error al cerrar la planilla!");
            resp = false;
        }
        return resp;
    }


    //Se crea el metodo para una nueva requisicion limpiando los campos de todos todos los activitys y regresando al activity principal
    private void nuevo_requisicion() {

        ePesoAlambron.setText("PESO");
        ePesoAlambron.setTextColor(Color.DKGRAY);
        lblCodigo.setText("LEA CÓDIGO");
        lblDescripcion.setText("LEA CÓDIGO");
        eCodBarrasCargue.setText("");
        eCodBarrasCargue.setTextColor(Color.DKGRAY);
        btncargar.setEnabled(false);
        ePesoAlambron.setEnabled(false);
        eCodBarrasCargue.requestFocus();
        id_Alambronrequision = "";
        TxtCountMovi.setText("");
        this.finish();
        //Intent intent = new Intent(this, Lector_Cod_Alambron.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //startActivity(intent);

    }



    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////METODOS PARA LOS MENSAJES PERSONALIZADOS DE ACIERTO Y ERROR ///////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    //METODO DE TOAST PERSONALIZADO PARA ERROR
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

    //METODO DE TOAST PERSONALIZADO PARA ACIERTOS
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


    //METODO DE TOAST PERSONALIZADO PARA ALERTAR
    public void toastAlert(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_per_no_encon, findViewById(R.id.ll_custom_toast_per_no_encon));
        view.setBackgroundColor(Color.YELLOW);
        TextView txtMensaje = view.findViewById(R.id.txtMensajeToast1);
        txtMensaje.setText(msg);
        txtMensaje.setTextColor(Color.BLACK); // Cambiar el color del texto a negro

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM,0,200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

}