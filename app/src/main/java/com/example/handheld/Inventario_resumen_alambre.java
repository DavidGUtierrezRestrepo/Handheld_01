package com.example.handheld;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.CorreoModelo;
import com.example.handheld.modelos.CorreoResumenModelo;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Inventario_resumen_alambre extends AppCompatActivity {


    CorreoModelo correo;

    private String nombreHoja;
    private ListView Resumen;
    private Button buttonAgregar;
    private Button buttonRetroceder; // Agregado

    private Button btnSalida; // Nuevo botón agregado
    String[] rec;
    String area, nit_usuario;

    Integer bodega;
    Context context = null;

    //Se inicializan elementos para enviar correos
    Session session = null;
    ProgressDialog pdialog = null;

    Conexion conexion = new Conexion();

    String subject, textMessage;

    List<String> atributos;

    CorreoResumenModelo email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_resumen_alambre);

        Resumen = findViewById(R.id.Resumen);
        buttonAgregar = findViewById(R.id.buttonAgregar);
        buttonRetroceder = findViewById(R.id.buttonRetroceder); // Inicializado
        btnSalida = findViewById(R.id.btnSalida); // Inicialización del nuevo botón
        area = getIntent().getStringExtra("area");
        bodega = getIntent().getIntExtra("bodega",0);
        nit_usuario = getIntent().getStringExtra("nit_usuario");



        // Lista de atributos
        switch (area) {
            case "ALAMBRÓN":
                switch (bodega) {
                    case 1:
                        nombreHoja = "Alambrón-Bodega 1-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nit_proveedor", "num_importacion", "id_solicitud_det", "numero_rollo", "peso", "costo_kilo");
                        break;
                    case 2:
                        nombreHoja = "Alambrón-Bodega 2-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nit_proveedor", "num_importacion", "id_solicitud_det", "numero_rollo", "peso", "costo_kilo");
                        break;

                }
                break;
            case "TREFILACIÓN":
                switch (bodega) {
                    case 2:
                        nombreHoja = "Trefilación-Bodega 2-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "consecutivo", "id_rollo", "operario", "diametro", "materia_prima", "colada", "traccion", "peso", "cod_orden", "fecha_hora", "cliente", "manual", "anulado", "destino");
                        break;
                    case 3:
                        nombreHoja = "Trefilación-Bodega 3-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre ", "consecutivo", "id_rollo", "operario", "diametro", "materia_prima", "colada", "traccion", "peso", "cod_orden", "fecha_hora", "cliente", "manual", "anulado", "destino");
                        break;
                    case 4:
                        nombreHoja = "Trefilación-Bodega 4 Codigo 2-No Conforme-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "consecutivo", "id_rollo", "operario", "diametro", "materia_prima", "colada", "traccion", "peso", "cod_orden", "fecha_hora", "cliente", "manual", "anulado", "destino");
                        break;
                    case 5:
                        nombreHoja = "Trefilación-Bodega 4 Codigo 3-No Conforme-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "consecutivo", "id_rollo", "operario", "diametro", "materia_prima", "colada", "traccion", "peso", "cod_orden", "fecha_hora", "cliente", "manual", "anulado", "destino");
                        break;
                }
                break;

            case "RECOCIDO":
                switch (bodega) {
                    case 2:
                        nombreHoja = "Recocido-Codigo 2-Bodega 2-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "cod_orden", "id_detalle", "id_rollo", "peso");
                        break;
                    case 3:
                        nombreHoja = "Recocido-Codigo 3-Bodega 2-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "cod_orden", "id_detalle", "id_rollo", "peso");
                        break;
                    case 4:
                        nombreHoja = "Recocido-Codigo 2-Bodega 4-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "cod_orden", "id_detalle", "id_rollo", "peso");
                        break;
                    case 5:
                        nombreHoja = "Recocido-Codigo 3-Bodega 4-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "cod_orden", "id_detalle", "id_rollo", "peso");
                        break;
                }
                break;
            case "GALVANIZADO":
                switch (bodega) {
                    case 2:
                        nombreHoja = "Galvanizado-Bodega 2-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "nro_orden", "nro_rollo", "tipo_trans",  "traslado", "peso", "fecha");
                        break;
                    case 11:
                        nombreHoja = "Galvanizado-Bodega 11-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "id_detalle", "id rollo", "traslado", "peso", "cod_orden", "fecha", "manual", "anulado", "destino");
                        break;
                    case 12:
                        nombreHoja = "Galvanizado-Bodega 12-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "nro_orden", "nro_rollo", "tipo_trans", "traslado", "peso", "fecha");
                        break;

                }
                break;
            case "PUNTILLERIA":
                switch (bodega) {
                    case 12:
                        nombreHoja = "Puntilleria-Bodega 12-Resumen Inventario";
                        atributos = Arrays.asList("codigo", "nombre", "id_detalle", "id rollo", "traslado", "peso", "cod_orden", "fecha_hora", "manual", "anulado", "destino");
                        break;

                }
                break;

        }

        // Se definen elementos para enviar correos
        context = this;

        email = conexion.correoResumen(Inventario_resumen_alambre.this,nit_usuario);

        rec = new String[]{
                email.getMail()
        };

        buttonAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarEmail();
            }
        });

        buttonRetroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrocederConInformacion();
            }
        });

        btnSalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salirHastaInventarioProceso();
            }
        });

        // Recuperar la lista de productos del Intent
        ArrayList<String> productosList = getIntent().getStringArrayListExtra("productosList");

        // Obtener la referencia al ListView
        ListView listViewProductos = findViewById(R.id.Resumen);

        // Crear un adaptador para los datos
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productosList);

        // Establecer el adaptador en el ListView
        listViewProductos.setAdapter(adapter);
    }

    private void enviarEmail() {
        // Verificar la conectividad antes de intentar enviar el correo
        if (isNetworkAvailable()) {
            // Crear el archivo Excel
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet(nombreHoja);

            // Llenar el archivo Excel con los datos
            int rowNum = 0;

            // Crear la fila para los nombres de los atributos
            Row rowAtributos = sheet.createRow(rowNum++);
            if (atributos != null) {
                for (int i = 0; i < atributos.size(); i++) {
                    rowAtributos.createCell(i).setCellValue(atributos.get(i));
                }
            }

            // Obtener los productos del Intent
            ArrayList<String> resumenList = getIntent().getStringArrayListExtra("resumenList");

            Set<String> processedProducts = new HashSet<>();

            // Agregar los valores de los productos en filas separadas
            for (String producto : resumenList) {
                // Crear una nueva fila para cada producto
                if (!processedProducts.contains(producto)) {
                    // Agregar los valores del producto en las columnas correspondientes
                    Row rowProducto = sheet.createRow(rowNum++);
                    // Comenzamos a agregar los valores a partir de la segunda columna
                    String[] valoresProducto = producto.split(",");
                    for (int i = 0; i < valoresProducto.length; i++) {
                        rowProducto.createCell(i).setCellValue(valoresProducto[i].split(":")[1].trim());
                    }
                }
                // Mark the product as processed
                processedProducts.add(producto);
            }

            // Guardar el archivo Excel en la memoria interna
            try {
                File file = new File(getFilesDir(), "resumen_inventario.xls");
                FileOutputStream fos = new FileOutputStream(file);
                workbook.write(fos);
                fos.close();

                // Preparar el correo electrónico
                correo = conexion.obtenerCorreo(Inventario_resumen_alambre.this);
                String email = correo.getCorreo();
                String pass = correo.getContrasena();
                subject = nombreHoja;
                textMessage = "Prueba de resumen en excel´s de los inventarios";

                // Resto del código para enviar el correo electrónico con el archivo adjunto
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

                // Adjuntar el archivo Excel al correo electrónico
                RetreiveFeedTask task = new RetreiveFeedTask(file);
                task.execute();
            } catch (IOException e) {
                e.printStackTrace();
                toastError("Error al guardar el archivo Excel");
            }
        } else {
            toastError("Problemas de conexión a Internet");
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

    private void retrocederConInformacion() {
        // Obtener la lista de productos del adaptador
        ArrayList<String> resumenList = new ArrayList<>();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) Resumen.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                resumenList.add(adapter.getItem(i));
            }
        }

        // Crear un Intent para volver a la actividad anterior (Inventario_transaccion)
        Intent intent = new Intent();
        intent.putExtra("nit_usuario", nit_usuario);
        // Envía nuevamente la información (productosList) a la actividad anterior
        intent.putStringArrayListExtra("resumenList",resumenList);
        // Establece el resultado como RESULT_OK y pasa el Intent de vuelta a la actividad anterior
        setResult(RESULT_OK, intent);
        // Finaliza esta actividad
        finish();
    }
    private void salirHastaInventarioProceso() {

        eliminarTabla();

        // Retroceder a la actividad Inventario_proceso
        Intent intent = new Intent(this, Inventario_proceso_alambre.class);
        intent.putExtra("nit_usuario", nit_usuario);
        startActivity(intent);
        finish();
    }

    private void eliminarTabla() {
        // Utilizar la instancia existente de DBHelper
        DBHelper dbHelper = new DBHelper(context);
        // Eliminar la tabla TABLE_SCANNED_PRODUCTS
        dbHelper.deleteAllRowsFromTable();
    }

    class RetreiveFeedTask extends AsyncTask<Void, Void, String> {

        private File archivoAdjunto;

        public RetreiveFeedTask(File file) {
            this.archivoAdjunto = file;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("emailservicecorsan@gmail.com"));
                InternetAddress[] toAddresses = new InternetAddress[rec.length];
                for (int i = 0; i < rec.length; i++) {
                    toAddresses[i] = new InternetAddress(rec[i]);
                }
                message.setRecipients(Message.RecipientType.TO, toAddresses);
                message.setSubject(subject);
                message.setContent(textMessage, "text/html; charset=utf-8");

                // Adjuntar el archivo Excel al mensaje
                MimeBodyPart adjunto = new MimeBodyPart();
                adjunto.attachFile(archivoAdjunto);

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(adjunto);
                message.setContent(multipart);

                Transport.send(message);
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pdialog.dismiss();
            toastAcierto("Correo Enviado");


        }

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
}