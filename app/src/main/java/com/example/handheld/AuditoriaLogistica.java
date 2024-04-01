package com.example.handheld;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld.ClasesOperativas.Gestion_alambronLn;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.DatosRecepcionLogistica;
import com.example.handheld.modelos.DatosRevisionCalidad;
import com.example.handheld.modelos.RolloGalvInfor;
import com.example.handheld.modelos.RolloRecoInfor;
import com.example.handheld.modelos.RolloTrefiInfor;

import java.nio.channels.OverlappingFileLockException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AuditoriaLogistica extends AppCompatActivity {

    Spinner spinnerArea;
    EditText escanerCodigo;
    TextView descRecepId,descRecepFecha,descRecepEntrega,descRecepRecibe,descReviId,descReviFecha,descReviInspector,descReviEstado;
    int yaentre = 0;
    String consecutivo;
    Gestion_alambronLn obj_gestion_alambronLn = new Gestion_alambronLn();

    Conexion conexion;

    List<String> listaAreas;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auditoria_logistica);

        spinnerArea = findViewById(R.id.spinnerArea);
        escanerCodigo = findViewById(R.id.escanerCodigo);
        descRecepId = findViewById(R.id.descRecepId);
        descRecepFecha = findViewById(R.id.descRecepFecha);
        descRecepEntrega = findViewById(R.id.descRecepEntrega);
        descRecepRecibe = findViewById(R.id.descRecepRecibe);
        descReviId = findViewById(R.id.descReviId);
        descReviFecha = findViewById(R.id.descReviFecha);
        descReviInspector = findViewById(R.id.descReviInspector);
        descReviEstado = findViewById(R.id.descReviEstado);

        //Llenamos el spinner con las diferentes area de la empresa
        listaAreas = llenarlistaspinner();
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(AuditoriaLogistica.this, android.R.layout.simple_spinner_dropdown_item, listaAreas);
        spinnerArea.setAdapter(adapter);

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se establece el foco en el edit text
        escanerCodigo.requestFocus();

        /////////////////////////////////////////////////////////////////////////////////////////////
        //Se programa para que al presionar (enter) en el EditText inicie el proceso
        escanerCodigo.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if(yaentre == 0){
                    if(escanerCodigo.getText().toString().equals("")){
                        toastError("Por favor escribir o escanear el codigo de barras");
                    }else{
                        if(!spinnerArea.getSelectedItem().equals("Seleccione área")){
                            //Ocultamos el teclado de la pantalla
                            closeTecladoMovil();
                            //Verificamos el codigo
                            try {
                                //Verificamos el codigo
                                codigoIngresado(spinnerArea.getSelectedItem().toString());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }else{
                            toastError("Por favor seleccione un área");
                            cargarNuevo();
                            escanerCodigo.requestFocus();
                        }
                    }
                }else{
                    //Cargamos de nuevo las varibles y cambiamos "yaentre" a 1 ó 0
                    cargarNuevo();
                }
                return true;
            }
            return false;
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    //Metodo que se ejecuta al leer un tiquete con el escaner
    private void codigoIngresado(String area) throws SQLException {
        switch (area){
            case "Galvanizado":
                conexion = new Conexion();
                RolloGalvInfor rolloGalvInfor;
                int verificarCodigoGalva = 0;
                consecutivo = escanerCodigo.getText().toString().trim();
                //Se verifica que el codigo sea de galvanizado, esto se hace porque el codigo de
                //galvanizado solo cuenta con dos datos en su codigo de barras
                for (int i = 0;i<= consecutivo.length()-1;i++){
                    if (consecutivo.charAt(i) == '-'){
                        verificarCodigoGalva += 1;
                    }
                }
                if (verificarCodigoGalva == 1){
                    //Una vez verificado que el tiquete es de galvanizado traemos la información del rollo que haya en la base de datos
                    String nro_orden = obj_gestion_alambronLn.extraerDatoCodigoBarrasGalvanizado("nro_orden", consecutivo);
                    String nro_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasGalvanizado("nro_rollo", consecutivo);
                    rolloGalvInfor = conexion.obtenerInforRolloGalv(AuditoriaLogistica.this,nro_orden,nro_rollo);
                    //Verificamos que si haya traido información de la base de datos una vez busco el rollo
                    if (rolloGalvInfor.getNro_orden().equals("")){
                        toastError("¡Rollo no encontrado en la base de datos!");
                        limpiarDatos();
                    }else{
                        //Verificamos que el rollo no este anulado
                        if(rolloGalvInfor.getAnulado() != null){
                            toastError("¡Este rollo esta anulado!");
                            limpiarDatos();
                        }else{
                            //Verificamos que el rollo tenga información de recepción
                            if(rolloGalvInfor.getNum_transa() == null){
                                toastError("Este rollo no se ha recepcionado aún");
                                limpiarDatos();
                            }else{
                                ///////////////////////////////////////////////////////////////////////////
                                //Vamos a modificar el formato de la fecha de recepción a uno mas legible
                                descRecepId.setText(rolloGalvInfor.getNum_transa());
                                // Crear un objeto SimpleDateFormat para el formato original
                                SimpleDateFormat formatoOriginalGalvRecep = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault());

                                try {
                                    // Parsear la fecha original
                                    Date fechaOriginal = formatoOriginalGalvRecep.parse(rolloGalvInfor.getFecha_recepcion());

                                    // Crear un nuevo objeto SimpleDateFormat para el formato deseado
                                    SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

                                    // Formatear la fecha en el nuevo formato
                                    assert fechaOriginal != null;
                                    String fechaFormateada = formatoDeseado.format(fechaOriginal);
                                    descRecepFecha.setText(fechaFormateada);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                //Enviamos todos los datos a la pantalla
                                descRecepEntrega.setText(conexion.obtenerNombrePersona(AuditoriaLogistica.this,rolloGalvInfor.getEntrega()));
                                descRecepRecibe.setText(conexion.obtenerNombrePersona(AuditoriaLogistica.this,rolloGalvInfor.getRecibe()));
                                descReviId.setText(getString(R.string.noDisponible));
                                descReviFecha.setText(getString(R.string.noDisponible));
                                descReviInspector.setText(getString(R.string.noDisponible));
                                descReviEstado.setText(getString(R.string.noDisponible));
                                toastAcierto("Rollo encontrado");
                            }
                        }
                    }
                    cargarNuevo();
                }else{
                    toastError("Error! El tiquete no es de esta área o esta defectuoso");
                    limpiarDatos();
                    cargarNuevo();
                }
                break;
            case "Trefilación":
                conexion = new Conexion();
                RolloTrefiInfor rolloTrefiInfor;
                int verificarCodigoTrefi = 0;
                consecutivo = escanerCodigo.getText().toString().trim();
                //Se verifica que el codigo sea de galvanizado, esto se hace porque el codigo de
                //galvanizado solo cuenta con dos datos en su codigo de barras
                for (int i = 0;i<= consecutivo.length()-1;i++){
                    if (consecutivo.charAt(i) == '-'){
                        verificarCodigoTrefi += 1;
                    }
                }
                if (verificarCodigoTrefi == 2){
                    //Una vez verificado que el tiquete es de galvanizado traemos la información del rollo que haya en la base de datos
                    String cod_orden = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", consecutivo);
                    String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_detalle", consecutivo);
                    String id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", consecutivo);
                    rolloTrefiInfor = conexion.obtenerInforRolloTrefi(AuditoriaLogistica.this,cod_orden,id_detalle,id_rollo);
                    //Verificamos que si haya traido información de la base de datos una vez busco el rollo
                    if (rolloTrefiInfor.getCod_orden().equals("")){
                        toastError("¡Rollo no encontrado en la base de datos!");
                        limpiarDatos();
                    }else{
                        //Verificamos que el rollo no este anulado
                        if(rolloTrefiInfor.getAnulado() != null){
                            toastError("¡Este rollo esta anulado!");
                            limpiarDatos();
                        }else{
                            if (rolloTrefiInfor.getId_revision() == null){
                                toastError("A este rollo aún no se le ha realizado revisión de calidad");
                                limpiarDatos();
                            }else{
                                DatosRevisionCalidad datosRevisionCalidad;
                                datosRevisionCalidad = conexion.obtenerDatosRevision(AuditoriaLogistica.this,rolloTrefiInfor.getId_revision());
                                rolloTrefiInfor.setFecha_revision(datosRevisionCalidad.getFecha_revision());
                                rolloTrefiInfor.setRevisor(conexion.obtenerNombrePersona(AuditoriaLogistica.this,datosRevisionCalidad.getRevisor()));
                                rolloTrefiInfor.setEstado(datosRevisionCalidad.getEstado());
                                descReviId.setText(rolloTrefiInfor.getId_revision());
                                ///////////////////////////////////////////////////////////////////////////
                                //Vamos a modificar el formato de la fecha de recepción a uno mas legible

                                // Crear un objeto SimpleDateFormat para el formato original
                                SimpleDateFormat formatoOriginalTrefiRevi = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault());

                                try {
                                    // Parsear la fecha original
                                    Date fechaOriginal = formatoOriginalTrefiRevi.parse(rolloTrefiInfor.getFecha_revision());

                                    // Crear un nuevo objeto SimpleDateFormat para el formato deseado
                                    SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

                                    // Formatear la fecha en el nuevo formato
                                    assert fechaOriginal != null;
                                    String fechaFormateada = formatoDeseado.format(fechaOriginal);
                                    descReviFecha.setText(fechaFormateada);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                descReviInspector.setText(rolloTrefiInfor.getRevisor());
                                descReviEstado.setText(rolloTrefiInfor.getEstado());
                                //Verificamos que el rollo tenga información de recepción
                                if(rolloTrefiInfor.getNum_transa() == null){
                                    toastError("Este rollo no se ha recepcionado aún");
                                    descRecepId.setText(getString(R.string.noDisponible));
                                    descRecepFecha.setText(getString(R.string.noDisponible));
                                    descRecepEntrega.setText(getString(R.string.noDisponible));
                                    descRecepRecibe.setText(getString(R.string.noDisponible));
                                }else{
                                    ///////////////////////////////////////////////////////////////////////////
                                    //Vamos a modificar el formato de la fecha de recepción a uno mas legible
                                    descRecepId.setText(rolloTrefiInfor.getNum_transa());
                                    // Crear un objeto SimpleDateFormat para el formato original
                                    SimpleDateFormat formatoOriginalTrefiRecep = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault());

                                    try {
                                        // Parsear la fecha original
                                        Date fechaOriginal = formatoOriginalTrefiRecep.parse(rolloTrefiInfor.getFecha_recepcion());

                                        // Crear un nuevo objeto SimpleDateFormat para el formato deseado
                                        SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

                                        // Formatear la fecha en el nuevo formato
                                        assert fechaOriginal != null;
                                        String fechaFormateada = formatoDeseado.format(fechaOriginal);
                                        descRecepFecha.setText(fechaFormateada);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    //Enviamos todos los datos a la pantalla
                                    descRecepEntrega.setText(conexion.obtenerNombrePersona(AuditoriaLogistica.this,rolloTrefiInfor.getEntrega()));
                                    descRecepRecibe.setText(conexion.obtenerNombrePersona(AuditoriaLogistica.this,rolloTrefiInfor.getRecibe()));
                                    toastAcierto("Rollo encontrado");
                                }
                            }
                        }
                    }
                    cargarNuevo();
                }else{
                    toastError("Error! El tiquete no es de esta área o esta defectuoso");
                    limpiarDatos();
                    cargarNuevo();
                }
                break;
            case "Recocido Industrial":
                conexion = new Conexion();
                RolloRecoInfor rolloRecoInfor;
                int verificarCodigoReco = 0;
                consecutivo = escanerCodigo.getText().toString().trim();
                //Se verifica que el codigo sea de galvanizado, esto se hace porque el codigo de
                //galvanizado solo cuenta con dos datos en su codigo de barras
                for (int i = 0;i<= consecutivo.length()-1;i++){
                    if (consecutivo.charAt(i) == '-'){
                        verificarCodigoReco += 1;
                    }
                }
                if (verificarCodigoReco == 2){
                    //Una vez verificado que el tiquete es de galvanizado traemos la información del rollo que haya en la base de datos
                    String cod_orden = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden", consecutivo);
                    String id_detalle = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle", consecutivo);
                    String id_rollo = obj_gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo", consecutivo);
                    rolloRecoInfor = conexion.obtenerInforRolloReco(AuditoriaLogistica.this,cod_orden,id_detalle,id_rollo);
                    //Verificamos que si haya traido información de la base de datos una vez busco el rollo
                    if (rolloRecoInfor.getCod_orden().equals("")){
                        toastError("¡Rollo no encontrado en la base de datos!");
                        limpiarDatos();
                    }else{
                        //Verificamos que el rollo no este anulado
                        if(rolloRecoInfor.getAnulado() != null){
                            toastError("¡Este rollo es no conforme!");
                            limpiarDatos();
                        }else{
                            if (rolloRecoInfor.getId_revision() == null){
                                toastError("A este rollo aún no se le ha realizado revisión de calidad");
                                limpiarDatos();
                            }else{
                                DatosRevisionCalidad datosRevisionCalidad;
                                datosRevisionCalidad = conexion.obtenerDatosRevisionReco(AuditoriaLogistica.this,rolloRecoInfor.getId_revision());
                                rolloRecoInfor.setFecha_revision(datosRevisionCalidad.getFecha_revision());
                                rolloRecoInfor.setRevisor(conexion.obtenerNombrePersona(AuditoriaLogistica.this,datosRevisionCalidad.getRevisor()));
                                rolloRecoInfor.setEstado(datosRevisionCalidad.getEstado());
                                descReviId.setText(rolloRecoInfor.getId_revision());
                                ///////////////////////////////////////////////////////////////////////////
                                //Vamos a modificar el formato de la fecha de recepción a uno mas legible

                                // Crear un objeto SimpleDateFormat para el formato original
                                SimpleDateFormat formatoOriginalTrefiRevi = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault());

                                try {
                                    // Parsear la fecha original
                                    Date fechaOriginal = formatoOriginalTrefiRevi.parse(rolloRecoInfor.getFecha_revision());

                                    // Crear un nuevo objeto SimpleDateFormat para el formato deseado
                                    SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

                                    // Formatear la fecha en el nuevo formato
                                    assert fechaOriginal != null;
                                    String fechaFormateada = formatoDeseado.format(fechaOriginal);
                                    descReviFecha.setText(fechaFormateada);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                descReviInspector.setText(rolloRecoInfor.getRevisor());
                                descReviEstado.setText(rolloRecoInfor.getEstado());
                                //Verificamos que el rollo tenga información de recepción
                                if(rolloRecoInfor.getId_recepcion() == null){
                                    toastError("Este rollo no se ha recepcionado aún");
                                    descRecepId.setText(getString(R.string.noDisponible));
                                    descRecepFecha.setText(getString(R.string.noDisponible));
                                    descRecepEntrega.setText(getString(R.string.noDisponible));
                                    descRecepRecibe.setText(getString(R.string.noDisponible));
                                }else{

                                    DatosRecepcionLogistica datosRecepcionLogistica;
                                    datosRecepcionLogistica = conexion.obtenerDatosRecepReco(AuditoriaLogistica.this,rolloRecoInfor.getId_recepcion());
                                    rolloRecoInfor.setNum_transa(datosRecepcionLogistica.getNum_transa());
                                    rolloRecoInfor.setFecha_recepcion(datosRecepcionLogistica.getFecha_recepcion());
                                    rolloRecoInfor.setEntrega(datosRecepcionLogistica.getEntrega());
                                    rolloRecoInfor.setRecibe(datosRecepcionLogistica.getRecibe());

                                    ///////////////////////////////////////////////////////////////////////////
                                    //Vamos a modificar el formato de la fecha de recepción a uno mas legible

                                    descRecepId.setText(rolloRecoInfor.getNum_transa());
                                    // Crear un objeto SimpleDateFormat para el formato original
                                    SimpleDateFormat formatoOriginalTrefiRecep = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault());

                                    try {
                                        // Parsear la fecha original
                                        Date fechaOriginal = formatoOriginalTrefiRecep.parse(rolloRecoInfor.getFecha_recepcion());

                                        // Crear un nuevo objeto SimpleDateFormat para el formato deseado
                                        SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

                                        // Formatear la fecha en el nuevo formato
                                        assert fechaOriginal != null;
                                        String fechaFormateada = formatoDeseado.format(fechaOriginal);
                                        descRecepFecha.setText(fechaFormateada);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    //Enviamos todos los datos a la pantalla
                                    descRecepEntrega.setText(conexion.obtenerNombrePersona(AuditoriaLogistica.this,rolloRecoInfor.getEntrega()));
                                    descRecepRecibe.setText(conexion.obtenerNombrePersona(AuditoriaLogistica.this,rolloRecoInfor.getRecibe()));
                                    toastAcierto("Rollo encontrado");
                                }
                            }
                        }
                    }
                    cargarNuevo();
                }else{
                    toastError("Error! El tiquete no es de esta área o esta defectuoso");
                    limpiarDatos();
                    cargarNuevo();
                }
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //Metodo para realizar la limpieza de los datos de la plantalla de un rollo
    private void limpiarDatos(){
        descReviId.setText(getString(R.string.noDisponible));
        descReviFecha.setText(getString(R.string.noDisponible));
        descReviInspector.setText(getString(R.string.noDisponible));
        descReviEstado.setText(getString(R.string.noDisponible));
        descRecepId.setText(getString(R.string.noDisponible));
        descRecepFecha.setText(getString(R.string.noDisponible));
        descRecepEntrega.setText(getString(R.string.noDisponible));
        descRecepRecibe.setText(getString(R.string.noDisponible));
    }
    ////////////////////////////////////////////////////////////////////////////
    //Metodo para llenar el spinner en la pantalla con las diferentes areas
    private List<String> llenarlistaspinner() {
        listaAreas = new ArrayList<>();

        listaAreas.add("Seleccione área");
        listaAreas.add("Galvanizado");
        listaAreas.add("Trefilación");
        listaAreas.add("Recocido Industrial");
        //listaAreas.add("Recocido");

        return listaAreas;
    }

    private void cargarNuevo() {
        escanerCodigo.setText("");
        if (yaentre == 0){
            yaentre = 1;
        }else{
            yaentre = 0;
            escanerCodigo.requestFocus();
        }
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
}