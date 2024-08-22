package com.example.handheld;

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

import androidx.appcompat.app.AppCompatActivity;

import com.example.handheld.atv.holder.adapters.listpedidoAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.PedidoModelo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Pedido_MP_Galvanizado_Devolucion extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //Se declaran los elementos del layout
    Button btnSalir, btnActualizar;
    TextView movimiento;

    //Se declaran los elementos necesarios para el list view
    ListView listviewPedido;
    List<PedidoModelo> ListaPedidos = new ArrayList<>();
    ListAdapter PedidoAdapter;
    PedidoModelo pedidoModelo;

    //Se declara un objeto conexion
    Conexion conexion;

    //Se declaran variables necesarias en la clase
    String nit_usuario;
    Integer bod_origen;
    Integer bod_destino;
    String modelo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_mp_galvanizado_devolucion);

        //Definimos los elemetos del layout en la clase
        btnSalir = findViewById(R.id.btnSalir);
        btnActualizar = findViewById(R.id.btnVolver);
        movimiento = findViewById(R.id.movimiento);

        //Definimos los elementos necesarios para el list view
        listviewPedido = findViewById(R.id.listviewPedido);
        listviewPedido.setOnItemClickListener(this); //Determinamos a que elemento va dirigido el OnItemClick
        pedidoModelo = new PedidoModelo();

        //Definimos la variables necesarias recibiendo los datos enviados por la anterior clase
        nit_usuario = getIntent().getStringExtra("nit_usuario");
        bod_origen = getIntent().getIntExtra("bod_origen", 0);
        bod_destino = getIntent().getIntExtra("bod_destino", 0);
        modelo = getIntent().getStringExtra("modelo");

        //Le enviamos al Textview "movimiento" un texto
        movimiento.setText("Movimiento: Bodega " + bod_origen.toString() + " - " + bod_destino.toString());

        mostrarConsulta();

        //Programos el boton "Actualizar" para que al presionarlo actualice y muestre un mensaje
        btnActualizar.setOnClickListener(view -> {
            mostrarConsulta();
            toastActualizado("Actualizado");
        });
    }

    private void mostrarConsulta() {
        conexion = new Conexion();

        ListaPedidos = conexion.obtenerPedidosGalvanizadoDevolucion(getApplication());
        PedidoAdapter = new listpedidoAdapter(Pedido_MP_Galvanizado_Devolucion.this,R.layout.item_row,ListaPedidos);
        listviewPedido.setAdapter(PedidoAdapter);
    }

    //METODO PARA CERRAR LA APLICACION
    @SuppressLint("")
    public void salir(View view){
        finishAffinity();
    }


    //Metodo "Al presionar algun elemento del listview"
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        int intPendiente = Integer.parseInt(ListaPedidos.get(position).getPendiente());

        if (intPendiente > 0){
            Intent intent = new Intent(this,Trans_MP_Galvanizado_Devolucion.class);
            //Enviamos al siguiente Activity los datos del Listview Seleccionado
            intent.putExtra("numero", ListaPedidos.get(position).getNumero());
            intent.putExtra("idDetalle", ListaPedidos.get(position).getIdDetalle());
            intent.putExtra("fecha",ListaPedidos.get(position).getFecha());
            intent.putExtra("codigo",ListaPedidos.get(position).getCodigo());
            intent.putExtra("pendiente",ListaPedidos.get(position).getPendiente());
            intent.putExtra("descripcion",ListaPedidos.get(position).getDescripcion());
            //Enviamos al siguiente activity los datos traidos desde el primer activity
            intent.putExtra("nit_usuario",nit_usuario);
            intent.putExtra("bod_origen",bod_origen);
            intent.putExtra("bod_destino",bod_destino);
            intent.putExtra("modelo",modelo);
            startActivity(intent);
        }else {
            toastError("!NO es posible leer más tiquetes de alambron¡");
        }
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
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}