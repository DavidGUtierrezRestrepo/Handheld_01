package com.example.handheld;


import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;

import android.widget.TextView;

import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.handheld.ClasesOperativas.Gestion_alambronLn;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.RolloGalvInventario;
import com.example.handheld.modelos.RolloGalvInventarioNoConforme;
import com.example.handheld.modelos.RollosAlambronInven;
import com.example.handheld.modelos.RollosMPGalvInven;
import com.example.handheld.modelos.RollosMPPuntInven;
import com.example.handheld.modelos.RollosRecocidoInven;
import com.example.handheld.modelos.RollosTrefiInven;
import com.example.handheld.modelos.RollosTrefiInvenNo_conforme;


import java.util.ArrayList;
import java.util.List;




public class Inventario_transaccion_alambre extends AppCompatActivity {

    private EditText codigoCajaRecep;
    private List<String> productosList;
    private List<String> resumenList;

    SoundPool sp;

    int yaentre = 0, leidos;

    int sonido_de_Reproduccion;

    Vibrator vibrator;

    private ArrayAdapter<String> adapter;
    private TextView totalRollosTextView,textView9;
    private boolean entradaManual = false;
    private boolean escaneoRealizado = false;
    private Conexion conexion;
    private RollosTrefiInven producto;

    private RollosTrefiInvenNo_conforme productoNo_conforme;

    private RollosMPGalvInven producto_g;

    private RolloGalvInventario producto_i;

    private RolloGalvInventarioNoConforme producto_i_no_conforme;

    private RollosRecocidoInven producto_r;

    private RollosAlambronInven producto_x;

    private RollosMPPuntInven producto_p;


    private Gestion_alambronLn gestion_alambronLn = new Gestion_alambronLn();
    private List<RollosTrefiInven> ListaRollosTrefiInven = new ArrayList<>();

    private List<RollosTrefiInvenNo_conforme> ListaRollosTrefiInvenNo_conforme = new ArrayList<>();

    private List<RollosMPGalvInven> ListaRollosMPGalvInven = new ArrayList<>();

    private List<RolloGalvInventario> ListaRolloGalvaInventario = new ArrayList<>();
    private List<RolloGalvInventarioNoConforme> ListaRolloGalvaInventarioNo_conforme = new ArrayList<>();

    private List<RollosMPPuntInven> ListaRollosMPPuntInven = new ArrayList<>();



    private List<RollosRecocidoInven> ListaRollosRecocidoInven = new ArrayList<>();

    private List<RollosAlambronInven> ListaRollosAlambronInven = new ArrayList<>();
    private int contadorRollos = 0;
    private DBHelper dbHelper;
    private String area;
    String  nit_usuario;
    String nro_orden, nro_rollo;
    Integer bodega;
    private RecyclerView recyclerViewdetalleTransaccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_transaccion_alambre);

        dbHelper = new DBHelper(this);

        codigoCajaRecep = findViewById(R.id.codigoCajaRecep);
        ListView listviewInventario = findViewById(R.id.listviewInventario);
        Button buttonSalir = findViewById(R.id.buttonSalir);
        Button buttonListo = findViewById(R.id.buttonListo);
        Button buttonAtras = findViewById(R.id.buttonAtras);
        totalRollosTextView = findViewById(R.id.textView16);



        //Se Define los varibles para el sonido de error
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 1);
        sonido_de_Reproduccion = sp.load(this, R.raw.sonido_error_2, 1);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        area = getIntent().getStringExtra("area");
        bodega = getIntent().getIntExtra("bodega", 0);
        nit_usuario = getIntent().getStringExtra("nit_usuario");

        //Colocamos el titulo con la informacion
        textView9 = findViewById(R.id.textView9);

        // Verifica si txTransaccion no es nulo antes de usarlo
        if (textView9 != null) {
            // Aquí puedes establecer el texto para el TextView
            textView9.setText("BODEGA " + bodega + " AREA " + area);
        } else {
            // Maneja el caso si txTransaccion es nulo
            Log.e(TAG, "txTransaccion es nulo");
        }

        productosList = new ArrayList<>();
        resumenList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.list_item_producto, android.R.id.text1, productosList);
        listviewInventario.setAdapter(adapter);

        conexion = new Conexion();

        codigoCajaRecep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        codigoCajaRecep.requestFocus();

        // Método para manejar la entrada de código de barras cuando se presiona la tecla Enter
        codigoCajaRecep.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    // Verificar si la entrada no es manual y no se ha realizado ningún escaneo previo
                    if (!entradaManual && !escaneoRealizado) {
                        // Seleccionar el área y la acción correspondiente según la configuración
                        switch (area) {
                            case "ALAMBRÓN":
                                switch (bodega) {
                                    case 1:
                                        escanearProductoAlambronCodigo1();
                                        break;
                                    case 2:
                                        escanearProductoAlambronCodigo1Bodega2();
                                        break;


                                }
                                break;
                            case "TREFILACIÓN":
                                switch (bodega) {
                                    case 2:
                                        escanearProductoCodigo2();
                                        break;
                                    case 3:
                                        escanearProductoCodigo3();
                                        break;
                                    case 4:
                                        escanearProductoNoConformeCodigo2();
                                        break;
                                    case 5:
                                        escanearProductoNoConformeCodigo3();
                                        break;
                                }
                                break;
                            case "RECOCIDO":
                                switch (bodega) {
                                    case 2:
                                        escanearProductoRecocidoCodigo2();
                                        break;
                                    case 3:
                                        escanearProductoRecocidoCodigo3();
                                        break;
                                    case 4:
                                        escanearProductoNoConformeRecocidoCodigo2();
                                        break;
                                    case 5:
                                        escanearProductoNoConformeRecocidoCodigo3();
                                        break;
                                }
                                break;
                            case "GALVANIZADO":
                                switch (bodega) {
                                    case 2:
                                        escanearProductoGalvanizadoCodigo2();
                                        break;
                                    case 11:
                                        escanearProductoDestinoGalvanizadoCodigo2();
                                        break;
                                    case 12:
                                        escanearProductoGalvanizadoCodigo12();
                                        break;

                                }
                                break;
                            case "PUNTILLERIA":
                                switch (bodega) {
                                    case 12:
                                        escanearProductoPuntilleriaCodigo2();
                                        break;


                                }
                                break;
                        }
                    }
                    // Indicar que la entrada es manual
                    entradaManual = true;
                    return true;
                } else {
                    entradaManual = false;
                }
                return false;
            }
        });

        // Método para manejar el botón de salir
        buttonSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Realizar la acción de salida según el área y la bodega
                switch (area) {
                    case "ALAMBRÓN":
                        switch (bodega) {
                            case 1:
                                eliminarTodosLosProductosAlambronCodigo1();
                                break;
                            case 2:
                                eliminarTodosLosProductosAlambronCodigo1Bodega2();
                                break;

                        }
                        break;
                    case "TREFILACIÓN":
                        switch (bodega) {
                            case 2:
                                eliminarTodosLosProductosCodigo2();
                                break;
                            case 3:
                                eliminarTodosLosProductosCodigo3();
                                break;
                            case 4:
                                eliminarTodosLosProductosNoConformesCodigo2();
                                break;
                            case 5:
                                eliminarTodosLosProductosNoConformesCodigo3();
                                break;
                        }
                        break;
                    case "RECOCIDO":
                        switch (bodega) {
                            case 2:
                                eliminarTodosLosProductosRecocidoCodigo2();
                                break;
                            case 3:
                                eliminarTodosLosProductosRecocidoCodigo3();
                                break;
                            case 4:
                                eliminarTodosLosProductoRecocidosNoConformesCodigo2();
                                break;
                            case 5:
                                eliminarTodosLosProductoRecocidosNoConformesCodigo3();
                                break;
                        }
                        break;
                    case "GALVANIZADO":
                        switch (bodega) {
                            case 2:
                                eliminarProductoGalvanizadoCodigo2();
                                break;
                            case 11:
                                eliminarProductoDestinoGalvanizadoCodigo2();
                                break;
                            case 12:
                                eliminarProductoGalvanizadoCodigo12();
                                break;

                        }
                        break;
                    case "PUNTILLERIA":
                        switch (bodega) {
                            case 12:
                                eliminarProductoPuntilleriaCodigo2();
                                break;

                        }
                        break;
                }
            }
        });

        // Método para manejar el botón de atrás
        buttonAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Regresar a la actividad anterior
                Intent intent = new Intent(Inventario_transaccion_alambre.this, Inventario_proceso_alambre.class);
                intent.putExtra("nit_usuario", nit_usuario);
                startActivity(intent);
                finish();
            }
        });

        // Método para manejar el botón de listo
        buttonListo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar si se han escaneado productos antes de proceder al resumen
                if (productosList.isEmpty()) {
                    toastError("No hay productos escaneados");
                } else {
                    // Mostrar el resumen de productos escaneados
                    Intent intent = new Intent(Inventario_transaccion_alambre.this, Inventario_resumen_alambre.class);
                    intent.putExtra("area", area);
                    intent.putExtra("bodega", bodega);
                    intent.putExtra("nit_usuario", nit_usuario);
                    intent.putStringArrayListExtra("productosList", new ArrayList<>(productosList));
                    intent.putStringArrayListExtra("resumenList", new ArrayList<>(resumenList));
                    startActivity(intent);
                }
            }
        });

        // Seleccionar la acción correspondiente según el área y la bodega al iniciar la actividad
        switch (area) {
            case "ALAMBRÓN":
                switch (bodega) {
                    case 1:
                        contadorRollos = obtenerCantidadProductoAlambronCodigo1();
                        mostrarDataAlambronCodigo1();
                        break;
                    case 2:
                        contadorRollos = obtenerCantidadProductoAlambronCodigo1Bodega2();
                        mostrarDataAlambronCodigo1Bodega2();
                        break;


                }
                break;
            case "TREFILACIÓN":
                switch (bodega) {
                    case 2:
                        contadorRollos = obtenerCantidadProductosEnBaseDeDatosCodigo2();
                        mostrarData();
                        break;
                    case 3:
                        contadorRollos = obtenerCantidadProductosEnBaseDeDatosCodigo3();
                        mostrarData();
                        break;
                    case 4:
                        contadorRollos = obtenerCantidadProductosNoConformeEnBaseDeDatosCodigo2();
                        mostrarDataNoConforme();
                        break;
                    case 5:
                        contadorRollos = obtenerCantidadProductosNoConformeEnBaseDeDatosCodigo3();
                        mostrarDataNoConforme();
                        break;
                }
                break;
            case "RECOCIDO":
                switch (bodega) {
                    case 2:
                        contadorRollos = obtenerCantidadProductoRecocidoCodigo2();
                        mostrarDataRecocidoCodigo2();
                        break;
                    case 3:
                        contadorRollos = obtenerCantidadProductoRecocidoCodigo3();
                        mostrarDataRecocidoCodigo3();
                        break;

                    case 4:
                        contadorRollos = obtenerCantidadProductosRecocidoNoConformeEnBaseDeDatosCodigo2();
                        mostrarDataRecocidoNoConformeCodigo2();
                        break;
                    case 5:
                        contadorRollos = obtenerCantidadProductoRecocidoNoConformeCodigo3();
                        mostrarDataRecocidoNoConformeCodigo3();
                        break;
                }
                break;
            case "GALVANIZADO":
                switch (bodega) {
                    case 2:
                        contadorRollos = obtenerCantidadProductoGalvanizadoCodigo2();
                        mostrarDataGalvanizado();
                        break;
                    case 11:
                        contadorRollos = obtenerCantidadProductoDestinoGalvanizadoCodigo2();
                        mostrarDataDestinoGalvanizado();
                        break;
                    case 12:
                        contadorRollos = obtenerCantidadProductoGalvanizadoCodigo12();
                        mostrarDataGalvanizado();
                        break;

                }
                break;
            case "PUNTILLERIA":
                switch (bodega) {
                    case 12:
                        contadorRollos = obtenerCantidadProductoPuntilleriaCodigo2();
                        mostrarDataPuntilleria();
                        break;

                }
                break;
        }
        ;

    }

    //Metodo para obtener la cantidad de productos de la BD interna Trefilacion Codigo 2
    private int obtenerCantidadProductosEnBaseDeDatosCodigo2() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_TREFILACION_CODIGO_2, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna Trefilacion Codigo 2 no conforme
    private int obtenerCantidadProductosNoConformeEnBaseDeDatosCodigo2() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_TREFILACION_CODIGO_2, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna Recocido Codigo 2
    private int obtenerCantidadProductoRecocidoCodigo2() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_2, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna Recocido Codigo 2 no conforme
    private int obtenerCantidadProductosRecocidoNoConformeEnBaseDeDatosCodigo2() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_2_NO_CONFORME, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna Alambron Codigo 1 Bodega 1
    private int obtenerCantidadProductoAlambronCodigo1() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_ALAMBRON_CODIGO_1, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }


    //Metodo para obtener la cantidad de productos de la BD interna Alambron Codigo 1 Bodega 2
    private int obtenerCantidadProductoAlambronCodigo1Bodega2() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_ALAMBRON_CODIGO_2, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna Recocido Codigo 3
    private int obtenerCantidadProductoRecocidoCodigo3() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_3, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna Recocido Codigo 3 no conforme
    private int obtenerCantidadProductoRecocidoNoConformeCodigo3() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_3_NO_CONFORME, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna Trefilación Codigo 3
    private int obtenerCantidadProductosEnBaseDeDatosCodigo3() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_TREFILACION_CODIGO_3, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna Trefilación Codigo 3 no conforme
    private int obtenerCantidadProductosNoConformeEnBaseDeDatosCodigo3() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_TREFILACION_CODIGO_3_NO_CONFORME, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna Destino Galvanizado Codigo 2
    private int obtenerCantidadProductoDestinoGalvanizadoCodigo2() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_DESTINO_GALVANIZADO_CODIGO_2, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna  Galvanizado Codigo 2
    private int obtenerCantidadProductoGalvanizadoCodigo2() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_GALVANIZADO_CODIGO_2, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }


    //Metodo para obtener la cantidad de productos de la BD interna  Galvanizado Codigo 2 Bodega 12
    private int obtenerCantidadProductoGalvanizadoCodigo12() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_GALVANIZADO_CODIGO_2_BODEGA_12, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para obtener la cantidad de productos de la BD interna  Puntilleria Codigo 2
    private int obtenerCantidadProductoPuntilleriaCodigo2() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_PUNTILLERIA_CODIGO_2, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    //Metodo para escanear Trefilacion Codigo 2
    private void escanearProductoCodigo2() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Verifica si el código leído está vacío
            for (RollosTrefiInven rollo : ListaRollosTrefiInven) {
                // Comprueba si el rollo ya ha sido escaneado anteriormente
                String codigoLeido = rollo.getCod_orden() + "-" + rollo.getId_detalle() + "-" + rollo.getId_rollo();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }
            // Extrae los datos del código de barras del rollo
            String cod_orden = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", codigoCaja);
            String id_detalle = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_detalle", codigoCaja);
            String id_rollo = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto = conexion.ObtenerRollosTrefiInve(Inventario_transaccion_alambre.this, cod_orden, id_detalle, id_rollo);
            if (producto.getCod_orden() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }

            // Extrae los detalles del producto
            String codigo = producto.getCodigo();
            String nombre = producto.getNombre();
            String consecutivo = producto.getConsecutivo();
            String id_rollo_tref = producto.getId_rollo();
            String operario = producto.getOperario();
            String diametro = producto.getDiametro();
            String materia_prima = producto.getMateria_prima();
            String colada = producto.getColada();
            String traccion = producto.getTraccion();
            String peso = producto.getPeso();
            String cod_orden_tref = producto.getCod_orden();
            String fecha_hora = producto.getFecha_hora();
            String cliente = producto.getCliente();
            String manual = producto.getManual();
            String anulado = producto.getAnulado();
            String destino = producto.getDestino();
            if (anulado == null) {

                if (destino == null) {
                    // Si el rollo no está anulado ni tiene un destino asignado, lo agrega a la lista de productos
                    String descripcionProducto = "codigo: " + codigo + ", consecutivo: " + consecutivo + ", id_rollo: " + id_rollo_tref + ", cod_orden: " + cod_orden_tref + ", peso: " + peso;
                    productosList.add(descripcionProducto);
                    String resumeInventario = "codigo: " + codigo + ", nombre: " + nombre + ", consecutivo: " + consecutivo + ", id_rollo: " + id_rollo + ", operario: " + operario + ", diametro: " + diametro + ", materia_prima: " + materia_prima + ", colada: " + colada + ", traccion: " + traccion + ", peso: " + peso + ", cod_orden: " + cod_orden + ", fecha_hora: " + fecha_hora + ", cliente: " + cliente + ", manual:" + manual;
                    resumenList.add(resumeInventario);
                    contadorRollos++;
                    ListaRollosTrefiInven.add(producto);
                    adapter.notifyDataSetChanged();
                    totalRollosTextView.setText(String.valueOf(contadorRollos));
                    codigoCajaRecep.setText("");
                    toastAcierto("Producto agregado");
                    guardarEnBaseDeDatosCodigo2(producto);
                } else {
                    // Si el rollo tiene un destino asignado, muestra un mensaje de error y limpia la caja de texto
                    toastError("El rollo ya está en proceso: '" + destino + "'");
                    codigoCajaRecep.setText("");
                }
            } else {
                // Si el rollo está anulado, muestra un mensaje de error
                toastError("El rollo  anulado");
            }
        }
    }

    //Metodo para escanear Trefilacion Codigo 2 no conforme
    private void escanearProductoNoConformeCodigo2() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Verifica si el código leído está vacío
            for (RollosTrefiInvenNo_conforme rollo : ListaRollosTrefiInvenNo_conforme) {
                // Comprueba si el rollo ya ha sido escaneado anteriormente
                String codigoLeido = rollo.getCod_orden() + "-" + rollo.getId_detalle() + "-" + rollo.getId_rollo();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }
            // Extrae los datos del código de barras del rollo
            String cod_orden = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", codigoCaja);
            String id_detalle = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_detalle", codigoCaja);
            String id_rollo = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            productoNo_conforme = conexion.ObtenerRollosTrefiInvNo_Conforme(Inventario_transaccion_alambre.this, cod_orden, id_detalle, id_rollo);
            if (productoNo_conforme.getCod_orden() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }

            // Extrae los detalles del producto
            String codigo = productoNo_conforme.getCodigo();
            String nombre = productoNo_conforme.getNombre();
            String consecutivo = productoNo_conforme.getConsecutivo();
            String id_rollo_tref = productoNo_conforme.getId_rollo();
            String operario = productoNo_conforme.getOperario();
            String diametro = productoNo_conforme.getDiametro();
            String materia_prima = productoNo_conforme.getMateria_prima();
            String colada = productoNo_conforme.getColada();
            String traccion = productoNo_conforme.getTraccion();
            String peso = productoNo_conforme.getPeso();
            String cod_orden_tref = productoNo_conforme.getCod_orden();
            String fecha_hora = productoNo_conforme.getFecha_hora();
            String cliente = productoNo_conforme.getCliente();
            String manual = productoNo_conforme.getManual();
            String anulado = productoNo_conforme.getAnulado();
            String destino = productoNo_conforme.getDestino();
            String no_conforme =productoNo_conforme.getNo_conforme();
            if (anulado == null) {

                if (destino == null) {
                    // Si el rollo no está anulado ni tiene un destino asignado, lo agrega a la lista de productos
                    String descripcionProducto = "codigo: " + codigo + ", consecutivo: " + consecutivo + ", id_rollo: " + id_rollo_tref + ", cod_orden: " + cod_orden_tref + ", peso: " + peso;
                    productosList.add(descripcionProducto);
                    String resumeInventario = "codigo: " + codigo + ", nombre: " + nombre + ", consecutivo: " + consecutivo + ", id_rollo: " + id_rollo + ", operario: " + operario + ", diametro: " + diametro + ", materia_prima: " + materia_prima + ", colada: " + colada + ", traccion: " + traccion + ", peso: " + peso + ", cod_orden: " + cod_orden + ", fecha_hora: " + fecha_hora + ", cliente: " + cliente + ", manual:" + manual + ", no_conforme:" + no_conforme;
                    resumenList.add(resumeInventario);
                    contadorRollos++;
                    ListaRollosTrefiInvenNo_conforme.add(productoNo_conforme);
                    adapter.notifyDataSetChanged();
                    totalRollosTextView.setText(String.valueOf(contadorRollos));
                    codigoCajaRecep.setText("");
                    toastAcierto("Producto agregado");
                    guardarEnBaseDeDatosCodigo2No_conforme(productoNo_conforme);
                } else {
                    // Si el rollo tiene un destino asignado, muestra un mensaje de error y limpia la caja de texto
                    toastError("El rollo ya está en proceso: '" + destino + "'");
                    codigoCajaRecep.setText("");
                }
            } else {
                // Si el rollo está anulado, muestra un mensaje de error
                toastError("El rollo anulado");
            }
        }
    }

    //Metodo para escanear Puntilleria Codigo 2
    private void escanearProductoPuntilleriaCodigo2() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosMPPuntInven rollo : ListaRollosMPPuntInven) {
                String codigoLeido = rollo.getCod_orden() + "-" + rollo.getId_detalle() + "-" + rollo.getId_rollo();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }
            // Extrae los datos del código de barras del rollo
            String cod_orden = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", codigoCaja);
            String id_detalle = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_detalle", codigoCaja);
            String id_rollo = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto_p = conexion.ObtenerRollosPunt(Inventario_transaccion_alambre.this, cod_orden, id_detalle, id_rollo);
            if (producto_p.getCod_orden() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }
            // Extrae los detalles del producto
            String codigo = producto_p.getCodigo();
            String nombre = producto_p.getNombre();

            String id_rollo_tref = producto_p.getId_rollo();
            String id_traslado = producto_p.getTraslado();
            String peso = producto_p.getPeso();
            String cod_orden_tref = producto_p.getCod_orden();
            String fecha_hora = producto_p.getFecha_hora();
            String manual = producto_p.getManuales();
            String anulado = producto_p.getAnulado();
            String destino = producto_p.getDestino();
            String scla = producto_p.getScla();

            if (anulado == null) {

                if (destino != null && destino.equals("P")) {
                    if (scla != null) {
                        toastError("Este rollo tiene el scla:" + scla);
                        codigoCajaRecep.setText("");
                    } else {
                        // Si el rollo no está anulado,tiene un destino de P y no tiene scla, lo agrega a la lista de productos
                        String descripcionProducto = "codigo: " + codigo + ", id_rollo: " + id_rollo_tref + ", cod_orden: " + cod_orden_tref + ", peso: " + peso;
                        productosList.add(descripcionProducto);
                        String resumeInventario = "codigo: " + codigo + ", nombre: " + nombre + ", id_detalle: " + id_detalle + ", id_rollo : " + id_rollo + ", traslado: " + id_traslado + ", peso: " + peso + ", fecha_hora: " + fecha_hora + ", cod_orden: " + cod_orden + ", manual:" + manual + ", destino:" + destino;
                        resumenList.add(resumeInventario);
                        contadorRollos++;
                        ListaRollosMPPuntInven.add(producto_p);
                        adapter.notifyDataSetChanged();
                        totalRollosTextView.setText(String.valueOf(contadorRollos));
                        codigoCajaRecep.setText("");
                        toastAcierto("Producto agregado");
                        guardarEnBaseDeDatosCodigoPunt2(producto_p);
                    }
                } else {
                    // Si el rollo tiene un destino asignado, muestra un mensaje de error y limpia la caja de texto
                    toastError("El rollo está en proceso: '" + destino + "'");
                    codigoCajaRecep.setText("");
                }
            } else {
                // Si el rollo está anulado, muestra un mensaje de error
                toastError("El rollo anulado");
            }
        }
    }

    //Metodo para escanear Destino Galvanizado Codigo 2
    private void escanearProductoDestinoGalvanizadoCodigo2() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosMPGalvInven rollo : ListaRollosMPGalvInven) {
                String codigoLeido = rollo.getCod_orden() + "-" + rollo.getId_detalle() + "-" + rollo.getId_rollo();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }
            // Extrae los datos del código de barras del rollo
            String cod_orden = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", codigoCaja);
            String id_detalle = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_detalle", codigoCaja);
            String id_rollo = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto_g = conexion.ObtenerRollosGalvanizado(Inventario_transaccion_alambre.this, cod_orden, id_detalle, id_rollo);
            if (producto_g.getCod_orden() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }
            // Extrae los detalles del producto
            String codigo = producto_g.getCodigo();
            String nombre = producto_g.getNombre();

            String id_rollo_tref = producto_g.getId_rollo();
            String id_traslado = producto_g.getTraslado();
            String peso = producto_g.getPeso();
            String cod_orden_tref = producto_g.getCod_orden();
            String fecha_hora = producto_g.getFecha_hora();
            String manual = producto_g.getManuales();
            String anulado = producto_g.getAnulado();
            String destino = producto_g.getDestino();
            String saga = producto_g.getSaga();
            if (anulado == null) {
                {
                    if (saga != null) {
                        toastError("Este rollo tiene el saga:" + saga);
                        codigoCajaRecep.setText("");
                    } else {
                        // Si el rollo no está anulado ni tiene y no tiene saga , lo agrega a la lista de productos
                        String descripcionProducto = "codigo: " + codigo + ", id_rollo: " + id_rollo_tref + ", cod_orden: " + cod_orden_tref + ", peso: " + peso;
                        productosList.add(descripcionProducto);
                        String resumeInventario = "codigo: " + codigo + ", nombre: " + nombre + ", id_detalle: " + id_detalle + ", id_rollo : " + id_rollo + ", traslado: " + id_traslado + ", peso: " + peso + ", fecha_hora: " + fecha_hora + ", cod_orden: " + cod_orden + ", manual:" + manual + ", destino:" + destino;
                        resumenList.add(resumeInventario);
                        contadorRollos++;
                        ListaRollosMPPuntInven.add(producto_p);
                        adapter.notifyDataSetChanged();
                        totalRollosTextView.setText(String.valueOf(contadorRollos));
                        codigoCajaRecep.setText("");
                        toastAcierto("Producto agregado");
                        guardarEnBaseDeDatosCodigo2DestinoGalva(producto_g);
                    }
                }
            } else {
                // Si el rollo está anulado, muestra un mensaje de error
                toastError("El rollo anulado");
            }
        }
    }

    //Metodo para escanear Galvanizado Codigo 2
    @SuppressLint("SetTextI18n")
    private void escanearProductoGalvanizadoCodigo2() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();

        // Verifica si el código leído está vacío
        if (TextUtils.isEmpty(codigoCaja)) {
            toastError("El código está vacío");
            return;
        }

        // Verifica si el código leído ya está en la lista de inventario
        for (RolloGalvInventario rollo : ListaRolloGalvaInventario) {
            String codigoLeido = rollo.getNro_orden() + "-" + rollo.getNro_rollo();
            if (codigoCaja.equals(codigoLeido)) {
                // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                toastError("Rollo ya leído");
                codigoCajaRecep.setText("");
                return;
            }
        }

        // Extrae los datos del código de barras del rollo
        String nro_orden = gestion_alambronLn.extraerDatoCodigoBarrasGalvanizado("nro_orden", codigoCaja);
        String nro_rollo = gestion_alambronLn.extraerDatoCodigoBarrasGalvanizado("nro_rollo", codigoCaja);

        // Obtiene la información del rollo desde la base de datos
        producto_i = conexion.ObtenerRollosGalva(Inventario_transaccion_alambre.this, nro_orden, nro_rollo);

        // Verifica si el producto se encuentra en la base de datos
        if (producto_i == null || producto_i.getNro_orden() == null) {
            // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
            toastError("Tiquete no pertenece a esta bodega");
            codigoCajaRecep.setText("");
            return;
        }else if (producto_i.getDestino() != null) {
            // Si el rollo tiene tipo_salida no nulo, muestra un mensaje de error y limpia la caja de texto
            toastError("Tiquete ya consumido");
            codigoCajaRecep.setText("");
            return;
        }


        // Extrae los detalles del producto
        String codigo = producto_i.getCodigo();
        String nombre = producto_i.getNombre();
        String peso = producto_i.getPeso();
        String tipo_trans = producto_i.getTipo_trans();
        String traslado = producto_i.getTraslado();
        String fecha_hora = producto_i.getFecha_hora();

        // Agrega el producto a las listas y actualiza la UI
        String descripcionProducto = "codigo: " + codigo + ", nombre: " + nombre + ", cod_orden: " + nro_orden + ", id_rollo : " + nro_rollo + ", peso: " + peso;
        productosList.add(descripcionProducto);

        String resumeInventario = "codigo: " + codigo + ", nombre: " + nombre + ", cod_orden: " + nro_orden + ", id_rollo : " + nro_rollo + ",tipo_trans: " + tipo_trans + ", traslado: " + traslado + ", peso: " + peso + ", fecha: " + fecha_hora;
        resumenList.add(resumeInventario);

        contadorRollos++;
        ListaRolloGalvaInventario.add(producto_i);

        // Notifica al adaptador de datos
        adapter.notifyDataSetChanged();

        // Actualiza el contador de rollos
        totalRollosTextView.setText(String.valueOf(contadorRollos));

        // Limpia la caja de texto
        codigoCajaRecep.setText("");

        // Muestra un mensaje de éxito
        toastAcierto("Producto agregado");

        // Guarda el producto en la base de datos
        guardarEnBaseDeDatosCodigoGalva2(producto_i);
    }

    //Metodo para escanear Galvanizado Bodega 12
    @SuppressLint("SetTextI18n")
    private void escanearProductoGalvanizadoCodigo12() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();

        // Verifica si el código leído está vacío
        if (TextUtils.isEmpty(codigoCaja)) {
            toastError("El código está vacío");
            return;
        }

        // Verifica si el código leído ya está en la lista de inventario
        for (RolloGalvInventario rollo : ListaRolloGalvaInventario) {
            String codigoLeido = rollo.getNro_orden() + "-" + rollo.getNro_rollo();
            if (codigoCaja.equals(codigoLeido)) {
                // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                toastError("Rollo ya leído");
                codigoCajaRecep.setText("");
                return;
            }
        }

        // Extrae los datos del código de barras del rollo
        String nro_orden = gestion_alambronLn.extraerDatoCodigoBarrasGalvanizado("nro_orden", codigoCaja);
        String nro_rollo = gestion_alambronLn.extraerDatoCodigoBarrasGalvanizado("nro_rollo", codigoCaja);

        // Obtiene la información del rollo desde la base de datos
        producto_i = conexion.ObtenerRollosGalva12(Inventario_transaccion_alambre.this, nro_orden, nro_rollo);

        // Verifica si el producto se encuentra en la base de datos
        if (producto_i == null || producto_i.getNro_orden() == null) {
            // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
            toastError("Tiquete no pertenece a esta bodega");
            codigoCajaRecep.setText("");
            return;
        }else if (producto_i.getDestino() != null) {
            // Si el rollo tiene tipo_salida no nulo, muestra un mensaje de error y limpia la caja de texto
            toastError("Tiquete ya consumido");
            codigoCajaRecep.setText("");
            return;
        }

        // Extrae los detalles del producto
        String codigo = producto_i.getCodigo();
        String nombre = producto_i.getNombre();
        String peso = producto_i.getPeso();
        String tipo_trans = producto_i.getTipo_trans();
        String traslado = producto_i.getTraslado();
        String fecha_hora = producto_i.getFecha_hora();

        // Agrega el producto a las listas y actualiza la UI
        String descripcionProducto = "codigo: " + codigo + ", nombre: " + nombre + ", cod_orden: " + nro_orden + ", id_rollo : " + nro_rollo + ", peso: " + peso;
        productosList.add(descripcionProducto);

        String resumeInventario = "codigo: " + codigo + ", nombre: " + nombre + ", cod_orden: " + nro_orden + ", id_rollo : " + nro_rollo + ",tipo_trans: " + tipo_trans + ", traslado: " + traslado + ", peso: " + peso + ", fecha: " + fecha_hora;
        resumenList.add(resumeInventario);

        contadorRollos++;
        ListaRolloGalvaInventario.add(producto_i);

        // Notifica al adaptador de datos
        adapter.notifyDataSetChanged();

        // Actualiza el contador de rollos
        totalRollosTextView.setText(String.valueOf(contadorRollos));

        // Limpia la caja de texto
        codigoCajaRecep.setText("");

        // Muestra un mensaje de éxito
        toastAcierto("Producto agregado");

        // Guarda el producto en la base de datos
        guardarEnBaseDeDatosCodigoGalva12(producto_i);
    }

    //Metodo para escanear Recocido Codigo 2
    private void escanearProductoRecocidoCodigo2() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosRecocidoInven rollo : ListaRollosRecocidoInven) {
                String codigoLeido = rollo.getCod_orden_rec() + "-" + rollo.getId_detalle_rec() + "-" + rollo.getId_rollo_rec();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }
            // Extrae los datos del código de barras del rollo
            String cod_orden_rec = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden", codigoCaja);
            String id_detalle_rec = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle", codigoCaja);
            String id_rollo_rec = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto_r = conexion.ObtenerRollosRecocidoCodigo2(Inventario_transaccion_alambre.this, cod_orden_rec, id_detalle_rec, id_rollo_rec);
            if (producto_r.getCod_orden_rec() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }
            // Extrae los detalles del producto
            String codigo = producto_r.getCodigo();
            String nombre = producto_r.getNombre();
            String peso = producto_r.getPeso();

            if (producto_r != null) {


                // Si el rollo no está anulado,tiene un destino de P y no tiene scla, lo agrega a la lista de productos
                String descripcionProducto = "Codigo: " + codigo + ", Id_rollo: " + id_rollo_rec + ", Cod_orden: " + cod_orden_rec + ", Peso: " + peso;
                productosList.add(descripcionProducto);
                String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Cod_orden: " + cod_orden_rec + ", Id_detalle : " + id_detalle_rec + ", Id_rollo: " + id_rollo_rec + ", Peso:" + peso;
                resumenList.add(resumeInventario);
                contadorRollos++;
                ListaRollosRecocidoInven.add(producto_r);
                adapter.notifyDataSetChanged();
                totalRollosTextView.setText(String.valueOf(contadorRollos));
                codigoCajaRecep.setText("");
                toastAcierto("Producto agregado");
                guardarEnBaseDeDatosCodigoRecocido2(producto_r);
            }

        } else {    // Si el rollo está anulado, muestra un mensaje de error
            toastError("Rollo anulado");
            cargarNuevo();
        }
    }

    //Metodo para escanear Puntilleria Codigo 2 no conforme
    private void escanearProductoNoConformeRecocidoCodigo2() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosRecocidoInven rollo : ListaRollosRecocidoInven) {
                String codigoLeido = rollo.getCod_orden_rec() + "-" + rollo.getId_detalle_rec() + "-" + rollo.getId_rollo_rec();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }
            // Extrae los datos del código de barras del rollo
            String cod_orden_rec = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden", codigoCaja);
            String id_detalle_rec = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle", codigoCaja);
            String id_rollo_rec = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto_r = conexion.ObtenerRollosRecocidoNoConformeCodigo2(Inventario_transaccion_alambre.this, cod_orden_rec, id_detalle_rec, id_rollo_rec);
            if (producto_r.getCod_orden_rec() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }
            // Extrae los detalles del producto
            String codigo = producto_r.getCodigo();
            String nombre = producto_r.getNombre();
            String peso = producto_r.getPeso();

            if (producto_r != null) {

                // Si el rollo no está anulado,tiene un destino de P y no tiene scla, lo agrega a la lista de productos
                String descripcionProducto = "Codigo: " + codigo + ", Id_rollo: " + id_rollo_rec + ", Cod_orden: " + cod_orden_rec + ", Peso: " + peso;
                productosList.add(descripcionProducto);
                String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Cod_orden: " + cod_orden_rec + ", Id_detalle : " + id_detalle_rec + ", Id_rollo: " + id_rollo_rec + ", Peso:" + peso;
                resumenList.add(resumeInventario);
                contadorRollos++;
                ListaRollosRecocidoInven.add(producto_r);
                adapter.notifyDataSetChanged();
                totalRollosTextView.setText(String.valueOf(contadorRollos));
                codigoCajaRecep.setText("");
                toastAcierto("Producto agregado");
                guardarEnBaseDeDatosNoConformeRecocidoCodigo2(producto_r);
            }

        } else {    // Si el rollo está anulado, muestra un mensaje de error
            toastError("Rollo anulado");
            cargarNuevo();
        }
    }

    //Metodo para escanear Recocido Codigo 3
    private void escanearProductoRecocidoCodigo3() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosRecocidoInven rollo : ListaRollosRecocidoInven) {
                String codigoLeido = rollo.getCod_orden_rec() + "-" + rollo.getId_detalle_rec() + "-" + rollo.getId_rollo_rec();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }
            // Extrae los datos del código de barras del rollo
            String cod_orden = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden", codigoCaja);
            String id_detalle = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle", codigoCaja);
            String id_rollo = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto_r = conexion.ObtenerRollosRecocidoCodigo3(Inventario_transaccion_alambre.this, cod_orden, id_detalle, id_rollo);
            if (producto_r.getCod_orden_rec() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }
            // Extrae los detalles del producto
            String codigo = producto_r.getCodigo();
            String nombre = producto_r.getNombre();
            String peso = producto_r.getPeso();

            if (producto_r != null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                // Si el rollo no está anulado,tiene un destino de P y no tiene scla, lo agrega a la lista de productos
                String descripcionProducto = "Codigo: " + codigo + ", Id_rollo: " + id_rollo + ", Cod_orden: " + cod_orden + ", Peso: " + peso;
                productosList.add(descripcionProducto);
                String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Cod_orden: " + cod_orden + ", Id_detalle : " + id_detalle + ", Id_rollo: " + id_rollo + ", Peso:" + peso;
                resumenList.add(resumeInventario);
                contadorRollos++;
                ListaRollosRecocidoInven.add(producto_r);
                adapter.notifyDataSetChanged();
                totalRollosTextView.setText(String.valueOf(contadorRollos));
                codigoCajaRecep.setText("");
                toastAcierto("Producto agregado");
                guardarEnBaseDeDatosCodigoRecocido3(producto_r);
            }
        } else {    // Si el rollo está anulado, muestra un mensaje de error
            toastError("Rollo anulado");
            cargarNuevo();
        }
    }

    //Metodo para escanear Recocido Codigo 2 no conforme
    private void escanearProductoNoConformeRecocidoCodigo3() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosRecocidoInven rollo : ListaRollosRecocidoInven) {
                String codigoLeido = rollo.getCod_orden_rec() + "-" + rollo.getId_detalle_rec() + "-" + rollo.getId_rollo_rec();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }
            // Extrae los datos del código de barras del rollo
            String cod_orden = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("cod_orden", codigoCaja);
            String id_detalle = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_detalle", codigoCaja);
            String id_rollo = gestion_alambronLn.extraerDatoCodigoBarrasRecocido("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto_r = conexion.ObtenerRollosRecocidoNoConformeCodigo3(Inventario_transaccion_alambre.this, cod_orden, id_detalle, id_rollo);
            if (producto_r.getCod_orden_rec() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }
            // Extrae los detalles del producto
            String codigo = producto_r.getCodigo();
            String nombre = producto_r.getNombre();
            String peso = producto_r.getPeso();

            if (producto_r != null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                // Si el rollo no está anulado,tiene un destino de P y no tiene scla, lo agrega a la lista de productos
                String descripcionProducto = "Codigo: " + codigo + ", Id_rollo: " + id_rollo + ", Cod_orden: " + cod_orden + ", Peso: " + peso;
                productosList.add(descripcionProducto);
                String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Cod_orden: " + cod_orden + ", Id_detalle : " + id_detalle + ", Id_rollo: " + id_rollo + ", Peso:" + peso;
                resumenList.add(resumeInventario);
                contadorRollos++;
                ListaRollosRecocidoInven.add(producto_r);
                adapter.notifyDataSetChanged();
                totalRollosTextView.setText(String.valueOf(contadorRollos));
                codigoCajaRecep.setText("");
                toastAcierto("Producto agregado");
                guardarEnBaseDeDatosRecocidoNoConfomeCodigo2(producto_r);
            }
        } else {    // Si el rollo está anulado, muestra un mensaje de error
            toastError("Rollo anulado");
            cargarNuevo();
        }
    }

    //Metodo para escanear Alambron Codigo 1 Bodega 1
    private void escanearProductoAlambronCodigo1() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosAlambronInven rollo : ListaRollosAlambronInven) {
                String codigoLeido = rollo.getNit_proveedor() + "-" + rollo.getNum_importacion() + "-" + rollo.getId_solicitud_det() + "-" + rollo.getNumero_rollo();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }

            // Extrae los datos del código de barras del rollo
            String nit_proveedor = gestion_alambronLn.extraerDatoCodigoBarras("nit_proveedor", codigoCaja);
            String num_importacion = gestion_alambronLn.extraerDatoCodigoBarras("num_importacion", codigoCaja);
            String id_solicitud_det = gestion_alambronLn.extraerDatoCodigoBarras("detalle", codigoCaja);
            String numero_rollo = gestion_alambronLn.extraerDatoCodigoBarras("num_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto_x = conexion.ObtenerRollosAlambronCodigo1(Inventario_transaccion_alambre.this, nit_proveedor, num_importacion, id_solicitud_det, numero_rollo);
            if (producto_x.getNit_proveedor() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }

            // Extrae los detalles del producto
            String codigo = producto_x.getCodigo();
            String costo_kilo = producto_x.getCosto_kilo();
            String peso = producto_x.getPeso();

            if (producto_x != null) {

                // Si el rollo no está anulado,tiene un destino de P y no tiene scla, lo agrega a la lista de productos
                String descripcionProducto = "Codigo: " + codigo + ", Nit_proveedor" + nit_proveedor + ", num_importacion" + num_importacion + ", Peso: " + peso;
                productosList.add(descripcionProducto);
                String resumeInventario = "Codigo: " + codigo + ", Nit_proveedor" + nit_proveedor + ", num_importacion" + num_importacion + ", Id_solicitud_det" + id_solicitud_det + ", numero_rollo" + numero_rollo + ", peso" + peso + ", costo_kilo" + costo_kilo;
                resumenList.add(resumeInventario);
                contadorRollos++;
                ListaRollosAlambronInven.add(producto_x);
                adapter.notifyDataSetChanged();
                totalRollosTextView.setText(String.valueOf(contadorRollos));
                codigoCajaRecep.setText("");
                toastAcierto("Producto agregado");
                guardarEnBaseDeDatosCodigoAlambron1(producto_x);
            }

        } else {    // Si el rollo está anulado, muestra un mensaje de error
            toastError("Rollo anulado");
            cargarNuevo();
        }
    }

    //Metodo para escanear Alambron Codigo 1 Bodega 2
    private void escanearProductoAlambronCodigo1Bodega2() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío
        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosAlambronInven rollo : ListaRollosAlambronInven) {
                String codigoLeido = rollo.getNit_proveedor() + "-" + rollo.getNum_importacion() + "-" + rollo.getId_solicitud_det()+"-"+rollo.getNumero_rollo();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }

            // Extrae los datos del código de barras del rollo
            String nit_proveedor = gestion_alambronLn.extraerDatoCodigoBarras("nit_proveedor", codigoCaja);
            String num_importacion = gestion_alambronLn.extraerDatoCodigoBarras("num_importacion", codigoCaja);
            String id_solicitud_det = gestion_alambronLn.extraerDatoCodigoBarras("detalle", codigoCaja);
            String numero_rollo = gestion_alambronLn.extraerDatoCodigoBarras("num_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto_x = conexion.ObtenerRollosAlambronCodigo2(Inventario_transaccion_alambre.this, nit_proveedor, num_importacion, id_solicitud_det,numero_rollo);
            if (producto_x.getNit_proveedor() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }else if (producto_x.getTipo_salida() != null) {
                // Si el rollo tiene tipo_salida no nulo, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete ya consumido");
                codigoCajaRecep.setText("");
                return;
            }

            // Extrae los detalles del producto
            String codigo = producto_x.getCodigo();
            String peso = producto_x.getPeso();
            String costo_kilo= producto_x.getCosto_kilo();

            if (producto_x != null) {

                AudioError();
                cargarNuevo();
                // Si el rollo no está anulado,tiene un destino de P y no tiene scla, lo agrega a la lista de productos
                String descripcionProducto = "Codigo: " + codigo + ", Nit_proveedor" +nit_proveedor +", num_importacion" + num_importacion +", Peso: " + peso;
                productosList.add(descripcionProducto);
                String resumeInventario = "Codigo: " + codigo + ", Nit_proveedor" +nit_proveedor +", num_importacion" + num_importacion + ", Id_solicitud_det" + id_solicitud_det + ", numero_rollo" +numero_rollo+ ", peso" + peso +", costo_kilo" +costo_kilo;
                resumenList.add(resumeInventario);
                contadorRollos++;
                ListaRollosAlambronInven.add(producto_x);
                adapter.notifyDataSetChanged();
                totalRollosTextView.setText(String.valueOf(contadorRollos));
                codigoCajaRecep.setText("");
                toastAcierto("Producto agregado");
                guardarEnBaseDeDatosCodigoAlambron2(producto_x);
            }

        } else {    // Si el rollo está anulado, muestra un mensaje de error
            toastError("Rollo anulado");
            cargarNuevo();
        }
    }

    //Metodo para escanear Trefilacion Codigo 3 no conforme
    private void escanearProductoNoConformeCodigo3() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío

        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosTrefiInvenNo_conforme rollo : ListaRollosTrefiInvenNo_conforme) {
                String codigoLeido = rollo.getCod_orden() + "-" + rollo.getId_detalle() + "-" + rollo.getId_rollo();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }
            // Extrae los datos del código de barras del rollo
            String cod_orden = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", codigoCaja);
            String id_detalle = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_detalle", codigoCaja);
            String id_rollo = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            productoNo_conforme = conexion.ObtenerRollosTrefi3InvNo_Conforme(Inventario_transaccion_alambre.this, cod_orden, id_detalle, id_rollo);
            if (productoNo_conforme.getCod_orden() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }

            // Extrae los detalles del producto
            String codigo = productoNo_conforme.getCodigo();
            String nombre = productoNo_conforme.getNombre();
            String consecutivo = productoNo_conforme.getConsecutivo();
            String id_rollo_tref = productoNo_conforme.getId_rollo();
            String operario = productoNo_conforme.getOperario();
            String diametro = productoNo_conforme.getDiametro();
            String materia_prima = productoNo_conforme.getMateria_prima();
            String colada = productoNo_conforme.getColada();
            String traccion = productoNo_conforme.getTraccion();
            String peso = productoNo_conforme.getPeso();
            String cod_orden_tref = productoNo_conforme.getCod_orden();
            String fecha_hora = productoNo_conforme.getFecha_hora();
            String cliente = productoNo_conforme.getCliente();
            String manual = productoNo_conforme.getManual();
            String anulado = productoNo_conforme.getAnulado();
            String destino = productoNo_conforme.getDestino();
            String no_conforme = productoNo_conforme.getNo_conforme();
            if (anulado == null) {

                if (destino == null) {

                    // Si el rollo no está anulado ni tiene un destino asignado, lo agrega a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Consecutivo: " + consecutivo + ", Id_rollo: " + id_rollo_tref + ", Cod_orden: " + cod_orden_tref + ", Peso: " + peso;
                    productosList.add(descripcionProducto);
                    String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Consecutivo: " + consecutivo + ", Id_rollo: " + id_rollo + ", Operario: " + operario + ", Diametro: " + diametro + ", Materia_prima: " + materia_prima + ", Colada: " + colada + ", Traccion: " + traccion + ", Peso: " + peso + ", Cod_orden: " + cod_orden + ", Fecha_hora: " + fecha_hora + ", Cliente: " + cliente + ", Manual:" + manual + ", No_conforme:" + no_conforme;
                    resumenList.add(resumeInventario);
                    contadorRollos++;
                    ListaRollosTrefiInvenNo_conforme.add(productoNo_conforme);
                    adapter.notifyDataSetChanged();
                    totalRollosTextView.setText(String.valueOf(contadorRollos));
                    codigoCajaRecep.setText("");
                    toastAcierto("Producto agregado");
                    guardarEnBaseDeDatosCodigo3No_conforme(productoNo_conforme);
                } else {

                    // Si el rollo tiene un destino asignado, muestra un mensaje de error y limpia la caja de texto
                    toastError("El rollo ya está en proceso: '" + destino + "'");
                    codigoCajaRecep.setText("");
                }
            } else {

                // Si el rollo está anulado, muestra un mensaje de error
                toastError("El rollo anulado");
            }
        }
    }

    //Metodo para escanear Trefilacion Codigo 3
    private void  escanearProductoCodigo3() {
        // Obtiene el código leído desde la caja de texto
        String codigoCaja = codigoCajaRecep.getText().toString().trim();
        // Verifica si el código leído está vacío

        if (!TextUtils.isEmpty(codigoCaja)) {
            // Comprueba si el rollo ya ha sido escaneado anteriormente
            for (RollosTrefiInven rollo : ListaRollosTrefiInven) {
                String codigoLeido = rollo.getCod_orden() + "-" + rollo.getId_detalle() + "-" + rollo.getId_rollo();
                if (codigoCaja.equals(codigoLeido)) {
                    // Si el rollo ya ha sido escaneado, muestra un mensaje de error y limpia la caja de texto
                    toastError("Rollo ya leído");
                    codigoCajaRecep.setText("");
                    return;
                }
            }

            // Extrae los datos del código de barras del rollo
            String cod_orden = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("cod_orden", codigoCaja);
            String id_detalle = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_detalle", codigoCaja);
            String id_rollo = gestion_alambronLn.extraerDatoCodigoBarrasTrefilacion("id_rollo", codigoCaja);
            // Obtiene la información del rollo desde la base de datos
            producto = conexion.ObtenerRollosTrefiInvCodigo(Inventario_transaccion_alambre.this, cod_orden, id_detalle, id_rollo);
            if (producto.getCod_orden() == null) {
                // Si el rollo no se encuentra en la base de datos, muestra un mensaje de error y limpia la caja de texto
                toastError("Tiquete no pertenece a esta bodega");
                codigoCajaRecep.setText("");
                return;
            }

            // Extrae los detalles del producto
            String codigo = producto.getCodigo();
            String nombre = producto.getNombre();
            String consecutivo = producto.getConsecutivo();
            String id_rollo_tref = producto.getId_rollo();
            String operario = producto.getOperario();
            String diametro = producto.getDiametro();
            String materia_prima = producto.getMateria_prima();
            String colada = producto.getColada();
            String traccion = producto.getTraccion();
            String peso = producto.getPeso();
            String cod_orden_tref = producto.getCod_orden();
            String fecha_hora = producto.getFecha_hora();
            String cliente = producto.getCliente();
            String manual = producto.getManual();
            String anulado = producto.getAnulado();
            String destino = producto.getDestino();
            if (anulado == null) {

                if (destino == null) {

                    // Si el rollo no está anulado ni tiene un destino asignado, lo agrega a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Consecutivo: " + consecutivo + ", Id_rollo: " + id_rollo_tref + ", Cod_orden: " + cod_orden_tref + ", Peso: " + peso;
                    productosList.add(descripcionProducto);
                    String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Consecutivo: " + consecutivo + ", Id_rollo: " + id_rollo + ", Operario: " + operario + ", Diametro: " + diametro + ", Materia_prima: " + materia_prima + ", Colada: " + colada + ", Traccion: " + traccion + ", Peso: " + peso + ", Cod_orden: " + cod_orden + ", Fecha_hora: " + fecha_hora + ", Cliente: " + cliente + ", Manual:" + manual;
                    resumenList.add(resumeInventario);
                    contadorRollos++;
                    ListaRollosTrefiInven.add(producto);
                    adapter.notifyDataSetChanged();
                    totalRollosTextView.setText(String.valueOf(contadorRollos));
                    codigoCajaRecep.setText("");
                    toastAcierto("Producto agregado");
                    guardarEnBaseDeDatosCodigo3(producto);
                } else {

                    // Si el rollo tiene un destino asignado, muestra un mensaje de error y limpia la caja de texto
                    toastError("El rollo ya está en proceso: '" + destino + "'");
                    codigoCajaRecep.setText("");
                }
            } else {

                // Si el rollo está anulado, muestra un mensaje de error
                toastError("El rollo anulado");
            }
        }
    }

    //Metodo averiguar si existe en base de Trefilacion Codigo 2
    private boolean existeEnBaseDeDatosCodigo2(String codigo, String codOrden, String idDetalle, String idRollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_TREFILACION_CODIGO_2 +
                " WHERE " + DBHelper.COLUMN_CODIGO + "=? AND " +
                DBHelper.COLUMN_COD_ORDEN + "=? AND " +
                DBHelper.COLUMN_ID_DETALLE + "=? AND " +
                DBHelper.COLUMN_ID_ROLLO + "=?", new String[]{codigo, codOrden, idDetalle, idRollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;
    }

    //Metodo averiguar si existe en base de Trefilacion Codigo 2 no conforme
    private boolean existeEnBaseDeDatosCodigo2No_conforme(String codigo, String codOrden, String idDetalle, String idRollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_TREFILACION_CODIGO_2_NO_CONFORME +
                " WHERE " + DBHelper.COLUMN_CODIGO_F + "=? AND " +
                DBHelper.COLUMN_COD_ORDEN_F + "=? AND " +
                DBHelper.COLUMN_ID_DETALLE_F + "=? AND " +
                DBHelper.COLUMN_ID_ROLLO_F + "=?", new String[]{codigo, codOrden, idDetalle, idRollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;
    }

    //Metodo averiguar si existe en base de Trefilacion Codigo 3
    private boolean existeEnBaseDeDatosCodigo3(String codigo, String codOrden, String idDetalle, String idRollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_TREFILACION_CODIGO_3 +
                " WHERE " + DBHelper.COLUMN_CODIGO + "=? AND " +
                DBHelper.COLUMN_COD_ORDEN + "=? AND " +
                DBHelper.COLUMN_ID_DETALLE + "=? AND " +
                DBHelper.COLUMN_ID_ROLLO + "=?", new String[]{codigo, codOrden, idDetalle, idRollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;
    }

    //Metodo averiguar si existe en base de Destino Galvanizado Codigo 2
    private boolean existeEnBaseDeDatosDestinoGalvaCodigo2(String codigo, String codOrden, String idDetalle, String idRollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_DESTINO_GALVANIZADO_CODIGO_2 +
                " WHERE " + DBHelper.COLUMN_CODIGO_G + "=? AND " +
                DBHelper.COLUMN_COD_ORDEN_G + "=? AND " +
                DBHelper.COLUMN_ID_DETALLE_G + "=? AND " +
                DBHelper.COLUMN_ID_ROLLO_G + "=?", new String[]{codigo, codOrden, idDetalle, idRollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;
    }

    //Metodo averiguar si existe en base de Puntilleria Codigo 2
    private boolean existeEnBaseDeDatosPuntCodigo2(String codigo, String codOrden, String idDetalle, String idRollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_PUNTILLERIA_CODIGO_2 +
                " WHERE " + DBHelper.COLUMN_CODIGO_P + "=? AND " +
                DBHelper.COLUMN_COD_ORDEN_P + "=? AND " +
                DBHelper.COLUMN_ID_DETALLE_P + "=? AND " +
                DBHelper.COLUMN_ID_ROLLO_P + "=?", new String[]{codigo, codOrden, idDetalle, idRollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;

    }

    //Metodo averiguar si existe en base de Recocido Codigo 2
    private boolean existeEnBaseDeDatosRecocidoCodigo2(String codigo, String codOrden, String idDetalle, String idRollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_2 +
                " WHERE " + DBHelper.COLUMN_CODIGO_R + "=? AND " +
                DBHelper.COLUMN_COD_ORDEN_R + "=? AND " +
                DBHelper.COLUMN_ID_DETALLE_R + "=? AND " +
                DBHelper.COLUMN_ID_ROLLO_R + "=?", new String[]{codigo, codOrden, idDetalle, idRollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;

    }

    //Metodo averiguar si existe en base de Alambron Codigo 1 Bodega 1
    private boolean existeEnBaseDeDatosalambronCodigo1(String codigo , String nit_proveedor, String detalle, String numero_rollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_ALAMBRON_CODIGO_1 +
                " WHERE " + DBHelper.COLUMN_NIT_PROVEEDOR_X + "=? AND " +
                DBHelper.COLUMN_NUM_IMPORTACION_X + "=? AND " +
                DBHelper.COLUMN_ID_SOLICITUD_DET_X + "=? AND " +
                DBHelper.COLUMN_NUMERO_ROLLO_X + "=?", new String[]{codigo, nit_proveedor, detalle, numero_rollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;

    }

    //Metodo averiguar si existe en base de Alambron Codigo 1 Bodega 2
    private boolean existeEnBaseDeDatosalambronCodigo2(String codigo , String nit_proveedor, String detalle, String numero_rollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_ALAMBRON_CODIGO_2 +
                " WHERE " + DBHelper.COLUMN_NIT_PROVEEDOR_Y + "=? AND " +
                DBHelper.COLUMN_NUM_IMPORTACION_Y + "=? AND " +
                DBHelper.COLUMN_ID_SOLICITUD_DET_Y + "=? AND " +
                DBHelper.COLUMN_NUMERO_ROLLO_Y + "=?", new String[]{codigo, nit_proveedor, detalle, numero_rollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;

    }

    //Metodo averiguar si existe en base de Recocido Codigo 2 no conforme
    private boolean existeEnBaseDeDatosRecocidoNoConformeCodigo2(String codigo, String codOrden, String idDetalle, String idRollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_2_NO_CONFORME +
                " WHERE " + DBHelper.COLUMN_CODIGO_R + "=? AND " +
                DBHelper.COLUMN_COD_ORDEN_RT + "=? AND " +
                DBHelper.COLUMN_ID_DETALLE_RT + "=? AND " +
                DBHelper.COLUMN_ID_ROLLO_RT + "=?", new String[]{codigo, codOrden, idDetalle, idRollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;

    }

    //Metodo averiguar si existe en base de Recocido Codigo 3
    private boolean existeEnBaseDeDatosRecocidoCodigo3(String codigo, String codOrden, String idDetalle, String idRollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_3 +
                " WHERE " + DBHelper.COLUMN_CODIGO_D + "=? AND " +
                DBHelper.COLUMN_COD_ORDEN_D + "=? AND " +
                DBHelper.COLUMN_ID_DETALLE_D + "=? AND " +
                DBHelper.COLUMN_ID_ROLLO_D + "=?", new String[]{codigo, codOrden, idDetalle, idRollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;

    }

    //Metodo averiguar si existe en base de Recocido Codigo 3 no conforme
    private boolean existeEnBaseDeDatosRecocidoNoConformeCodigo3(String codigo, String codOrden, String idDetalle, String idRollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_3_NO_CONFORME +
                " WHERE " + DBHelper.COLUMN_CODIGO_DT + "=? AND " +
                DBHelper.COLUMN_COD_ORDEN_DT + "=? AND " +
                DBHelper.COLUMN_ID_DETALLE_DT + "=? AND " +
                DBHelper.COLUMN_ID_ROLLO_DT + "=?", new String[]{codigo, codOrden, idDetalle, idRollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;

    }

    //Metodo averiguar si existe en base de Galvanizado Codigo 2
    private boolean existeEnBaseDeDatosGalvCodigo2(String codigo, String cod_orden, String nro_rollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_GALVANIZADO_CODIGO_2 +
                " WHERE " + DBHelper.COLUMN_CODIGO_C + "=? AND " +
                DBHelper.COLUMN_NRO_ORDEN_C + "=? AND " +
                DBHelper.COLUMN_NRO_ROLLO_C + "=?", new String[]{codigo, cod_orden, nro_rollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;
    }

    //Metodo averiguar si existe en base de Galvanizado Codigo 12
    private boolean existeEnBaseDeDatosGalvCodigo12(String codigo, String cod_orden, String nro_rollo) {
        // Obtiene una instancia legible de la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Realiza una consulta SQL para verificar la existencia del rollo en la tabla correspondiente
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_GALVANIZADO_CODIGO_2_BODEGA_12 +
                " WHERE " + DBHelper.COLUMN_CODIGO_C + "=? AND " +
                DBHelper.COLUMN_NRO_ORDEN_C + "=? AND " +
                DBHelper.COLUMN_NRO_ROLLO_C + "=?", new String[]{codigo, cod_orden, nro_rollo});
        // Verifica si el cursor contiene algún resultado
        boolean exists = cursor.getCount() > 0;
        // Cierra el cursor para liberar recursos
        cursor.close();
        // Devuelve true si el rollo existe en la base de datos, de lo contrario, devuelve false
        return exists;
    }

    //Metodo obtener los datos de la BD interna
    private Cursor obtenerDatosEscaneados() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        // Determina el área y la bodega para seleccionar la tabla correspondiente de la base de datos
        switch (area) {
            case "ALAMBRÓN":
                switch (bodega) {
                    case 1:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_ALAMBRON_CODIGO_1, null);
                        break;
                    case 2:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_ALAMBRON_CODIGO_2, null);
                        break;

                }
                break;
            case "TREFILACIÓN":
                switch (bodega) {
                    case 2:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_TREFILACION_CODIGO_2, null);
                        break;
                    case 3:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_TREFILACION_CODIGO_3, null);
                        break;
                    case 4:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_TREFILACION_CODIGO_2_NO_CONFORME, null);
                        break;
                    case 5:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_TREFILACION_CODIGO_3_NO_CONFORME, null);
                        break;
                }
                break;
            case "RECOCIDO":
                switch (bodega) {
                    case 2:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_2, null);
                        break;
                    case 3:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_3, null);
                        break;

                    case 4:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_2_NO_CONFORME, null);
                        break;
                    case 5:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_RECOCIDO_CODIGO_3_NO_CONFORME, null);
                        break;
                }

                break;
            case "GALVANIZADO":
                switch (bodega) {
                    case 2:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_GALVANIZADO_CODIGO_2, null);
                        break;
                    case 11:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_DESTINO_GALVANIZADO_CODIGO_2, null);
                        break;
                    case 12:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_GALVANIZADO_CODIGO_2_BODEGA_12, null);
                        break;

                }
                break;
            case "PUNTILLERIA":
                switch (bodega) {
                    case 12:
                        cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_PUNTILLERIA_CODIGO_2, null);
                        break;

                }
            default:
                // Manejar caso por defecto, como lanzar una excepción
                break;
        }

        // Devuelve el cursor con los datos escaneados de la tabla correspondiente
        return cursor;
    }

    // Método para eliminar productos de la tabla de puntillería con código 2
    private void eliminarProductoPuntilleriaCodigo2() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de puntillería con código 2
                            db.delete(DBHelper.TABLE_PUNTILLERIA_CODIGO_2, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarDataPuntilleria(); // Mostrar nuevamente los datos de la puntillería en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });

        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Destino Galvanizado Codigo 2
    private void eliminarProductoDestinoGalvanizadoCodigo2() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de destino galvanizado con código 2
                            db.delete(DBHelper.TABLE_DESTINO_GALVANIZADO_CODIGO_2, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarDataDestinoGalvanizado(); // Mostrar nuevamente los datos de destino galvanizado en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });

        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Galvanizado Codigo 2
    private void eliminarProductoGalvanizadoCodigo2() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de galvanizado con código 2
                            db.delete(DBHelper.TABLE_GALVANIZADO_CODIGO_2, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarDataGalvanizado(); // Mostrar nuevamente los datos de galvanizado en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });

        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Galvanizado Codigo 12
    private void eliminarProductoGalvanizadoCodigo12() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de galvanizado con código 2
                            db.delete(DBHelper.TABLE_GALVANIZADO_CODIGO_2_BODEGA_12, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarDataGalvanizado(); // Mostrar nuevamente los datos de galvanizado en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Trefilacion Codigo 3
    private void eliminarTodosLosProductosCodigo3() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 3
                            db.delete(DBHelper.TABLE_TREFILACION_CODIGO_3, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Trefilacion Codigo 3 no conforme
    private void  eliminarTodosLosProductosNoConformesCodigo3() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 3
                            db.delete(DBHelper.TABLE_TREFILACION_CODIGO_3, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Trefilacion Codigo 2
    private void eliminarTodosLosProductosCodigo2() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 2
                            db.delete(DBHelper.TABLE_TREFILACION_CODIGO_2, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Trefilacion Codigo 2 no conforme
    private void  eliminarTodosLosProductosNoConformesCodigo2() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 2
                            db.delete(DBHelper.TABLE_TREFILACION_CODIGO_2, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Recocido Codigo 2
    private void eliminarTodosLosProductosRecocidoCodigo2() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 2
                            db.delete(DBHelper.TABLE_RECOCIDO_CODIGO_2, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Recocido Codigo 2 no conforme
    private void eliminarTodosLosProductoRecocidosNoConformesCodigo2() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 2
                            db.delete(DBHelper.TABLE_RECOCIDO_CODIGO_2_NO_CONFORME, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Recocido Codigo 3
    private void eliminarTodosLosProductosRecocidoCodigo3() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 2
                            db.delete(DBHelper.TABLE_RECOCIDO_CODIGO_3, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Recocido Codigo 3 no conformes
    private void eliminarTodosLosProductoRecocidosNoConformesCodigo3() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 2
                            db.delete(DBHelper.TABLE_RECOCIDO_CODIGO_3_NO_CONFORME, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Alambron Codigo 1 Bodega 1
    private void eliminarTodosLosProductosAlambronCodigo1() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 2
                            db.delete(DBHelper.TABLE_ALAMBRON_CODIGO_1, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para eliminar productos de la tabla de Alambron Codigo 1 Bodega 2
    private void eliminarTodosLosProductosAlambronCodigo1Bodega2() {
        // Construcción del diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Quiere borrar esta información?") // Mensaje de confirmación
                .setCancelable(false) // No se permite cancelar el diálogo haciendo clic fuera de él
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() { // Botón "Sí" para confirmar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtener una instancia de la base de datos en modo escritura
                        try {
                            // Eliminar todos los productos de la tabla de trefilación con código 2
                            db.delete(DBHelper.TABLE_ALAMBRON_CODIGO_2, null, null);
                            contadorRollos = 0; // Restablecer el contador de rollos a cero
                            productosList.clear(); // Limpiar la lista de productos
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                            totalRollosTextView.setText(String.valueOf(contadorRollos)); // Actualizar el TextView que muestra el total de rollos
                            mostrarData(); // Mostrar nuevamente los datos en la interfaz
                            toastAcierto("Se eliminaron todos los productos"); // Mostrar un mensaje de éxito al usuario
                        } catch (Exception e) {
                            // Manejar cualquier excepción que pueda ocurrir al eliminar productos de la base de datos
                            Log.e("DB_ERROR", "Error al eliminar productos de la base de datos: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
                            if (db != null) {
                                db.close();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { // Botón "No" para cancelar la acción
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Cancelar el diálogo
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Método para guardar un producto en la base de datos con código 2
    private boolean guardarEnBaseDeDatosCodigo2(RollosTrefiInven producto) {
        // Obtener los atributos del producto
        String codigo = producto.getCodigo();
        String codOrden = producto.getCod_orden();
        String idDetalle = producto.getId_detalle();
        String idRollo = producto.getId_rollo();

        // Verificar si el producto ya existe en la base de datos por la combinación de códigos
        if (existeEnBaseDeDatosCodigo2(codigo, codOrden, idDetalle, idRollo)) {
            // Si el producto ya existe, retornar falso y no realizar la inserción
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se puede abrir la base de datos para escritura, retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO, producto.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE, producto.getNombre());
            values.put(DBHelper.COLUMN_CONSECUTIVO, producto.getConsecutivo());
            values.put(DBHelper.COLUMN_ID_ROLLO, producto.getId_rollo());
            values.put(DBHelper.COLUMN_OPERARIO, producto.getOperario());
            values.put(DBHelper.COLUMN_DIAMETRO, producto.getDiametro());
            values.put(DBHelper.COLUMN_MATERIA_PRIMA, producto.getMateria_prima());
            values.put(DBHelper.COLUMN_COLADA, producto.getColada());
            values.put(DBHelper.COLUMN_TRACCION, producto.getTraccion());
            values.put(DBHelper.COLUMN_PESO, producto.getPeso());
            values.put(DBHelper.COLUMN_COD_ORDEN, producto.getCod_orden());
            values.put(DBHelper.COLUMN_FECHA_HORA, producto.getFecha_hora());
            values.put(DBHelper.COLUMN_CLIENTE, producto.getCliente());
            values.put(DBHelper.COLUMN_MANUAL, producto.getManual());
            values.put(DBHelper.COLUMN_ANULADO, producto.getAnulado());
            values.put(DBHelper.COLUMN_DESTINO, producto.getDestino());

            // Insertar el producto en la tabla de trefilación con código 2
            long result = db.insert(DBHelper.TABLE_TREFILACION_CODIGO_2, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenados();

            // Verificar si la inserción fue exitosa
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto en la base de datos con código 2 no conforme
    private boolean guardarEnBaseDeDatosCodigo2No_conforme(RollosTrefiInvenNo_conforme productoNo_conforme) {
        // Obtener los atributos del producto
        String codigo = productoNo_conforme.getCodigo();
        String codOrden = productoNo_conforme.getCod_orden();
        String idDetalle = productoNo_conforme.getId_detalle();
        String idRollo = productoNo_conforme.getId_rollo();

        // Verificar si el producto ya existe en la base de datos por la combinación de códigos
        if (existeEnBaseDeDatosCodigo2No_conforme(codigo, codOrden, idDetalle, idRollo)) {
            // Si el producto ya existe, retornar falso y no realizar la inserción
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se puede abrir la base de datos para escritura, retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_F, productoNo_conforme.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_F, productoNo_conforme.getNombre());
            values.put(DBHelper.COLUMN_CONSECUTIVO_F, productoNo_conforme.getConsecutivo());
            values.put(DBHelper.COLUMN_ID_ROLLO_F, productoNo_conforme.getId_rollo());
            values.put(DBHelper.COLUMN_OPERARIO_F, productoNo_conforme.getOperario());
            values.put(DBHelper.COLUMN_DIAMETRO_F, productoNo_conforme.getDiametro());
            values.put(DBHelper.COLUMN_MATERIA_PRIMA_F, productoNo_conforme.getMateria_prima());
            values.put(DBHelper.COLUMN_COLADA_F, productoNo_conforme.getColada());
            values.put(DBHelper.COLUMN_TRACCION_F, productoNo_conforme.getTraccion());
            values.put(DBHelper.COLUMN_PESO_F, productoNo_conforme.getPeso());
            values.put(DBHelper.COLUMN_COD_ORDEN_F, productoNo_conforme.getCod_orden());
            values.put(DBHelper.COLUMN_FECHA_HORA_F, productoNo_conforme.getFecha_hora());
            values.put(DBHelper.COLUMN_CLIENTE_F, productoNo_conforme.getCliente());
            values.put(DBHelper.COLUMN_MANUAL_F, productoNo_conforme.getManual());
            values.put(DBHelper.COLUMN_ANULADO_F, productoNo_conforme.getAnulado());
            values.put(DBHelper.COLUMN_DESTINO_F, productoNo_conforme.getDestino());
            values.put(DBHelper.COLUMN_NO_CONFORME_F, productoNo_conforme.getNo_conforme());

            // Insertar el producto en la tabla de trefilación con código 2
            long result = db.insert(DBHelper.TABLE_TREFILACION_CODIGO_2_NO_CONFORME, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosNo_conformes();

            // Verificar si la inserción fue exitosa
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + productoNo_conforme.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto en la base de datos con código 3
    private boolean guardarEnBaseDeDatosCodigo3(RollosTrefiInven producto) {
        // Obtener los atributos del producto
        String codigo = producto.getCodigo();
        String codOrden = producto.getCod_orden();
        String idDetalle = producto.getId_detalle();
        String idRollo = producto.getId_rollo();

        // Verificar si el producto ya existe en la base de datos por la combinación de códigos
        if (existeEnBaseDeDatosCodigo3(codigo, codOrden, idDetalle, idRollo)) {
            // Si el producto ya existe, retornar falso y no realizar la inserción
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se puede abrir la base de datos para escritura, retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO, producto.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE, producto.getNombre());
            values.put(DBHelper.COLUMN_CONSECUTIVO, producto.getConsecutivo());
            values.put(DBHelper.COLUMN_ID_ROLLO, producto.getId_rollo());
            values.put(DBHelper.COLUMN_OPERARIO, producto.getOperario());
            values.put(DBHelper.COLUMN_DIAMETRO, producto.getDiametro());
            values.put(DBHelper.COLUMN_MATERIA_PRIMA, producto.getMateria_prima());
            values.put(DBHelper.COLUMN_COLADA, producto.getColada());
            values.put(DBHelper.COLUMN_TRACCION, producto.getTraccion());
            values.put(DBHelper.COLUMN_PESO, producto.getPeso());
            values.put(DBHelper.COLUMN_COD_ORDEN, producto.getCod_orden());
            values.put(DBHelper.COLUMN_FECHA_HORA, producto.getFecha_hora());
            values.put(DBHelper.COLUMN_CLIENTE, producto.getCliente());
            values.put(DBHelper.COLUMN_MANUAL, producto.getManual());
            values.put(DBHelper.COLUMN_ANULADO, producto.getAnulado());
            values.put(DBHelper.COLUMN_DESTINO, producto.getDestino());

            // Insertar el producto en la tabla de trefilación con código 3
            long result = db.insert(DBHelper.TABLE_TREFILACION_CODIGO_3, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenados();

            // Verificar si la inserción fue exitosa
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto en la base de datos con código 3 no conforme
    private boolean guardarEnBaseDeDatosCodigo3No_conforme(RollosTrefiInvenNo_conforme productoNo_conforme) {
        // Obtener los atributos del producto
        String codigo = productoNo_conforme.getCodigo();
        String codOrden = productoNo_conforme.getCod_orden();
        String idDetalle = productoNo_conforme.getId_detalle();
        String idRollo = productoNo_conforme.getId_rollo();

        // Verificar si el producto ya existe en la base de datos por la combinación de códigos
        if (existeEnBaseDeDatosCodigo3(codigo, codOrden, idDetalle, idRollo)) {
            // Si el producto ya existe, retornar falso y no realizar la inserción
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se puede abrir la base de datos para escritura, retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_FT, productoNo_conforme.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_FT, productoNo_conforme.getNombre());
            values.put(DBHelper.COLUMN_CONSECUTIVO_FT, productoNo_conforme.getConsecutivo());
            values.put(DBHelper.COLUMN_ID_ROLLO_FT, productoNo_conforme.getId_rollo());
            values.put(DBHelper.COLUMN_OPERARIO_FT, productoNo_conforme.getOperario());
            values.put(DBHelper.COLUMN_DIAMETRO_FT, productoNo_conforme.getDiametro());
            values.put(DBHelper.COLUMN_MATERIA_PRIMA_FT, productoNo_conforme.getMateria_prima());
            values.put(DBHelper.COLUMN_COLADA_FT, productoNo_conforme.getColada());
            values.put(DBHelper.COLUMN_TRACCION_FT, productoNo_conforme.getTraccion());
            values.put(DBHelper.COLUMN_PESO_FT, productoNo_conforme.getPeso());
            values.put(DBHelper.COLUMN_COD_ORDEN_FT, productoNo_conforme.getCod_orden());
            values.put(DBHelper.COLUMN_FECHA_HORA_FT, productoNo_conforme.getFecha_hora());
            values.put(DBHelper.COLUMN_CLIENTE_FT, productoNo_conforme.getCliente());
            values.put(DBHelper.COLUMN_MANUAL_FT, productoNo_conforme.getManual());
            values.put(DBHelper.COLUMN_ANULADO_FT, productoNo_conforme.getAnulado());
            values.put(DBHelper.COLUMN_DESTINO_FT, productoNo_conforme.getDestino());
            values.put(DBHelper.COLUMN_NO_CONFORME_FT, productoNo_conforme.getNo_conforme());

            // Insertar el producto en la tabla de trefilación con código 3
            long result = db.insert(DBHelper.TABLE_TREFILACION_CODIGO_3_NO_CONFORME, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenados();

            // Verificar si la inserción fue exitosa
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + productoNo_conforme.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Destino Galvanizado Codigo 2
    private boolean guardarEnBaseDeDatosCodigo2DestinoGalva(RollosMPGalvInven producto) {
        // Obtener los atributos del producto
        String codigo = producto.getCodigo();
        String codOrden = producto.getCod_orden();
        String idDetalle = producto.getId_detalle();
        String idRollo = producto.getId_rollo();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosDestinoGalvaCodigo2(codigo, codOrden, idDetalle, idRollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_G, producto.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_G, producto.getNombre());
            values.put(DBHelper.COLUMN_ID_DETALLE_G, idDetalle);
            values.put(DBHelper.COLUMN_ID_ROLLO_G, producto.getId_rollo());
            values.put(DBHelper.COLUMN_TRASLADO_G, producto.getTraslado());
            values.put(DBHelper.COLUMN_PESO_G, producto.getPeso());
            values.put(DBHelper.COLUMN_COD_ORDEN_G, producto.getCod_orden());
            values.put(DBHelper.COLUMN_FECHA_HORA_G, producto.getFecha_hora());
            values.put(DBHelper.COLUMN_MANUAL_G, producto.getManuales());
            values.put(DBHelper.COLUMN_ANULADO_G, producto.getAnulado());
            values.put(DBHelper.COLUMN_DESTINO_G, producto.getDestino());

            // Insertar el producto en la tabla de destino de galvanizado con código 2
            long result = db.insert(DBHelper.TABLE_DESTINO_GALVANIZADO_CODIGO_2, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosDestinoGalvanizado();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Puntilleria Codigo 2
    private boolean guardarEnBaseDeDatosCodigoPunt2(RollosMPPuntInven producto) {
        // Obtener los atributos del producto
        String codigo = producto.getCodigo();
        String codOrden = producto.getCod_orden();
        String idDetalle = producto.getId_detalle();
        String idRollo = producto.getId_rollo();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosPuntCodigo2(codigo, codOrden, idDetalle, idRollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_P, producto.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_p, producto.getNombre());
            values.put(DBHelper.COLUMN_ID_DETALLE_P, idDetalle);
            values.put(DBHelper.COLUMN_ID_ROLLO_P, producto.getId_rollo());
            values.put(DBHelper.COLUMN_TRASLADO_P, producto.getTraslado());
            values.put(DBHelper.COLUMN_PESO_P, producto.getPeso());
            values.put(DBHelper.COLUMN_COD_ORDEN_P, producto.getCod_orden());
            values.put(DBHelper.COLUMN_FECHA_HORA_P, producto.getFecha_hora());
            values.put(DBHelper.COLUMN_MANUAL_P, producto.getManuales());
            values.put(DBHelper.COLUMN_ANULADO_P, producto.getAnulado());
            values.put(DBHelper.COLUMN_DESTINO_P, producto.getDestino());

            // Insertar el producto en la tabla de puntillería con código 2
            long result = db.insert(DBHelper.TABLE_PUNTILLERIA_CODIGO_2, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosPuntilleria();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Recocido Codigo 2
    private boolean guardarEnBaseDeDatosCodigoRecocido2(RollosRecocidoInven producto_r) {
        // Obtener los atributos del producto
        String codigo = producto_r.getCodigo();
        String codOrden = producto_r.getCod_orden_rec();
        String idDetalle = producto_r.getId_detalle_rec();
        String idRollo = producto_r.getId_rollo_rec();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosRecocidoCodigo2(codigo, codOrden, idDetalle, idRollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto_r.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_R, producto_r.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_R, producto_r.getNombre());
            values.put(DBHelper.COLUMN_COD_ORDEN_R, producto_r.getCod_orden_rec());
            values.put(DBHelper.COLUMN_ID_DETALLE_R, producto_r.getId_detalle_rec());
            values.put(DBHelper.COLUMN_ID_ROLLO_R, producto_r.getId_rollo_rec());
            values.put(DBHelper.COLUMN_PESO_R, producto_r.getPeso());



            // Insertar el producto en la tabla de recocido con código 2
            long result = db.insert(DBHelper.TABLE_RECOCIDO_CODIGO_2, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosRecocidoCodigo2();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto_r.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Recocido Codigo 2 no conforme
    private boolean guardarEnBaseDeDatosNoConformeRecocidoCodigo2(RollosRecocidoInven producto_r) {
        // Obtener los atributos del producto
        String codigo = producto_r.getCodigo();
        String codOrden = producto_r.getCod_orden_rec();
        String idDetalle = producto_r.getId_detalle_rec();
        String idRollo = producto_r.getId_rollo_rec();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosRecocidoNoConformeCodigo2(codigo, codOrden, idDetalle, idRollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto_r.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_RT, producto_r.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_RT, producto_r.getNombre());
            values.put(DBHelper.COLUMN_COD_ORDEN_RT, producto_r.getCod_orden_rec());
            values.put(DBHelper.COLUMN_ID_DETALLE_RT, producto_r.getId_detalle_rec());
            values.put(DBHelper.COLUMN_ID_ROLLO_RT, producto_r.getId_rollo_rec());
            values.put(DBHelper.COLUMN_PESO_RT, producto_r.getPeso());



            // Insertar el producto en la tabla de recocido con código 2
            long result = db.insert(DBHelper.TABLE_RECOCIDO_CODIGO_2_NO_CONFORME, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosRecocidoNoConformeCodigo2();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto_r.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Alambron Codigo 1 Bodega 1
    private boolean guardarEnBaseDeDatosCodigoAlambron1(RollosAlambronInven producto_x) {
        // Obtener los atributos del producto
        String codigo = producto_x.getCodigo();
        String nit_proveedor = producto_x.getNit_proveedor();
        String detalle = producto_x.getId_solicitud_det();
        String numero_rollo = producto_x.getNumero_rollo();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosalambronCodigo1(codigo, nit_proveedor, detalle, numero_rollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto_x.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_NIT_PROVEEDOR_X, producto_x.getNit_proveedor());
            values.put(DBHelper.COLUMN_NUM_IMPORTACION_X, producto_x.getNum_importacion());
            values.put(DBHelper.COLUMN_ID_SOLICITUD_DET_X, producto_x.getId_solicitud_det());
            values.put(DBHelper.COLUMN_NUMERO_ROLLO_X, producto_x.getNumero_rollo());
            values.put(DBHelper.COLUMN_PESO_X, producto_x.getPeso());
            values.put(DBHelper.COLUMN_CODIGO_X, producto_x.getCodigo());

            values.put(DBHelper.COLUMN_COSTO_KILO_X, producto_x.getCosto_kilo());




            // Insertar el producto en la tabla de recocido con código 2
            long result = db.insert(DBHelper.TABLE_ALAMBRON_CODIGO_1, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosAlambronCodigo1();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto_x.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Alambron Codigo 1 Bodega 2
    private boolean guardarEnBaseDeDatosCodigoAlambron2(RollosAlambronInven producto_x) {
        // Obtener los atributos del producto
        String codigo = producto_x.getCodigo();
        String nit_proveedor = producto_x.getNit_proveedor();
        String detalle = producto_x.getId_solicitud_det();
        String numero_rollo = producto_x.getNumero_rollo();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosalambronCodigo2(codigo, nit_proveedor, detalle, numero_rollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto_x.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_NIT_PROVEEDOR_X, producto_x.getNit_proveedor());
            values.put(DBHelper.COLUMN_NUM_IMPORTACION_X, producto_x.getNum_importacion());
            values.put(DBHelper.COLUMN_ID_SOLICITUD_DET_X, producto_x.getId_solicitud_det());
            values.put(DBHelper.COLUMN_NUMERO_ROLLO_X, producto_x.getNumero_rollo());
            values.put(DBHelper.COLUMN_PESO_X, producto_x.getPeso());
            values.put(DBHelper.COLUMN_CODIGO_X, producto_x.getCodigo());

            values.put(DBHelper.COLUMN_COSTO_KILO_X, producto_x.getCosto_kilo());




            // Insertar el producto en la tabla de recocido con código 2
            long result = db.insert(DBHelper.TABLE_ALAMBRON_CODIGO_2, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosAlambronCodigo2();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto_x.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Recocido Codigo 3
    private boolean guardarEnBaseDeDatosCodigoRecocido3(RollosRecocidoInven producto_r) {
        // Obtener los atributos del producto
        String codigo = producto_r.getCodigo();
        String codOrden = producto_r.getCod_orden_rec();
        String idDetalle = producto_r.getId_detalle_rec();
        String idRollo = producto_r.getId_rollo_rec();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosRecocidoCodigo3(codigo, codOrden, idDetalle, idRollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto_r.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_D, producto_r.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_D, producto_r.getNombre());
            values.put(DBHelper.COLUMN_COD_ORDEN_D, producto_r.getCod_orden_rec());
            values.put(DBHelper.COLUMN_ID_DETALLE_D, producto_r.getId_detalle_rec());
            values.put(DBHelper.COLUMN_ID_ROLLO_D, producto_r.getId_rollo_rec());
            values.put(DBHelper.COLUMN_PESO_D, producto_r.getPeso());



            // Insertar el producto en la tabla de puntillería con código 2
            long result = db.insert(DBHelper.TABLE_RECOCIDO_CODIGO_3, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosRecocidoCodigo3();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto_r.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Recocido Codigo 3 no conformes
    private boolean guardarEnBaseDeDatosRecocidoNoConfomeCodigo2(RollosRecocidoInven producto_r) {
        // Obtener los atributos del producto
        String codigo = producto_r.getCodigo();
        String codOrden = producto_r.getCod_orden_rec();
        String idDetalle = producto_r.getId_detalle_rec();
        String idRollo = producto_r.getId_rollo_rec();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosRecocidoNoConformeCodigo3(codigo, codOrden, idDetalle, idRollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto_r.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_DT, producto_r.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_DT, producto_r.getNombre());
            values.put(DBHelper.COLUMN_COD_ORDEN_DT, producto_r.getCod_orden_rec());
            values.put(DBHelper.COLUMN_ID_DETALLE_DT, producto_r.getId_detalle_rec());
            values.put(DBHelper.COLUMN_ID_ROLLO_DT, producto_r.getId_rollo_rec());
            values.put(DBHelper.COLUMN_PESO_DT, producto_r.getPeso());



            // Insertar el producto en la tabla de puntillería con código 2
            long result = db.insert(DBHelper.TABLE_RECOCIDO_CODIGO_3_NO_CONFORME, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosRecocidoNoConformeCodigo3();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto_r.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Galvanizado Codigo 2
    private boolean guardarEnBaseDeDatosCodigoGalva2(RolloGalvInventario producto_i) {
        // Obtener los atributos del producto
        String codigo = producto_i.getCodigo();
        String nro_orden = producto_i.getNro_orden();
        String nro_rollo = producto_i.getNro_rollo();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosGalvCodigo2(codigo, nro_orden, nro_rollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto_i.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_C, producto_i.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_C, producto_i.getNombre());
            values.put(DBHelper.COLUMN_NRO_ORDEN_C, producto_i.getNro_orden());
            values.put(DBHelper.COLUMN_NRO_ROLLO_C, producto_i.getNro_rollo());
            values.put(DBHelper.COLUMN_TIPO_TRANS_C,producto_i.getTipo_trans());
            values.put(DBHelper.COLUMN_TRASLADO_C, producto_i.getTraslado());
            values.put(DBHelper.COLUMN_PESO_C, producto_i.getPeso());
            values.put(DBHelper.COLUMN_FECHA_HORA_C, producto_i.getFecha_hora());


            // Insertar el producto en la tabla de galvanizado con código 2
            long result = db.insert(DBHelper.TABLE_GALVANIZADO_CODIGO_2, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosGalvanizado();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto_i.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para guardar un producto Galvanizado Codigo 12
    private boolean guardarEnBaseDeDatosCodigoGalva12(RolloGalvInventario producto) {
        // Obtener los atributos del producto
        String codigo = producto.getCodigo();
        String nro_orden = producto.getNro_orden();
        String nro_rollo = producto.getNro_rollo();

        // Verificar si la combinación ya existe en la base de datos
        if (existeEnBaseDeDatosGalvCodigo12(codigo, nro_orden, nro_rollo)) {
            // Si la combinación ya existe, mostrar un mensaje de registro duplicado y retornar falso
            Log.d("Inventario_transaccion", "Información ya guardada: " + producto.toString());
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            if (db == null) {
                // Si no se pudo abrir la base de datos para escritura, mostrar un mensaje de error y retornar falso
                Log.e("DB_ERROR", "No se pudo abrir la base de datos para escritura");
                return false;
            }

            // Crear un objeto ContentValues para almacenar los valores del producto
            ContentValues values = new ContentValues();
            // Agregar los valores del producto al objeto ContentValues
            values.put(DBHelper.COLUMN_CODIGO_C, producto.getCodigo());
            values.put(DBHelper.COLUMN_NOMBRE_C, producto.getNombre());
            values.put(DBHelper.COLUMN_NRO_ORDEN_C, producto.getNro_orden());
            values.put(DBHelper.COLUMN_NRO_ROLLO_C, producto.getNro_rollo());
            values.put(DBHelper.COLUMN_TIPO_TRANS_C,producto.getTipo_trans());
            values.put(DBHelper.COLUMN_TRASLADO_C, producto.getTraslado());
            values.put(DBHelper.COLUMN_PESO_C, producto.getPeso());
            values.put(DBHelper.COLUMN_FECHA_HORA_C, producto.getFecha_hora());

            // Insertar el producto en la tabla de galvanizado con código 2
            long result = db.insert(DBHelper.TABLE_GALVANIZADO_CODIGO_2_BODEGA_12, null, values);

            // Mostrar los datos almacenados después de la inserción
            mostrarDatosAlmacenadosGalvanizado();

            // Verificar si la inserción fue exitosa y retornar el resultado
            if (result != -1) {
                // Si la inserción fue exitosa, registrar el evento y retornar verdadero
                Log.d("Inventario_transaccion", "Guardando en base de datos: " + producto.toString());
                return true;
            } else {
                // Si la inserción no fue exitosa, mostrar un mensaje de error y retornar falso
                return false;
            }
        } catch (Exception e) {
            // Manejar cualquier excepción que pueda ocurrir durante la inserción de datos
            Log.e("DB_ERROR", "Error al insertar datos en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Asegurarse de que la base de datos se cierre correctamente, incluso si ocurre una excepción
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenados() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista principal
            mostrarData();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosNo_conformes() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista principal
            mostrarDataNoConforme();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosRecocidoCodigo3() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista principal
            mostrarDataRecocidoCodigo3();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosRecocidoNoConformeCodigo3() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista principal
            mostrarDataRecocidoNoConformeCodigo3();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosRecocidoCodigo2() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista principal
            mostrarDataRecocidoCodigo2();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosRecocidoNoConformeCodigo2() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista principal
            mostrarDataRecocidoNoConformeCodigo2();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosAlambronCodigo1() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista principal
            mostrarDataAlambronCodigo1();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosAlambronCodigo2() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista principal
            mostrarDataAlambronCodigo1Bodega2();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosDestinoGalvanizado() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista de destino galvanizado
            mostrarDataDestinoGalvanizado();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosPuntilleria() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista de puntillería
            mostrarDataPuntilleria();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    // Método para mostrar los datos almacenados en la actividad principal
    public void mostrarDatosAlmacenadosGalvanizado() {
        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            // Si el cursor no es nulo, mostrar los datos en la vista de galvanizado
            mostrarDataGalvanizado();
            // Cerrar el cursor después de usarlo para liberar recursos
            cursor.close();
        } else {
            // Si el cursor es nulo, registrar un error en el registro
            Log.e("DB_ERROR", "Cursor es nulo");
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.

    public void mostrarData() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO));
                    @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE));
                    @SuppressLint("Range") String consecutivo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CONSECUTIVO));
                    @SuppressLint("Range") String id_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_ROLLO));
                    @SuppressLint("Range") String operario = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OPERARIO));
                    @SuppressLint("Range") String diametro = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DIAMETRO));
                    @SuppressLint("Range") String materia_prima = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MATERIA_PRIMA));
                    @SuppressLint("Range") String colada = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COLADA));
                    @SuppressLint("Range") String traccion = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TRACCION));
                    @SuppressLint("Range") String peso= cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO));
                    @SuppressLint("Range") String cod_orden = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COD_ORDEN));
                    @SuppressLint("Range") String fecha_hora = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FECHA_HORA));
                    @SuppressLint("Range") String cliente = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CLIENTE));
                    @SuppressLint("Range") String manual = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MANUAL));
                    @SuppressLint("Range") String anulado = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ANULADO));
                    @SuppressLint("Range") String destino = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESTINO));

                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Consecutivo: " + consecutivo + ", Id_rollo: " + id_rollo + ", Cod_orden: " + cod_orden + ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "codigo: " + codigo + ", Nombre: " + nombre + ", Consecutivo: " + consecutivo + ", Id_rollo: " + id_rollo + ", Operario: " + operario + ", Diametro: " + diametro + ", Materia_prima: " + materia_prima + ", Colada: " + colada + ", Traccion: " + traccion + ", Peso: " + peso + ", Cod_orden: " + cod_orden + ", Fecha_hora: " + fecha_hora + ", Cliente: " + cliente + ", Manual:" + manual + ", Anulado:" + anulado + ", Destino:" + destino;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void  mostrarDataNoConforme() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_F));
                    @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE_F));
                    @SuppressLint("Range") String consecutivo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CONSECUTIVO_F));
                    @SuppressLint("Range") String id_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_ROLLO_F));
                    @SuppressLint("Range") String operario = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OPERARIO_F));
                    @SuppressLint("Range") String diametro = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DIAMETRO_F));
                    @SuppressLint("Range") String materia_prima = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MATERIA_PRIMA_F));
                    @SuppressLint("Range") String colada = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COLADA_F));
                    @SuppressLint("Range") String traccion = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TRACCION_F));
                    @SuppressLint("Range") String peso= cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_F));
                    @SuppressLint("Range") String cod_orden = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COD_ORDEN_F));
                    @SuppressLint("Range") String fecha_hora = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FECHA_HORA_F));
                    @SuppressLint("Range") String cliente = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CLIENTE_F));
                    @SuppressLint("Range") String manual = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MANUAL_F));
                    @SuppressLint("Range") String anulado = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ANULADO_F));
                    @SuppressLint("Range") String destino = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESTINO_F));
                    @SuppressLint("Range") String no_conforme = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NO_CONFORME_F));

                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Consecutivo: " + consecutivo + ", Id_rollo: " + id_rollo + ", Cod_orden: " + cod_orden + ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "codigo: " + codigo + ", Nombre: " + nombre + ", Consecutivo: " + consecutivo + ", Id_rollo: " + id_rollo + ", Operario: " + operario + ", Diametro: " + diametro + ", Materia_prima: " + materia_prima + ", Colada: " + colada + ", Traccion: " + traccion + ", Peso: " + peso + ", Cod_orden: " + cod_orden + ", Fecha_hora: " + fecha_hora + ", Cliente: " + cliente + ", Manual:" + manual + ", Anulado:" + anulado + ", Destino:" + destino+ ", No_Conforme:" + no_conforme;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void mostrarDataRecocidoCodigo3() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_D));
                    @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE_D));
                    @SuppressLint("Range") String cod_orden = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COD_ORDEN_D));
                    @SuppressLint("Range") String id_detalle = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_DETALLE_D));
                    @SuppressLint("Range") String id_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_ROLLO_D));
                    @SuppressLint("Range") String peso = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_D));


                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Cod_orden: " + cod_orden + ", Id_detalle: " + id_detalle + ", Id_rollo: " + id_rollo+ ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Cod_orden: " + cod_orden + ", Id_detalle: " + id_detalle + ", Id_rollo: " + id_rollo + ", Peso: " + peso;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void mostrarDataRecocidoNoConformeCodigo3() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_DT));
                    @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE_DT));
                    @SuppressLint("Range") String cod_orden = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COD_ORDEN_DT));
                    @SuppressLint("Range") String id_detalle = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_DETALLE_DT));
                    @SuppressLint("Range") String id_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_ROLLO_DT));
                    @SuppressLint("Range") String peso = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_DT));


                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Cod_orden: " + cod_orden + ", Id_detalle: " + id_detalle + ", Id_rollo: " + id_rollo+ ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Cod_orden: " + cod_orden + ", Id_detalle: " + id_detalle + ", Id_rollo: " + id_rollo + ", Peso: " + peso;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void mostrarDataRecocidoCodigo2() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_R));
                    @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE_R));
                    @SuppressLint("Range") String cod_orden = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COD_ORDEN_R));
                    @SuppressLint("Range") String id_detalle = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_DETALLE_R));
                    @SuppressLint("Range") String id_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_ROLLO_R));
                    @SuppressLint("Range") String peso = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_R));


                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Cod_orden: " + cod_orden + ", Id_detalle: " + id_detalle + ", Id_rollo: " + id_rollo+ ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Cod_orden: " + cod_orden + ", Id_detalle: " + id_detalle + ", Id_rollo: " + id_rollo + ", Peso: " + peso;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void mostrarDataRecocidoNoConformeCodigo2() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_RT));
                    @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE_RT));
                    @SuppressLint("Range") String cod_orden = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COD_ORDEN_RT));
                    @SuppressLint("Range") String id_detalle = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_DETALLE_RT));
                    @SuppressLint("Range") String id_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_ROLLO_RT));
                    @SuppressLint("Range") String peso = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_RT));


                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Cod_orden: " + cod_orden + ", Id_detalle: " + id_detalle + ", Id_rollo: " + id_rollo+ ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Cod_orden: " + cod_orden + ", Id_detalle: " + id_detalle + ", Id_rollo: " + id_rollo + ", Peso: " + peso;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void mostrarDataAlambronCodigo1() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String nit_proveedor = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NIT_PROVEEDOR_X));
                    @SuppressLint("Range") String num_importacion = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NUM_IMPORTACION_X));
                    @SuppressLint("Range") String id_solicitud_det = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_SOLICITUD_DET_X));
                    @SuppressLint("Range") String numero_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NUMERO_ROLLO_X));
                    @SuppressLint("Range") String peso = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_X));
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_X));
                    @SuppressLint("Range") String costo_kilo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COSTO_KILO_X));



                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", nit_proveedor: " + nit_proveedor + ", id_solicitud_det: " + id_solicitud_det + ", numero_rollo: " + numero_rollo + ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "Codigo: " + codigo + ", nit_proveedor: " + nit_proveedor + ", num_importacion: " + num_importacion + ", id_solicitud_det: " + id_solicitud_det + ", numero_rollo: " + numero_rollo + ", Peso: " + peso +  ", costo_kilo: " + costo_kilo ;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void mostrarDataAlambronCodigo1Bodega2() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String nit_proveedor = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NIT_PROVEEDOR_Y));
                    @SuppressLint("Range") String num_importacion = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NUM_IMPORTACION_Y));
                    @SuppressLint("Range") String id_solicitud_det = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_SOLICITUD_DET_Y));
                    @SuppressLint("Range") String numero_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NUMERO_ROLLO_Y));
                    @SuppressLint("Range") String peso = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_Y));
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_Y));
                    @SuppressLint("Range") String costo_kilo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COSTO_KILO_Y));


                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", nit_proveedor: " + nit_proveedor + ", id_solicitud_det: " + id_solicitud_det + ", numero_rollo: " + numero_rollo + ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "Codigo: " + codigo + ", nit_proveedor: " + nit_proveedor + ", num_importacion: " + num_importacion + ", id_solicitud_det: " + id_solicitud_det + ", numero_rollo: " + numero_rollo + ", Peso: " + peso +  ", costo_kilo: " + costo_kilo ;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void mostrarDataDestinoGalvanizado() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_G));
                    @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE_G));
                    @SuppressLint("Range") String Id_detalle = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_DETALLE_G));
                    @SuppressLint("Range") String id_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_ROLLO_G));
                    @SuppressLint("Range") String peso = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_G));
                    @SuppressLint("Range") String traslado = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TRASLADO_G));
                    @SuppressLint("Range") String cod_orden = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COD_ORDEN_G));
                    @SuppressLint("Range") String fecha_hora = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FECHA_HORA_G));
                    @SuppressLint("Range") String manual = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MANUAL_G));
                    @SuppressLint("Range") String anulado = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ANULADO_G));
                    @SuppressLint("Range") String destino = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESTINO_G));

                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Nombre: " + nombre + ", Id_detalle: " + Id_detalle + ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Id_detalle: " + Id_detalle + ", Id_rollo: " + id_rollo + ", Traslado: " + traslado + ", Peso: " + peso + ", Cod_orden: " + cod_orden + ", Fecha_hora: " + fecha_hora + ", Manual:" + manual + ", Anulado:" + anulado + ", Destino:" + destino;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void mostrarDataPuntilleria() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_P));
                    @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE_p));
                    @SuppressLint("Range") String Id_detalle = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_DETALLE_P));
                    @SuppressLint("Range") String id_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID_ROLLO_P));
                    @SuppressLint("Range") String peso = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_P));
                    @SuppressLint("Range") String traslado = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TRASLADO_P));
                    @SuppressLint("Range") String cod_orden = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COD_ORDEN_P));
                    @SuppressLint("Range") String fecha_hora = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FECHA_HORA_P));
                    @SuppressLint("Range") String manual = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MANUAL_P));
                    @SuppressLint("Range") String anulado = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ANULADO_P));
                    @SuppressLint("Range") String destino = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESTINO_P));

                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Nombre: " + nombre + ", Id_detalle: " + Id_detalle + ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Id_detalle: " + Id_detalle + ", Id_rollo: " + id_rollo + ", Traslado: " + traslado + ", Peso: " + peso + ", Cod_orden: " + cod_orden + ", Fecha_hora: " + fecha_hora + ", Manual:" + manual + ", Anulado:" + anulado + ", Destino:" + destino;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Método para mostrar los datos almacenados en la vista principal.
    public void mostrarDataGalvanizado() {
        // Limpiar la lista de productos antes de mostrar los nuevos datos
        productosList.clear();

        // Obtener un cursor con los datos escaneados desde la base de datos
        Cursor cursor = obtenerDatosEscaneados();
        if (cursor != null) {
            try {
                // Iterar sobre cada fila del cursor
                while (cursor.moveToNext()) {
                    // Obtener los valores de cada columna para el producto actual
                    @SuppressLint("Range") String codigo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CODIGO_C));
                    @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE_C));
                    @SuppressLint("Range") String nro_orden = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NRO_ORDEN_C));
                    @SuppressLint("Range") String nro_rollo = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NRO_ROLLO_C));
                    @SuppressLint("Range") String peso = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PESO_C));
                    @SuppressLint("Range") String tipo_trans = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TIPO_TRANS_C));
                    @SuppressLint("Range") String traslado = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TRASLADO_C));
                    @SuppressLint("Range") String fecha_hora = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FECHA_HORA_C));

                    // Construir una descripción del producto y agregarla a la lista de productos
                    String descripcionProducto = "Codigo: " + codigo + ", Nombre: " + nombre + ", Nro_orden: " + nro_orden + ", Peso: " + peso;
                    productosList.add(descripcionProducto);

                    // Construir un resumen detallado del producto y agregarlo a la lista de resumen
                    String resumeInventario = "Codigo: " + codigo + ", Nombre: " + nombre + ", Nro_orden: " + nro_orden + ", Id_rollo: " + nro_rollo + ", Tipo_trans: "+ tipo_trans+ ", Traslado: " + traslado + ", Peso: " + peso + ", Fecha_hora: " + fecha_hora;
                    resumenList.add(resumeInventario);
                }
                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
                // Actualizar el contador de rollos mostrando el tamaño de la lista de productos
                totalRollosTextView.setText(String.valueOf(productosList.size()));
                // Registrar en el registro que se han mostrado los datos almacenados
                Log.d("Inventario_transaccion", "Datos almacenados en la base de datos: " + productosList);
            } finally {
                // Cerrar el cursor después de usarlo para liberar recursos
                cursor.close();
            }
        }
    }

    //Metodo que borra el codigo del EditText y cambia la variable "yaentre"
    private void cargarNuevo() {
        codigoCajaRecep.setText("");
        if (yaentre == 0){
            yaentre = 1;
        }else{
            yaentre = 0;
            codigoCajaRecep.requestFocus();
        }
    }

    ////////////////METODOS PARA LOS MENSAJES PERSONALIZADOS DE ACIERTO Y ERROR ///////////////////

    // Método de Toast personalizado para error
    public void toastError(String msg) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_per_no_encon, findViewById(R.id.ll_custom_toast_per_no_encon));
        TextView txtMensaje = view.findViewById(R.id.txtMensajeToast1);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();

        // Duración de la vibración para error (por ejemplo, 500 milisegundos)
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(900); // Duración de la vibración para error (por ejemplo, 500 milisegundos)
        }
    }

    // Método de Toast personalizado para aciertos
    public void toastAcierto(String msg) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_acierto, findViewById(R.id.ll_custom_toast_acierto));
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView txtMens = view.findViewById(R.id.txtMensa);
        txtMens.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();

        // Duración de la vibración para aciertos (por ejemplo, 200 milisegundos)
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(100); // Duración de la vibración para aciertos (por ejemplo, 200 milisegundos)
        }
    }



    /////////////////////////
    public void AudioError(){
        sp.play(sonido_de_Reproduccion,100,100,1,0,0);
        vibrator.vibrate(2000);
    }
}