package com.example.handheld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Inventario_proceso_alambre extends AppCompatActivity {

    Spinner spinnerProcesos;
    Spinner spinnerBodega;
    AlertDialog alertDialog;
    EditText editTextCodigo;

    String bodega;
    String consultaSQL;

    String nit_usuario;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_proceso_alambre);

        // Inicializar vistas
        spinnerProcesos = findViewById(R.id.spinnerProceso);
        spinnerBodega = findViewById(R.id.spinnerBodega);
        nit_usuario = getIntent().getStringExtra("nit_usuario");

        // Configurar los spinners
        String[] elementosProcesos = {"Seleccionar", "Alambrón", "Trefilación", "Recocido", "Galvanizado","Puntilleria"};
        String[] elementosBodega = {"Seleccionar", "Bodega 1-Codigo 1", "Bodega 2-Código 1", "Bodega 2-Código 2", "Bodega 2-Código 3", "Bodega 4-Codigo 2", "Bodega 4-Codigo 3","Bodega 11-Codigo 2","Bodega 12-Codigo 2"};
        ArrayAdapter<String> adapterProcesos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, elementosProcesos);
        adapterProcesos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProcesos.setAdapter(adapterProcesos);
        spinnerProcesos.setSelection(0);

        ArrayAdapter<String> adapterBodega = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, elementosBodega);
        adapterBodega.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBodega.setAdapter(adapterBodega);
        spinnerBodega.setSelection(0);
    }

    // Método para mostrar un mensaje de alerta si no se selecciona un proceso o una bodega
    private void mostrarAlerta() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Inventario_proceso_alambre.this);
            builder.setMessage("Por favor, seleccione un proceso y una bodega.")
                    .setTitle("Campo Obligatorio")
                    .setPositiveButton("Aceptar", null);
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    // Manejador del evento de clic del botón
    public void buttonPress(View view) {
        // Mensaje de registro
        String msg = "Entró exitosamente";
        String tag = null;
        Log.i(tag, msg);

        // Obtener el proceso y la bodega seleccionados
        int procesoSeleccionado = spinnerProcesos.getSelectedItemPosition();
        int bodegaSeleccionada = spinnerBodega.getSelectedItemPosition();
        String area = "";
        Integer bodega = 0;

        // Verificar si no se ha seleccionado un proceso o una bodega
        if (procesoSeleccionado == 0 || bodegaSeleccionada == 0) {
            toastError("Selecciona un proceso y una bodega");
            return;
        } else {
            // Determinar el área y la bodega en función de la selección
            switch (procesoSeleccionado) {
                case 1:
                    area = "ALAMBRÓN";
                    switch (bodegaSeleccionada) {
                        case 1:
                            bodega = 1; // Bodega 1 - Código 1
                            break;
                        case 2:
                            bodega = 2; // Bodega 2 - Código 1
                            break;

                        default:
                            area = "seleccionar";
                            toastError("¡BLOQUEADO! Selecciona bodega correcta para Alambron");
                            return;
                    }
                    break;
                case 2:
                    area = "TREFILACIÓN";
                    switch (bodegaSeleccionada) {
                        case 3:
                            bodega = 2; // Bodega 2 - Código 2
                            break;
                        case 4:
                            bodega = 3; // Bodega 2 - Código 3
                            break;
                        case 5:
                            bodega = 4; // Bodega 4 - Código 2
                            break;
                        case 6:
                            bodega = 5; // Bodega 4 - Código 3
                            break;
                        default:
                            area = "seleccionar";
                            toastError("¡BLOQUEADO! Selecciona bodega correcta para Trefilación");
                            return;
                    }
                    break;
                case 3:
                    area = "RECOCIDO";
                    switch (bodegaSeleccionada) {
                        case 3:
                            bodega = 2; // Bodega 2 - Código 2
                            break;
                        case 4:
                            bodega = 3; // Bodega 2 - Código 3
                            break;
                        case 5:
                            bodega = 4; // Bodega 4 - Código 2
                            break;
                        case 6:
                            bodega = 5; // Bodega 4 - Código 3
                            break;
                        default:
                            area = "seleccionar";
                            toastError("¡BLOQUEADO! Selecciona bodega correcta para Recocido");
                            return;
                    }
                    break;
                case 4:
                    area = "GALVANIZADO";
                    switch (bodegaSeleccionada) {
                        case 3:
                            bodega = 2; // Bodega 2 - Código 2
                            break;
                        case 7:
                            bodega = 11; // Bodega 11 - Código 2
                            break;
                        case 8:
                            bodega = 12; // Bodega 12 - Código 2
                            break;

                        default:
                            area = "seleccionar";
                            toastError("¡BLOQUEADO! Selecciona bodega correcta para Galvanizado");
                            return;
                    }
                    break;
                default:
                case 5:
                    area = "PUNTILLERIA";
                    switch (bodegaSeleccionada) {
                        case 8:
                            bodega = 12; // Bodega 12 - Código 2
                            break;

                        default:
                            area = "seleccionar";
                            toastError("¡BLOQUEADO! Selecciona bodega correcta para Galvanizado");
                            return;
                    }break;

            }
        }

        // Redirigir a la siguiente página si se selecciona un proceso y una bodega válidos
        Intent intent = new Intent(Inventario_proceso_alambre.this, Inventario_transaccion_alambre.class);
        intent.putExtra("area", area);
        intent.putExtra("bodega", bodega);
        intent.putExtra("nit_usuario", nit_usuario);

        startActivity(intent);
    }

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
}