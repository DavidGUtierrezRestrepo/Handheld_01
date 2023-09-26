package com.example.handheld;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld.ClasesOperativas.MyHolder;
import com.example.handheld.atv.model.TreeNode;
import com.example.handheld.atv.view.AndroidTreeView;
import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.PersonaModelo;


public class MainActivity extends AppCompatActivity {

    //Se declaran los elementos del layout
    EditText cedula;
    ImageButton consultar;
    TextView mensaje;

    //Se declara un objeto conexion
    Conexion conexion;

    //Se declaran variables necesarias
    PersonaModelo persona;
    String nombre_usuario;
    String cd;

    ProgressBar progressBar;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Se inicializan los elementos del layout
        cedula = findViewById(R.id.txtcedula);
        consultar = findViewById(R.id.btnBuscarPersona);
        mensaje = findViewById(R.id.txtMensaje);
        progressBar = findViewById(R.id.progress_bar);

        //Se inicializa el objeto conexión
        conexion = new Conexion();

        //Se programa el boton consultar
        consultar.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            if(validar()){
                cd = cedula.getText().toString();
                persona = conexion.obtenerPersona(MainActivity.this,cd );
                nombre_usuario = persona.getNombres();

                if(nombre_usuario.equals("")){
                    toastError("Persona no encontrada");
                    progressBar.setVisibility(View.GONE);
                    cedula.setText("");
                }else{
                    progressBar.setVisibility(View.GONE);
                    mensaje.setText("Bienvenido " + nombre_usuario);
                    agregarTreeview();
                    consultar.setEnabled(false);
                    cedula.setEnabled(false);
                }

            }else{
                toastEscribir("Por favor escribir tu cedula");
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //Metodo que valida que el campo EditText "cedula" no este vacia
    public boolean validar(){
        boolean retorno = true;

        String text = cedula.getText().toString();
        if(text.isEmpty()){
            retorno = false;
        }
        return retorno;
    }

    //METODO DE TOAST PERSONALIZADO : PERSONA NO ENCONTRADA
    public void toastError(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_per_no_encon,  findViewById(R.id.ll_custom_toast_per_no_encon));
        TextView txtMensaje = view.findViewById(R.id.txtMensajeToast1);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM,0,200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    //METODO DE TOAST PERSONALIZADO : ESCRIBIR CEDULA
    public void toastEscribir(String msg) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_toast_escribir_cedula, findViewById(R.id.ll_custom_toast_escribir_cedula));
        TextView txtMensaje = view.findViewById(R.id.txtMensajeToast);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    public void agregarTreeview(){
        //Root
        TreeNode root = TreeNode.root();

        //Parent
        //MyHolder.IconTreeItem nodeItem = new MyHolder.IconTreeItem(R.drawable.ic_arrow_drop_down, "Parent");
        //TreeNode parent = new TreeNode(nodeItem).setViewHolder(new MyHolder(getApplicationContext(), true, MyHolder.DEFAULT, MyHolder.DEFAULT));

        //Grupo"Gestion de Alambron"
        MyHolder.IconTreeItem childItem1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Gestion de Alambron");
        TreeNode child1 = new TreeNode(childItem1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Gestion de Alambron"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem1_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Traslado de Bodega (1-2)");
        TreeNode subChild1_1 = new TreeNode(subChildItem1_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild1_1.setClickListener((node, value) -> {
            Intent i = new Intent(MainActivity.this,Pedido.class);
            i.putExtra("nit_usuario",cd);
            i.putExtra("bod_origen",1);
            i.putExtra("bod_destino",2);
            i.putExtra("modelo","08");

            startActivity(i);
        });

        //Subgrupo2"Gestion de Alambron"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem1_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Traslado de Bodega (2-1)");
        TreeNode subChild1_2 = new TreeNode(subChildItem1_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));


        //SubGrupo3 "Gestion de Alambron"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem1_3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Descargue de Alambron");
        TreeNode subChild1_3 = new TreeNode(subChildItem1_3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild1_3.setClickListener((node, value) -> {
            Intent i = new Intent(MainActivity.this,Lector_Cod_Alambron.class);
            i.putExtra("nit_usuario",cd);
            startActivity(i);
        });

        //Agregamos subgrupo1"Gestion de Alambron".
        child1.addChild(subChild1_1);
        child1.addChild(subChild1_3);

        //Agregamos subgrupo2"Gestion de Alambron".
        //child1.addChild(subChild1_2);

        //Agregamos Grupo"Gestion de Alambron".
        //parent.addChildren(child1);
        root.addChild(child1);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Gestion de Gestion Galvanizado"
        MyHolder.IconTreeItem childItem2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Gestion Galvanizado");
        TreeNode child2 = new TreeNode(childItem2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Gestion Galvanizado"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem2_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Traslado de Bodega (2-11)");
        TreeNode subChild2_1 = new TreeNode(subChildItem2_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //SubGrupo2"Gestion Galvanizado"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem2_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Traslado de Bodega (11-2)");
        TreeNode subChild2_2 = new TreeNode(subChildItem2_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Agregamos subgrupo1"Gestion Galvanizado".
        //child2.addChild(subChild2_1);

        //Agregamos subgrupo2"Gestion Galvanizado".
        //child2.addChild(subChild2_2);

        //Agregamos Grupo"Gestion Galvanizado".
        //parent.addChildren(child1);
        //root.addChild(child2);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Gestion de Gestion Puas"
        MyHolder.IconTreeItem childItem3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Gestion Puas");
        TreeNode child3 = new TreeNode(childItem3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Gestion Puas"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem3_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Entrega de Materia Prima Puas");
        TreeNode subChild3_1 = new TreeNode(subChildItem3_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //SubGrupo2"Gestion Puas"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem3_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Devolver materia prima");
        TreeNode subChild3_2 = new TreeNode(subChildItem3_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Agregamos subgrupo1"Gestion Puas".
        //child3.addChild(subChild3_1);

        //Agregamos subgrupo2"Gestion Puas".
        //child3.addChild(subChild3_2);

        //Agregamos Grupo"Gestion Puas".
        //parent.addChildren(child1);
        root.addChild(child3);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Gestion Puntilleria"
        MyHolder.IconTreeItem childItem4 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Gestion Puntilleria");
        TreeNode child4 = new TreeNode(childItem4).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Gestion Puntilleria"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem4_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Mesas de Empaque");
        TreeNode subChild4_1 = new TreeNode(subChildItem4_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        subChild4_1.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, IngreProTerPunti.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            startActivity(intent);
        });

        //SubGrupo2"Gestion Puntilleria"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem4_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Devoluciones Puntilleria");
        TreeNode subChild4_2 = new TreeNode(subChildItem4_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Agregamos subgrupo1"Gestion Puntilleria".
        //child4.addChild(subChild4_1);

        //Agregamos subgrupo2"Gestion Puntilleria".
        //child4.addChild(subChild4_2);

        //Agregamos Grupo"Gestion Puntilleria".
        //parent.addChildren(child1);
        root.addChild(child4);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Revisión - Calidad"
        MyHolder.IconTreeItem childItem5 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Revisión - Calidad");
        TreeNode child5 = new TreeNode(childItem5).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Revisión - Calidad"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem5_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Galvanizado");
        TreeNode subChild5_1 = new TreeNode(subChildItem5_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        /* Todavia no se ha estructurado un modulo de calidad para galvanizado
        subChild5_1.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, EscanerInventario.class);
            intent.putExtra("nit_usuario", cd);
            startActivity(intent);
        });
        */

        //SubGrupo2"Revisión - Calidad"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem5_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Trefilación");
        TreeNode subChild5_2 = new TreeNode(subChildItem5_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild5_2.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, RevisionTerminadoTrefilacion.class);
            intent.putExtra("nit_usuario", cd);
            startActivity(intent);
        });

        //SubGrupo3"Mesas Empaque"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem5_3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Puntilleria");
        TreeNode subChild5_3 = new TreeNode(subChildItem5_3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild5_3.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, ResumenPunti.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            startActivity(intent);
        });

        //Agregamos subgrupo1"Revisión - Calidad: Galvanizado".
        //child5.addChild(subChild5_1); Todavia no hay desarrollado un modulo de calidad para galvanizado

        //Agregamos subgrupo2"Revisión - Calidad: Trefilación".
        //child5.addChild(subChild5_2);

        //Agregamos subgrupo2"Revisión - Calidad: Mesas Empaque".
        //child5.addChild(subChild5_3); Todavia no hay desarrollado un modulo de calidad para empaque

        //Agregamos Grupo"Revisión - Calidad".
        //parent.addChildren(child1);
        root.addChild(child5);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Logistica - Recepción"
        MyHolder.IconTreeItem childItem6 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Logistica - Recepción");
        TreeNode child6 = new TreeNode(childItem6).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Logistica - Recepción "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Galvanizado");
        TreeNode subChild6_1 = new TreeNode(subChildItem6_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_1.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, EscanerInventario.class);
            intent.putExtra("nit_usuario", cd);
            startActivity(intent);
        });

        //SubGrupo2"Logistica - Recepción "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Trefilación");
        TreeNode subChild6_2 = new TreeNode(subChildItem6_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_2.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, RecepcionTerminadoTrefilacion.class);
            intent.putExtra("nit_usuario", cd);
            startActivity(intent);
        });

        //SubGrupo3"Mesas Empaque"
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem6_3 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Puntilleria");
        TreeNode subChild6_3 = new TreeNode(subChildItem6_3).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild6_3.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, ResumenPunti.class);
            intent.putExtra("nit_usuario", cd);
            intent.putExtra("nombre_usuario", nombre_usuario);
            startActivity(intent);
        });

        //Agregamos subgrupo1"Logistica - Recepción: Galvanizado".
        child6.addChild(subChild6_1);

        //Agregamos subgrupo2"Logistica - Recepción: Trefilación".
        //child6.addChild(subChild6_2);

        //Agregamos subgrupo2"Mesas Empaque".
        //child6.addChild(subChild6_3);

        //Agregamos Grupo"Logistica - Recepción".
        //parent.addChildren(child1);
        root.addChild(child6);


        ////////////////////////////////////////////////////////////////////////////////////////////

        //Grupo"Otros"
        MyHolder.IconTreeItem childItem7 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Otros");
        TreeNode child7 = new TreeNode(childItem7).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //SubGrupo1"Otros "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem7_1 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Recepción Producto Terminado");
        TreeNode subChild7_1 = new TreeNode(subChildItem7_1).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild7_1.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, PedidoInventario.class);
            intent.putExtra("nit_usuario", cd);
            startActivity(intent);
        });

        /////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////// SE CREA SUBGRUPO PARA INVENTARIOS //////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////
        //SubGrupo2"Otros "
        //Enviamos el icono y el texto para el SubGrupo
        MyHolder.IconTreeItem subChildItem7_2 = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Control de inventario");
        TreeNode subChild7_2 = new TreeNode(subChildItem7_2).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 100));

        //Al darle clic a este elemento en el treeview se abrira una nueva pantalla y se enviaran unos datos
        subChild7_2.setClickListener((node, value) -> {
            Intent intent = new Intent(MainActivity.this, Control_Inventarios.class);
            intent.putExtra("nit_usuario", cd);
            startActivity(intent);
        });

        //Agregamos subgrupo1"Otros".
        //child6.addChild(subChild7_1); No mostramos el subgrupo de otros
        //child7.addChild(subChild7_2);

        //Agregamos Grupo"Otros".
        //parent.addChildren(child1);
        root.addChild(child7);

        ////////////////////////////////////////////////////////////////////////////////////////////

        //Agregamos AndroidTreeView en la vista.
        AndroidTreeView tView = new AndroidTreeView(getApplicationContext(), root);
        ((LinearLayout) findViewById(R.id.ll_parent)).addView(tView.getView());
    }
}