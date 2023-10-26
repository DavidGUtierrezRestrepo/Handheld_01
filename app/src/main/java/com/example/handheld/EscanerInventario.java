package com.example.handheld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.app.ProgressDialog;
import android.os.AsyncTask;
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
import com.example.handheld.atv.holder.adapters.listGalvTerminadoAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.CorreoModelo;
import com.example.handheld.modelos.GalvRecepcionModelo;
import com.example.handheld.modelos.GalvRecepcionadoRollosModelo;
import com.example.handheld.modelos.PersonaModelo;

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

public class EscanerInventario extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //se declaran las variables de los elementos del Layout
    EditText codigoGalva;
    TextView txtTotal, txtTotalSinLeer, txtRollosLeidos;
    Button btnTransaGalv, btnCancelarTrans;

    //se declaran las variables donde estaran los datos que vienen de la anterior clase
    String nit_usuario;

    //Se declaran los elementos necesarios para el list view
    ListView listviewGalvTerminado;
    List<GalvRecepcionModelo> ListaGalvTerminado= new ArrayList<>();
    List<GalvRecepcionModelo> ListaGalvRollosRecep;
    ListAdapter GalvTerminadoAdapter;
    GalvRecepcionModelo galvRecepcionModelo;

    //Se declara un objeto conexion
    Conexion conexion;

    //Se inicializa variables necesarias en la clase
    int yaentre = 0, leidos;
    String consecutivo, error, fechaTransaccion;
    Integer numero_transaccion, repeticiones;
    String centro = "";

    Boolean incompleta = false;
    PersonaModelo personaLogistica;
    CorreoModelo correo;
    ObjTraslado_bodLn objTraslado_bodLn = new ObjTraslado_bodLn();
    Ing_prod_ad ing_prod_ad = new Ing_prod_ad();
    List<GalvRecepcionadoRollosModelo> ListarefeRecepcionados= new ArrayList<>();

    //Lista para relacionar rollos con la transacción.
    List<Object> listTransactionTrb1 = new ArrayList<>();
    List<Object> listTransactionGal;
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
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escaner_inventario);

        //Definimos los elementos del Layout
        codigoGalva = findViewById(R.id.codigoCajaRecep);
        txtTotal = findViewById(R.id.txtTotal);
        txtTotalSinLeer = findViewById(R.id.txtTotalSinLeer);
        txtRollosLeidos = findViewById(R.id.txtRollosLeidos);
        btnTransaGalv = findViewById(R.id.btnTransaEmp);
        btnCancelarTrans = findViewById(R.id.btnCancelarTrans);

        //Recibimos los datos desde la class PedidoInventraio
        nit_usuario = getIntent().getStringExtra("nit_usuario");
        //fecha_inicio = getIntent().getStringExtra("fecha_inicio"); //YA NO SE RECIBE FECHA INICIO
        //fecha_final = getIntent().getStringExtra("fecha_final"); //YA NO SE RECIBE FECHA FINAL

        //Definimos los elementos necesarios para el list view
        listviewGalvTerminado = findViewById(R.id.listviewTrefiTerminado);
        listviewGalvTerminado.setOnItemClickListener(this); //Determinamos a que elemento va dirigido el OnItemClick
        galvRecepcionModelo = new GalvRecepcionModelo();

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
        //Se establece el foco en el edit text
        codigoGalva.requestFocus();

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar (enter) en el EditText inicie el proceso
        codigoGalva.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (incompleta){
                    codigoGalva.setText("");
                    AudioError();
                    toastAtencion("No se pueden leer más tiquetes");
                }else{
                    if(yaentre == 0){
                        if(codigoGalva.getText().toString().equals("")){
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
        btnTransaGalv.setOnClickListener(v -> {
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
    }

    @SuppressLint("SetTextI18n")
    private void alertDialogTransaccion(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EscanerInventario.this);
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
            if (isNetworkAvailable()) {
                String CeLog = txtCedulaLogistica.getText().toString().trim();
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
                        personaLogistica = conexion.obtenerPersona(EscanerInventario.this,CeLog );
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
        listTransactionGal = new ArrayList<>();
        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        List<Object> listTransaccionBodega;
        //Lista donde revertimos la primer consulta si el segundo proceso no se realiza bien
        //List<Object> listTransactionError = new ArrayList<>(); se comenta porque se decide no revertir la primera consulta sino terminar las incompletas
        //Lista donde agregamos las consultas que agrearan el campo trb1


        // Obtén la fecha y hora actual
        Date fechaActual = new Date();
        Calendar calendar = Calendar.getInstance();

        // Define el formato de la fecha y hora que deseas obtener
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatoFechaTransaccion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoMonth = new SimpleDateFormat("MM");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoYear = new SimpleDateFormat("yyyy");

        // Convierte la fecha actual en un String con el formato definido
        String fechaActualString = formatoFecha.format(fechaActual);
        fechaTransaccion = formatoFechaTransaccion.format(fechaActual);
        String monthActualString = formatoMonth.format(fechaActual);
        String yearActualString = formatoYear.format(fechaActual);

        //se adicionan los campos recepcionado, nit_recepcionado y fecha_recepcionado a la tabla
        for(int i=0;i<ListaGalvRollosRecep.size();i++){
            String nro_orden = ListaGalvRollosRecep.get(i).getNro_orden();
            String nro_rollo = ListaGalvRollosRecep.get(i).getNro_rollo();

            String sql_rollo= "UPDATE D_rollo_galvanizado_f SET recepcionado='SI', nit_recepcionado='"+ nit_usuario +"', fecha_recepcion='"+ fechaActualString +"', nit_entrega='"+ personaLogistica.getNit() +"' WHERE nro_orden='"+ nro_orden +"' AND consecutivo_rollo='"+nro_rollo+"'";

            try {
                //Se añade el sql a la lista
                listTransactionGal.add(sql_rollo);
            }catch (Exception e){
                Toast.makeText(EscanerInventario.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (listTransactionGal.size()>0){
            //Ejecutamos la consultas que llenan los campos de recepción
            repeticiones = 0;
            error = ciclo1();
            if (error.equals("")){
                ListarefeRecepcionados = conexion.galvRefeRecepcionados(EscanerInventario.this,fechaActualString, monthActualString, yearActualString);
                numero_transaccion = Integer.valueOf(Obj_ordenprodLn.mover_consecutivo("TRB1", EscanerInventario.this));
                listTransaccionBodega = traslado_bodega(ListarefeRecepcionados, calendar);
                //Ejecutamos la lista de consultas para hacer la TRB1
                error = ing_prod_ad.ExecuteSqlTransaction(listTransaccionBodega, "JJVDMSCIERREAGOSTO", EscanerInventario.this);
                if (error.equals("")){
                    for(int u=0;u<ListaGalvRollosRecep.size();u++){
                        String nro_orden = ListaGalvRollosRecep.get(u).getNro_orden();
                        String nro_rollo = ListaGalvRollosRecep.get(u).getNro_rollo();
                        String sql_trb1= "UPDATE D_rollo_galvanizado_f SET trb1="+ numero_transaccion +", tipo_transacion='TRB1' WHERE nro_orden='"+ nro_orden +"' AND consecutivo_rollo='"+nro_rollo+"'";
                        try {
                            //Se añade el sql a la lista
                            listTransactionTrb1.add(sql_trb1);
                        }catch (Exception e){
                            Toast.makeText(EscanerInventario.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    repeticiones = 0;
                    ciclo3();
                }else{
                    incompleta =  true;
                    AudioError();
                    AlertDialog.Builder builder = new AlertDialog.Builder(EscanerInventario.this);
                    View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                    TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                    alertMensaje.setText("Hubo un error en el paso 2 de la transacción. \n'" + error + "'\n ¡Vuelve a intentar realizar la transacción!");
                    Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                    builder.setView(mView);
                    AlertDialog alertDialog = builder.create();
                    btnAceptar.setOnClickListener(v -> {
                        repeticiones = 0;
                        reanudarTransacion();
                        alertDialog.dismiss();
                    });
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }else{
                incompleta =  true;
                AudioError();
                AlertDialog.Builder builder = new AlertDialog.Builder(EscanerInventario.this);
                View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                alertMensaje.setText("Hubo un error en el paso 1 de la transacción. \n'" + error + "'\n ¡Vuelve a intentar realizar la transacción!");
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

    private String ciclo1() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listTransactionGal, "JJVPRGPRODUCCION", EscanerInventario.this);
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
            for(int i=0;i<ListaGalvRollosRecep.size();i++){
                String nro_orden = ListaGalvRollosRecep.get(i).getNro_orden();
                String nro_rollo = ListaGalvRollosRecep.get(i).getNro_rollo();

                String sql_rollo= "UPDATE D_rollo_galvanizado_f SET recepcionado=null, nit_recepcionado=null, fecha_recepcion=null, nit_entrega=null WHERE nro_orden='"+ nro_orden +"' AND consecutivo_rollo='"+nro_rollo+"'";

                try {
                    //Se añade el sql a la lista - esto es un
                    listReanudarTransa.add(sql_rollo);
                }catch (Exception e){
                    Toast.makeText(EscanerInventario.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            error = ing_prod_ad.ExecuteSqlTransaction(listReanudarTransa,"JJVPRGPRODUCCION",EscanerInventario.this);
            repeticiones = repeticiones + 1;
            if (error.equals("")){
                toastAcierto("Transacción Cancelada correctamente");

                incompleta = false;
                consultarGalvTerminado();
            }else{
                incompleta =  true;
                reanudarTransacion();
            }
        }else{
            incompleta =  true;
            btnTransaGalv.setEnabled(false);
            btnCancelarTrans.setEnabled(false);
            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(EscanerInventario.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("No se pudo cancelar el paso 1 de la transacción, \n '" + error + "'\n Por favor comunicarse inmediatamente con el área de sistemas, \n para poder continuar con las transacciones, de lo \n contrario no se le permitira continuar");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
            alertDialog.setCancelable(false);
            alertDialog.show();

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
            correo = conexion.obtenerCorreo(EscanerInventario.this);
            String email = correo.getCorreo();
            String pass = correo.getContrasena();
            subject = "El paso 1 de una transacción en Control en Piso Galvanizado no se pudo cancelar";
            textMessage = "El paso 1 de la Transacción de recepcion de producto terminado del area de Galvanizado no se pudo cancelar correctamente \n" +
                    "Detalles de la recepción: \n" +
                    mensajeFinal +
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
            }
        }
        }

    @SuppressLint("SetTextI18n")
    private void ciclo3() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listTransactionTrb1, "JJVPRGPRODUCCION", EscanerInventario.this);
            if(error.equals("")){
                consultarGalvTerminado();
                incompleta = false;
                /////////////////////////////////////////////////////////////
                //Correo electronico funciono la transacción
                correo = conexion.obtenerCorreo(EscanerInventario.this);
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
                }
                //////////////////////////////////////////////////////////////////////////////////
                toastAcierto("Transaccion Realizada con Exito! -- " + numero_transaccion);
            }else{
                incompleta =  true;
                ciclo3();
            }
        }else{
            incompleta =  true;
            btnTransaGalv.setEnabled(false);
            btnCancelarTrans.setEnabled(false);
            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(EscanerInventario.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("Hubo un problema en el paso 3 de la transacción #" + numero_transaccion + " de los " + leidos + " Rollos leidos, \n '" + error + "' \n Por favor comunicarse inmediatamente con el área de sistemas, \n para poder continuar con las transacciones, de lo \n contrario no se le permitira continuar");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> {
                StringBuilder mensaje = new StringBuilder(); // Usamos StringBuilder para construir el mensaje

                for (Object objeto : listTransactionTrb1) {
                    // Convierte el objeto a String
                    String objetoComoString = objeto.toString();

                    // Agrega el objeto convertido al mensaje
                    mensaje.append(objetoComoString).append("\n");
                }

                // Muestra el mensaje
                String mensajeFinal = mensaje.toString();
                correo = conexion.obtenerCorreo(EscanerInventario.this);
                String email = correo.getCorreo();
                String pass = correo.getContrasena();
                subject = "Error en el paso 3 de la transacción Control en Piso Galvanizado";
                textMessage = "La transacción #" + numero_transaccion + " de recepcion de producto terminado del area de Galvanizado se fué incompleta \n" +
                        "Detalles de la transacción: \n" +
                        mensajeFinal +
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
                }
                alertDialog.dismiss();
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
    private List<Object> traslado_bodega(List<GalvRecepcionadoRollosModelo> ListarefeRecepcionados, Calendar calendar){
        List<Object> listSql;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
        String fecha = dateFormat.format(calendar.getTime());
        String usuario = nit_usuario;
        String notas = "MOVIL fecha:" + fecha + " usuario:" + usuario;

        listSql = objTraslado_bodLn.listaTrasladoBodegaGalv(ListarefeRecepcionados,numero_transaccion, 17, 3, calendar, notas, usuario, "TRB1", "30",EscanerInventario.this);
        return listSql;
    }

    @SuppressLint("SetTextI18n")
    private void consultarTransIncompleta(){
        conexion = new Conexion();
        //Inicializamos la lista de los rollos escaneados
        ListaGalvRollosRecep = new ArrayList<>();

        //Consultamos si hay rollos con transacciones incompletas
        ListaGalvRollosRecep = conexion.consultarGalvIncomple(getApplication());

        if (ListaGalvRollosRecep.isEmpty()){
            /////////////////////////////////////////////////////////////////////////////////////////////
            //Llamamos al metodo para consultar los rollos de galvanizados listos para recoger
            consultarGalvTerminado();
        }else{
            incompleta = true;
            btnTransaGalv.setEnabled(false);
            btnCancelarTrans.setEnabled(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(EscanerInventario.this);
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
            ListaGalvTerminado = conexion.obtenerGalvTerminado(getApplication());

            //Enviamos la lista vacia de rollos escaneados al listview
            GalvTerminadoAdapter = new listGalvTerminadoAdapter(EscanerInventario.this,R.layout.item_row_galvterminado,ListaGalvRollosRecep);
            listviewGalvTerminado.setAdapter(GalvTerminadoAdapter);

            //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
            String totalRollos = String.valueOf(ListaGalvTerminado.size() + ListaGalvRollosRecep.size());
            txtTotal.setText(totalRollos);

            //Contamos los rollos leidos y sin leer para mostrarlos en los TextView
            contarSinLeer();
            contarLeidos();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que consulta los rollos que hay en producción que no se han recepcionado e
    //inicializa el listview
    private void consultarGalvTerminado() {
        conexion = new Conexion();
        //Inicializamos la lista de los rollos escaneados
        ListaGalvRollosRecep = new ArrayList<>();

        //Consultamos los rollos de producción que no se han recepcionado en la base de datos
        ListaGalvTerminado = conexion.obtenerGalvTerminado(getApplication());
        //Enviamos la lista vacia de rollos escaneados al listview
        GalvTerminadoAdapter = new listGalvTerminadoAdapter(EscanerInventario.this,R.layout.item_row_galvterminado,ListaGalvRollosRecep);
        listviewGalvTerminado.setAdapter(GalvTerminadoAdapter);

        //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
        String totalRollos = String.valueOf(ListaGalvTerminado.size());
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
        consecutivo = codigoGalva.getText().toString().trim();
        boolean encontrado = false;
        int position = 0;
        for (int i=0;i<ListaGalvTerminado.size();i++){
            String codigoList = ListaGalvTerminado.get(i).getNro_orden()+"-"+ListaGalvTerminado.get(i).getNro_rollo();
            if(consecutivo.equals(codigoList)){
                encontrado = true;
                position = i;
            }
        }
        //Si el rollos es encontrado o no se muestra mensaje
        if (encontrado){
            //Si el rollo encontrado esta pintado de verde ya fue leido anteriormente
            if(ListaGalvTerminado.get(position).getColor().equals("GREEN")){
                toastError("Rollo Ya leido");
                AudioError();
                cargarNuevo();
            }else{
                //Copiamos el rollo encontrado de la lista de producción
                galvRecepcionModelo = ListaGalvTerminado.get(position);
                //Agregamos la copia a la de los rollos escaneados
                ListaGalvRollosRecep.add(galvRecepcionModelo);
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

        String nro_orden = ListaGalvTerminado.get(p).getNro_orden();
        String nro_rollo = ListaGalvTerminado.get(p).getNro_rollo();


        String sql= "UPDATE D_rollo_galvanizado_f SET recepcionado='SI', nit_recepcionado='"+ nit_usuario +"', fecha_recepcion='"+ fechaActualString +"' WHERE nro_orden='"+ nro_orden +"' AND consecutivo_rollo='"+nro_rollo+"'";
        objOperacionesDb.ejecutarUpdateDbProduccion(sql,EscanerInventario.this);

    }
     */

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
        sinLeer = Integer.parseInt((String) txtTotal.getText()) - ListaGalvRollosRecep.size();
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
        Leido = ListaGalvRollosRecep.size();
        txtRollosLeidos.setText(Integer.toString(Leido));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que borra el codigo del EditText y cambia la variable "yaentre"
    private void cargarNuevo() {
        codigoGalva.setText("");
        if (yaentre == 0){
            yaentre = 1;
        }else{
            yaentre = 0;
            codigoGalva.requestFocus();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que pinta el rollo encontrado en la lista de producción y muestra en el listView la lista
    //De rollos leidos
    private void pintarRollo(int posicion) {
        ListaGalvTerminado.get(posicion).setColor("GREEN");
        GalvTerminadoAdapter = new listGalvTerminadoAdapter(EscanerInventario.this,R.layout.item_row_galvterminado,ListaGalvRollosRecep);
        listviewGalvTerminado.setAdapter(GalvTerminadoAdapter);
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

    //METODO DE TOAST PERSONALIZADO : ACTUALIZADO
    public void toastActualizado(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_actualizado, findViewById(R.id.ll_custom_toast_actualizado));
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView txtMensaje = view.findViewById(R.id.txtMsgToast);
        txtMensaje.setText(msg);

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
}