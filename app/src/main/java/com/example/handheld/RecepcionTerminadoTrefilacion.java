package com.example.handheld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld.ClasesOperativas.Gestion_alambronLn;
import com.example.handheld.ClasesOperativas.Ing_prod_ad;
import com.example.handheld.ClasesOperativas.ObjTraslado_bodLn;
import com.example.handheld.ClasesOperativas.Obj_ordenprodLn;
import com.example.handheld.atv.holder.adapters.listTrefiTerminadoAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.conexionDB.ConfiguracionBD;
import com.example.handheld.modelos.CorreoModelo;
import com.example.handheld.modelos.PermisoPersonaModelo;
import com.example.handheld.modelos.PersonaModelo;
import com.example.handheld.modelos.RolloTrefiTransa;
import com.example.handheld.modelos.TrefiRecepcionModelo;
import com.example.handheld.modelos.TrefiRecepcionadoRollosModelo;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RecepcionTerminadoTrefilacion extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //se declaran las variables de los elementos del Layout
    EditText codigoTrefi;
    TextView txtTotal, txtTotalSinLeer, txtRollosLeidos;
    Button btnTransaTrefi, btnCancelarTrans;

    //se declaran las variables donde estaran los datos que vienen de la anterior clase
    String nit_usuario, CeLog;

    //Se declaran los elementos necesarios para el list view
    ListView listviewTrefiTerminado;
    List<TrefiRecepcionModelo> ListaTrefiTerminado= new ArrayList<>();
    List<TrefiRecepcionModelo> ListaTrefiRollosRecep;
    ListAdapter TrefiTerminadoAdapter;
    TrefiRecepcionModelo trefiRecepcionModelo;

    //Se declara un objeto conexion
    Conexion conexion;

    //Se inicializa variables necesarias en la clase
    int yaentre = 0, leidos;
    String consecutivo, error, fechaTransaccion, cod_orden,id_detalle,id_rollo,db_produccion;
    Integer numero_transaccion;
    String permiso = "";
    String centro = "";

    Boolean incompleta = false;
    PersonaModelo personaLogistica;

    PermisoPersonaModelo personaProduccion;

    CorreoModelo correo;
    ObjTraslado_bodLn objTraslado_bodLn = new ObjTraslado_bodLn();

    Gestion_alambronLn obj_gestion_alambronLn = new Gestion_alambronLn();
    Ing_prod_ad ing_prod_ad = new Ing_prod_ad();
    List<TrefiRecepcionadoRollosModelo> ListarefeRecepcionados= new ArrayList<>();

    List<Object> listTransactionTrefi;

    //Se inicializa los varibles para el sonido de error
    SoundPool sp;
    int sonido_de_Reproduccion;

    //Se inicializa una instancia para hacer vibrar el celular
    Vibrator vibrator;

    //Se inicializan elementos para enviar correos
    Session session = null;
    ProgressDialog pdialog = null;
    Context context = null;

    String[] rec;
    String subject, textMessage;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepcion_terminado_trefilacion);

        //Definimos los elementos del Layout
        codigoTrefi = findViewById(R.id.codigoCajaRecep);
        txtTotal = findViewById(R.id.txtTotal);
        txtTotalSinLeer = findViewById(R.id.txtTotalSinLeer);
        txtRollosLeidos = findViewById(R.id.txtRollosLeidos);
        btnTransaTrefi = findViewById(R.id.btnTransaEmp);
        btnCancelarTrans = findViewById(R.id.btnCancelarTrans);

        //Recibimos los datos desde la class PedidoInventraio
        nit_usuario = getIntent().getStringExtra("nit_usuario");
        //fecha_inicio = getIntent().getStringExtra("fecha_inicio"); //YA NO SE RECIBE FECHA INICIO
        //fecha_final = getIntent().getStringExtra("fecha_final"); //YA NO SE RECIBE FECHA FINAL

        //Definimos los elementos necesarios para el list view
        listviewTrefiTerminado = findViewById(R.id.listviewTrefiTerminado);
        listviewTrefiTerminado.setOnItemClickListener(this); //Determinamos a que elemento va dirigido el OnItemClick
        trefiRecepcionModelo = new TrefiRecepcionModelo();

        //Se Define los varibles para el sonido de error
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC,1);
        sonido_de_Reproduccion = sp.load(this, R.raw.sonido_error_2,1);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Definimos en una variable la base de datos que estamos utilizando en prgproduccion
        db_produccion = ConfiguracionBD.obtenerNombreBD(2) + ".dbo.";

        //Se definen elementos para enviar correos
        context = this;

        rec = new String[] {
                "auxiliar3.TI@corsan.com.co",
                "isabel.gomez@corsan.com.co",
                "auditoria@corsan.com.co"
        };

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar (enter) en el EditText inicie el proceso
        codigoTrefi.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (incompleta){
                    codigoTrefi.setText("");
                    AudioError();
                    toastAtencion("No se pueden leer más tiquetes");
                }else{
                    if(yaentre == 0){
                        if(codigoTrefi.getText().toString().equals("")){
                            AudioError();
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
        btnTransaTrefi.setOnClickListener(v -> {
            int sleer = Integer.parseInt(txtTotalSinLeer.getText().toString());
            int total = Integer.parseInt(txtTotal.getText().toString());
            leidos = (total - sleer);
            //Verificamos que la cantidad de rollos sin leer sea 0 y si hubiera produccion en
            //galvanizado que leer
            if(sleer==0 && total>0){
                //Mostramos el mensaje para logistica
                alertDialogTransaccion();
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
                        alertDialogTransaccion();
                    }
                }
            }
        });

        ingresarCedulas();
    }

    private void ingresarCedulas() {
        conexion = new Conexion();
        AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoTrefilacion.this);
        View mView = getLayoutInflater().inflate(R.layout.alertdialog_cedularecepciona,null);
        final EditText txtCedulaProduccion = mView.findViewById(R.id.txtCedulaLogistica);
        txtCedulaProduccion.setHint("Cedula Producción");
        TextView txtMrollos = mView.findViewById(R.id.txtMrollos);
        txtMrollos.setVisibility(View.GONE);
        TextView txtMensajeCedula = mView.findViewById(R.id.textView6);
        txtMensajeCedula.setText(R.string.ingresarCeduProd);
        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
        Button btnCancelar = mView.findViewById(R.id.btnCancelar);
        ProgressBar Barraprogreso = mView.findViewById(R.id.progress_bar);
        builder.setView(mView);
        AlertDialog alertDialog = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            if (isNetworkAvailable()) {
                String CeProdu = txtCedulaProduccion.getText().toString().trim();
                if (CeProdu.equals("")){
                    AudioError();
                    toastError("Ingresar la cedula de la persona que entrega");
                }else{
                    // Verificamos el numero de documentos de la persona en la base de datos
                    personaProduccion = conexion.obtenerPermisoPersonaAlambre(RecepcionTerminadoTrefilacion.this, CeProdu, "entrega");
                    permiso = personaProduccion.getPermiso();

                    // Verificamos que la persona sea de logistica
                    if (permiso.equals("E")) {
                        Barraprogreso.setVisibility(View.VISIBLE);
                        Handler handler = new Handler(Looper.getMainLooper());
                        new Thread(() -> {
                            try {
                                runOnUiThread(() -> {
                                    // Código a ejecutar cuando el permiso es "E"
                                    nit_usuario = CeProdu;

                                    // Llamamos al metodo para consultar los rollos de galvanizados listos para recoger
                                    consultarTransIncompleta();

                                    Barraprogreso.setVisibility(View.GONE);
                                    alertDialog.dismiss();

                                    // Se establece el foco en el edit text
                                    codigoTrefi.requestFocus();

                                    closeTecladoMovil();
                                });
                            } catch (Exception e) {
                                handler.post(() -> {
                                    AudioError();
                                    toastError(e.getMessage());
                                    Barraprogreso.setVisibility(View.GONE);
                                });
                            }
                        }).start();
                        closeTecladoMovil();
                    }else{
                        if (permiso == null){
                            txtCedulaProduccion.setText("");
                            AudioError();
                            toastError("Persona no encontrada");
                        }else{
                            txtCedulaProduccion.setText("");
                            AudioError();
                            toastError("La cedula ingresada no pertenece a producción!");
                        }
                    }
                }
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });
        btnCancelar.setOnClickListener(v -> {
            alertDialog.dismiss();
            finish();
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    //Alert dialog Transacción
    @SuppressLint("SetTextI18n")
    private void alertDialogTransaccion(){
        AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoTrefilacion.this);
        View mView = getLayoutInflater().inflate(R.layout.alertdialog_cedularecepciona,null);
        final EditText txtCedulaLogistica = mView.findViewById(R.id.txtCedulaLogistica);
        TextView txtMrollos = mView.findViewById(R.id.txtMrollos);
        txtMrollos.setText("Se han leido: "+ leidos +" Rollos");
        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
        Button btnCancelar = mView.findViewById(R.id.btnCancelar);
        ProgressBar Barraprogreso = mView.findViewById(R.id.progress_bar);
        builder.setView(mView);
        AlertDialog alertDialog = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            if(isNetworkAvailable()){
                CeLog = txtCedulaLogistica.getText().toString().trim();
                if (CeLog.equals("")){
                    toastError("Ingresar la cedula de la persona que recepciona");
                }else{
                    if(CeLog.equals(nit_usuario)){
                        toastError("La Cedula de la persona que recepciona no puede ser igual al de la persona que entrega");
                    }else{
                        //Verificamos el numero de documentos de la persona en la base da datos
                        personaLogistica = conexion.obtenerPersona(RecepcionTerminadoTrefilacion.this,CeLog );
                        centro = personaLogistica.getCentro();
                        //Verificamos que la persona sea de logistica
                        if (centro.equals("3500")){
                            Barraprogreso.setVisibility(View.VISIBLE);
                            Handler handler = new Handler(Looper.getMainLooper());
                            new Thread(() -> {
                                try {
                                    runOnUiThread(this::realizarTransaccion);
                                    handler.post(() -> {
                                        Barraprogreso.setVisibility(View.GONE);
                                        alertDialog.dismiss();
                                        closeTecladoMovil();
                                    });
                                } catch (Exception e) {
                                    handler.post(() -> {
                                        AudioError();
                                        toastError(e.getMessage());
                                        Barraprogreso.setVisibility(View.GONE);
                                    });
                                }
                            }).start();
                            closeTecladoMovil();
                        }else{
                            if (centro.equals("")){
                                txtCedulaLogistica.setText("");
                                AudioError();
                                toastError("Persona no encontrada");
                            }else{
                                txtCedulaLogistica.setText("");
                                AudioError();
                                toastError("La cedula ingresada no pertenece a logistica!");
                            }
                        }
                    }
                }
            }else{
                toastError("Problemas de conexión a Internet");
            }
        });
        btnCancelar.setOnClickListener(v1 -> alertDialog.dismiss());
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Funcion que genera todas las listas de consultas en la base de datos, las ejecuta generando
    //Una TRB1 en el sistema de bodega 2 a bodega 3 con los rollos leidos
    @SuppressLint("SetTextI18n")
    private void realizarTransaccion() {
        StringBuilder stringConstructor = new StringBuilder();
        stringConstructor.append("AND ((R.cod_orden=" + ListaTrefiRollosRecep.get(0).getCod_orden() + " and R.id_detalle=" + ListaTrefiRollosRecep.get(0).getId_detalle() + " and R.id_rollo=" + ListaTrefiRollosRecep.get(0).getId_rollo() + ")");
        if (ListaTrefiRollosRecep.size() > 1) {
            for (int j = 1; j < ListaTrefiRollosRecep.size(); j++) {
                stringConstructor.append(" OR (R.cod_orden_rec=" + ListaTrefiRollosRecep.get(j).getCod_orden() + " and R.id_detalle_rec=" + ListaTrefiRollosRecep.get(j).getId_detalle() + " and R.id_rollo_rec= " + ListaTrefiRollosRecep.get(j).getId_rollo() + ")");
            }
        }
        stringConstructor.append(")");
        String complementoSql = stringConstructor.toString();

        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        listTransactionTrefi = new ArrayList<>();

        // Obtén la fecha y hora actual
        Date fechaActual = new Date();
        Calendar calendar = Calendar.getInstance();

        // Define el formato de la fecha y hora que deseas obtener
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoFechaTransaccion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoMonth = new SimpleDateFormat("MM");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoYear = new SimpleDateFormat("yyyy");

        // Convierte la fecha actual en un String con el formato definido
        String fechaActualString = formatoFecha.format(fechaActual);
        fechaTransaccion = formatoFechaTransaccion.format(fechaActual);
        String monthActualString = formatoMonth.format(fechaActual);
        String yearActualString = formatoYear.format(fechaActual);

        ListarefeRecepcionados = conexion.trefiRefeRecepcionados(RecepcionTerminadoTrefilacion.this, monthActualString, yearActualString, complementoSql);

        //se adicionan los campos recepcionado, nit_recepcionado y fecha_recepcionado a la tabla
        for (int i = 0; i < ListaTrefiRollosRecep.size(); i++) {
            String cod_orden = ListaTrefiRollosRecep.get(i).getCod_orden();
            String id_detalle = ListaTrefiRollosRecep.get(i).getId_detalle();
            String id_rollo = ListaTrefiRollosRecep.get(i).getId_rollo();

            String sql_rollo = "UPDATE " + db_produccion + "J_rollos_tref SET recepcionado='SI', nit_recepcionado='" + nit_usuario + "', fecha_recepcion='" + fechaActualString + "', nit_entrega='" + personaLogistica.getNit() + "' WHERE cod_orden='" + cod_orden + "' AND id_detalle='" + id_detalle + "' AND id_rollo='" + id_rollo + "'";

            try {
                //Se añade el sql a la lista
                listTransactionTrefi.add(sql_rollo);
            } catch (Exception e) {
                Toast.makeText(RecepcionTerminadoTrefilacion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        numero_transaccion = Integer.valueOf(Obj_ordenprodLn.mover_consecutivo("TRB1", RecepcionTerminadoTrefilacion.this));
        listTransactionTrefi.addAll(traslado_bodega(ListarefeRecepcionados, calendar));

        for (int u = 0; u < ListaTrefiRollosRecep.size(); u++) {
            String cod_orden = ListaTrefiRollosRecep.get(u).getCod_orden();
            String id_detalle = ListaTrefiRollosRecep.get(u).getId_detalle();
            String id_rollo = ListaTrefiRollosRecep.get(u).getId_rollo();
            String sql_trb1 = "UPDATE " + db_produccion + "J_rollos_tref SET trb1=" + numero_transaccion + " WHERE cod_orden='" + cod_orden + "' AND id_detalle='" + id_detalle + "' AND id_rollo='" + id_rollo + "'";
            try {
                //Se añade el sql a la lista
                listTransactionTrefi.add(sql_trb1);
            } catch (Exception e) {
                Toast.makeText(RecepcionTerminadoTrefilacion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        //Ejecutamos la lista de consultas para hacer la TRB1
        error = ing_prod_ad.ExecuteSqlTransaction(listTransactionTrefi, ConfiguracionBD.obtenerNombreBD(1), RecepcionTerminadoTrefilacion.this);
        if(error.equals("")){
            consultarTrefiTerminado();
            incompleta = false;
            toastAcierto("Transaccion Realizada con Exito! --" + numero_transaccion);
        }else{
            incompleta =  true;
            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoTrefilacion.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("No se pudo realizar la transacción, \n '" + error + "'\n Por favor volver a intentar realizarla");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> {
                if (isNetworkAvailable()) {
                    /////////////////////////////////////////////////////////////
                    //Correo electronico para notificar error en la transacción
                    correo = conexion.obtenerCorreo(RecepcionTerminadoTrefilacion.this);
                    String email = correo.getCorreo();
                    String pass = correo.getContrasena();
                    subject = "la transaccion #" + numero_transaccion + " de Control en Piso Trefilación no se pudo realizar";
                    textMessage = "La Transacción de recepcion de producto terminado del area de Trefilación #" + numero_transaccion + " no se pudo cancelar correctamente \n" +
                            "Detalles de la recepción: \n" +
                            "Error: '" + error + "'\n" +
                            "Numero de rollos: " + leidos + " \n" +
                            "Nit quien entrega (Producción): " + nit_usuario + " \n" +
                            "Nit quien recibe (Logistica): " + personaLogistica.getNit() + " \n" +
                            "Fecha transacción: " + fechaTransaccion + "";

                    // Resto del código para enviar el correo electrónico
                    Properties props = new Properties();
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.socketFactory.port", "465");
                    props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.auth","true");
                    props.put("mail.smtp.port", "465");

                    session = Session.getDefaultInstance(props, new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(email,pass);
                        }
                    });

                    pdialog = ProgressDialog.show(context,"","Sending Mail...", true);

                    RetreiveFeedTask task = new RetreiveFeedTask();
                    task.execute();
                    alertDialog.dismiss();
                } else {
                    toastError("Problemas de conexión a Internet");
                }
            });
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Funciones para enviar correos de error a auditoria y sistemas
    //Evidencia

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    class RetreiveFeedTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("emailservicecorsan@gmail.com"));
                InternetAddress [] toAddresses = new InternetAddress[rec.length];
                for (int i = 0; i< rec.length;i++){
                    toAddresses[i] = new InternetAddress(rec[i]);
                }
                message.setRecipients(Message.RecipientType.TO, toAddresses);
                message.setSubject(subject);
                message.setContent(textMessage, "text/html; charset=utf-8");
                Transport.send(message);
            }catch (MessagingException e) {
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            pdialog.dismiss();
            toastAcierto("Message sent");
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
        String nombre_usuario = conexion.obtenerNombrePersona(RecepcionTerminadoTrefilacion.this,CeLog);
        String usuario = CeLog;
        String notas = "MOVIL fecha: " + fecha + " usuario: " + nombre_usuario;

        listSql = objTraslado_bodLn.listaTrasladoBodegaTrefi(ListarefeRecepcionados,numero_transaccion, 2, 3, calendar, notas, usuario, "TRB1", "11",RecepcionTerminadoTrefilacion.this);
        return listSql;
    }

    @SuppressLint("SetTextI18n")
    private void consultarTransIncompleta(){
        conexion = new Conexion();
        //Inicializamos la lista de los rollos escaneados
        ListaTrefiRollosRecep = new ArrayList<>();

        //Consultamos si hay rollos con transacciones incompletas
        ListaTrefiRollosRecep = conexion.consultarTrefiIncomple(getApplication());

        if(ListaTrefiRollosRecep.isEmpty()){
            /////////////////////////////////////////////////////////////////////////////////////////////
            //Llamamos al metodo para consultar los rollos de trefilación listos para recoger
            consultarTrefiTerminado();
        }else{
            incompleta = true;
            btnTransaTrefi.setEnabled(false);
            btnCancelarTrans.setEnabled(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoTrefilacion.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("Hay una transaccion anterior incompleta , \n Por favor comunicarse inmediatamente con el área de sistemas, \n para poder continuar con las transacciones, de lo \n contrario no se le permitira continuar");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
            alertDialog.setCancelable(false);
            alertDialog.show();

            //Consultamos los rollos de producción que no se han recepcionado en la base de datos
            ListaTrefiTerminado = conexion.obtenerTrefiTerminado(getApplication());
            //Enviamos la lista vacia de rollos escaneados al listview
            TrefiTerminadoAdapter = new listTrefiTerminadoAdapter(RecepcionTerminadoTrefilacion.this,R.layout.item_row_trefiterminado,ListaTrefiRollosRecep);
            listviewTrefiTerminado.setAdapter(TrefiTerminadoAdapter);

            //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
            String totalRollos = String.valueOf(ListaTrefiTerminado.size() + ListaTrefiRollosRecep.size());
            txtTotal.setText(totalRollos);

            //Contamos los rollos leidos y sin leer para mostrarlos en los TextView
            contarSinLeer();
            contarLeidos();
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
        ListaTrefiTerminado = conexion.obtenerTrefiTerminado(getApplication());
        //Enviamos la lista vacia de rollos escaneados al listview
        TrefiTerminadoAdapter = new listTrefiTerminadoAdapter(RecepcionTerminadoTrefilacion.this,R.layout.item_row_trefiterminado,ListaTrefiRollosRecep);
        listviewTrefiTerminado.setAdapter(TrefiTerminadoAdapter);

        //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
        String totalRollos = String.valueOf(ListaTrefiTerminado.size());
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
    @SuppressLint("SetTextI18n")
    private void codigoIngresado() throws SQLException {
        consecutivo = codigoTrefi.getText().toString().trim();
        boolean encontrado = false;
        int position = 0;
        if (isNetworkAvailable()) {
            for (int i=0;i<ListaTrefiTerminado.size();i++){
                String codigoList = ListaTrefiTerminado.get(i).getCod_orden()+"-"+ListaTrefiTerminado.get(i).getId_detalle()+"-"+ListaTrefiTerminado.get(i).getId_rollo();
                if(consecutivo.equals(codigoList)){
                    encontrado = true;
                    position = i;
                    break;
                }
            }
            //Si el rollos es encontrado o no se muestra mensaje
            if (encontrado){
                //Si el rollo encontrado esta pintado de verde ya fue leido anteriormente
                if(ListaTrefiTerminado.get(position).getColor().equals("GREEN")){
                    toastError("Rollo Ya leido");
                    AudioError();
                    cargarNuevo();
                }else{
                    //Copiamos el rollo encontrado de la lista de producción
                    trefiRecepcionModelo = ListaTrefiTerminado.get(position);
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
                RolloTrefiTransa TransRollo;
                cod_orden = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", consecutivo);
                id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_detalle", consecutivo);
                id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", consecutivo);
                TransRollo = conexion.obtenerRolloTransTrefi(RecepcionTerminadoTrefilacion.this,cod_orden,id_detalle,id_rollo);
                if (TransRollo.getCod_orden() != null && !TransRollo.getCod_orden().equals("")){
                    if (TransRollo.getEstado() == null){
                        AudioError();
                        cargarNuevo();
                        AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoTrefilacion.this);
                        View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                        TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                        alertMensaje.setText("Rollo NO revisado por calidad \n ¡Este rollo no puede ser entregado a Logistica!");
                        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                        btnAceptar.setText("Aceptar");
                        builder.setView(mView);
                        AlertDialog alertDialog = builder.create();
                        btnAceptar.setOnClickListener(v -> {
                            alertDialog.dismiss();
                        });
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                    } else if (TransRollo.getEstado().equals("R")){
                        AudioError();
                        cargarNuevo();
                        AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoTrefilacion.this);
                        View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                        TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                        alertMensaje.setText("Rollo rechazado por calidad \n ¡Este rollo no puede ser entregado a Logistica!");
                        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                        btnAceptar.setText("Aceptar");
                        builder.setView(mView);
                        AlertDialog alertDialog = builder.create();
                        btnAceptar.setOnClickListener(v -> {
                            alertDialog.dismiss();
                        });
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                    } else if (TransRollo.getEstado().equals("A") && TransRollo.getTrb1() != null) {
                        AudioError();
                        cargarNuevo();
                        AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoTrefilacion.this);
                        View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                        TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                        alertMensaje.setText("Rollo ya transladado a bodega 3 \n Fecha: " + TransRollo.getFecha_recepcion() + " \n Transacción #" + TransRollo.getTrb1());
                        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                        btnAceptar.setText("Aceptar");
                        builder.setView(mView);
                        AlertDialog alertDialog = builder.create();
                        btnAceptar.setOnClickListener(v -> {
                            alertDialog.dismiss();
                        });
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                    }else{
                        toastError("Actualiza el modulo, \n para encontrar el rollo");
                        AudioError();
                        cargarNuevo();
                    }

                }else{
                    toastError("Rollo no encontrado");
                    AudioError();
                    cargarNuevo();
                }
            }
        } else {
            cargarNuevo();
            toastError("Problemas de conexión a Internet");
        }
    }

    //Se realiza para realizar transaccion rollo a rollo, pero despues se cambia de idea
    /*
    private void recepcionarBD(int p) throws SQLException {
        // Obtén la fecha y hora actual
        Date fechaActual = new Date();

        // Define el formato de la fecha y hora que deseas obtener
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Convierte la fecha actual en un String con el formato definido
        String fechaActualString = formatoFecha.format(fechaActual);

        String nro_orden = ListaTrefiTerminado.get(p).getNro_orden();
        String nro_rollo = ListaTrefiTerminado.get(p).getNro_rollo();


        String sql= "UPDATE D_rollo_galvanizado_f SET recepcionado='SI', nit_recepcionado='"+ nit_usuario +"', fecha_recepcion='"+ fechaActualString +"' WHERE nro_orden='"+ nro_orden +"' AND consecutivo_rollo='"+nro_rollo+"'";
        objOperacionesDb.ejecutarUpdateDbProduccion(sql,RecepcionTerminadoTrefilacion.this);

    }
     */

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que cuenta los rollos que faltan por leer y muestra la cantidad en el TextView
    @SuppressLint("SetTextI18n")
    private void contarSinLeer() {
        int sinLeer = 0;
        /*
        for(int x=0;x<ListaTrefiTerminado.size();x++){
            if(ListaTrefiTerminado.get(x).getColor().equals("RED")){
                sinLeer++;
            }
        }*/
        sinLeer = ListaTrefiTerminado.size() - ListaTrefiRollosRecep.size();
        txtTotalSinLeer.setText(Integer.toString(sinLeer));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que cuenta los rollos leeidos y muestra la cantidad en el TextView
    @SuppressLint("SetTextI18n")
    private void contarLeidos() {
        int Leido = 0;
        /*
        for(int x=0;x<ListaTrefiTerminado.size();x++){
            if(ListaTrefiTerminado.get(x).getColor().equals("GREEN")){
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
        ListaTrefiTerminado.get(posicion).setColor("GREEN");
        TrefiTerminadoAdapter = new listTrefiTerminadoAdapter(RecepcionTerminadoTrefilacion.this,R.layout.item_row_trefiterminado,ListaTrefiRollosRecep);
        listviewTrefiTerminado.setAdapter(TrefiTerminadoAdapter);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

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
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM,0,200);
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

}