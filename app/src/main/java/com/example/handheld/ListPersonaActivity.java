package com.example.handheld;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.handheld.atv.holder.adapters.listpersonaAdapter;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.Persona;

import java.util.ArrayList;
import java.util.List;

public class ListPersonaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //Elementos para el listview
    ListView listviewEscaners;
    List<Persona> ListaPersona = new ArrayList<>();
    ListAdapter PersonaAdapter;
    Persona persona;
    //Se declara un objeto conexion
    Conexion conexion;
    String nit_usuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_persona);
        nit_usuario = getIntent().getStringExtra("nit_usuario");
        listviewEscaners = findViewById(R.id.listviewPersona);
        listviewEscaners.setOnItemClickListener(this); //Determinamos a que elemento va dirigido el OnItemClick
        persona = new Persona();
        //Llamamos al metodo para consultar los pedidos
        consultarPedidos();
    }
    public void consultarPedidos(){
        conexion = new Conexion();
        ListaPersona = conexion.obtenerListaMuestreo(this.getApplicationContext(),nit_usuario);
        PersonaAdapter = new listpersonaAdapter(ListPersonaActivity.this, R.layout.lista_personas,ListaPersona);
        listviewEscaners.setAdapter(PersonaAdapter);
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

}