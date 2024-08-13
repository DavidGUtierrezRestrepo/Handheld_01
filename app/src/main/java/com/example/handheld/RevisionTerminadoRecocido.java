package com.example.handheld;

import static com.example.handheld.R.id.txtCedulaLogistica;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.handheld.ClasesOperativas.Gestion_alambronLn;
import com.example.handheld.ClasesOperativas.Ing_prod_ad;
import com.example.handheld.ClasesOperativas.ObjTraslado_bodLn;
import com.example.handheld.ClasesOperativas.Obj_ordenprodLn;
import com.example.handheld.ClasesOperativas.objOperacionesDb;
import com.example.handheld.atv.holder.adapters.listRecoTerminadoAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.conexionDB.ConfiguracionBD;
import com.example.handheld.modelos.CorreoModelo;
import com.example.handheld.modelos.PermisoPersonaModelo;
import com.example.handheld.modelos.RecoRecepcionModelo;
import com.example.handheld.modelos.RecoRecepcionadoRollosModelo;
import com.example.handheld.modelos.RolloRecoRevisionModelo;

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

public class RevisionTerminadoRecocido extends AppCompatActivity {

    //se declaran las variables de los elementos del Layout
    EditText codigoReco;
    TextView txtTotal, txtTotalSinLeer, txtRollosLeidos, tituloRecocido;
    Button btnAprobado, btnRechazado, btnCancelarTrans;

    //se declaran las variables donde estaran los datos que vienen de la anterior clase
    String nit_usuario, tipo;

    //Se declaran los elementos necesarios para el list view
    ListView listviewRecoTerminado;
    List<RecoRecepcionModelo> ListaRecoRevisado, ListaRecoRollosRecep;
    List<Object> listRevisionReco, listTransaccionBodega, listTransactionTrb1;
    ListAdapter RecoTerminadoAdapter;
    RecoRecepcionModelo recoRecepcionModelo;

    //Se declara un objeto conexion
    Conexion conexion;

    //Se inicializa variables necesarias en la clase
    String consecutivo, motivo, traccion1, diametro1,traccion2,diametro2,traccion3,diametro3,pesoReal,pesoRequerido,apariencia, permiso = "", error, fechaActualString, monthActualString, yearActualString, CeLog;
    Integer numero_transaccion, numero_revision, repeticiones, paso = 0, yaentre = 0, id_revision;

    Calendar calendar;

    Boolean incompleta = false;
    PermisoPersonaModelo personaCalidad;
    CorreoModelo correo;

    RolloRecoRevisionModelo revisionRollo;

    Gestion_alambronLn obj_gestion_alambronLn = new Gestion_alambronLn();
    ObjTraslado_bodLn objTraslado_bodLn = new ObjTraslado_bodLn();
    objOperacionesDb objOperacionesDb = new objOperacionesDb();
    Ing_prod_ad ing_prod_ad = new Ing_prod_ad();
    List<RecoRecepcionadoRollosModelo> ListarefeRecepcionados= new ArrayList<>();

    //Se inicializa los varibles para el sonido de error
    SoundPool sp;
    int sonido_de_Reproduccion, leidos;

    //Se inicializa una instancia para hacer vibrar el celular
    Vibrator vibrator;

    //Se inicializan las listas para el comboBox
    ArrayList<String> listaRechazos, listaRecoRechazos;

    private AlertDialog alertDialogRevision, alertDialogTransaccion;

    //Se inicializan elementos para enviar correos
    Session session = null;
    ProgressDialog pdialog = null;
    Context context = null;

    String[] rec;
    String subject, textMessage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revision_terminado_recocido);

        //Definimos los elementos del Layout
        codigoReco = findViewById(R.id.codigoCajaRecep);
        txtTotal = findViewById(R.id.txtTotal);
        txtTotalSinLeer = findViewById(R.id.txtTotalSinLeer);
        txtRollosLeidos = findViewById(R.id.txtRollosLeidos);
        btnAprobado = findViewById(R.id.btnAprobado);
        btnRechazado = findViewById(R.id.btnRechazado);
        btnCancelarTrans = findViewById(R.id.btnCancelarTrans);
        tituloRecocido = findViewById(R.id.tituloRecocido);

        //Recibimos los datos desde la class PedidoInventraio
        nit_usuario = getIntent().getStringExtra("nit_usuario");
        tipo = getIntent().getStringExtra("tipo");

        if (tipo.equals("industrial")){
            tituloRecocido.setText(R.string.TMenuRecepRecoIndu);
        } else if (tipo.equals("construccion")) {
            tituloRecocido.setText(R.string.TMenuRecepRecoCons);
        }

        //Definimos los elementos necesarios para el list view
        listviewRecoTerminado = findViewById(R.id.listviewRecoTerminado);
        recoRecepcionModelo = new RecoRecepcionModelo();

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
        codigoReco.requestFocus();

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar (enter) en el EditText inicie el proceso
        codigoReco.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (incompleta){
                    codigoReco.setText("");
                    toastAtencion("No se pueden leer más tiquetes");
                }else{
                    if(yaentre == 0){
                        if(codigoReco.getText().toString().equals("")){
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
        AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
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
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editTraccion1 = mView.findViewById(R.id.editTraccion1);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editDiametro1 = mView.findViewById(R.id.editDiametro1);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editTraccion2 = mView.findViewById(R.id.editTraccion2);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editDiametro2 = mView.findViewById(R.id.editDiametro2);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editTraccion3 = mView.findViewById(R.id.editTraccion3);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText editDiametro3 = mView.findViewById(R.id.editDiametro3);
        builder.setView(mView);
        alertDialogRevision = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            if (isNetworkAvailable()) {
                String CeLog = txtCedulaCalidad.getText().toString().trim();
                if (editTraccion1.getText().toString().equals("") || editTraccion2.getText().toString().equals("") || editTraccion3.getText().toString().equals("")){
                    AudioError();
                    toastError("Por favor ingresar Tracciones");
                }else{
                    if(editDiametro1.getText().toString().equals("") || editDiametro2.getText().toString().equals("") || editDiametro3.getText().toString().equals("")){
                        AudioError();
                        toastError("Por favor ingresar Diametros");
                    }else{
                        if (CeLog.equals("")){
                            AudioError();
                            toastError("Ingresar la cedula de la persona que inspecciona");
                        }else{
                            //Verificamos el numero de documentos de la persona en la base da datos
                            personaCalidad = conexion.obtenerPermisoPersona(RevisionTerminadoRecocido.this,CeLog,"mod_revision_calidad_recocido" );
                            permiso = personaCalidad.getNit();
                            //Verificamos que la persona sea de calidad
                            if (!permiso.equals("")){
                                traccion1 = editTraccion1.getText().toString();
                                diametro1 = editDiametro1.getText().toString();
                                traccion2 = editTraccion2.getText().toString();
                                diametro2 = editDiametro2.getText().toString();
                                traccion3 = editTraccion3.getText().toString();
                                diametro3 = editDiametro3.getText().toString();
                                Barraprogreso.setVisibility(View.VISIBLE);
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
                                            alertDialogRevision.dismiss();
                                            closeTecladoMovil();
                                        });
                                    } catch (Exception e) {
                                        handler.post(() -> {
                                            Barraprogreso.setVisibility(View.GONE);
                                            alertDialogRevision.dismiss();
                                            toastError(e.getMessage());
                                        });
                                    }
                                }).start();
                                closeTecladoMovil();
                            }else{
                                txtCedulaCalidad.setText("");
                                AudioError();
                                toastError("La cedula ingresada no tiene permiso para hacer revisiones de calidad!");
                            }
                        }
                    }
                }
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });
        btnCancelar.setOnClickListener(v1 -> alertDialogRevision.dismiss());
        alertDialogRevision.setCancelable(false);
        alertDialogRevision.show();
    }

    @SuppressLint("SetTextI18n")
    private void realizarRevision() throws SQLException {
        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        listRevisionReco = new ArrayList<>();

        if (paso==0){
            // Obtén la fecha y hora actual
            Date fechaActual = new Date();
            //Calendar calendar = Calendar.getInstance();

            // Define el formato de la fecha y hora que deseas obtener
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Convierte la fecha actual en un String con el formato definido
            fechaActualString = formatoFecha.format(fechaActual);

            String sql_revision= "INSERT INTO jd_revision_calidad_recocido(fecha_hora,revisor,estado,traccion_1,diametro_1,traccion_2,diametro_2,traccion_3,diametro_3)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','A','" + traccion1 + "','" + diametro1 + "','" + traccion2 + "','" + diametro2 + "','" + traccion3 + "','" + diametro3 + "')";

            try {
                //Se ejecuta el sql_revision en la base de datos
                paso = objOperacionesDb.ejecutarInsertJjprgproduccion(sql_revision,RevisionTerminadoRecocido.this);
            }catch (Exception e){
                Toast.makeText(RevisionTerminadoRecocido.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (paso==1){
            //se adicionan los campos recepcionado, nit_recepcionado y fecha_recepcionado a la tabla.
            String obtenerId = "select id_revision from jd_revision_calidad_recocido where fecha_hora='" + fechaActualString + "'";
            numero_revision = conexion.obtenerIdRevision(RevisionTerminadoRecocido.this, obtenerId );
            String sql_rollo;
            if (tipo.equals("construccion")){
                String cod_orden = ListaRecoRollosRecep.get(0).getCod_orden();
                String id_detalle = ListaRecoRollosRecep.get(0).getId_detalle();

                sql_rollo = "UPDATE JB_rollos_rec SET id_revision="+ numero_revision +" WHERE cod_orden_rec='"+ cod_orden +"' and id_detalle_rec='" + id_detalle + "' and id_revision is null";

                try {
                    //Se añade el sql a la lista
                    listRevisionReco.add(sql_rollo);
                }catch (Exception e){
                    Toast.makeText(RevisionTerminadoRecocido.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }else{
                for(int i=0;i<ListaRecoRollosRecep.size();i++){
                    String cod_orden = ListaRecoRollosRecep.get(i).getCod_orden();
                    String id_detalle = ListaRecoRollosRecep.get(i).getId_detalle();
                    String id_rollo = ListaRecoRollosRecep.get(i).getId_rollo();

                    sql_rollo = "UPDATE JB_rollos_rec SET id_revision="+ numero_revision +" WHERE cod_orden_rec='"+ cod_orden +"' AND id_detalle_rec='"+id_detalle+"' AND id_rollo_rec='"+id_rollo+"'";

                    try {
                        //Se añade el sql a la lista
                        listRevisionReco.add(sql_rollo);
                    }catch (Exception e){
                        Toast.makeText(RevisionTerminadoRecocido.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if (listRevisionReco.size()>0){
                //Ejecutamos la consultas que llenan los campos de recepción
                repeticiones = 0;
                error = revision();
                if (error.equals("")){
                    consultarRecoTerminado();
                    paso=0;
                    toastAcierto("Revision Realizada con Exito! - " + numero_revision);
                }else{
                    paso=1;
                    btnRechazado.setEnabled(false);
                    incompleta =  true;
                    AudioError();
                    AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
                    View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                    TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                    alertMensaje.setText("Error al realizar la revision en el paso 2! \n'" + error + "'\n ¡Vuelve a intentar realizar la revisión!");
                    Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                    btnAceptar.setText("Aceptar");
                    builder.setView(mView);
                    AlertDialog alertDialog = builder.create();
                    btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }
        }else{
            btnRechazado.setEnabled(false);
            incompleta =  true;
            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("Error al realizar la revision en el paso 1! \n'" + error + "'\n ¡Vuelve a intentar realizar la revisión!");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    @SuppressLint("SetTextI18n")
    private String revision() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listRevisionReco, ConfiguracionBD.obtenerNombreBD(2), RevisionTerminadoRecocido.this);
            if(error.equals("")){
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
        listaRechazos.add("Fuera de Medida");
        listaRechazos.add("Baja/Alta tracción");
        listaRechazos.add("Piel de naranja");
        listaRechazos.add("Daño por montacarga");
        listaRechazos.add("Rayado");
        listaRechazos.add("Tallado");
        listaRechazos.add("Oxidación");
        listaRechazos.add("Mal conformado");
        listaRechazos.add("Peso");
        listaRechazos.add("Soldadura");
        listaRechazos.add("Apariencia");
        listaRechazos.add("Excedente de producción");

        return listaRechazos;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Funcion que genera todas las listas de consultas en la base de datos, las ejecuta generando
    //Una TRB1 en el sistema de bodega 2 a bodega 3 con los rollos leidos
    @SuppressLint("SetTextI18n")
    private void realizarTransaccion(Integer m) {
        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        listTransaccionBodega = new ArrayList<>();
        //Lista donde revertimos la primer consulta si el segundo proceso no se realiza bien
        listRevisionReco = new ArrayList<>();
        //Lista donde agregamos las consultas que agrearan el campo trb1
        listTransactionTrb1 = new ArrayList<>();
        //varible para verificar que se registo la recepcion en base de datos

        if (paso==0){
            // Obtén la fecha y hora actual
            Date fechaActual = new Date();
            calendar = Calendar.getInstance();

            // Define el formato de la fecha y hora que deseas obtener
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoMonth = new SimpleDateFormat("MM");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoYear = new SimpleDateFormat("yyyy");

            // Convierte la fecha actual en un String con el formato definido
            fechaActualString = formatoFecha.format(fechaActual);
            monthActualString = formatoMonth.format(fechaActual);
            yearActualString = formatoYear.format(fechaActual);

            String sql_revisionTransa;

            switch (m){
                case 1:
                    sql_revisionTransa= "INSERT INTO jd_revision_calidad_recocido(fecha_hora,revisor,estado,defecto,traccion_1,tipo_transa)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','R','" + motivo + "','" + traccion1 + "','TRB1')";
                    break;
                case 2:
                    sql_revisionTransa= "INSERT INTO jd_revision_calidad_recocido(fecha_hora,revisor,estado,defecto,diametro_1,tipo_transa)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','R','" + motivo + "','" + diametro1 + "','TRB1')";
                    break;
                case 3:
                    sql_revisionTransa= "INSERT INTO jd_revision_calidad_recocido(fecha_hora,revisor,estado,defecto,peso_real,peso_requerido,tipo_transa)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','R','" + motivo + "'," + pesoReal + "," + pesoRequerido + ",'TRB1')";
                    break;
                case 4:
                    sql_revisionTransa= "INSERT INTO jd_revision_calidad_recocido(fecha_hora,revisor,estado,defecto,tipo_transa)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','R','Apariencia: " + apariencia + "','TRB1')";
                    break;
                default:
                    sql_revisionTransa= "INSERT INTO jd_revision_calidad_recocido(fecha_hora,revisor,estado,defecto,tipo_transa)VALUES('" + fechaActualString + "','" + personaCalidad.getNit() + "','R','" + motivo + "','TRB1')";
                    break;
            }

            try {
                //Se ejecuta el sql_revision en la base de datos
                paso = objOperacionesDb.ejecutarInsertJjprgproduccion(sql_revisionTransa,RevisionTerminadoRecocido.this);
            }catch (Exception e){
                Toast.makeText(RevisionTerminadoRecocido.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


        if (paso==1){
            //se adicionan los campos recepcionado, nit_recepcionado y fecha_recepcionado a la tabla
            String obtenerId;
            obtenerId = "select id_revision from jd_revision_calidad_recocido where fecha_hora='" + fechaActualString + "'";
            numero_revision = conexion.obtenerIdRevision(RevisionTerminadoRecocido.this, obtenerId );
            for(int i=0;i<ListaRecoRollosRecep.size();i++){
                String cod_orden = ListaRecoRollosRecep.get(i).getCod_orden();
                String id_detalle = ListaRecoRollosRecep.get(i).getId_detalle();
                String id_rollo = ListaRecoRollosRecep.get(i).getId_rollo();

                String sql_rollo= "UPDATE JB_rollos_rec SET id_revision="+ numero_revision +" WHERE cod_orden_rec='"+ cod_orden +"' AND id_detalle_rec='"+id_detalle+"' AND id_rollo_rec='"+id_rollo+"'";

                try {
                    //Se añade el sql a la lista
                    listRevisionReco.add(sql_rollo);
                }catch (Exception e){
                    Toast.makeText(RevisionTerminadoRecocido.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            if (listRevisionReco.size()>0){
                //Ejecutamos la consultas que llenan los campos de recepción
                repeticiones = 0;
                error = produccion1();
                if (error.equals("")){
                    ListarefeRecepcionados = conexion.recoRefeRevisados(RevisionTerminadoRecocido.this,numero_revision, monthActualString, yearActualString);
                    numero_transaccion = Integer.valueOf(Obj_ordenprodLn.mover_consecutivo("TRB1", RevisionTerminadoRecocido.this));
                    listTransaccionBodega = traslado_bodega(ListarefeRecepcionados, calendar);
                    //Ejecutamos la lista de consultas para hacer la TRB1
                    paso=2;
                }else{
                    btnAprobado.setEnabled(false);
                    incompleta =  true;
                    AudioError();
                    AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
                    View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                    TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                    alertMensaje.setText("Error al relacionar la revision en el paso 2! \n'" + error + "'\n ¡Vuelve a intentarlo de nuevo!");
                    Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                    btnAceptar.setText("Aceptar");
                    builder.setView(mView);
                    AlertDialog alertDialog = builder.create();
                    btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }
        }else{
            btnAprobado.setEnabled(false);
            incompleta =  true;
            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("Error al realizar la revision en el Paso 1! \n ¡Vuelve a intentarlo de nuevo!");
            Button btnAceptar = mView.findViewById(R.id.btnAceptar);
            btnAceptar.setText("Aceptar");
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            btnAceptar.setOnClickListener(v -> alertDialog.dismiss());
            alertDialog.setCancelable(false);
            alertDialog.show();
        }

        if(paso==2){
            repeticiones = 0;
            error = transaccion();
            if (error.equals("")){

                String sql_trb1= "UPDATE jd_revision_calidad_recocido SET num_transa="+ numero_transaccion +" WHERE id_revision='"+ numero_revision +"'";
                try {
                    //Se añade el sql a la lista
                    listTransactionTrb1.add(sql_trb1);
                }catch (Exception e){
                    Toast.makeText(RevisionTerminadoRecocido.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                //Ejecutamos la lista de consultas para relacionar la transaccion con la revision
                repeticiones = 0;
                produccion2();
            }else{
                btnAprobado.setEnabled(false);
                incompleta =  true;
                AudioError();
                AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
                View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                alertMensaje.setText("Hubo un problema al realizar la transacción de la revisión #" + numero_revision + " , \n '" + error + "'\n ¡Vuelve a intentar realizar el rechazo de nuevo!");
                Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                btnAceptar.setText("Aceptar");
                builder.setView(mView);
                AlertDialog alertDialog = builder.create();
                btnAceptar.setOnClickListener(v -> {
                    // Verificar la conectividad antes de intentar enviar el correo
                    if (isNetworkAvailable()) {
                        /////////////////////////////////////////////////////////////
                        //Correo electronico funciono la transacción
                        correo = conexion.obtenerCorreo(RevisionTerminadoRecocido.this);
                        String email = correo.getCorreo();
                        String pass = correo.getContrasena();
                        subject = "Revision de calidad #" + numero_revision + " incompleta";
                        textMessage = "La revision #" + numero_revision + " de rechazo de calidad de producto terminado del area de recocido no se completo y quedo sin transacción \n" +
                                "Detalles de la revision: \n" +
                                "Error: '" + error + "'\n" +
                                "Numero de rollos: " + leidos + " \n" +
                                "Nit inspector (Producción): " + permiso + " \n" +
                                "Fecha revisión: " + fechaActualString + "";

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

                        RevisionTerminadoRecocido.RetreiveFeedTask task = new RevisionTerminadoRecocido.RetreiveFeedTask();
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
    }

    @SuppressLint("SetTextI18n")
    private String produccion1() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listRevisionReco, ConfiguracionBD.obtenerNombreBD(2), RevisionTerminadoRecocido.this);
            if(error.equals("")){
                return error;
            }else{
                produccion1();
            }
        }else{
            return error;
        }
        return error;
    }

    @SuppressLint("SetTextI18n")
    private String transaccion() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listTransaccionBodega, ConfiguracionBD.obtenerNombreBD(1), RevisionTerminadoRecocido.this);
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

    @SuppressLint("SetTextI18n")
    private void produccion2() {
        repeticiones = repeticiones + 1;
        if(repeticiones<=5){
            error = ing_prod_ad.ExecuteSqlTransaction(listTransactionTrb1, ConfiguracionBD.obtenerNombreBD(2), RevisionTerminadoRecocido.this);
            if(error.equals("")){
                consultarRecoTerminado();
                incompleta = false;
                btnAprobado.setEnabled(true);
                btnRechazado.setEnabled(true);
                btnCancelarTrans.setEnabled(true);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("Hubo un problema en la relacion de la revision #" + numero_revision + " con la transacción #" + numero_transaccion + ", \n '" + error + "' \n Por favor comunicarse inmediatamente con el área de sistemas, \n para poder continuar con las transacciones, de lo \n contrario no se le permitira continuar");
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
                    correo = conexion.obtenerCorreo(RevisionTerminadoRecocido.this);
                    String email = correo.getCorreo();
                    String pass = correo.getContrasena();
                    subject = "Revision de calidad #" + numero_revision + " sin relación con transacción #" + numero_transaccion;
                    textMessage = "La revisión #" + numero_revision + " de calidad de producto terminado del area de recocido se fué incompleta sin la relación \n" +
                            "con la transacción # " + numero_transaccion +
                            "Detalles de la Revision: \n" +
                            mensajeFinal +
                            "Error: '" + error + "'\n" +
                            "Numero de rollos: " + leidos + " \n" +
                            "Nit inspector (Producción): " + permiso + " \n" +
                            "Fecha revisión: " + fechaActualString + "";

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

                    RevisionTerminadoRecocido.RetreiveFeedTask task = new RevisionTerminadoRecocido.RetreiveFeedTask();
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
    private List<Object> traslado_bodega(List<RecoRecepcionadoRollosModelo> ListarefeRecepcionados, Calendar calendar){
        List<Object> listSql;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
        String fecha = dateFormat.format(calendar.getTime());
        String usuario = personaCalidad.getNit();
        String notas = "MOVIL fecha:" + fecha + " usuario:" + usuario;

        listSql = objTraslado_bodLn.listaTrasladoBodegaReco(ListarefeRecepcionados,numero_transaccion, 2, 4, calendar, notas, usuario, "TRB1", "20",RevisionTerminadoRecocido.this);
        return listSql;
    }

    @SuppressLint("SetTextI18n")
    private void consultarTransIncompleta(){
        conexion = new Conexion();
        //Inicializamos la lista de los rollos escaneados
        ListaRecoRollosRecep = new ArrayList<>();
        ListaRecoRevisado = new ArrayList<>();

        //Consultamos si hay rollos con transacciones incompletas
        id_revision = conexion.consultarReviRecoIncomple(RevisionTerminadoRecocido.this, tipo);

        if (id_revision.equals(0)){
            /////////////////////////////////////////////////////////////////////////////////////////////
            //Llamamos al metodo para consultar los rollos de galvanizados listos para recoger
            consultarRecoTerminado();
        }else{
            incompleta = true;
            btnAprobado.setEnabled(false);
            btnRechazado.setEnabled(false);
            btnCancelarTrans.setEnabled(false);

            ListaRecoRollosRecep = conexion.obtenerReviRecoTerminado(RevisionTerminadoRecocido.this, id_revision);

            //Consultamos los rollos de producción que no se han recepcionado en la base de datos
            ListaRecoRevisado = conexion.obtenerRecoRevision(RevisionTerminadoRecocido.this,tipo);

            //Enviamos la lista vacia de rollos escaneados al listview
            RecoTerminadoAdapter = new listRecoTerminadoAdapter(RevisionTerminadoRecocido.this,R.layout.item_row_trefiterminado,ListaRecoRollosRecep);
            listviewRecoTerminado.setAdapter(RecoTerminadoAdapter);

            //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
            String totalRollos = String.valueOf(ListaRecoRevisado.size() + ListaRecoRollosRecep.size());
            txtTotal.setText(totalRollos);

            //Contamos los rollos leidos y sin leer para mostrarlos en los TextView
            contarSinLeer();
            contarLeidos();

            AudioError();
            AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
            View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
            TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
            alertMensaje.setText("Hay una Transacción incompleta, \n Por favor comunicarse inmediatamente con el área de sistemas, \n para poder continuar con las transacciones, de lo \n contrario no se le permitira continuar");
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
    //Metodo que consulta los rollos que hay en producción que no se han recepcionado
    //inicializa el listview
    private void consultarRecoTerminado() {
        conexion = new Conexion();
        //Inicializamos la lista de los rollos escaneados
        ListaRecoRollosRecep = new ArrayList<>();

        //Consultamos los rollos de producción que no se han recepcionado en la base de datos
        ListaRecoRevisado = conexion.obtenerRecoRevision(getApplication(), tipo);
        //Enviamos la lista vacia de rollos escaneados al listview
        RecoTerminadoAdapter = new listRecoTerminadoAdapter(RevisionTerminadoRecocido.this,R.layout.item_row_trefiterminado,ListaRecoRollosRecep);
        listviewRecoTerminado.setAdapter(RecoTerminadoAdapter);

        //Enviamos la cantidad de rollos de producción que no se han recepcionado al TextView
        String totalRollos = String.valueOf(ListaRecoRevisado.size());
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
        consecutivo = codigoReco.getText().toString().trim();
        boolean encontrado = false;
        int position = 0;
        for (int i=0;i<ListaRecoRevisado.size();i++){
            String codigoList = ListaRecoRevisado.get(i).getCod_orden()+"-"+ListaRecoRevisado.get(i).getId_detalle()+"-"+ListaRecoRevisado.get(i).getId_rollo();
            if(consecutivo.equals(codigoList)){
                encontrado = true;
                position = i;
                break;
            }
        }
        //Si el rollos es encontrado o no se muestra mensaje
        if (encontrado){
            //Si el rollo encontrado esta pintado de verde ya fue leido anteriormente
            if(ListaRecoRevisado.get(position).getColor().equals("GREEN")){
                toastError("Rollo Ya leido");
                AudioError();
                cargarNuevo();
            }else{
                if (ListaRecoRollosRecep.size() > 0){
                    if (ListaRecoRollosRecep.get(0).getCod_orden().equals(ListaRecoRevisado.get(position).getCod_orden()) && ListaRecoRollosRecep.get(0).getId_detalle().equals(ListaRecoRevisado.get(position).getId_detalle())){
                        //Copiamos el rollo encontrado de la lista de producción
                        recoRecepcionModelo = ListaRecoRevisado.get(position);
                        //Agregamos la copia a la de los rollos escaneados
                        ListaRecoRollosRecep.add(recoRecepcionModelo);
                        //Pintamos el rollo de verde en la lista de produccion para no poder volverlo a leer
                        pintarRollo(position);
                        //Contamos los rollos leidos y no leidos
                        contarSinLeer();
                        contarLeidos();
                        //Mostramos mensaje
                        toastAcierto("Rollo encontrado");
                        //Inicializamos la lectura
                        cargarNuevo();
                    }else{
                        toastError("No puede leer rollos \n de diferentes ordenes \n en una misma revision");
                        AudioError();
                        cargarNuevo();
                    }
                }else{
                    //Copiamos el rollo encontrado de la lista de producción
                    recoRecepcionModelo = ListaRecoRevisado.get(position);
                    //Agregamos la copia a la de los rollos escaneados
                    ListaRecoRollosRecep.add(recoRecepcionModelo);
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
            }
        }else{
            String cod_orden = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden",consecutivo);
            String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle",consecutivo);
            String id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo",consecutivo);

            revisionRollo = conexion.obtenerRolloRevisionReco(RevisionTerminadoRecocido.this,cod_orden,id_detalle,id_rollo);

            if (revisionRollo.getId_revision().equals("")){
                toastError("Rollo no encontrado");
                AudioError();
                cargarNuevo();
            } else if (revisionRollo.getEstado().equals("A") && revisionRollo.getId_recepcion() != null) {
                AudioError();
                cargarNuevo();
                AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
                View mView = getLayoutInflater().inflate(R.layout.alertdialog_anular,null);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                alertMensaje.setText("Rollo ya Autorizado \n Fecha: " + revisionRollo.getFecha_hora() + " \n Ya fue recepcionado por logistica");
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnAnular = mView.findViewById(R.id.btnAnular);
                btnAceptar.setText("Aceptar");
                builder.setView(mView);
                AlertDialog alertDialog = builder.create();
                btnAceptar.setOnClickListener(v -> {
                    alertDialog.dismiss();
                });
                btnAnular.setOnClickListener(v -> {
                    toastError("No se puede anular porque logistica ya recepcionó");
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            } else if (revisionRollo.getEstado().equals("A") && revisionRollo.getId_recepcion() == null) {
                AudioError();
                cargarNuevo();
                AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
                View mView = getLayoutInflater().inflate(R.layout.alertdialog_anular,null);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                alertMensaje.setText("Rollo ya Autorizado \n Fecha: " + revisionRollo.getFecha_hora());
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnAnular = mView.findViewById(R.id.btnAnular);
                btnAceptar.setText("Aceptar");
                builder.setView(mView);
                AlertDialog alertDialog = builder.create();
                btnAceptar.setOnClickListener(v -> {
                    alertDialog.dismiss();
                });
                btnAnular.setOnClickListener(v -> {
                    alertDialog.dismiss();
                    alertDialogEliminar(cod_orden,id_detalle,id_rollo);
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            } else if (revisionRollo.getEstado().equals("R")) {
                AudioError();
                cargarNuevo();
                AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
                View mView = getLayoutInflater().inflate(R.layout.alertdialog_aceptar,null);
                TextView alertMensaje = mView.findViewById(R.id.alertMensaje);
                alertMensaje.setText("Rollo ya Rechazado \n Fecha: " + revisionRollo.getFecha_hora());
                Button btnAceptar = mView.findViewById(R.id.btnAceptar);
                btnAceptar.setText("Aceptar");
                builder.setView(mView);
                AlertDialog alertDialog = builder.create();
                btnAceptar.setOnClickListener(v -> {
                    alertDialog.dismiss();
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            } else if (revisionRollo.getEstado().isEmpty()){
                toastError("Actualiza el modulo, \n para encontrar el rollo");
                AudioError();
                cargarNuevo();
            }
        }
    }

    //Alert dialog Transacción
    @SuppressLint("SetTextI18n")
    private void alertDialogEliminar(String cod_orden,String id_detalle,String id_rollo){
        AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
        View mView = getLayoutInflater().inflate(R.layout.alertdialog_eliminar,null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView txtMrechazo = mView.findViewById(R.id.txtMrechazo);
        txtMrechazo.setText("¿Desea anular la revision de calidad del rollo seleccionado?");
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView textView6 = mView.findViewById(R.id.textView6);
        textView6.setText("Ingrese la cedula persona calidad");
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText txtCedulaLogistica = mView.findViewById(R.id.txtCedulaLogistica);
        txtCedulaLogistica.setHint("Cedula Calidad");
        Button btnAceptar = mView.findViewById(R.id.btnAceptar);
        Button btnCancelar = mView.findViewById(R.id.btnCancelar);
        builder.setView(mView);
        AlertDialog alertDialog = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            if(isNetworkAvailable()){
                String CeLog = txtCedulaLogistica.getText().toString().trim();
                if (CeLog.equals("")){
                    toastError("Ingresar la cedula de la persona de calidad");
                }else{
                    //Verificamos el numero de documentos de la persona en la base da datos
                    personaCalidad = conexion.obtenerPermisoPersona(RevisionTerminadoRecocido.this,CeLog,"mod_revision_calidad_recocido" );
                    permiso = personaCalidad.getNit();
                    //Verificamos que la persona sea de calidad
                    if (!permiso.equals("")){
                        Integer ejecutar = 0;

                        String sql = "UPDATE JB_rollos_rec set id_revision=null where cod_orden_rec='" + cod_orden + "' and id_detalle_rec='" + id_detalle + "' and id_rollo_rec='" + id_rollo + "'";

                        ejecutar = Obj_ordenprodLn.realizarUpdateProduccion(sql, RevisionTerminadoRecocido.this);

                        if (ejecutar.equals(1)){
                            toastAcierto("Se anulo la revision de calidad del rollo correctamente");
                            alertDialog.dismiss();
                        }else{
                            toastError("Problemas para rechazar rollo, vuelve a intentarlo");
                            alertDialog.dismiss();
                        }
                    }else{
                        txtCedulaLogistica.setText("");
                        AudioError();
                        toastError("La cedula ingresada no tiene permiso para hacer revisiones de calidad!");
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
        sinLeer = Integer.parseInt((String) txtTotal.getText()) - ListaRecoRollosRecep.size();
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
        Leido = ListaRecoRollosRecep.size();
        txtRollosLeidos.setText(Integer.toString(Leido));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que borra el codigo del EditText y cambia la variable "yaentre"
    private void cargarNuevo() {
        codigoReco.setText("");
        if (yaentre == 0){
            yaentre = 1;
        }else{
            yaentre = 0;
            codigoReco.requestFocus();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodo que pinta el rollo encontrado en la lista de producción y muestra en el listView la lista
    //De rollos leidos
    private void pintarRollo(int posicion) {
        ListaRecoRevisado.get(posicion).setColor("GREEN");
        RecoTerminadoAdapter = new listRecoTerminadoAdapter(RevisionTerminadoRecocido.this,R.layout.item_row_trefiterminado,ListaRecoRollosRecep);
        listviewRecoTerminado.setAdapter(RecoTerminadoAdapter);
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
        leidos = (total - sleer);
        //Verificamos que la cantidad de rollos sin leer sea 0 y si hubiera produccion en
        //Recocido que leer
        if (sleer == 0 && total > 0) {
            //Mostramos el mensaje para logistica
            alertDialogTransaccion();
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
                    alertDialogTransaccion();
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void alertDialogTransaccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RevisionTerminadoRecocido.this);
        View mView = getLayoutInflater().inflate(R.layout.alertdialog_rechazado, null);
        @SuppressLint("CutPasteId") final EditText txtCedulaLogistica = mView.findViewById(R.id.txtCedulaLogistica);
        EditText editTraccion = mView.findViewById(R.id.editTraccion);
        EditText editPeso = mView.findViewById(R.id.editPeso);
        EditText editApariencia = mView.findViewById(R.id.editApariencia);
        Spinner spinnerRechazo = mView.findViewById(R.id.spinnerRechazo);
        listaRecoRechazos = llenarlistaspinner();
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(RevisionTerminadoRecocido.this, android.R.layout.simple_spinner_dropdown_item, listaRecoRechazos);
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
        builder.setView(mView);
        alertDialogTransaccion = builder.create();
        btnAceptar.setOnClickListener(v12 -> {
            if (isNetworkAvailable()) {
                if (!spinnerRechazo.getSelectedItem().equals("Seleccione motivo rechazo")) {
                    motivo = spinnerRechazo.getSelectedItem().toString();
                    CeLog = txtCedulaLogistica.getText().toString().trim();
                    switch (motivo) {
                        case "Baja/Alta tracción":
                            if(editTraccion.getText().toString().equals("")) {
                                AudioError();
                                toastError("Por favor ingresar tracción");
                            }else{
                                if (CeLog.equals("")) {
                                    AudioError();
                                    toastError("Ingresar la cedula de la persona que inspecciona");
                                }else{
                                    personaCalidad = conexion.obtenerPermisoPersona(RevisionTerminadoRecocido.this, CeLog,"mod_revision_calidad_recocido");
                                    permiso = personaCalidad.getNit();
                                    //Verificamos que la persona pertenezca al centro de logistica
                                    if (!permiso.equals("")){
                                        traccion1 = editTraccion.getText().toString();
                                        Barraprogreso.setVisibility(View.VISIBLE);
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        new Thread(() -> {
                                            try {
                                                runOnUiThread(() -> realizarTransaccion(1));
                                                handler.post(() -> {
                                                    Barraprogreso.setVisibility(View.GONE);
                                                    alertDialogTransaccion.dismiss();
                                                    closeTecladoMovil();
                                                });
                                            } catch (Exception e) {
                                                handler.post(() -> {
                                                    toastError(e.getMessage());
                                                    Barraprogreso.setVisibility(View.GONE);
                                                    alertDialogTransaccion.dismiss();
                                                });
                                            }
                                        }).start();
                                        closeTecladoMovil();
                                    }else{
                                        txtCedulaLogistica.setText("");
                                        AudioError();
                                        toastError("La cedula ingresada no tiene permiso para hacer revisiones de calidad!");
                                    }
                                }
                            }
                            break;
                        case "Fuera de Medida":
                            if(editTraccion.getText().toString().equals("")) {
                                AudioError();
                                toastError("Por favor ingresar Diametro");
                            }else{
                                if (CeLog.equals("")) {
                                    AudioError();
                                    toastError("Ingresar la cedula de la persona que inspecciona");
                                }else{
                                    personaCalidad = conexion.obtenerPermisoPersona(RevisionTerminadoRecocido.this, CeLog,"mod_revision_calidad_recocido");
                                    permiso = personaCalidad.getNit();
                                    //Verificamos que la persona pertenezca al centro de logistica
                                    if (!permiso.equals("")){
                                        diametro1 = editTraccion.getText().toString();
                                        Barraprogreso.setVisibility(View.VISIBLE);
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        new Thread(() -> {
                                            try {
                                                runOnUiThread(() -> realizarTransaccion(2));
                                                handler.post(() -> {
                                                    Barraprogreso.setVisibility(View.GONE);
                                                    alertDialogTransaccion.dismiss();
                                                    closeTecladoMovil();
                                                });
                                            } catch (Exception e) {
                                                handler.post(() -> {
                                                    toastError(e.getMessage());
                                                    Barraprogreso.setVisibility(View.GONE);
                                                    alertDialogTransaccion.dismiss();
                                                });
                                            }
                                        }).start();
                                        closeTecladoMovil();
                                    }else{
                                        txtCedulaLogistica.setText("");
                                        AudioError();
                                        toastError("La cedula ingresada no tiene permiso para hacer revisiones de calidad!");
                                    }
                                }
                            }
                            break;
                        case "Peso":
                            if(editTraccion.getText().toString().equals("")) {
                                AudioError();
                                toastError("Por favor ingresar Peso Requerido");
                            }else{
                                if (editPeso.getText().toString().equals("")){
                                    AudioError();
                                    toastError("Por favor ingresar Peso Real");
                                }else{
                                    if (CeLog.equals("")) {
                                        AudioError();
                                        toastError("Ingresar la cedula de la persona que inspecciona");
                                    }else{
                                        personaCalidad = conexion.obtenerPermisoPersona(RevisionTerminadoRecocido.this, CeLog,"mod_revision_calidad_trefilacion");
                                        permiso = personaCalidad.getNit();
                                        //Verificamos que la persona pertenezca al centro de logistica
                                        if (!permiso.equals("")){
                                            pesoRequerido = editTraccion.getText().toString();
                                            pesoReal = editPeso.getText().toString();
                                            Barraprogreso.setVisibility(View.VISIBLE);
                                            Handler handler = new Handler(Looper.getMainLooper());
                                            new Thread(() -> {
                                                try {
                                                    runOnUiThread(() -> realizarTransaccion(3));
                                                    handler.post(() -> {
                                                        Barraprogreso.setVisibility(View.GONE);
                                                        alertDialogTransaccion.dismiss();
                                                        closeTecladoMovil();
                                                    });
                                                } catch (Exception e) {
                                                    handler.post(() -> {
                                                        toastError(e.getMessage());
                                                        Barraprogreso.setVisibility(View.GONE);
                                                        alertDialogTransaccion.dismiss();
                                                    });
                                                }
                                            }).start();
                                            closeTecladoMovil();
                                        }else{
                                            txtCedulaLogistica.setText("");
                                            AudioError();
                                            toastError("La cedula ingresada no tiene permiso para hacer revisiones de calidad!");
                                        }
                                    }
                                }
                            }
                            break;
                        case "Apariencia":
                            if(editApariencia.getText().toString().equals("")) {
                                AudioError();
                                toastError("Por favor ingresar descripcion Apariencia");
                            }else{
                                if (CeLog.equals("")) {
                                    AudioError();
                                    toastError("Ingresar la cedula de la persona que inspecciona");
                                }else{
                                    personaCalidad = conexion.obtenerPermisoPersona(RevisionTerminadoRecocido.this, CeLog,"mod_revision_calidad_trefilacion");
                                    permiso = personaCalidad.getNit();
                                    //Verificamos que la persona pertenezca al centro de logistica
                                    if (!permiso.equals("")){
                                        apariencia = editApariencia.getText().toString();
                                        Barraprogreso.setVisibility(View.VISIBLE);
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        new Thread(() -> {
                                            try {
                                                runOnUiThread(() -> realizarTransaccion(4));
                                                handler.post(() -> {
                                                    Barraprogreso.setVisibility(View.GONE);
                                                    alertDialogTransaccion.dismiss();
                                                    closeTecladoMovil();
                                                });
                                            } catch (Exception e) {
                                                handler.post(() -> {
                                                    toastError(e.getMessage());
                                                    Barraprogreso.setVisibility(View.GONE);
                                                    alertDialogTransaccion.dismiss();
                                                });
                                            }
                                        }).start();
                                        closeTecladoMovil();
                                    }else{
                                        txtCedulaLogistica.setText("");
                                        AudioError();
                                        toastError("La cedula ingresada no tiene permiso para hacer revisiones de calidad!");
                                    }
                                }
                            }
                            break;
                        default:
                            if (CeLog.equals("")) {
                                AudioError();
                                toastError("Ingresar la cedula de la persona que inspecciona");
                            } else {
                                personaCalidad = conexion.obtenerPermisoPersona(RevisionTerminadoRecocido.this, CeLog,"mod_revision_calidad_recocido");
                                permiso = personaCalidad.getNit();
                                //Verificamos que la persona pertenezca al centro de logistica
                                if (!permiso.equals("")) {
                                    Barraprogreso.setVisibility(View.VISIBLE);
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    new Thread(() -> {
                                        try {
                                            runOnUiThread(() -> realizarTransaccion(5));
                                            handler.post(() -> {
                                                Barraprogreso.setVisibility(View.GONE);
                                                alertDialogTransaccion.dismiss();
                                                closeTecladoMovil();
                                            });
                                        } catch (Exception e) {
                                            handler.post(() -> {
                                                toastError(e.getMessage());
                                                Barraprogreso.setVisibility(View.GONE);
                                                alertDialogTransaccion.dismiss();
                                            });
                                        }
                                    }).start();
                                    closeTecladoMovil();
                                } else {
                                    txtCedulaLogistica.setText("");
                                    AudioError();
                                    toastError("La cedula ingresada no tiene permiso para hacer revisiones de calidad!");
                                }
                            }
                            break;
                    }
                } else {
                    AudioError();
                    toastError("Seleccione motivo rechazo");
                }
            } else {
                toastError("Problemas de conexión a Internet");
            }
        });
        btnCancelar.setOnClickListener(v1 -> alertDialogTransaccion.dismiss());
        alertDialogTransaccion.setCancelable(false);
        alertDialogTransaccion.show();
    }

    @SuppressLint("SetTextI18n")
    private void actualizarAlertDialog(String selectedOption, View mView){
        TextView txtTraccion = mView.findViewById(R.id.txtTraccion);
        EditText editTraccion = mView.findViewById(R.id.editTraccion);
        TextView txtPeso = mView.findViewById(R.id.txtPeso);
        EditText editPeso = mView.findViewById(R.id.editPeso);
        TextView txtApariencia = mView.findViewById(R.id.txtApariencia);
        EditText editApariencia = mView.findViewById(R.id.editApariencia);
        switch (selectedOption) {
            case "Fuera de Medida":
                txtTraccion.setVisibility(View.VISIBLE);
                txtTraccion.setText("Diametro:");
                editTraccion.setVisibility(View.VISIBLE);
                editTraccion.setText("");
                txtPeso.setVisibility(View.GONE);
                editPeso.setVisibility(View.GONE);
                editPeso.setText("");
                txtApariencia.setVisibility(View.GONE);
                editApariencia.setVisibility(View.GONE);
                editApariencia.setText("");
                break;
            case "Baja/Alta tracción":
                txtTraccion.setVisibility(View.VISIBLE);
                txtTraccion.setText("Tracción:");
                editTraccion.setVisibility(View.VISIBLE);
                editTraccion.setText("");
                txtPeso.setVisibility(View.GONE);
                editPeso.setVisibility(View.GONE);
                editPeso.setText("");
                txtApariencia.setVisibility(View.GONE);
                editApariencia.setVisibility(View.GONE);
                editApariencia.setText("");
                break;
            case "Peso":
                txtTraccion.setVisibility(View.VISIBLE);
                txtTraccion.setText("Requerido:");
                editTraccion.setVisibility(View.VISIBLE);
                editTraccion.setText("");
                txtPeso.setVisibility(View.VISIBLE);
                editPeso.setVisibility(View.VISIBLE);
                editPeso.setText("");
                txtApariencia.setVisibility(View.GONE);
                editApariencia.setVisibility(View.GONE);
                editApariencia.setText("");
                break;
            case "Apariencia":
                txtTraccion.setVisibility(View.GONE);
                editTraccion.setVisibility(View.GONE);
                editTraccion.setText("");
                txtPeso.setVisibility(View.GONE);
                editPeso.setVisibility(View.GONE);
                editPeso.setText("");
                txtApariencia.setVisibility(View.VISIBLE);
                editApariencia.setVisibility(View.VISIBLE);
                editApariencia.setText("");
                break;
            case "Seleccione motivo rechazo":
            case "Piel de naranja":
            case "Daño por montacarga":
            case "Rayado":
            case "Tallado":
            case "Oxidación":
            case "Mal conformado":
            case "Soldadura":
            case "Excedente de producción":
            default:
                txtTraccion.setVisibility(View.GONE);
                editTraccion.setVisibility(View.GONE);
                editTraccion.setText("");
                txtPeso.setVisibility(View.GONE);
                editPeso.setVisibility(View.GONE);
                editPeso.setText("");
                txtApariencia.setVisibility(View.GONE);
                editApariencia.setVisibility(View.GONE);
                editApariencia.setText("");
                break;
        }
    }
}