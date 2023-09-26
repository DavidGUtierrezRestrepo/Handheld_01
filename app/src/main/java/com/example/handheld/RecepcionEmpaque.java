package com.example.handheld;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld.ClasesOperativas.Ing_prod_ad;
import com.example.handheld.ClasesOperativas.ObjTraslado_bodLn;
import com.example.handheld.ClasesOperativas.Obj_ordenprodLn;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.CajasRefeModelo;
import com.example.handheld.modelos.EmpRecepcionadoCajasModelo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecepcionEmpaque extends AppCompatActivity {

    //se declaran las variables donde estaran los datos que vienen de la anterior clase
    String referencia, descripcion, mesa, nit_persona;

    //Se declara un objeto conexion
    Conexion conexion;

    //se declaran las variables de los elementos del Layout
    EditText codigoCajaRecep;
    TextView txtTRefCaja, txtCajasSisteEmpa, txtCajasFisiEmpa;
    Button btnCarton,btnPlegable,btnTransaEmp;

    Button btnCancelarTrans;

    //Se inicializa variables necesarias en la clase
    int yaentre = 0;
    List<CajasRefeModelo> ListaCajasRefe;
    List<EmpRecepcionadoCajasModelo> ListarefeRecepcionados= new ArrayList<>();

    Integer numero_transaccion;
    ObjTraslado_bodLn objTraslado_bodLn = new ObjTraslado_bodLn();
    //objOperacionesDb objOperacionesDb = new objOperacionesDb();
    Ing_prod_ad ing_prod_ad = new Ing_prod_ad();


    //Se inicializa los varibles para el sonido de error
    SoundPool sp;
    int sonido_de_Reproduccion;

    //Se inicializa una instancia para hacer vibrar el celular
    Vibrator vibrator;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepcion_empaque);

        //Definimos los elementos del Layout
        txtTRefCaja = findViewById(R.id.txtTRefCaja);
        txtCajasSisteEmpa = findViewById(R.id.txtCajasSisteEmpa);
        txtCajasFisiEmpa = findViewById(R.id.txtCajasFisiEmpa);
        codigoCajaRecep = findViewById(R.id.codigoCajaRecep);
        btnCarton = findViewById(R.id.btnCarton);
        btnPlegable = findViewById(R.id.btnPlegable);
        btnTransaEmp = findViewById(R.id.btnTransaEmp);
        btnCancelarTrans = findViewById(R.id.btnCancelarTrans);


        //Recibimos los datos desde la class PedidoInventraio
        descripcion = getIntent().getStringExtra("descripcion");
        referencia = getIntent().getStringExtra("referencia");
        mesa = getIntent().getStringExtra("mesa");
        nit_persona = getIntent().getStringExtra("nit_usuario");

        txtTRefCaja.setText(descripcion);

        //Se Define los varibles para el sonido de error
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC,1);
        sonido_de_Reproduccion = sp.load(this, R.raw.sonido_error_2,1);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        /////////////////////////////////////////////////////////////////////////////////////////
        //Se bloquean los botones que solos e activaran para acero
        botones(false);

        /////////////////////////////////////////////////////////////////////////////////////////
        //Llamamos al metodo para consultar los rollos de galvanizados listos para recoger
        consultarCajasRefe();


        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se establece el foco en el edit text
        codigoCajaRecep.requestFocus();

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar enter en el edit text haga el proceso
        codigoCajaRecep.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if(yaentre == 0){
                    if(codigoCajaRecep.getText().toString().equals("")){
                        toastError("Por favor escribir o escanear el codigo de barras");
                    }else{
                        closeTecladoMovil();
                        codigoIngresado();
                    }
                }else{
                    cargarNuevo();
                }
                return true;
            }
            return false;
        });

        btnCarton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numFisi = Integer.parseInt(txtCajasFisiEmpa.getText().toString());
                numFisi = numFisi + 50;
                int numSiste = Integer.parseInt(txtCajasSisteEmpa.getText().toString());
                if(numFisi>numSiste){
                    toastError("No es posible leer una caja más");
                }else{
                    txtCajasFisiEmpa.setText(String.valueOf(numFisi));
                    toastAcierto("Codigo Leido");
                    reiniciar();
                }
            }
        });

        btnPlegable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numFisi = Integer.parseInt(txtCajasFisiEmpa.getText().toString());
                numFisi++;
                int numSiste = Integer.parseInt(txtCajasSisteEmpa.getText().toString());
                if(numFisi>numSiste){
                    toastError("No es posible leer una caja más");
                }else{
                    txtCajasFisiEmpa.setText(String.valueOf(numFisi));
                    toastAcierto("Codigo Leido");
                    reiniciar();
                }
            }
        });

        btnCancelarTrans.setOnClickListener(this::salir);

        btnTransaEmp.setOnClickListener(v -> {
            int numFisi = Integer.parseInt(txtCajasFisiEmpa.getText().toString());
            if(numFisi>0){
                AlertDialog.Builder builder = new AlertDialog.Builder(RecepcionEmpaque.this);
                builder.setIcon(R.mipmap.ic_alert).
                        setTitle("Atención").
                        setMessage("Se han leido: "+ numFisi +" Cartones \n" +
                                "Se iniciara el translado solo con el numero de cartones leidos!").
                        setPositiveButton("Aceptar", (dialog, which) -> {
                            try {
                                realizarTransaccion();
                            }catch (Exception e){
                                toastError(e.getMessage());
                            }
                        }).setNegativeButton("Cancelar", (dialog, which) -> {

                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }else{
                toastError("No se ha leido cartones para recepcionar");
                AudioError();
            }
        });
    }

    public void reiniciar(){
        codigoCajaRecep.setEnabled(true);
        codigoCajaRecep.setText("");
        botones(false);
        codigoCajaRecep.requestFocus();
        yaentre = 0;
    }

    private void realizarTransaccion() {
        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        List<Object> listTransactionEmp = new ArrayList<>();
        //Creamos una lista para almacenar todas las consultas que se realizaran en la base de datos
        List<Object> listTransaccionBodega;

        // Obtén la fecha y hora actual
        Date fechaActual = new Date();
        Calendar calendar = Calendar.getInstance();

        // Define el formato de la fecha y hora que deseas obtener
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoMonth = new SimpleDateFormat("MM");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatoYear = new SimpleDateFormat("yyyy");

        // Convierte la fecha actual en un String con el formato definido
        String fechaActualString = formatoFecha.format(fechaActual);
        String monthActualString = formatoMonth.format(fechaActual);
        String yearActualString = formatoYear.format(fechaActual);

        int numFisi = Integer.parseInt(txtCajasFisiEmpa.getText().toString());

        //se adicionan los campos recepcionado, nit_recepcionado y fecha_recepcionado a la tabla
        for(int i=0;i<ListaCajasRefe.size();i++){
            String referencia = ListaCajasRefe.get(i).getReferencia();
            String fecha = ListaCajasRefe.get(i).getFecha();
            String mesa = ListaCajasRefe.get(i).getMesa();
            Integer cantidad = ListaCajasRefe.get(i).getCantidad();
            if((numFisi-cantidad)>=0){
                String sql_carton= "UPDATE F_Recepcion_puntilleria SET RECEPCIONADO='SI', NIT_RECEPCIONA='"+ nit_persona +"', fecha_recepcionado='"+ fechaActualString +"' WHERE MESA='"+ mesa +"' AND FECHA='"+fecha+"' AND REFERENCIA='"+ referencia +"' ";

                try {
                    //Se añade el sql a la lista
                    listTransactionEmp.add(sql_carton);
                }catch (Exception e){
                    Toast.makeText(RecepcionEmpaque.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                numFisi = numFisi - cantidad;
            }
        }

        if (listTransactionEmp.size()>0){
            if (ing_prod_ad.ExecuteSqlTransaction(listTransactionEmp, "JJVPRGPRODUCCION", RecepcionEmpaque.this).equals("")){
                ListarefeRecepcionados = conexion.empaRefeRecepcionados(RecepcionEmpaque.this,fechaActualString, monthActualString, yearActualString);
                numero_transaccion = Integer.valueOf(Obj_ordenprodLn.mover_consecutivo("EDEP", RecepcionEmpaque.this));
                listTransaccionBodega = traslado_bodega(ListarefeRecepcionados, calendar, fechaActualString);
                if (ing_prod_ad.ExecuteSqlTransaction(listTransaccionBodega, "JJVDMSCIERREAGOSTO", RecepcionEmpaque.this).equals("")){
                    toastAcierto("Transaccion Realizada con Exito! --" + numero_transaccion);
                }else{
                    toastError("Problemas, No se realizó correctamente la transacción!");
                }
            }else{
                toastError("Error al realizar la transacción!");
            }
        }
    }

    private List<Object> traslado_bodega(List<EmpRecepcionadoCajasModelo> ListarefeRecepcionados, Calendar calendar, String fechatransa){
        List<Object> listSql;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String fecha = dateFormat.format(calendar.getTime());
        String usuario = nit_persona;
        String notas = "MOVIL fecha:" + fecha + " usuario:" + usuario;

        listSql = objTraslado_bodLn.listaTrasladoBodegaEmp(ListarefeRecepcionados,numero_transaccion, 3, calendar, notas, usuario, "EDEP", "01",RecepcionEmpaque.this, fechatransa);
        return listSql;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //METODO QUE CONSULTA LAS CAJAS DE UNA REFERENCIA POR MESA Y LOS MUESTRA COMO CAJAS EN EL SISTEMA
    private void consultarCajasRefe() {
        conexion = new Conexion();

        ListaCajasRefe = conexion.obtenerCajasRefe(getApplication(),mesa,referencia);

        Integer CantT = 0;
        for (int i=0; i<ListaCajasRefe.size();i++){
            Integer cantidad = ListaCajasRefe.get(i).getCantidad();
            CantT = CantT + cantidad;
        }
        String totalCartones = String.valueOf(CantT);
        txtCajasSisteEmpa.setText(totalCartones);

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
    //METODO QUE VERIFICA QUE EL CODIGO LEIDO SEA DE LA REFERENCIA SELECCIONADA Y AGREGA UNO AL FISICO
    //DE LO CONTRARION MUESTRA UN MENSAJE DE ERRROR
    private void codigoIngresado() {
        String codigo = codigoCajaRecep.getText().toString().trim();
        if (codigo.equals(referencia)){
            String conversion = conexion.obtenerConversionReferencias(RecepcionEmpaque.this,referencia);
            int numFisi = Integer.parseInt(txtCajasFisiEmpa.getText().toString());
            int numSiste = Integer.parseInt(txtCajasSisteEmpa.getText().toString());
            if (numFisi >= numSiste){
                toastError("No se pueden leer más Codigos");
                AudioError();
                cargarNuevo();
            }else{
                if (conversion.equals("0.5")){
                    botones(true);
                    toastError("Seleccione que esta recepcionando");
                    codigoCajaRecep.setEnabled(false);
                }else{
                    numFisi++;
                    txtCajasFisiEmpa.setText(String.valueOf(numFisi));
                    toastAcierto("Codigo Leido");
                    cargarNuevo();
                }
            }
        }else{
            toastError("Codigo Referencia equivocada");
            AudioError();
            cargarNuevo();
        }
    }

    private void botones(boolean b) {
        btnCarton.setEnabled(b);
        btnPlegable.setEnabled(b);
    }

    private void cargarNuevo() {
        codigoCajaRecep.setText("");
        if (yaentre == 0){
            yaentre = 1;
        }else{
            yaentre = 0;
            codigoCajaRecep.requestFocus();
        }
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

    //METODO QUE GENERA UN SONIDO DE ERROR
    public void AudioError(){
        sp.play(sonido_de_Reproduccion,100,100,1,0,0);
        vibrator.vibrate(2000);
    }
}