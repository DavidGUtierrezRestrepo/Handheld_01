package com.example.handheld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import com.example.handheld.ClasesOperativas.Ing_prod_ad;
import com.example.handheld.ClasesOperativas.ObjTraslado_bodLn;
import com.example.handheld.ClasesOperativas.Obj_ordenprodLn;
import com.example.handheld.ClasesOperativas.objOperacionesDb;
import com.example.handheld.atv.holder.adapters.listPuasTerminadoAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.conexionDB.ConfiguracionBD;
import com.example.handheld.modelos.CorreoModelo;
import com.example.handheld.modelos.PersonaModelo;
import com.example.handheld.modelos.PuaRecepcionModelo;
import com.example.handheld.modelos.PuasRecepcionadoRollosModelo;

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

public class RecepcionTerminadoPuas extends AppCompatActivity implements AdapterView.OnItemClickListener {
    TextView txtTotal;
    Button btnTransaPuas, btnCancelarTrans;

    //se declaran las variables donde estaran los datos que vienen de la anterior clase
    String nit_usuario, CeLog;
    String referencia,nombre_operario;

    //Se declaran los elementos necesarios para el list view
    ListView listviewPuasTerminado;
    List<PuaRecepcionModelo> ListaPuasTerminado= new ArrayList<>();
    ListAdapter PuasTerminadoAdapter;
    PuaRecepcionModelo puasRecepcionModelo;

    //Se declara un objeto conexion
    Conexion conexion;

    //Se inicializa variables necesarias en la clase
    int leidos;
    String error, fechaTransaccion;
    Integer numero_transaccion, repeticiones, paso = 0, numero_recepcion;
    String centro = "";

    String fechaActualString, monthActualString, yearActualString;

    Calendar calendar;
    Boolean incompleta = false;
    PersonaModelo personaLogistica;
    CorreoModelo correo;
    ObjTraslado_bodLn objTraslado_bodLn = new ObjTraslado_bodLn();

    objOperacionesDb ObjOperacionesDB = new objOperacionesDb();
    Ing_prod_ad ing_prod_ad = new Ing_prod_ad();
    List<PuasRecepcionadoRollosModelo> ListarefeRecepcionados= new ArrayList<>();

    //Lista para relacionar rollos con la transacción.
    List<Object> listTransactionTrb1 = new ArrayList<>(); //Lista donde agregamos las consultas que agrearan el campo trb1
    List<Object> listTransactionPua;
    List<Object> listReanudarTransa;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepcion_terminado_puas);

        //Definimos los elementos del Layout
        txtTotal = findViewById(R.id.txtRollosLeidos);
        btnTransaPuas = findViewById(R.id.btnTransaEmp);
        btnCancelarTrans = findViewById(R.id.btnCancelarTrans);

        //Recibimos los datos desde la class PedidoInventraio
        nit_usuario = getIntent().getStringExtra("nit");
        nombre_operario = getIntent().getStringExtra("nombre");
        referencia = getIntent().getStringExtra("referencia");

        //Definimos los elementos necesarios para el list view
        listviewPuasTerminado = findViewById(R.id.listviewTrefiTerminado);
        listviewPuasTerminado.setOnItemClickListener(this); //Determinamos a que elemento va dirigido el OnItemClick
        puasRecepcionModelo = new PuaRecepcionModelo();

        //Se Define los varibles para el sonido de error
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC,1);
        sonido_de_Reproduccion = sp.load(this, R.raw.sonido_error_2,1);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Se definen elementos para enviar correos
        context = this;

        rec = new String[] {
                "auxiliar3.TI@corsan.com.co",
                "isabel.gomez@corsan.com.co",
                "auditoria@corsan.com.co"
        };

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Llamamos al metodo para consultar si hay alguna transaccion incompleta
        consultarTransIncompleta();

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar el boton se salga del programa
        btnCancelarTrans.setOnClickListener(v -> {
            Intent intent = new Intent(RecepcionTerminadoPuas.this,RecepcionTerminadoPuasReferencias.class);
            //Enviamos al siguiente Activity los datos del Listview Seleccionado
            intent.putExtra("nit", nit_usuario);
            intent.putExtra("nombre", nombre_operario);
            startActivity(intent);
        });

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar el boton inicie el proceso de transacción
        btnTransaPuas.setOnClickListener(v -> {
            alertDialogTransaccion();
        });
    }

    @SuppressLint("SetTextI18n")
    private void alertDialogTransaccion(){
        AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoPuas.this);
        View mView = getLayoutInflater().inflate(R.layout.alertdialog_cedularecepciona,null);
        final EditText txtCedulaLogistica = mView.findViewById(R.id.txtCedulaLogistica);
        TextView txtMrollos = mView.findViewById(R.id.txtMrollos);
        txtMrollos.setText("Se transladaran: "+ txtTotal.getText() +" Rollos");
        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
        Button btnCancelar = mView.findViewById(R.id.btnCancelar);
        ProgressBar Barraprogreso = mView.findViewById(R.id.progress_bar);
        builder.setView(mView);
        AlertDialog alertDialog = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            if (isNetworkAvailable()) {
                CeLog = txtCedulaLogistica.getText().toString().trim();
                if (CeLog.equals("")){
                    AudioError();
                    toastError("Ingresar la cedula de la persona que recepciona");
                }else{
                    if(CeLog.equals(nit_usuario)){
                        txtCedulaLogistica.setText("");
                        AudioError();
                        toastError("La Cedula de la persona que recepciona no puede ser igual al de la persona que entrega");
                    }else{
                        //Verificamos el numero de documentos de la persona en la base da datos
                        personaLogistica = conexion.obtenerPersona(RecepcionTerminadoPuas.this,CeLog );
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
            } else {
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
        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        listTransactionPua = new ArrayList<>();
        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        List<Object> listTransaccionBodega;

        if (paso.equals(0)) {
            // Obtén la fecha y hora actual
            Date fechaActual = new Date();
            calendar = Calendar.getInstance();

            // Define el formato de la fecha y hora que deseas obtener
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoFechaTransaccion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoMonth = new SimpleDateFormat("MM");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoYear = new SimpleDateFormat("yyyy");

            // Convierte la fecha actual en un String con el formato definido
            fechaActualString = formatoFecha.format(fechaActual);
            fechaTransaccion = formatoFechaTransaccion.format(fechaActual);
            monthActualString = formatoMonth.format(fechaActual);
            yearActualString = formatoYear.format(fechaActual);

            String sql_registroRecepcion = "INSERT INTO jd_detalle_recepcion_puas(nit_prod_entrega,nit_log_recibe,fecha_recepcion)VALUES('" + nit_usuario + "','" + personaLogistica.getNit() + "','" + fechaActualString + "')";

            try {
                //Se ejecuta el sql_revision en la base de datos
                paso = ObjOperacionesDB.ejecutarInsertJjprgproduccion(sql_registroRecepcion, RecepcionTerminadoPuas.this);
            } catch (Exception e) {
                incompleta = true;
                AudioError();
                AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoPuas.this);
                View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar, null);
                TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                alertMensaje.setText("Hubo un error en el paso 1 de la recepción. \n'" + error + "'\n ¡Vuelve a intentar realizar la transacción!");
                Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                btnAceptar.setText("Aceptar");
                builder.setView(mView);
                AlertDialog alertDialog = builder.create();
                btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        }

        if (paso.equals(1)){
            String obtenerId = "select id_recepcion from jd_detalle_recepcion_puas where fecha_recepcion='" + fechaActualString + "'";
            numero_recepcion = conexion.obtenerIdRecepcion(RecepcionTerminadoPuas.this, obtenerId );
            //se adicionan los campos recepcionado, nit_recepcionado y fecha_recepcionado a la tabla
            for(int i=0;i<ListaPuasTerminado.size();i++){
                String nro_orden = ListaPuasTerminado.get(i).getNro_orden();
                String nro_rollo = ListaPuasTerminado.get(i).getConsecutivo_rollo();

                String sql_rollo= "UPDATE D_orden_prod_puas_producto SET id_recepcion='"+ numero_recepcion +"' WHERE nro_orden='"+ nro_orden +"' AND consecutivo_rollo='"+nro_rollo+"'";

                try {
                    //Se añade el sql a la lista
                    listTransactionPua.add(sql_rollo);
                }catch (Exception e){
                    Toast.makeText(RecepcionTerminadoPuas.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            if (listTransactionPua.size()>0){
                //Ejecutamos la consultas que llenan los campos de recepción
                repeticiones = 0;
                error = ciclo1();
                if (error.equals("")){
                    paso = 2;
                }else{
                    incompleta =  true;
                    AudioError();
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoPuas.this);
                    View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                    TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                    alertMensaje.setText("Hubo un error en el paso 2 de la transacción. \n'" + error + "'\n ¡Vuelve a intentar realizar la transacción!");
                    Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                    btnAceptar.setText("Aceptar");
                    builder.setView(mView);
                    AlertDialog alertDialog = builder.create();
                    btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }
        }
        if (paso==2){
            ListarefeRecepcionados = conexion.puasRefeRecepcionados(RecepcionTerminadoPuas.this,fechaActualString, monthActualString, yearActualString);
            numero_transaccion = Integer.valueOf(Obj_ordenprodLn.mover_consecutivo("TRB1", RecepcionTerminadoPuas.this));
            listTransaccionBodega = traslado_bodega(ListarefeRecepcionados, calendar);
            //Ejecutamos la lista de consultas para hacer la TRB1
            error = ing_prod_ad.ExecuteSqlTransaction(listTransaccionBodega, ConfiguracionBD.obtenerNombreBD(1), RecepcionTerminadoPuas.this);
            if (error.equals("")){
                paso=3;
            }else{
                incompleta =  true;
                AudioError();
                AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoPuas.this);
                View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                alertMensaje.setText("Hubo un error en el paso 3 de la transacción. \n'" + error + "'\n ¡Vuelve a intentar realizar la transacción!");
                Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                builder.setView(mView);
                AlertDialog alertDialog = builder.create();
                btnAceptar.setOnClickListener(v -> {
                    if (isNetworkAvailable()) {
                        repeticiones = 0;
                        //reanudarTransacion();
                        alertDialog.dismiss();
                    } else {
                        toastError("Problemas de conexión a Internet");
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        }

        if (paso==3){
            String sql_trb1= "UPDATE jd_detalle_recepcion_puas SET trb1="+ numero_transaccion +" WHERE id_recepcion='"+ numero_recepcion +"'";
            try {
                //Se añade el sql a la lista
                listTransactionTrb1.add(sql_trb1);
            }catch (Exception e){
                Toast.makeText(RecepcionTerminadoPuas.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            repeticiones = 0;
            ciclo3();
        }
    }

    private String ciclo1() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listTransactionPua, ConfiguracionBD.obtenerNombreBD(2), RecepcionTerminadoPuas.this);
            if(error.equals("")){
                return error;
            }else{
                ciclo1();
            }
        }else{
            return error;
        }
        return error;
    }

    @SuppressLint("SetTextI18n")
    private void reanudarTransacion(){
        if (repeticiones<=4){
            listReanudarTransa = new ArrayList<>();
            for(int i=0;i<ListaPuasTerminado.size();i++){
                String nro_orden = ListaPuasTerminado.get(i).getNro_orden();
                String nro_rollo = ListaPuasTerminado.get(i).getConsecutivo_rollo();

                String sql_rollo= "UPDATE D_orden_prod_puas_producto SET id_recepcion=null WHERE nro_orden='"+ nro_orden +"' AND consecutivo_rollo='"+nro_rollo+"'";

                try {
                    //Se añade el sql a la lista - esto es un
                    listReanudarTransa.add(sql_rollo);
                }catch (Exception e){
                    Toast.makeText(RecepcionTerminadoPuas.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            String sql_anular_recepcion = "UPDATE jd_detalle_recepcion_puas SET trb1=0 WHERE id_recepcion=" + numero_recepcion + "";

            try {
                //Se añade el sql a la lista - esto es un
                listReanudarTransa.add(sql_anular_recepcion);
            }catch (Exception e){
                Toast.makeText(RecepcionTerminadoPuas.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            error = ing_prod_ad.ExecuteSqlTransaction(listReanudarTransa,ConfiguracionBD.obtenerNombreBD(2),RecepcionTerminadoPuas.this);
            repeticiones = repeticiones + 1;
            if (error.equals("")){
                paso = 0;
                toastAcierto("Transacción Cancelada correctamente");

                incompleta = false;
                consultarPuasTerminado();
            }else{
                incompleta =  true;
                reanudarTransacion();
            }
        }else{
            incompleta =  true;
            btnTransaPuas.setEnabled(false);
            btnCancelarTrans.setEnabled(false);
            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoPuas.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("No se pudo cancelar la transacción, \n '" + error + "'\n Por favor comunicarse inmediatamente con el área de sistemas, \n para poder continuar con las transacciones, de lo \n contrario no se le permitira continuar");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> {
                // Verificar la conectividad antes de intentar enviar el correo
                if (isNetworkAvailable()) {
                    StringBuilder mensaje = new StringBuilder(); // Usamos StringBuilder para construir el mensaje

                    for (Object objeto : listReanudarTransa) {
                        // Convierte el objeto a String
                        String objetoComoString = objeto.toString();

                        // Agrega el objeto convertido al mensaje
                        mensaje.append(objetoComoString).append("\n");
                    }

                    // Muestra el mensaje
                    String mensajeFinal = mensaje.toString();
                    /////////////////////////////////////////////////////////////
                    //Correo electronico funciono la transacción
                    correo = conexion.obtenerCorreo(RecepcionTerminadoPuas.this);
                    String email = correo.getCorreo();
                    String pass = correo.getContrasena();
                    subject = "Una transacción en Control en Piso Puas no se pudo cancelar";
                    textMessage = "El paso 3 de la Transacción de recepcion de producto terminado del area de Puas no se pudo cancelar correctamente \n" +
                            "Detalles de la recepción: \n" +
                            mensajeFinal +
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

                    RecepcionTerminadoPuas.RetreiveFeedTask task = new RecepcionTerminadoPuas.RetreiveFeedTask();
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

    @SuppressLint("SetTextI18n")
    private void ciclo3() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listTransactionTrb1, ConfiguracionBD.obtenerNombreBD(2), RecepcionTerminadoPuas.this);
            if(error.equals("")){
                consultarPuasTerminado();
                incompleta = false;
                ///////////////////////////////////////////////////////////
                //Correo electronico funciono la transacción
                /*correo = conexion.obtenerCorreo(EscanerInventario.this);
                String email = correo.getCorreo();
                String pass = correo.getContrasena();
                subject = "Transacción Exitosa Control en Piso Galvanizado";
                textMessage = "La transacción #" + numero_transaccion + " de recepcion de producto terminado del area de Galvanizado se realizó correctamente \n" +
                        "Detalles de la transacción: \n" +
                        "Numero de rollos: " + leidos + " \n" +
                        "Nit quien entrega (Producción): " + nit_usuario + " \n" +
                        "Nit quien recibe (Logistica): " + personaLogistica.getNit() + " \n" +
                        "Fecha transacción: " + fechaTransaccion + "";

                // Verificar la conectividad antes de intentar enviar el correo
                if (isNetworkAvailable()) {
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
                } else {
                    toastError("Problemas de conexión a Internet");
                }*/
                //////////////////////////////////////////////////////////////////////////////////
                toastAcierto("Transaccion Realizada con Exito! -- " + numero_transaccion);
                Intent intent = new Intent(RecepcionTerminadoPuas.this,RecepcionTerminadoPuasReferencias.class);
                //Enviamos al siguiente Activity los datos del Listview Seleccionado
                intent.putExtra("nit", nit_usuario);
                intent.putExtra("nombre", nombre_operario);
                startActivity(intent);
            }else{
                incompleta =  true;
                ciclo3();
            }
        }else{
            incompleta =  true;
            btnTransaPuas.setEnabled(false);
            btnCancelarTrans.setEnabled(false);
            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoPuas.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("Hubo un problema en el paso 3 de la transacción #" + numero_transaccion + " de los " + leidos + " Rollos leidos, \n '" + error + "' \n Por favor comunicarse inmediatamente con el área de sistemas, \n para poder continuar con las transacciones, de lo \n contrario no se le permitira continuar");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> {
                // Verificar la conectividad antes de intentar enviar el correo
                if (isNetworkAvailable()) {
                    StringBuilder mensaje = new StringBuilder(); // Usamos StringBuilder para construir el mensaje

                    for (Object objeto : listTransactionTrb1) {
                        // Convierte el objeto a String
                        String objetoComoString = objeto.toString();

                        // Agrega el objeto convertido al mensaje
                        mensaje.append(objetoComoString).append("\n");
                    }

                    // Muestra el mensaje
                    String mensajeFinal = mensaje.toString();
                    correo = conexion.obtenerCorreo(RecepcionTerminadoPuas.this);
                    String email = correo.getCorreo();
                    String pass = correo.getContrasena();
                    subject = "Error en el paso 3 de la transacción Control en Piso Puas";
                    textMessage = "La transacción #" + numero_transaccion + " de recepcion de producto terminado del area de Puas se fué incompleta \n" +
                            "Detalles de la transacción: \n" +
                            mensajeFinal +
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

                    RecepcionTerminadoPuas.RetreiveFeedTask task = new RecepcionTerminadoPuas.RetreiveFeedTask();
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

    //Funcion para verificar una buena conexion de internet
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
    private List<Object> traslado_bodega(List<PuasRecepcionadoRollosModelo> ListarefeRecepcionados, Calendar calendar){
        List<Object> listSql;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
        String fecha = dateFormat.format(calendar.getTime());
        String nombre_usuario = conexion.obtenerNombrePersona(RecepcionTerminadoPuas.this,CeLog);
        String usuario = CeLog;
        String notas = "MOVIL fecha:" + fecha + " usuario: " + nombre_usuario;

        listSql = objTraslado_bodLn.listaTrasladoBodegaPuas(ListarefeRecepcionados,numero_transaccion, 2, 3, calendar, notas, usuario, "TRB1", "11",RecepcionTerminadoPuas.this, ListaPuasTerminado);
        return listSql;
    }

    @SuppressLint("SetTextI18n")
    private void consultarTransIncompleta(){
        conexion = new Conexion();
        //Inicializamos la lista de los rollos escaneados
        ListaPuasTerminado = new ArrayList<>();

        //Consultamos si hay rollos con transacciones incompletas
        ListaPuasTerminado = conexion.consultarPuasIncomple(getApplication());

        if (ListaPuasTerminado.isEmpty()){
            /////////////////////////////////////////////////////////////////////////////////////////////
            //Llamamos al metodo para consultar los rollos de puas listos para recoger 
            consultarPuasTerminado();
        }else{
            incompleta = true;
            btnTransaPuas.setEnabled(false);
            btnCancelarTrans.setEnabled(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionTerminadoPuas.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("Hay una transaccion anterior incompleta , \nPor favor comunicarse inmediatamente con el área de sistemas, \n para poder continuar con las transacciones, de lo \n contrario no se le permitira continuar");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
            alertDialog.setCancelable(false);
            alertDialog.show();

            //Enviamos la lista vacia de rollos escaneados al listview
            PuasTerminadoAdapter = new listPuasTerminadoAdapter(RecepcionTerminadoPuas.this,R.layout.item_row_galvterminado,ListaPuasTerminado);
            listviewPuasTerminado.setAdapter(PuasTerminadoAdapter);

            //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
            String totalRollos = String.valueOf(ListaPuasTerminado.size());
            txtTotal.setText(totalRollos);

        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que consulta los rollos que hay en producción que no se han recepcionado e
    //inicializa el listview
    private void consultarPuasTerminado() {
        conexion = new Conexion();
        //Inicializamos la lista de los rollos escaneados
        ListaPuasTerminado = new ArrayList<>();

        //Consultamos los rollos de producción que no se han recepcionado en la base de datos
        ListaPuasTerminado = conexion.obtenerPuasTerminado(getApplication(),nit_usuario,referencia);
        //Enviamos la lista vacia de rollos escaneados al listview
        PuasTerminadoAdapter = new listPuasTerminadoAdapter(RecepcionTerminadoPuas.this,R.layout.item_row_galvterminado,ListaPuasTerminado);
        listviewPuasTerminado.setAdapter(PuasTerminadoAdapter);

        //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
        String totalRollos = String.valueOf(ListaPuasTerminado.size());
        txtTotal.setText(totalRollos);
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