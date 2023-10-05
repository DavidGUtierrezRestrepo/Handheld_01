package com.example.handheld;

import static com.example.handheld.R.id.mensaje_cargando;
import static com.example.handheld.R.id.txtCedulaLogistica;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.example.handheld.ClasesOperativas.Ing_prod_ad;
import com.example.handheld.ClasesOperativas.ObjTraslado_bodLn;
import com.example.handheld.ClasesOperativas.Obj_ordenprodLn;
import com.example.handheld.ClasesOperativas.objOperacionesDb;
import com.example.handheld.atv.holder.adapters.listTrefiTerminadoAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.PersonaModelo;
import com.example.handheld.modelos.TrefiRecepcionModelo;
import com.example.handheld.modelos.TrefiRecepcionadoRollosModelo;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RevisionTerminadoTrefilacion extends AppCompatActivity{

    //se declaran las variables de los elementos del Layout
    EditText codigoTrefi;
    TextView txtTotal, txtTotalSinLeer, txtRollosLeidos;
    Button btnAprobado, btnRechazado, btnCancelarTrans;

    //se declaran las variables donde estaran los datos que vienen de la anterior clase
    String nit_usuario;

    //Se declaran los elementos necesarios para el list view
    ListView listviewTrefiTerminado;
    List<TrefiRecepcionModelo> ListaTrefiRevisado, ListaTrefiRollosRecep;
    List<Object> listRevisionTrefi, listTransaccionBodega, listTransactionTrb1;
    ListAdapter TrefiTerminadoAdapter;
    TrefiRecepcionModelo trefiRecepcionModelo;

    //Se declara un objeto conexion
    Conexion conexion;

    //Se inicializa variables necesarias en la clase
    String consecutivo, motivo, traccion, diametro, centro = "", error, fechaActualString, monthActualString, yearActualString;
    Integer numero_transaccion, numero_revision, repeticiones, paso = 0, yaentre = 0, id_revision;

    Calendar calendar;

    Boolean incompleta = false;
    PersonaModelo personaCalidad;
    ObjTraslado_bodLn objTraslado_bodLn = new ObjTraslado_bodLn();
    objOperacionesDb objOperacionesDb = new objOperacionesDb();
    Ing_prod_ad ing_prod_ad = new Ing_prod_ad();
    List<TrefiRecepcionadoRollosModelo> ListarefeRecepcionados= new ArrayList<>();

    //Se inicializa los varibles para el sonido de error
    SoundPool sp;
    int sonido_de_Reproduccion;

    //Se inicializa una instancia para hacer vibrar el celular
    Vibrator vibrator;

    //Se inicializan las listas para el comboBox
    ArrayList<String> listaRechazos, listaTrefiRechazos;

    private AlertDialog alertDialogRevision, alertDialogTransaccion;
    private TextView cargandoAlertDialogRevision, cargandoAlertDialogTransaccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revision_terminado_trefilacion);

        //Definimos los elementos del Layout
        codigoTrefi = findViewById(R.id.codigoCajaRecep);
        txtTotal = findViewById(R.id.txtTotal);
        txtTotalSinLeer = findViewById(R.id.txtTotalSinLeer);
        txtRollosLeidos = findViewById(R.id.txtRollosLeidos);
        btnAprobado = findViewById(R.id.btnAprobado);
        btnRechazado = findViewById(R.id.btnRechazado);
        btnCancelarTrans = findViewById(R.id.btnCancelarTrans);

        //Recibimos los datos desde la class PedidoInventraio
        nit_usuario = getIntent().getStringExtra("nit_usuario");
        //fecha_inicio = getIntent().getStringExtra("fecha_inicio"); //YA NO SE RECIBE FECHA INICIO
        //fecha_final = getIntent().getStringExtra("fecha_final"); //YA NO SE RECIBE FECHA FINAL

        //Definimos los elementos necesarios para el list view
        listviewTrefiTerminado = findViewById(R.id.listviewTrefiTerminado);
        trefiRecepcionModelo = new TrefiRecepcionModelo();

        //Se Define los varibles para el sonido de error
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC,1);
        sonido_de_Reproduccion = sp.load(this, R.raw.sonido_error_2,1);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Llamamos al metodo para consultar si hay alguna transaccion incompleta
        consultarTransIncompleta();

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se establece el foco en el edit text
        codigoTrefi.requestFocus();

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar (enter) en el EditText inicie el proceso
        codigoTrefi.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (incompleta){
                    codigoTrefi.setText("");
                    toastAtencion("No se pueden leer más tiquetes");
                }else{
                    if(yaentre == 0){
                        if(codigoTrefi.getText().toString().equals("")){
                            toastError("Por favor escribir o escanear el codigo de barras");
                        }else{
                            //Ocultamos el teclado de la pantalla
                            closeTecladoMovil();
                            try {
                                //Verificamos el codigo
                                codigoIngresado();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        //Cargamos de nuevo las varibles y cambiamos "yaentre" a 1 ó 0
                        cargarNuevo();
                    }
                }
                return true;
            }
            return false;
        });

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar el boton se salga del programa
        btnCancelarTrans.setOnClickListener(this::salir);

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar el boton inicie el proceso de transacción
        btnAprobado.setOnClickListener(v -> {
            int sleer = Integer.parseInt(txtTotalSinLeer.getText().toString());
            int total = Integer.parseInt(txtTotal.getText().toString());
            int leidos = (total - sleer);
            //Verificamos que la cantidad de rollos sin leer sea 0 y si hubiera produccion en
            //galvanizado que leer
            if(sleer==0 && total>0){
                //Mostramos el mensaje para logistica
                alertDialogRevision(leidos);
            }else{
                if(sleer==0 && total==0){
                    toastError("No hay rollos por leer");
                    AudioError();
                }else{
                    if (total == sleer){
                        toastError("No se ha leido ningun rollo");
                        AudioError();
                    }
                    else{
                        alertDialogRevision(leidos);
                    }
                }
            }
        });

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar el boton inicie el proceso de transacción
        btnRechazado.setOnClickListener(this::onClick);

    }

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    private void alertDialogRevision(int leidos){
        AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoTrefilacion.this);
        View mView = getLayoutInflater().inflate(R.layout.alertdialog_aprobado,null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) final EditText txtCedulaCalidad = mView.findViewById(txtCedulaLogistica);
        txtCedulaCalidad.setHint("Cedula Calidad");
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView textView6 = mView.findViewById(R.id.textView6);
        textView6.setText("Ingrese la cedula persona calidad");
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView txtMrollos = mView.findViewById(R.id.txtMrollos);
        txtMrollos.setText("Se han leido: "+ leidos +" Rollos");
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnAceptar = mView.findViewById(R.id.btnAceptar);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnCancelar = mView.findViewById(R.id.btnCancelar);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ProgressBar Barraprogreso = mView.findViewById(R.id.progress_bar);
        cargandoAlertDialogRevision = mView.findViewById(mensaje_cargando);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editTraccion = mView.findViewById(R.id.editTraccion);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editDiametro = mView.findViewById(R.id.editDiametro);
        builder.setView(mView);
        alertDialogRevision = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            String CeLog = txtCedulaCalidad.getText().toString().trim();
            if (editTraccion.getText().toString().equals("")){
                toastError("Por favor ingresar Tracción");
            }else{
                if(editDiametro.getText().toString().equals("")){
                    toastError("Por favor ingresar Diametro");
                }else{
                    if (CeLog.equals("")){
                        toastError("Ingresar la cedula de la persona que recepciona");
                    }else{
                        //Verificamos el numero de documentos de la persona en la base da datos
                        personaCalidad = conexion.obtenerPersona(RevisionTerminadoTrefilacion.this,CeLog );
                        centro = personaCalidad.getCentro();
                        //Verificamos que la persona sea de calidad
                        if (centro.equals("4110")){
                            traccion = editTraccion.getText().toString();
                            diametro = editDiametro.getText().toString();
                            Barraprogreso.setVisibility(View.VISIBLE);
                            cargandoAlertDialogRevision.setVisibility(View.VISIBLE);
                            Handler handler = new Handler(Looper.getMainLooper());
                            new Thread(() -> {
                                try {
                                    runOnUiThread(() -> {
                                        try {
                                            realizarRevision();
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                    handler.post(() -> {
                                        Barraprogreso.setVisibility(View.GONE);
                                        cargandoAlertDialogRevision.setVisibility(View.GONE);
                                        alertDialogRevision.dismiss();
                                        closeTecladoMovil();
                                    });
                                } catch (Exception e) {
                                    handler.post(() -> {
                                        Barraprogreso.setVisibility(View.GONE);
                                        cargandoAlertDialogRevision.setVisibility(View.GONE);
                                        alertDialogRevision.dismiss();
                                        toastError(e.getMessage());
                                    });
                                }
                            }).start();
                            closeTecladoMovil();
                        }else{
                            if (centro.equals("")){
                                txtCedulaCalidad.setText("");
                                toastError("Persona no encontrada");
                            }else{
                                txtCedulaCalidad.setText("");
                                toastError("La cedula ingresada no pertenece a calidad!");
                            }
                        }
                    }
                }
            }
        });
        btnCancelar.setOnClickListener(v1 -> alertDialogRevision.dismiss());
        alertDialogRevision.setCancelable(false);
        alertDialogRevision.show();
    }

    private void realizarRevision() throws SQLException {
        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        listRevisionTrefi = new ArrayList<>();

        if (paso==0){
            // Obtén la fecha y hora actual
            Date fechaActual = new Date();
            //Calendar calendar = Calendar.getInstance();

            // Define el formato de la fecha y hora que deseas obtener
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");

            // Convierte la fecha actual en un String con el formato definido
            fechaActualString = formatoFecha.format(fechaActual);

            String sql_revision= "INSERT INTO jd_revision_calidad(fecha_hora,revisor,estado,traccion,diametro)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','A','" + traccion + "','" + diametro + "')";

            try {
                //Se ejecuta el sql_revision en la base de datos
                paso = objOperacionesDb.ejecutarInsertJjprgproduccion(sql_revision,RevisionTerminadoTrefilacion.this);
            }catch (Exception e){
                Toast.makeText(RevisionTerminadoTrefilacion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (paso==1){
            //se adicionan los campos recepcionado, nit_recepcionado y fecha_recepcionado a la tabla.
            String obtenerId = "select id_revision from jd_revision_calidad where fecha_hora='" + fechaActualString + "'";
            numero_revision = conexion.obtenerIdRevision(RevisionTerminadoTrefilacion.this, obtenerId );
            for(int i=0;i<ListaTrefiRollosRecep.size();i++){
                String cod_orden = ListaTrefiRollosRecep.get(i).getCod_orden();
                String id_detalle = ListaTrefiRollosRecep.get(i).getId_detalle();
                String id_rollo = ListaTrefiRollosRecep.get(i).getId_rollo();

                String sql_rollo= "UPDATE J_rollos_tref SET id_revision="+ numero_revision +" WHERE cod_orden='"+ cod_orden +"' AND id_detalle='"+id_detalle+"' AND id_rollo='"+id_rollo+"'";

                try {
                    //Se añade el sql a la lista
                    listRevisionTrefi.add(sql_rollo);
                }catch (Exception e){
                    Toast.makeText(RevisionTerminadoTrefilacion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            if (listRevisionTrefi.size()>0){
                //Ejecutamos la consultas que llenan los campos de recepción
                repeticiones = 0;
                error = revision();
                if (error.equals("")){
                    consultarTrefiTerminado();
                    paso=0;
                    toastAcierto("Revision Realizada con Exito! - " + numero_revision);
                }else{
                    paso=1;
                    btnRechazado.setEnabled(false);
                    incompleta =  true;
                    AudioError();
                    toastError("Error al realizar la revision!" +
                            "Intentelo de nuevo");
                }
            }
        }else{
            btnRechazado.setEnabled(false);
            incompleta =  true;
            AudioError();
            toastError("Error al realizar la revision!" +
                    "Intentelo de nuevo");
        }
    }

    private String revision() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            cargandoAlertDialogRevision.setText("Intento produccion " + repeticiones + "/5");
            error = ing_prod_ad.ExecuteSqlTransaction(listRevisionTrefi, "JJVPRGPRODUCCION", RevisionTerminadoTrefilacion.this);
            if(error.equals("")){
                cargandoAlertDialogRevision.setText("");
                return error;
            }else{
                revision();
            }
        }else{
            return error;
        }
        return error;
    }

    private ArrayList<String> llenarlistaspinner(){
        listaRechazos = new ArrayList<>();

        listaRechazos.add("Seleccione motivo rechazo");
        listaRechazos.add("Piel de naranja");
        listaRechazos.add("Baja/Alta tracción");
        listaRechazos.add("Poroso");
        listaRechazos.add("Daño por montacarga");
        listaRechazos.add("Rayado");
        listaRechazos.add("Tallado");
        listaRechazos.add("Oxidación");
        listaRechazos.add("Fuera de Medida");

        return listaRechazos;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Funcion que genera todas las listas de consultas en la base de datos, las ejecuta generando
    //Una TRB1 en el sistema de bodega 2 a bodega 3 con los rollos leidos
    private void realizarTransaccion(Integer m) {
        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        listTransaccionBodega = new ArrayList<>();
        //Lista donde revertimos la primer consulta si el segundo proceso no se realiza bien
        listRevisionTrefi = new ArrayList<>();
        //Lista donde agregamos las consultas que agrearan el campo trb1
        listTransactionTrb1 = new ArrayList<>();
        //varible para verificar que se registo la recepcion en base de datos

        if (paso==0){
            // Obtén la fecha y hora actual
            Date fechaActual = new Date();
            calendar = Calendar.getInstance();

            // Define el formato de la fecha y hora que deseas obtener
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoMonth = new SimpleDateFormat("MM");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoYear = new SimpleDateFormat("yyyy");

            // Convierte la fecha actual en un String con el formato definido
            fechaActualString = formatoFecha.format(fechaActual);
            monthActualString = formatoMonth.format(fechaActual);
            yearActualString = formatoYear.format(fechaActual);

            String sql_revisionTransa;

            switch (m){
                case 1:
                    sql_revisionTransa= "INSERT INTO jd_revision_calidad(fecha_hora,revisor,estado,defecto,traccion,tipo_transa)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','R','" + motivo + "','" + traccion + "','TRB1')";
                    break;
                case 2:
                    sql_revisionTransa= "INSERT INTO jd_revision_calidad(fecha_hora,revisor,estado,defecto,diametro,tipo_transa)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','R','" + motivo + "',''" + diametro + ",'TRB1')";
                    break;
                default:
                    sql_revisionTransa= "INSERT INTO jd_revision_calidad(fecha_hora,revisor,estado,defecto,tipo_transa)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','R','" + motivo + "','TRB1')";
                    break;
            }

            try {
                //Se ejecuta el sql_revision en la base de datos
                paso = objOperacionesDb.ejecutarInsertJjprgproduccion(sql_revisionTransa,RevisionTerminadoTrefilacion.this);
            }catch (Exception e){
                Toast.makeText(RevisionTerminadoTrefilacion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


        if (paso==1){
            //se adicionan los campos recepcionado, nit_recepcionado y fecha_recepcionado a la tabla
            String obtenerId;
            obtenerId = "select id_revision from jd_revision_calidad where fecha_hora='" + fechaActualString + "'";
            numero_revision = conexion.obtenerIdRevision(RevisionTerminadoTrefilacion.this, obtenerId );
            for(int i=0;i<ListaTrefiRollosRecep.size();i++){
                String cod_orden = ListaTrefiRollosRecep.get(i).getCod_orden();
                String id_detalle = ListaTrefiRollosRecep.get(i).getId_detalle();
                String id_rollo = ListaTrefiRollosRecep.get(i).getId_rollo();

                String sql_rollo= "UPDATE J_rollos_tref SET id_revision="+ numero_revision +" WHERE cod_orden='"+ cod_orden +"' AND id_detalle='"+id_detalle+"' AND id_rollo='"+id_rollo+"'";

                try {
                    //Se añade el sql a la lista
                    listRevisionTrefi.add(sql_rollo);
                }catch (Exception e){
                    Toast.makeText(RevisionTerminadoTrefilacion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            if (listRevisionTrefi.size()>0){
                //Ejecutamos la consultas que llenan los campos de recepción
                repeticiones = 0;
                error = produccion1();
                if (error.equals("")){
                    ListarefeRecepcionados = conexion.trefiRefeRevisados(RevisionTerminadoTrefilacion.this,numero_revision, monthActualString, yearActualString);
                    numero_transaccion = Integer.valueOf(Obj_ordenprodLn.mover_consecutivo("TRB1", RevisionTerminadoTrefilacion.this));
                    listTransaccionBodega = traslado_bodega(ListarefeRecepcionados, calendar);
                    //Ejecutamos la lista de consultas para hacer la TRB1
                    paso=2;
                }else{
                    btnAprobado.setEnabled(false);
                    incompleta =  true;
                    AudioError();
                    toastError("Error al realizar la revision!" +
                            "Intentelo de nuevo");
                }
            }
        }else{
            btnAprobado.setEnabled(false);
            incompleta =  true;
            AudioError();
            toastError("Error al realizar la revision!" +
                    "Intentelo de nuevo");
        }

        if(paso==2){
            repeticiones = 0;
            error = transaccion();
            if (error.equals("")){

                String sql_trb1= "UPDATE jd_revision_calidad SET num_transa="+ numero_transaccion +" WHERE id_revision='"+ numero_revision +"'";
                try {
                    //Se añade el sql a la lista
                    listTransactionTrb1.add(sql_trb1);
                }catch (Exception e){
                    Toast.makeText(RevisionTerminadoTrefilacion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                //Ejecutamos la lista de consultas para relacionar la transaccion con la revision
                repeticiones = 0;
                produccion2();
            }else{
                btnAprobado.setEnabled(false);
                incompleta =  true;
                AudioError();
                toastError("Error al realizar la transacción!" +
                        "Intentelo de nuevo");
            }
        }
    }

    private String produccion1() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            cargandoAlertDialogTransaccion.setText("Intento produccion 1 " + repeticiones + "/5");
            error = ing_prod_ad.ExecuteSqlTransaction(listRevisionTrefi, "JJVPRGPRODUCCION", RevisionTerminadoTrefilacion.this);
            if(error.equals("")){
                cargandoAlertDialogTransaccion.setText("");
                return error;
            }else{
                produccion1();
            }
        }else{
            return error;
        }
        return error;
    }

    private String transaccion() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            cargandoAlertDialogTransaccion.setText("Intento transacción " + repeticiones + "/5");
            error = ing_prod_ad.ExecuteSqlTransaction(listTransaccionBodega, "JJVDMSCIERREAGOSTO", RevisionTerminadoTrefilacion.this);
            if(error.equals("")){
                cargandoAlertDialogTransaccion.setText("");
                return error;
            }else{
                transaccion();
            }
        }else{
            return error;
        }
        return error;
    }

    @SuppressLint("SetTextI18n")
    private void produccion2() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            cargandoAlertDialogTransaccion.setText("Intento produccion 2 " + repeticiones + "/5");
            error = ing_prod_ad.ExecuteSqlTransaction(listTransactionTrb1, "JJVPRGPRODUCCION", RevisionTerminadoTrefilacion.this);
            if(error.equals("")){
                cargandoAlertDialogTransaccion.setText("");
                consultarTrefiTerminado();
                incompleta = false;
                btnAprobado.setEnabled(true);
                paso=0;
                toastAcierto("Transaccion Realizada con Exito! --" + numero_transaccion);
            }else{
                incompleta =  true;
                produccion2();
            }
        }else{
            btnAprobado.setEnabled(false);
            btnRechazado.setEnabled(false);
            btnCancelarTrans.setEnabled(false);
            incompleta =  true;
            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoTrefilacion.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("Hubo un problema en el paso 3 de la transacción, \n Por favor comunicarse inmediatamente con el área de sistemas, \n para poder continuar con las transacciones, de lo \n contrario no se le permitira continuar");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Funcion que genera la lista de consultas que modifican las tablas en la base da datos de Corsan
    //Para generar la transacción
    private List<Object> traslado_bodega(List<TrefiRecepcionadoRollosModelo> ListarefeRecepcionados, Calendar calendar){
        List<Object> listSql;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
        String fecha = dateFormat.format(calendar.getTime());
        String usuario = personaCalidad.getNit();
        String notas = "MOVIL fecha:" + fecha + " usuario:" + usuario;

        listSql = objTraslado_bodLn.listaTrasladoBodegaTrefi(ListarefeRecepcionados,numero_transaccion, 2, 4, calendar, notas, usuario, "TRB1", "20",RevisionTerminadoTrefilacion.this);
        return listSql;
    }

    private void consultarTransIncompleta(){
        conexion = new Conexion();
        //Inicializamos la lista de los rollos escaneados
        ListaTrefiRollosRecep = new ArrayList<>();
        ListaTrefiRevisado = new ArrayList<>();

        //Consultamos si hay rollos con transacciones incompletas
        id_revision = conexion.consultarReviTrefiIncomple(RevisionTerminadoTrefilacion.this);

        if (id_revision.equals(0)){
            /////////////////////////////////////////////////////////////////////////////////////////////
            //Llamamos al metodo para consultar los rollos de galvanizados listos para recoger
            consultarTrefiTerminado();
        }else{
            incompleta = true;
            btnAprobado.setEnabled(false);

            ListaTrefiRollosRecep = conexion.obtenerReviTrefiTerminado(RevisionTerminadoTrefilacion.this, id_revision);

            //Consultamos los rollos de producción que no se han recepcionado en la base de datos
            ListaTrefiRevisado = conexion.obtenerTrefiRevision(RevisionTerminadoTrefilacion.this);

            //Enviamos la lista vacia de rollos escaneados al listview
            TrefiTerminadoAdapter = new listTrefiTerminadoAdapter(RevisionTerminadoTrefilacion.this,R.layout.item_row_trefiterminado,ListaTrefiRollosRecep);
            listviewTrefiTerminado.setAdapter(TrefiTerminadoAdapter);

            //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
            String totalRollos = String.valueOf(ListaTrefiRevisado.size() + ListaTrefiRollosRecep.size());
            txtTotal.setText(totalRollos);

            //Contamos los rollos leidos y sin leer para mostrarlos en los TextView
            contarSinLeer();
            contarLeidos();

            toastAtencion("Transacción incompleta \n" +
                    "Por favor terminarla");
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que consulta los rollos que hay en producción que no se han recepcionado e
    //inicializa el listview
    private void consultarTrefiTerminado() {
        conexion = new Conexion();
        //Inicializamos la lista de los rollos escaneados
        ListaTrefiRollosRecep = new ArrayList<>();

        //Consultamos los rollos de producción que no se han recepcionado en la base de datos
        ListaTrefiRevisado = conexion.obtenerTrefiRevision(getApplication());
        //Enviamos la lista vacia de rollos escaneados al listview
        TrefiTerminadoAdapter = new listTrefiTerminadoAdapter(RevisionTerminadoTrefilacion.this,R.layout.item_row_trefiterminado,ListaTrefiRollosRecep);
        listviewTrefiTerminado.setAdapter(TrefiTerminadoAdapter);

        //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
        String totalRollos = String.valueOf(ListaTrefiRevisado.size());
        txtTotal.setText(totalRollos);

        //Contamos los rollos leidos y sin leer para mostrarlos en los TextView
        contarSinLeer();
        contarLeidos();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo para ocultar el teclado virtual
    private void closeTecladoMovil() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que verifica que el codigo escaneado se encuentre en la lista de rollos de producción
    //No recepcionados
    private void codigoIngresado() throws SQLException {
        consecutivo = codigoTrefi.getText().toString().trim();
        boolean encontrado = false;
        int position = 0;
        for (int i=0;i<ListaTrefiRevisado.size();i++){
            String codigoList = ListaTrefiRevisado.get(i).getCod_orden()+"-"+ListaTrefiRevisado.get(i).getId_detalle()+"-"+ListaTrefiRevisado.get(i).getId_rollo();
            if(consecutivo.equals(codigoList)){
                encontrado = true;
                position = i;
                break;
            }
        }
        //Si el rollos es encontrado o no se muestra mensaje
        if (encontrado){
            //Si el rollo encontrado esta pintado de verde ya fue leido anteriormente
            if(ListaTrefiRevisado.get(position).getColor().equals("GREEN")){
                toastError("Rollo Ya leido");
                AudioError();
                cargarNuevo();
            }else{
                //Copiamos el rollo encontrado de la lista de producción
                trefiRecepcionModelo = ListaTrefiRevisado.get(position);
                //Agregamos la copia a la de los rollos escaneados
                ListaTrefiRollosRecep.add(trefiRecepcionModelo);
                //Pintamos el rollo de verde en la lista de produccion para no poder volverlo a leer
                pintarRollo(position);
                //Contamos los rollos leidos y no leidos
                contarSinLeer();
                contarLeidos();
                //Mostramos mensaje
                toastAcierto("Rollo encontrado");
                //Inicializamos la lectura
                cargarNuevo();
            }
        }else{
            toastError("Rollo no encontrado");
            AudioError();
            cargarNuevo();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que cuenta los rollos que faltan por leer y muestra la cantidad en el TextView
    @SuppressLint("SetTextI18n")
    private void contarSinLeer() {
        int sinLeer = 0;
        /*
        for(int x=0;x<ListaGalvTerminado.size();x++){
            if(ListaGalvTerminado.get(x).getColor().equals("RED")){
                sinLeer++;
            }
        }*/
        sinLeer = Integer.parseInt((String) txtTotal.getText()) - ListaTrefiRollosRecep.size();
        txtTotalSinLeer.setText(Integer.toString(sinLeer));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que cuenta los rollos leeidos y muestra la cantidad en el TextView
    @SuppressLint("SetTextI18n")
    private void contarLeidos() {
        int Leido = 0;
        /*
        for(int x=0;x<ListaGalvTerminado.size();x++){
            if(ListaGalvTerminado.get(x).getColor().equals("GREEN")){
                Leido++;
            }
        }*/
        Leido = ListaTrefiRollosRecep.size();
        txtRollosLeidos.setText(Integer.toString(Leido));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que borra el codigo del EditText y cambia la variable "yaentre"
    private void cargarNuevo() {
        codigoTrefi.setText("");
        if (yaentre == 0){
            yaentre = 1;
        }else{
            yaentre = 0;
            codigoTrefi.requestFocus();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que pinta el rollo encontrado en la lista de producción y muestra en el listView la lista
    //De rollos leidos
    private void pintarRollo(int posicion) {
        ListaTrefiRevisado.get(posicion).setColor("GREEN");
        TrefiTerminadoAdapter = new listTrefiTerminadoAdapter(RevisionTerminadoTrefilacion.this,R.layout.item_row_trefiterminado,ListaTrefiRollosRecep);
        listviewTrefiTerminado.setAdapter(TrefiTerminadoAdapter);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    //METODO PARA CERRAR LA APLICACION
    @SuppressLint("")
    public void salir(View view){
        finishAffinity();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
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
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER,0,200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    //METODO DE TOAST PERSONALIZADO : ATENCION
    public void toastAtencion(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_atencion, findViewById(R.id.ll_custom_toast_atencion));
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView txtMens = view.findViewById(R.id.txtMensajeToastAtencion);
        txtMens.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER,0,200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que reproduce sonido y hace vibrar el dispositivo
    public void AudioError(){
        sp.play(sonido_de_Reproduccion,100,100,1,0,0);
        vibrator.vibrate(2000);
    }

    private void onClick(View v) {
        int sleer = Integer.parseInt(txtTotalSinLeer.getText().toString());
        int total = Integer.parseInt(txtTotal.getText().toString());
        int leidos = (total - sleer);
        //Verificamos que la cantidad de rollos sin leer sea 0 y si hubiera produccion en
        //Trefilación que leer
        if (sleer == 0 && total > 0) {
            //Mostramos el mensaje para logistica
            alertDialogTransaccion(leidos);
        } else {
            if (sleer == 0 && total == 0) {
                toastError("No hay rollos por leer");
                AudioError();
            } else {
                if (total == sleer) {
                    toastError("No se ha leido ningun rollo");
                    AudioError();
                } else {
                    //Segundo AlertDialog
                    alertDialogTransaccion(leidos);
                }
            }
        }
    }

    private void alertDialogTransaccion(int leidos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoTrefilacion.this);
        View mView = getLayoutInflater().inflate(R.layout.alertdialog_rechazado, null);
        @SuppressLint("CutPasteId") final EditText txtCedulaLogistica = mView.findViewById(R.id.txtCedulaLogistica);
        EditText editTraccion = mView.findViewById(R.id.editTraccion);
        Spinner spinnerRechazo = mView.findViewById(R.id.spinnerRechazo);
        listaTrefiRechazos = llenarlistaspinner();
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(RevisionTerminadoTrefilacion.this, android.R.layout.simple_spinner_dropdown_item, listaTrefiRechazos);
        spinnerRechazo.setAdapter(adapter);
        spinnerRechazo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                actualizarAlertDialog(selectedOption, mView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
        TextView txtMrollos = mView.findViewById(R.id.txtMrollos);
        txtMrollos.setText("Se han leido: " + leidos + " Rollos");
        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
        Button btnCancelar = mView.findViewById(R.id.btnCancelar);
        ProgressBar Barraprogreso = mView.findViewById(R.id.progress_bar);
        cargandoAlertDialogTransaccion = mView.findViewById(mensaje_cargando);
        builder.setView(mView);
        alertDialogTransaccion = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            if (!spinnerRechazo.getSelectedItem().equals("Seleccione motivo rechazo")) {
                motivo = spinnerRechazo.getSelectedItem().toString();
                String CeLog = txtCedulaLogistica.getText().toString().trim();
                switch (motivo) {
                    case "Baja/Alta tracción":
                        if(editTraccion.getText().toString().equals("")) {
                            toastError("Por favor ingresar tracción");
                        }else{
                            if (CeLog.equals("")) {
                                toastError("Ingresar la cedula de la persona que recepciona");
                            }else{
                                personaCalidad = conexion.obtenerPersona(RevisionTerminadoTrefilacion.this, CeLog);
                                centro = personaCalidad.getCentro();
                                //Verificamos que la persona pertenezca al centro de logistica
                                if (centro.equals("4110")){
                                    traccion = editTraccion.getText().toString();
                                    Barraprogreso.setVisibility(View.VISIBLE);
                                    cargandoAlertDialogTransaccion.setVisibility(View.VISIBLE);
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    new Thread(() -> {
                                        try {
                                            runOnUiThread(() -> realizarTransaccion(1));
                                            handler.post(() -> {
                                                Barraprogreso.setVisibility(View.GONE);
                                                cargandoAlertDialogTransaccion.setVisibility(View.GONE);
                                                alertDialogTransaccion.dismiss();
                                                closeTecladoMovil();
                                            });
                                        } catch (Exception e) {
                                            handler.post(() -> {
                                                toastError(e.getMessage());
                                                Barraprogreso.setVisibility(View.GONE);
                                                cargandoAlertDialogTransaccion.setVisibility(View.GONE);
                                                alertDialogTransaccion.dismiss();
                                            });
                                        }
                                    }).start();
                                    closeTecladoMovil();
                                }else{
                                    if (centro.equals("")) {
                                        txtCedulaLogistica.setText("");
                                        toastError("Persona no encontrada");
                                    } else {
                                        txtCedulaLogistica.setText("");
                                        toastError("La cedula ingresada no pertenece a calidad!");
                                    }
                                }
                            }
                        }
                        break;
                    case "Fuera de Medida":
                        if(editTraccion.getText().toString().equals("")) {
                            toastError("Por favor ingresar Diametro");
                        }else{
                            if (CeLog.equals("")) {
                                toastError("Ingresar la cedula de la persona que recepciona");
                            }else{
                                personaCalidad = conexion.obtenerPersona(RevisionTerminadoTrefilacion.this, CeLog);
                                centro = personaCalidad.getCentro();
                                //Verificamos que la persona pertenezca al centro de logistica
                                if (centro.equals("4110")){
                                    diametro = editTraccion.getText().toString();
                                    Barraprogreso.setVisibility(View.VISIBLE);
                                    cargandoAlertDialogTransaccion.setVisibility(View.VISIBLE);
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    new Thread(() -> {
                                        try {
                                            runOnUiThread(() -> realizarTransaccion(2));
                                            handler.post(() -> {
                                                Barraprogreso.setVisibility(View.GONE);
                                                cargandoAlertDialogTransaccion.setVisibility(View.GONE);
                                                alertDialogTransaccion.dismiss();
                                                closeTecladoMovil();
                                            });
                                        } catch (Exception e) {
                                            handler.post(() -> {
                                                toastError(e.getMessage());
                                                Barraprogreso.setVisibility(View.GONE);
                                                cargandoAlertDialogTransaccion.setVisibility(View.GONE);
                                                alertDialogTransaccion.dismiss();
                                            });
                                        }
                                    }).start();
                                    closeTecladoMovil();
                                }else{
                                    if (centro.equals("")) {
                                        txtCedulaLogistica.setText("");
                                        toastError("Persona no encontrada");
                                    } else {
                                        txtCedulaLogistica.setText("");
                                        toastError("La cedula ingresada no pertenece a calidad!");
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        if (CeLog.equals("")) {
                            toastError("Ingresar la cedula de la persona que recepciona");
                        } else {
                            personaCalidad = conexion.obtenerPersona(RevisionTerminadoTrefilacion.this, CeLog);
                            centro = personaCalidad.getCentro();
                            //Verificamos que la persona pertenezca al centro de logistica
                            if (centro.equals("4110")) {
                                Barraprogreso.setVisibility(View.VISIBLE);
                                cargandoAlertDialogTransaccion.setVisibility(View.VISIBLE);
                                Handler handler = new Handler(Looper.getMainLooper());
                                new Thread(() -> {
                                    try {
                                        runOnUiThread(() -> realizarTransaccion(3));
                                        handler.post(() -> {
                                            Barraprogreso.setVisibility(View.GONE);
                                            cargandoAlertDialogTransaccion.setVisibility(View.GONE);
                                            alertDialogTransaccion.dismiss();
                                            closeTecladoMovil();
                                        });
                                    } catch (Exception e) {
                                        handler.post(() -> {
                                            toastError(e.getMessage());
                                            Barraprogreso.setVisibility(View.GONE);
                                            cargandoAlertDialogTransaccion.setVisibility(View.GONE);
                                            alertDialogTransaccion.dismiss();
                                        });
                                    }
                                }).start();
                                closeTecladoMovil();
                            } else {
                                if (centro.equals("")) {
                                    txtCedulaLogistica.setText("");
                                    toastError("Persona no encontrada");
                                } else {
                                    txtCedulaLogistica.setText("");
                                    toastError("La cedula ingresada no pertenece a calidad!");
                                }
                            }
                        }
                        break;
                }
            } else {
                toastError("Seleccione motivo rechazo");
            }
        });
        btnCancelar.setOnClickListener(v1 -> alertDialogTransaccion.dismiss());
        alertDialogTransaccion.setCancelable(false);
        alertDialogTransaccion.show();
    }

    private void actualizarAlertDialog(String selectedOption, View mView){
        TextView txtTraccion = mView.findViewById(R.id.txtTraccion);
        EditText editTraccion = mView.findViewById(R.id.editTraccion);
        switch (selectedOption) {
            case "Fuera de Medida":
                txtTraccion.setVisibility(View.VISIBLE);
                txtTraccion.setText("Diametro:");
                editTraccion.setVisibility(View.VISIBLE);
                break;
            case "Baja/Alta tracción":
                txtTraccion.setVisibility(View.VISIBLE);
                txtTraccion.setText("Tracción:");
                editTraccion.setVisibility(View.VISIBLE);
                break;
            case "Seleccione motivo rechazo":
            case "Poroso":
            case "Piel de naranja":
            case "Daño por montacarga":
            case "Rayado":
            case "Tallado":
            case "Oxidación":
            default:
                txtTraccion.setVisibility(View.GONE);
                editTraccion.setVisibility(View.GONE);
                editTraccion.setText("");
                break;
        }
    }

}