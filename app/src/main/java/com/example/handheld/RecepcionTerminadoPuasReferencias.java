package com.example.handheld;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld.atv.holder.adapters.listOperariosPuasRecepcionAdapter;
import com.example.handheld.atv.holder.adapters.listReferenciasPuasRecepcionAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.OperariosPuasRecepcionModelo;
import com.example.handheld.modelos.ReferenciasPuasRecepcionModelo;

import java.util.ArrayList;
import java.util.List;

public class RecepcionTerminadoPuasReferencias extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //Se declaran los elementos del layout
    TextView txtNomOperario,txtDocuOperario,txtCodigoOperario;
    Button btnSalir, btnActualizar;

    //Se declaran los elementos necesarios para el list view
    ListView listviewReferenciasOperariosPuas;
    List<ReferenciasPuasRecepcionModelo> ListaReferenciasPuasRecepcion = new ArrayList<>();
    ListAdapter RefereciasAdapter;

    //Declaramos la variables necesarias recibiendo los datos enviados por la anterior clase
    String nit_operario, nombre_operario, codigo;

    //Se declara un objeto conexion
    Conexion conexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepcion_terminado_puas_referencias);

        //Definimos los elemetos del layout en la clase
        txtNomOperario = findViewById(R.id.txtNomOperario);
        txtDocuOperario = findViewById(R.id.txtDocuOperario);
        txtCodigoOperario = findViewById(R.id.txtTCodigoOperario);
        btnSalir = findViewById(R.id.btnSalir);
        btnActualizar = findViewById(R.id.btnVolver);

        //Definimos la variables necesarias recibiendo los datos enviados por la anterior clase
        nit_operario = getIntent().getStringExtra("nit");
        nombre_operario = getIntent().getStringExtra("nombre");
        codigo = getIntent().getStringExtra("codigo");

        txtDocuOperario.setText(nit_operario);
        txtNomOperario.setText(nombre_operario);
        txtCodigoOperario.setText(codigo);

        //Definimos los elementos necesarios para el list view
        listviewReferenciasOperariosPuas = findViewById(R.id.listviewReferenciasPuas);
        listviewReferenciasOperariosPuas.setOnItemClickListener(this); //Determinamos a que elemento va dirigido el OnItemClick


        //Llamamos al metodo para consultar los pedidos
        consultarReferencias();

        //Programos el boton "Actualizar" para que al presionarlo actualice y muestre un mensaje
        btnActualizar.setOnClickListener(view -> {
            consultarReferencias();
            toastActualizado("Actualizado");
        });

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecepcionTerminadoPuasReferencias.this, RecepcionTerminadoPuasOperarios.class);
                startActivity(intent);
            }
        });
    }

    //METODO CONSULTAR PEDIDOS
    public void consultarReferencias(){
        conexion = new Conexion();

        ListaReferenciasPuasRecepcion = conexion.obtenerReferenciasPuasRecepcion(getApplication(),nit_operario);
        RefereciasAdapter = new listReferenciasPuasRecepcionAdapter(RecepcionTerminadoPuasReferencias.this,R.layout.item_row_referencias_puas_recepcion,ListaReferenciasPuasRecepcion);
        listviewReferenciasOperariosPuas.setAdapter(RefereciasAdapter);
    }


    //METODO DE TOAST PERSONALIZADO : PERSONA NO ENCONTRADA
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this,RecepcionTerminadoPuas.class);
        //Enviamos al siguiente Activity los datos del Listview Seleccionado
        intent.putExtra("nit", nit_operario);
        intent.putExtra("nombre", nombre_operario);
        intent.putExtra("referencia", ListaReferenciasPuasRecepcion.get(position).getCodigo());
        startActivity(intent);
    }
}