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
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.OperariosPuasRecepcionModelo;

import java.util.ArrayList;
import java.util.List;

public class RecepcionTerminadoPuasOperarios extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //Se declaran los elementos del layout
    Button btnSalir, btnActualizar;

    //Se declaran los elementos necesarios para el list view
    ListView listviewOperariosPuas;
    List<OperariosPuasRecepcionModelo> ListaOperariosPuasRecepcion = new ArrayList<>();
    ListAdapter OperariosAdapter;

    //Se declara un objeto conexion
    Conexion conexion;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepcion_terminado_puas_operarios);

        //Definimos los elemetos del layout en la clase
        btnSalir = findViewById(R.id.btnSalir);
        btnActualizar = findViewById(R.id.btnVolver);

        //Definimos los elementos necesarios para el list view
        listviewOperariosPuas = findViewById(R.id.listviewOperariosPuas);
        listviewOperariosPuas.setOnItemClickListener(this); //Determinamos a que elemento va dirigido el OnItemClick


        //Llamamos al metodo para consultar los pedidos
        consultarOperarios();

        //Programos el boton "Actualizar" para que al presionarlo actualice y muestre un mensaje
        btnActualizar.setOnClickListener(view -> {
            consultarOperarios();
            toastActualizado("Actualizado");
        });
    }

    //METODO CONSULTAR PEDIDOS
    public void consultarOperarios(){
        conexion = new Conexion();

        ListaOperariosPuasRecepcion = conexion.obtenerOperariosPuasRecepcion(getApplication());
        OperariosAdapter = new listOperariosPuasRecepcionAdapter(RecepcionTerminadoPuasOperarios.this,R.layout.item_row_operarios_puas_recepcion,ListaOperariosPuasRecepcion);
        listviewOperariosPuas.setAdapter(OperariosAdapter);
    }


    //METODO PARA CERRAR LA APLICACION
    @SuppressLint("")
    public void salir(View view){
        finishAffinity();
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
        Intent intent = new Intent(this,RecepcionTerminadoPuasReferencias.class);
        //Enviamos al siguiente Activity los datos del Listview Seleccionado
        intent.putExtra("nit", ListaOperariosPuasRecepcion.get(position).getNit());
        intent.putExtra("nombre", ListaOperariosPuasRecepcion.get(position).getNombre());
        intent.putExtra("codigo", ListaOperariosPuasRecepcion.get(position).getCodigo());
        startActivity(intent);
    }
}