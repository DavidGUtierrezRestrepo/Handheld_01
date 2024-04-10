package com.example.handheld.ClasesOperativas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.SQLException;
import android.os.Build;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.handheld.conexionDB.Conexion;
import com.example.handheld.modelos.EmpRecepcionadoCajasModelo;
import com.example.handheld.modelos.GalvRecepcionadoRollosModelo;
import com.example.handheld.modelos.PuasRecepcionadoRollosModelo;
import com.example.handheld.modelos.RecoRecepcionadoRollosModelo;
import com.example.handheld.modelos.TrefiRecepcionadoRollosModelo;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ObjTraslado_bodLn {

    Obj_ordenprodLn obj_ordenprodLn = new Obj_ordenprodLn();
    Conexion conexion = new Conexion();

    public List<Object> listaTransaccionDatable_traslado_bodega(Integer num, String cod, Integer bod_orig, Integer bod_dest, Calendar dFec, String notas, String usuario, Double cantidad, String tipo, String modelo, Double costo_kilo, Context context){
        String sql;
        String sql_lin_salida = "";
        String sql_lin_entrada = "";
        int seq = 0;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String fecha_hora = dateFormat.format(dFec.getTime());
        List<Object> listSql = new ArrayList<>();;
        String nit = "890900160";
        double vrTotal = costo_kilo * cantidad;
        Double costo_total = costo_kilo * cantidad;
        int vendedor = 0;
        String pc = Build.BRAND +"-"+ Build.MODEL;
        String sFecha_hora = "";
        String sFecha = "";
        int swDoc = 16;
        int swDoc_lin = 0;
        double vr_unitario = costo_kilo;

        if (obj_ordenprodLn.insertarProxMes(cod,context)){
            dFec.add(Calendar.MONTH, 1);

            //Capturamos el mes en un String
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
            String month = dateFormatMoth.format(dFec.getTime());

            //Capturamos el año en un String
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
            String year = dateFormatYear.format(dFec.getTime());

            sFecha_hora = year + "-" + month + "-01";

            //Capturamos las horas
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH");
            String hour = dateFormatHour.format(dFec.getTime());

            //Capturamos los minutos
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");
            String minute = dateFormatMinute.format(dFec.getTime());

            //Capturamos los segundos
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("ss");
            String seconds = dateFormatSeconds.format(dFec.getTime());

            fecha_hora = "" + month + "-" + "01" + "-" + year + " " + hour + ":" + minute + ":" + seconds + "";

            sFecha = sFecha_hora;

        }else{
            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatsFecha_hora = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            sFecha_hora = dateFormatsFecha_hora.format(calendar.getTime());

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatfecha_hora = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            fecha_hora = dateFormatfecha_hora.format(calendar.getTime());


            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatsFecha = new SimpleDateFormat("yyyy-MM-dd");
            sFecha = dateFormatsFecha.format(calendar.getTime());
        }

        if (existe_referencias_sto(cod, bod_dest, dFec, context).equals(false)){
            try {
                listSql.add(crear_referencias_sto(cod, bod_dest, dFec));
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        sql = "INSERT INTO  documentos (sw,tipo,numero,nit,fecha,vencimiento,valor_total,vendedor,valor_aplicado" +
                ",anulado,modelo,notas ,usuario,pc,fecha_hora,bodega,duracion,concepto ,centro_doc,spic) VALUES " +
                "(" + swDoc + ",'" + tipo + "'," + num + "," + Integer.parseInt(nit) + ",'" + sFecha + "','" + sFecha + "'," +
                "" + vrTotal + "," + vendedor + "," + 0 + ",0 ,'" + modelo + "','" + notas + "','" + usuario + "" +
                "','" + pc + "','" + sFecha_hora + "'," + bod_orig + ",15,0,0,'S') ";

        try {
            listSql.add(sql);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //'******************* ----------Se adiciona la salida

        seq = 1;
        swDoc_lin = 16;

        sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva," +
                "valor_unitario,porcentaje_descuento,costo_unitario,adicional,vendedor,bodega,und," +
                "cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + "," +
                "'" + sFecha + "'," + nit + "," + cantidad + ",16," + vr_unitario + ",0," + costo_kilo + "," +
                "'" + notas + "',0," + bod_orig + ",'UND',1,0,'S',0.0000000000000000,1) ";

        try {
            listSql.add(sql);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //'Script para ingresar a referencias_sto (STOCK)
        listSql.add(actualizarRefSto(cantidad, costo_kilo, cod, dFec, bod_orig, swDoc_lin));
        listSql.add(sqlActUltEntradaUltSalida(swDoc_lin, cod));


        //'******************* ----------Se adiciona la Entrada
        vr_unitario = 0;
        seq = 2;
        swDoc_lin = 12;
        sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva,valor_unitario,porcentaje_descuento ," +
                "costo_unitario,adicional,vendedor,bodega,und,cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + ",'" + sFecha + "'," + nit + "," + cantidad +
                ",16," + vr_unitario +",0, " + costo_kilo + ",'" + notas + "',0," + bod_dest + ",'UND',1,0,'S',0.0000000000000000,1) ";

        try {
            listSql.add(sql);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //'Script para ingresar a referencias_sto (STOCK)
        String actRef = actualizarRefSto(cantidad, costo_kilo, cod, dFec, bod_dest, swDoc_lin);
        listSql.add(actRef);
        String actUlt = sqlActUltEntradaUltSalida(swDoc_lin, cod);
        listSql.add(actUlt);

        return (listSql);
    }

    public List<Object> listaTrasladoBodegaGalv(List<GalvRecepcionadoRollosModelo> listInfRerencias, Integer num, Integer bod_orig, Integer bod_dest, Calendar dFec, String notas, String usuario, String tipo, String modelo, Context context){
        List<Object> listSql = new ArrayList<>();
        String sFecha_hora;
        String sFecha;
        double vrTotalEncabezado = (double) 0;

        int seq;
        int swDoc_lin;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha_hora = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sFecha_hora = dateFormatsFecha_hora.format(dFec.getTime());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha = new SimpleDateFormat("yyyy-MM-dd");
        sFecha = dateFormatsFecha.format(dFec.getTime());

        Calendar pruebaFec = dFec;

        //For para identificar si hay alguna referecia que debe crearse para el proximo mes
        for(int y=0; y<listInfRerencias.size();y++){
            String cod = listInfRerencias.get(y).getReferencia();


            if (obj_ordenprodLn.insertarProxMes(cod,context)){
                pruebaFec.add(Calendar.MONTH, 1);
                if (pruebaFec.after(dFec)){
                    dFec.add(Calendar.MONTH, 1);

                    //Capturamos el mes en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
                    String month = dateFormatMoth.format(dFec.getTime());

                    //Capturamos el año en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
                    String year = dateFormatYear.format(dFec.getTime());

                    sFecha = year + "-" + month + "-01";

                    //Capturamos las horas
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH");
                    String hour = dateFormatHour.format(dFec.getTime());

                    //Capturamos los minutos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");
                    String minute = dateFormatMinute.format(dFec.getTime());

                    //Capturamos los segundos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("ss");
                    String seconds = dateFormatSeconds.format(dFec.getTime());

                    sFecha_hora = "" + month + "-" + "01" + "-" + year + " " + hour + ":" + minute + ":" + seconds + "";

                    pruebaFec.add(Calendar.MONTH, -1);
                }else{
                    pruebaFec.add(Calendar.MONTH, -1);
                }
            }
        }

        //For para verificar una por una si las referencias exiten
        for(int a=0; a<listInfRerencias.size();a++){
            String cod = listInfRerencias.get(a).getReferencia();

            if (existe_referencias_sto(cod, bod_dest, dFec, context).equals(false)){
                try {
                    listSql.add(crear_referencias_sto(cod, bod_dest, dFec));
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        //For para sumar todas los valores de todas las referencias en una variable para el Encabe
        //Se comenta el codigo debido a que no se guarda el valor de la transaccion
        /*
        for(int i=0; i<listInfRerencias.size();i++){
            Double peso = listInfRerencias.get(i).getPeso();
            Double promedio =  listInfRerencias.get(i).getPromedio();
            Double costo_unitario = listInfRerencias.get(i).getCosto_unitario();

            if (promedio == 0){
                vrTotalEncabezado = vrTotalEncabezado + (peso * costo_unitario);
            }else{
                vrTotalEncabezado = vrTotalEncabezado + (peso * promedio);
            }
        }*/

        int swDoc = 16;
        String nit = "890900160";
        int vendedor = 0;
        String pc = Build.BRAND +"-"+ Build.MODEL;
        String sql;

        //Ingresamos el Encabezado a la lista
        sql = "INSERT INTO  documentos (sw,tipo,numero,nit,fecha,vencimiento,valor_total,vendedor,valor_aplicado" +
                ",anulado,modelo,notas ,usuario,pc,fecha_hora,bodega,duracion,concepto ,centro_doc,spic) VALUES " +
                "(" + swDoc + ",'" + tipo + "'," + num + "," + Integer.parseInt(nit) + ",'" + sFecha + "','" + sFecha + "'," +
                "" + vrTotalEncabezado + "," + vendedor + "," + 0 + ",0 ,'" + modelo + "','" + notas + "','" + usuario + "" +
                "','" + pc + "','" + sFecha_hora + "'," + bod_orig + ",15,0,0,'S') ";

        try {
            listSql.add(sql);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        seq = 0;

        //For para ingresar a la lista todos los detalles de cada referencia
        for(int b=0; b<listInfRerencias.size();b++){
            String cod = listInfRerencias.get(b).getReferencia().trim();
            Double peso = listInfRerencias.get(b).getPeso();
            Double promedio =  listInfRerencias.get(b).getPromedio();
            Double costo_unitario = listInfRerencias.get(b).getCosto_unitario();
            Double costo_kilo;

            if (promedio==0){
                costo_kilo = costo_unitario;
            }else{
                costo_kilo = promedio;
            }

            //'******************* ----------Se adiciona la salida

            seq = seq + 1;
            swDoc_lin = 16;

            Double iva = conexion.obtenerIvaReferencia(context,cod);

            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva," +
                    "valor_unitario,porcentaje_descuento,costo_unitario,adicional,vendedor,bodega,und," +
                    "cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + "," +
                    "'" + sFecha + "'," + nit + "," + peso + ","+ iva +",0,0," + costo_kilo + "," +
                    "'" + notas + "',0," + bod_orig + ",'UND',1,0,'S',0.0000000000000000,1) ";

            try {
                listSql.add(sql);
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //'Script para ingresar a referencias_sto (STOCK)
            listSql.add(actualizarRefSto(peso, costo_kilo, cod, dFec, bod_orig, swDoc_lin));
            listSql.add(sqlActUltEntradaUltSalida(swDoc_lin, cod));

            //'******************* ----------Se adiciona la Entrada


            int valor_unit = 0;
            seq = seq + 1;
            swDoc_lin = 12;
            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva,valor_unitario,porcentaje_descuento ," +
                    "costo_unitario,adicional,vendedor,bodega,und,cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + ",'" + sFecha + "'," + nit + "," + peso +
                    ",0," + valor_unit +",0, " + costo_kilo + ",'" + notas + "',0," + bod_dest + ",'UND',1,0,'S',0.0000000000000000,1) ";

            try {
                listSql.add(sql);
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //'Script para ingresar a referencias_sto (STOCK)
            String actRef = actualizarRefSto(peso, costo_kilo, cod, dFec, bod_dest, swDoc_lin);
            listSql.add(actRef);
            String actUlt = sqlActUltEntradaUltSalida(swDoc_lin, cod);
            listSql.add(actUlt);

        }

        return (listSql);
    }

    //funcion traslado bodega recepcion producto terminado trefilacion
    public List<Object> listaTrasladoBodegaTrefi(List<TrefiRecepcionadoRollosModelo> listInfRerencias, Integer num, Integer bod_orig, Integer bod_dest, Calendar dFec, String notas, String usuario, String tipo, String modelo, Context context){
        List<Object> listSql = new ArrayList<>();
        String sFecha_hora;
        String sFecha;
        double vrTotalEncabezado = (double) 0;

        int seq;
        int swDoc_lin;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha_hora = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sFecha_hora = dateFormatsFecha_hora.format(dFec.getTime());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha = new SimpleDateFormat("yyyy-MM-dd");
        sFecha = dateFormatsFecha.format(dFec.getTime());

        Calendar pruebaFec = dFec;

        //For para identificar si hay alguna referecia que debe crearse para el proximo mes
        for(int y=0; y<listInfRerencias.size();y++){
            String cod = listInfRerencias.get(y).getReferencia();


            if (obj_ordenprodLn.insertarProxMes(cod,context)){
                pruebaFec.add(Calendar.MONTH, 1);
                if (pruebaFec.after(dFec)){
                    dFec.add(Calendar.MONTH, 1);

                    //Capturamos el mes en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
                    String month = dateFormatMoth.format(dFec.getTime());

                    //Capturamos el año en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
                    String year = dateFormatYear.format(dFec.getTime());

                    sFecha = year + "-" + month + "-01";

                    //Capturamos las horas
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH");
                    String hour = dateFormatHour.format(dFec.getTime());

                    //Capturamos los minutos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");
                    String minute = dateFormatMinute.format(dFec.getTime());

                    //Capturamos los segundos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("ss");
                    String seconds = dateFormatSeconds.format(dFec.getTime());

                    sFecha_hora = "" + month + "-" + "01" + "-" + year + " " + hour + ":" + minute + ":" + seconds + "";

                    pruebaFec.add(Calendar.MONTH, -1);
                }else{
                    pruebaFec.add(Calendar.MONTH, -1);
                }
            }
        }

        //For para verificar una por una si las referencias exiten
        for(int a=0; a<listInfRerencias.size();a++){
            String cod = listInfRerencias.get(a).getReferencia();

            if (existe_referencias_sto(cod, bod_dest, dFec, context).equals(false)){
                try {
                    listSql.add(crear_referencias_sto(cod, bod_dest, dFec));
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        //For para sumar todas los valores de todas las referencias en una variable para el Encabe
        //Se comenta el codigo debido a que no se guarda el valor de la transaccion
        /*
        for(int i=0; i<listInfRerencias.size();i++){
            Double peso = listInfRerencias.get(i).getPeso();
            Double promedio =  listInfRerencias.get(i).getPromedio();
            Double costo_unitario = listInfRerencias.get(i).getCosto_unitario();

            if (promedio == 0){
                vrTotalEncabezado = vrTotalEncabezado + (peso * costo_unitario);
            }else{
                vrTotalEncabezado = vrTotalEncabezado + (peso * promedio);
            }
        }*/

        int swDoc = 16;
        String nit = "890900160";
        int vendedor = 0;
        String pc = Build.BRAND +"-"+ Build.MODEL;
        String sql;

        //Ingresamos el Encabezado a la lista
        sql = "INSERT INTO  documentos (sw,tipo,numero,nit,fecha,vencimiento,valor_total,vendedor,valor_aplicado" +
                ",anulado,modelo,notas ,usuario,pc,fecha_hora,bodega,duracion,concepto ,centro_doc,spic) VALUES " +
                "(" + swDoc + ",'" + tipo + "'," + num + "," + Integer.parseInt(nit) + ",'" + sFecha + "','" + sFecha + "'," +
                "" + vrTotalEncabezado + "," + vendedor + "," + 0 + ",0 ,'" + modelo + "','" + notas + "','" + usuario + "" +
                "','" + pc + "','" + sFecha_hora + "'," + bod_orig + ",15,0,0,'S') ";

        try {
            listSql.add(sql);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        seq = 0;

        //For para ingresar a la lista todos los detalles de cada referencia
        for(int b=0; b<listInfRerencias.size();b++){
            String cod = listInfRerencias.get(b).getReferencia().trim();
            Double peso = listInfRerencias.get(b).getPeso();
            Double promedio =  listInfRerencias.get(b).getPromedio();
            Double costo_unitario = listInfRerencias.get(b).getCosto_unitario();
            Double costo_kilo;

            if (promedio==0){
                costo_kilo = costo_unitario;
            }else{
                costo_kilo = promedio;
            }

            //'******************* ----------Se adiciona la salida

            seq = seq + 1;
            swDoc_lin = 16;

            Double iva = conexion.obtenerIvaReferencia(context,cod);

            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva," +
                    "valor_unitario,porcentaje_descuento,costo_unitario,adicional,vendedor,bodega,und," +
                    "cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + "," +
                    "'" + sFecha + "'," + nit + "," + peso + ","+ iva +"," + costo_kilo + ",0," + costo_kilo + "," +
                    "'" + notas + "',0," + bod_orig + ",'UND',1,0,'S',0.0000000000000000,1) ";

            try {
                listSql.add(sql);
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //'Script para ingresar a referencias_sto (STOCK)
            listSql.add(actualizarRefSto(peso, costo_kilo, cod, dFec, bod_orig, swDoc_lin));
            listSql.add(sqlActUltEntradaUltSalida(swDoc_lin, cod));

            //'******************* ----------Se adiciona la Entrada


            int valor_unit = 0;
            seq = seq + 1;
            swDoc_lin = 12;
            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva,valor_unitario,porcentaje_descuento ," +
                    "costo_unitario,adicional,vendedor,bodega,und,cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + ",'" + sFecha + "'," + nit + "," + peso +
                    ",0," + valor_unit +",0, " + costo_kilo + ",'" + notas + "',0," + bod_dest + ",'UND',1,0,'S',0.0000000000000000,1) ";

            try {
                listSql.add(sql);
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //'Script para ingresar a referencias_sto (STOCK)
            String actRef = actualizarRefSto(peso, costo_kilo, cod, dFec, bod_dest, swDoc_lin);
            listSql.add(actRef);
            String actUlt = sqlActUltEntradaUltSalida(swDoc_lin, cod);
            listSql.add(actUlt);

        }

        return (listSql);
    }

    //funcion traslado bodega recepcion producto terminado recocido
    public List<Object> listaTrasladoBodegaReco(List<RecoRecepcionadoRollosModelo> listInfRerencias, Integer num, Integer bod_orig, Integer bod_dest, Calendar dFec, String notas, String usuario, String tipo, String modelo, Context context){
        List<Object> listSql = new ArrayList<>();
        String sFecha_hora;
        String sFecha;
        double vrTotalEncabezado = (double) 0;

        int seq;
        int swDoc_lin;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha_hora = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sFecha_hora = dateFormatsFecha_hora.format(dFec.getTime());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha = new SimpleDateFormat("yyyy-MM-dd");
        sFecha = dateFormatsFecha.format(dFec.getTime());

        Calendar pruebaFec = dFec;

        //For para identificar si hay alguna referecia que debe crearse para el proximo mes
        for(int y=0; y<listInfRerencias.size();y++){
            String cod = listInfRerencias.get(y).getReferencia();


            if (obj_ordenprodLn.insertarProxMes(cod,context)){
                pruebaFec.add(Calendar.MONTH, 1);
                if (pruebaFec.after(dFec)){
                    dFec.add(Calendar.MONTH, 1);

                    //Capturamos el mes en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
                    String month = dateFormatMoth.format(dFec.getTime());

                    //Capturamos el año en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
                    String year = dateFormatYear.format(dFec.getTime());

                    sFecha = year + "-" + month + "-01";

                    //Capturamos las horas
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH");
                    String hour = dateFormatHour.format(dFec.getTime());

                    //Capturamos los minutos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");
                    String minute = dateFormatMinute.format(dFec.getTime());

                    //Capturamos los segundos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("ss");
                    String seconds = dateFormatSeconds.format(dFec.getTime());

                    sFecha_hora = "" + month + "-" + "01" + "-" + year + " " + hour + ":" + minute + ":" + seconds + "";

                    pruebaFec.add(Calendar.MONTH, -1);
                }else{
                    pruebaFec.add(Calendar.MONTH, -1);
                }
            }
        }

        //For para verificar una por una si las referencias exiten
        for(int a=0; a<listInfRerencias.size();a++){
            String cod = listInfRerencias.get(a).getReferencia();

            if (existe_referencias_sto(cod, bod_dest, dFec, context).equals(false)){
                try {
                    listSql.add(crear_referencias_sto(cod, bod_dest, dFec));
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        //For para sumar todas los valores de todas las referencias en una variable para el Encabe
        //Se comenta el codigo debido a que no se guarda el valor de la transaccion
        /*
        for(int i=0; i<listInfRerencias.size();i++){
            Double peso = listInfRerencias.get(i).getPeso();
            Double promedio =  listInfRerencias.get(i).getPromedio();
            Double costo_unitario = listInfRerencias.get(i).getCosto_unitario();

            if (promedio == 0){
                vrTotalEncabezado = vrTotalEncabezado + (peso * costo_unitario);
            }else{
                vrTotalEncabezado = vrTotalEncabezado + (peso * promedio);
            }
        }*/

        int swDoc = 16;
        String nit = "890900160";
        int vendedor = 0;
        String pc = Build.BRAND +"-"+ Build.MODEL;
        String sql;

        //Ingresamos el Encabezado a la lista
        sql = "INSERT INTO  documentos (sw,tipo,numero,nit,fecha,vencimiento,valor_total,vendedor,valor_aplicado" +
                ",anulado,modelo,notas ,usuario,pc,fecha_hora,bodega,duracion,concepto ,centro_doc,spic) VALUES " +
                "(" + swDoc + ",'" + tipo + "'," + num + "," + Integer.parseInt(nit) + ",'" + sFecha + "','" + sFecha + "'," +
                "" + vrTotalEncabezado + "," + vendedor + "," + 0 + ",0 ,'" + modelo + "','" + notas + "','" + usuario + "" +
                "','" + pc + "','" + sFecha_hora + "'," + bod_orig + ",15,0,0,'S') ";

        try {
            listSql.add(sql);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        seq = 0;

        //For para ingresar a la lista todos los detalles de cada referencia
        for(int b=0; b<listInfRerencias.size();b++){
            String cod = listInfRerencias.get(b).getReferencia().trim();
            Double peso = listInfRerencias.get(b).getPeso();
            Double promedio =  listInfRerencias.get(b).getPromedio();
            Double costo_unitario = listInfRerencias.get(b).getCosto_unitario();
            Double costo_kilo;

            if (promedio==0){
                costo_kilo = costo_unitario;
            }else{
                costo_kilo = promedio;
            }

            //'******************* ----------Se adiciona la salida

            seq = seq + 1;
            swDoc_lin = 16;

            Double iva = conexion.obtenerIvaReferencia(context,cod);

            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva," +
                    "valor_unitario,porcentaje_descuento,costo_unitario,adicional,vendedor,bodega,und," +
                    "cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + "," +
                    "'" + sFecha + "'," + nit + "," + peso + ","+ iva +"," + costo_kilo + ",0," + costo_kilo + "," +
                    "'" + notas + "',0," + bod_orig + ",'UND',1,0,'S',0.0000000000000000,1) ";

            try {
                listSql.add(sql);
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //'Script para ingresar a referencias_sto (STOCK)
            listSql.add(actualizarRefSto(peso, costo_kilo, cod, dFec, bod_orig, swDoc_lin));
            listSql.add(sqlActUltEntradaUltSalida(swDoc_lin, cod));

            //'******************* ----------Se adiciona la Entrada


            int valor_unit = 0;
            seq = seq + 1;
            swDoc_lin = 12;
            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva,valor_unitario,porcentaje_descuento ," +
                    "costo_unitario,adicional,vendedor,bodega,und,cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + ",'" + sFecha + "'," + nit + "," + peso +
                    ",0," + valor_unit +",0, " + costo_kilo + ",'" + notas + "',0," + bod_dest + ",'UND',1,0,'S',0.0000000000000000,1) ";

            try {
                listSql.add(sql);
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //'Script para ingresar a referencias_sto (STOCK)
            String actRef = actualizarRefSto(peso, costo_kilo, cod, dFec, bod_dest, swDoc_lin);
            listSql.add(actRef);
            String actUlt = sqlActUltEntradaUltSalida(swDoc_lin, cod);
            listSql.add(actUlt);

        }

        return (listSql);
    }

    public List<Object> listaTrasladoBodegaPuas(List<PuasRecepcionadoRollosModelo> listInfRerencias, Integer num, Integer bod_orig, Integer bod_dest, Calendar dFec, String notas, String usuario, String tipo, String modelo, Context context){
        List<Object> listSql = new ArrayList<>();
        String sFecha_hora;
        String sFecha;
        double vrTotalEncabezado = (double) 0;

        int seq;
        int swDoc_lin;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha_hora = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sFecha_hora = dateFormatsFecha_hora.format(dFec.getTime());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha = new SimpleDateFormat("yyyy-MM-dd");
        sFecha = dateFormatsFecha.format(dFec.getTime());

        Calendar pruebaFec = dFec;

        //For para identificar si hay alguna referecia que debe crearse para el proximo mes
        for(int y=0; y<listInfRerencias.size();y++){
            String cod = listInfRerencias.get(y).getReferencia();


            if (obj_ordenprodLn.insertarProxMes(cod,context)){
                pruebaFec.add(Calendar.MONTH, 1);
                if (pruebaFec.after(dFec)){
                    dFec.add(Calendar.MONTH, 1);

                    //Capturamos el mes en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
                    String month = dateFormatMoth.format(dFec.getTime());

                    //Capturamos el año en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
                    String year = dateFormatYear.format(dFec.getTime());

                    sFecha = year + "-" + month + "-01";

                    //Capturamos las horas
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH");
                    String hour = dateFormatHour.format(dFec.getTime());

                    //Capturamos los minutos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");
                    String minute = dateFormatMinute.format(dFec.getTime());

                    //Capturamos los segundos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("ss");
                    String seconds = dateFormatSeconds.format(dFec.getTime());

                    sFecha_hora = "" + month + "-" + "01" + "-" + year + " " + hour + ":" + minute + ":" + seconds + "";

                    pruebaFec.add(Calendar.MONTH, -1);
                }else{
                    pruebaFec.add(Calendar.MONTH, -1);
                }
            }
        }

        //For para verificar una por una si las referencias exiten
        for(int a=0; a<listInfRerencias.size();a++){
            String cod = listInfRerencias.get(a).getReferencia();

            if (existe_referencias_sto(cod, bod_dest, dFec, context).equals(false)){
                try {
                    listSql.add(crear_referencias_sto(cod, bod_dest, dFec));
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        //For para sumar todas los valores de todas las referencias en una variable para el Encabe
        //Se comenta el codigo debido a que no se guarda el valor de la transaccion
        /*
        for(int i=0; i<listInfRerencias.size();i++){
            Double peso = listInfRerencias.get(i).getPeso();
            Double promedio =  listInfRerencias.get(i).getPromedio();
            Double costo_unitario = listInfRerencias.get(i).getCosto_unitario();

            if (promedio == 0){
                vrTotalEncabezado = vrTotalEncabezado + (peso * costo_unitario);
            }else{
                vrTotalEncabezado = vrTotalEncabezado + (peso * promedio);
            }
        }*/

        int swDoc = 16;
        String nit = "890900160";
        int vendedor = 0;
        String pc = Build.BRAND +"-"+ Build.MODEL;
        String sql;

        //Ingresamos el Encabezado a la lista
        sql = "INSERT INTO  documentos (sw,tipo,numero,nit,fecha,vencimiento,valor_total,vendedor,valor_aplicado" +
                ",anulado,modelo,notas ,usuario,pc,fecha_hora,bodega,duracion,concepto ,centro_doc,spic) VALUES " +
                "(" + swDoc + ",'" + tipo + "'," + num + "," + Integer.parseInt(nit) + ",'" + sFecha + "','" + sFecha + "'," +
                "" + vrTotalEncabezado + "," + vendedor + "," + 0 + ",0 ,'" + modelo + "','" + notas + "','" + usuario + "" +
                "','" + pc + "','" + sFecha_hora + "'," + bod_orig + ",15,0,0,'S') ";

        try {
            listSql.add(sql);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        seq = 0;

        //For para ingresar a la lista todos los detalles de cada referencia
        for(int b=0; b<listInfRerencias.size();b++){
            String cod = listInfRerencias.get(b).getReferencia().trim();
            Double peso = listInfRerencias.get(b).getPeso();
            Double promedio =  listInfRerencias.get(b).getPromedio();
            Double costo_unitario = listInfRerencias.get(b).getCosto_unitario();
            Double costo_kilo;

            if (promedio==0){
                costo_kilo = costo_unitario;
            }else{
                costo_kilo = promedio;
            }

            //'******************* ----------Se adiciona la salida

            seq = seq + 1;
            swDoc_lin = 16;

            Double iva = conexion.obtenerIvaReferencia(context,cod);

            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva," +
                    "valor_unitario,porcentaje_descuento,costo_unitario,adicional,vendedor,bodega,und," +
                    "cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + "," +
                    "'" + sFecha + "'," + nit + "," + peso + ","+ iva +",0,0," + costo_kilo + "," +
                    "'" + notas + "',0," + bod_orig + ",'UND',1,0,'S',0.0000000000000000,1) ";

            try {
                listSql.add(sql);
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //'Script para ingresar a referencias_sto (STOCK)
            listSql.add(actualizarRefSto(peso, costo_kilo, cod, dFec, bod_orig, swDoc_lin));
            listSql.add(sqlActUltEntradaUltSalida(swDoc_lin, cod));

            //'******************* ----------Se adiciona la Entrada


            int valor_unit = 0;
            seq = seq + 1;
            swDoc_lin = 12;
            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva,valor_unitario,porcentaje_descuento ," +
                    "costo_unitario,adicional,vendedor,bodega,und,cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + ",'" + sFecha + "'," + nit + "," + peso +
                    ",0," + valor_unit +",0, " + costo_kilo + ",'" + notas + "',0," + bod_dest + ",'UND',1,0,'S',0.0000000000000000,1) ";

            try {
                listSql.add(sql);
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //'Script para ingresar a referencias_sto (STOCK)
            String actRef = actualizarRefSto(peso, costo_kilo, cod, dFec, bod_dest, swDoc_lin);
            listSql.add(actRef);
            String actUlt = sqlActUltEntradaUltSalida(swDoc_lin, cod);
            listSql.add(actUlt);

        }

        return (listSql);
    }

    public List<Object> listaTrasladoBodegaEmp(List<EmpRecepcionadoCajasModelo> listInfRerencias, Integer num, Integer bod_dest, Calendar dFec, String notas, String usuario, String tipo, String modelo, Context context, String fechatransa){
        List<Object> listSql = new ArrayList<>();
        String sFecha_hora;
        String sFecha;
        //double vrTotalEncabezado = (double) 0;

        int seq;
        int swDoc_lin;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha_hora = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sFecha_hora = dateFormatsFecha_hora.format(dFec.getTime());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatsFecha = new SimpleDateFormat("yyyy-MM-dd");
        sFecha = dateFormatsFecha.format(dFec.getTime());

        Calendar pruebaFec = dFec;

        //For para identificar si hay alguna referecia que debe crearse para el proximo mes
        for(int y=0; y<listInfRerencias.size();y++){
            String cod = listInfRerencias.get(y).getREFERENCIA();


            if (obj_ordenprodLn.insertarProxMes(cod,context)){
                pruebaFec.add(Calendar.MONTH, 1);
                if (pruebaFec.after(dFec)){
                    dFec.add(Calendar.MONTH, 1);

                    //Capturamos el mes en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
                    String month = dateFormatMoth.format(dFec.getTime());

                    //Capturamos el año en un String
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
                    String year = dateFormatYear.format(dFec.getTime());

                    sFecha = year + "-" + month + "-01";

                    //Capturamos las horas
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH");
                    String hour = dateFormatHour.format(dFec.getTime());

                    //Capturamos los minutos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");
                    String minute = dateFormatMinute.format(dFec.getTime());

                    //Capturamos los segundos
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("ss");
                    String seconds = dateFormatSeconds.format(dFec.getTime());

                    sFecha_hora = "" + month + "-" + "01" + "-" + year + " " + hour + ":" + minute + ":" + seconds + "";

                    pruebaFec.add(Calendar.MONTH, -1);
                }else{
                    pruebaFec.add(Calendar.MONTH, -1);
                }
            }
        }

        //For para verificar una por una si las referencias exiten
        for(int a=0; a<listInfRerencias.size();a++){
            String cod = listInfRerencias.get(a).getREFERENCIA();

            if (existe_referencias_sto(cod, bod_dest, dFec, context).equals(false)){
                try {
                    listSql.add(crear_referencias_sto(cod, bod_dest, dFec));
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        /*
        //For para sumar todas los valores de todas las referencias en una variable para el Encabe
        for(int i=0; i<listInfRerencias.size();i++){
            Double peso = listInfRerencias.get(i).getCantidad();
            Double promedio =  listInfRerencias.get(i).getPromedio();
            Double costo_unitario = listInfRerencias.get(i).getCosto_unitario();

            if (promedio == 0){
                vrTotalEncabezado = vrTotalEncabezado + (peso * costo_unitario);
            }else{
                vrTotalEncabezado = vrTotalEncabezado + (peso * promedio);
            }
        }
         */

        int swDoc = 12;
        String Empresa = conexion.obtenerEmpresa(context,fechatransa);
        String nit;
        if (Empresa.equals("Corsan")){
            nit = "890900160";
        }else{
            nit = "900029909";
        }

        int vendedor = 0;
        String pc = Build.BRAND +"-"+ Build.MODEL;
        String sql;

        //Ingresamos el Encabezado a la lista
        sql = "INSERT INTO  documentos (sw,tipo,numero,nit,fecha,vencimiento,valor_total,vendedor,valor_aplicado" +
                ",anulado,modelo,notas ,usuario,pc,fecha_hora,bodega,duracion,concepto ,centro_doc,spic) VALUES " +
                "(" + swDoc + ",'" + tipo + "'," + num + "," + Integer.parseInt(nit) + ",'" + sFecha + "','" + sFecha + "'," +
                "0," + vendedor + "," + 0 + ",0 ,'" + modelo + "','" + notas + "','" + usuario + "" +
                "','" + pc + "','" + sFecha_hora + "'," + bod_dest + ",50,0,0,'S') ";

        try {
            listSql.add(sql);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //For para ingresar a la lista todos los detalles de cada referencia
        for(int b=0; b<listInfRerencias.size();b++){
            String cod = listInfRerencias.get(b).getREFERENCIA().trim();
            Double peso = listInfRerencias.get(b).getCantidad();
            Double promedio =  listInfRerencias.get(b).getPromedio();
            Double costo_unitario = listInfRerencias.get(b).getCosto_unitario();
            Double costo_kilo;

            if (promedio==0){
                costo_kilo = costo_unitario;
            }else{
                costo_kilo = promedio;
            }

            //'******************* ----------Se adiciona la Entrada

            Double iva = conexion.obtenerIvaReferencia(context,cod);

            int valor_unit = 0;
            seq = 1;
            swDoc_lin = 3;
            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva,valor_unitario,porcentaje_descuento ," +
                    "costo_unitario,adicional,vendedor,bodega,und,cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + num + ",'" + cod + "'," + seq + ",'" + sFecha + "'," + nit + "," + peso +
                    ","+ iva +"," + valor_unit +",0, " + costo_kilo + ",'" + notas + "',0," + bod_dest + ",'UND',1,0,'S',0.0000000000000000,1) ";

            try {
                listSql.add(sql);
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //'Script para ingresar a referencias_sto (STOCK)
            String actRef = actualizarRefSto(peso, costo_kilo, cod, dFec, bod_dest, swDoc_lin);
            listSql.add(actRef);
            String actUlt = sqlActUltEntradaUltSalida(swDoc_lin, cod);
            listSql.add(actUlt);

        }

        return (listSql);
    }

    public String obtenerBodegaXcodigo(String codigo){
        char primerCaracter = codigo.charAt(0);
        return Character.toString(primerCaracter);
    }

    //Se va hacia la tabla referencias y se actualiza la fecha de la ultima entrada o salida de una referencia
    public String sqlActUltEntradaUltSalida(Integer swTipo, String codigo){
        String sql = "";
        if (swTipo == 11 || swTipo == 16){
            sql = "UPDATE referencias SET fec_ultima_salida = GETDATE () WHERE codigo = '" + codigo + "'";
        }else if (swTipo == 12 || swTipo == 3){
            sql = "UPDATE referencias SET fec_ultima_entrada = GETDATE () WHERE codigo = '" + codigo + "'";
        }
        return sql;
    }



    private String actualizarRefSto(Double kilos , Double costUnit, String codigo, Calendar dFec, Integer bodega, Integer swTipo){
        String sql = "";
        //DecimalFormat df = new DecimalFormat("#0.0");

        //Capturamos el mes en un String
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
        String month = dateFormatMoth.format(dFec.getTime());

        //Capturamos el año en un String
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        String year = dateFormatYear.format(dFec.getTime());

        //'sw 11 resta(salida) , sw 12 suma(entrada)
        if (swTipo == 11 || swTipo == 16){
            sql = "UPDATE referencias_sto SET can_sal += " + kilos + " , cos_sal +=" + (costUnit * kilos) + " " +
                    ", cos_otr_sal +=" + (costUnit * kilos) + " , can_otr_sal += " + kilos + " , nro_com = " +
                    "(CASE WHEN nro_com is null  THEN 1 ELSE nro_com + 1 END ) WHERE bodega = " + bodega + " " +
                    "AND codigo ='" + codigo + "' AND mes = " + month + " and ano = " + year + " ";
        }else if (swTipo == 12 || swTipo == 3){
            sql = "UPDATE referencias_sto SET can_ent = (CASE WHEN can_ent is null  THEN " + kilos + " " +
                    "ELSE can_ent +" + kilos + " END ), cos_ent = (CASE WHEN cos_ent is null  " +
                    "THEN " + (costUnit * kilos) + " ELSE cos_ent +" + (costUnit * kilos) + " END ), " +
                    "can_com = (CASE WHEN can_com is null  THEN " + kilos + " ELSE can_com +" + kilos + " " +
                    "END ), cos_com = (CASE WHEN cos_com is null  THEN " + (costUnit * kilos) + " " +
                    "ELSE cos_com +" + (costUnit * kilos) + " END ), nro_com = (CASE WHEN nro_com is null  " +
                    "THEN 1 ELSE nro_com + 1 END )  WHERE bodega = " + bodega + " AND codigo ='" + codigo + "' " +
                    "AND mes = " + month + " and ano = " + year + " ";
        }
        return sql;
    }

    private Boolean existe_referencias_sto(String codigo, Integer bodega, Calendar fec, Context context){
        boolean resp = false;

        //Capturamos el mes en un String
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
        String month = dateFormatMoth.format(fec.getTime());

        //Capturamos el año en un String
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        String year = dateFormatYear.format(fec.getTime());

        String sql = "SELECT codigo FROM referencias_sto WHERE codigo = '" + codigo + "' AND mes = " + month + " AND ano = " + year + " AND bodega = " + bodega + " ";

        if (!conexion.obtenerCodigoReferencias(context, sql).equals("")){
            resp = true;
        }

        return resp;
    }

    private String crear_referencias_sto(String codigo , Integer bodega, Calendar fec){
        //Capturamos el mes en un String
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
        String mes = dateFormatMoth.format(fec.getTime());

        //Capturamos el año en un String
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        String ano = dateFormatYear.format(fec.getTime());

        return "INSERT INTO referencias_sto (bodega, codigo, ano, mes, can_ini, can_ent, can_sal, cos_ini, cos_ent, cos_sal, can_vta, cos_vta, val_vta, can_dev_vta, cos_dev_vta, val_dev, can_com, cos_com, can_dev_com, cos_dev_com, can_otr_ent, cos_otr_ent, can_otr_sal, cos_otr_sal, can_tra, cos_tra, sub_cos, baj_cos, nro_vta, nro_dev_vta, nro_com, nro_dev_com, nro_ped, can_ped, cos_ini_aju, cos_ent_aju, cos_sal_aju) VALUES (" + bodega + ", '" + codigo + "', " + ano + ", " + mes + ", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)";
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////// METODO PARA LA TRANSACCION DE DESCARGUE DE ALAMBRON ///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    public List<Object> listaTransaccionTableLayout_importaciones(int numero, TableLayout dt_codigos_valores, String bodega, Calendar dFec, String notas, String usuario, String tipo, Double nit_proveedor, @NonNull String modelo, Context context) throws SQLException {
        // Convertir modelo a "01" si es "1"

        if (modelo.equals("1")) {
            modelo = "01";
        }
        String sql;
        int seq = 0;
        List<Object> listSql = new ArrayList<Object>();
        double valorUnitario = 0;
        String nit = nit_proveedor.toString();
        double vrTotal = calcular_costo_total_importacion(dt_codigos_valores);
        int vendedor = 0;
        String pc = Build.MODEL;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String fecha_hora = dateFormat.format(dFec.getTime());
        String sFecha_hora = "";
        String sFecha = "";
        int swDoc = consultarSwTipo(tipo,context);
        int swDoc_lin = consultarSwTipo(tipo,context);
        double costo_kilo = 0;
        double porc_iva = conexion.obtenerIvaPorc(context);
        double vr_iva = 0;
        double vr_tot_mercancia = 0;

        // Calcular vr_iva, vrTotal y vr_tot_mercancia si tipo = "CMP1" y modelo = "01" o "03"
        if (tipo.equals("CMP1") && (modelo.equals("01") || modelo.equals("03"))) {
            vr_iva = vrTotal * porc_iva;
            vrTotal += vr_iva;
            vr_tot_mercancia = vrTotal - vr_iva;
        }

        // Actualizar swDoc_lin si swDoc es 11 o 12
        if (swDoc == 11) {
            swDoc_lin = swDoc;
        } else if (swDoc == 12) {
            swDoc_lin = 3;
        }

        // Insertar el registro en la tabla orden_prod
        if (obj_ordenprodLn.insertarProxMes(String.valueOf(dt_codigos_valores.getChildAt(0).getId()),context)) {
            dFec.add(Calendar.MONTH, 1);

            //Capturamos el mes en un String
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatMoth = new SimpleDateFormat("MM");
            String month = dateFormatMoth.format(dFec.getTime());

            //Capturamos el año en un String
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
            String year = dateFormatYear.format(dFec.getTime());

            sFecha_hora = year + "-" + month + "-01";

            //Capturamos las horas
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH");
            String hour = dateFormatHour.format(dFec.getTime());

            //Capturamos los minutos
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");
            String minute = dateFormatMinute.format(dFec.getTime());

            //Capturamos los segundos
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("ss");
            String seconds = dateFormatSeconds.format(dFec.getTime());

            fecha_hora = "" + month + "-" + "01" + "-" + year + " " + hour + ":" + minute + ":" + seconds + "";

            sFecha = sFecha_hora;

        }else{
            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatsFecha_hora = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            sFecha_hora = dateFormatsFecha_hora.format(calendar.getTime());

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatfecha_hora = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            fecha_hora = dateFormatfecha_hora.format(calendar.getTime());


            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormatsFecha = new SimpleDateFormat("yyyy-MM-dd");
            sFecha = dateFormatsFecha.format(calendar.getTime());
        }

        sql = "INSERT INTO documentos " +
                "(sw, tipo, numero, nit, fecha, vencimiento, valor_total, vendedor, valor_aplicado, " +
                "anulado, modelo, notas, usuario, pc, fecha_hora, bodega, duracion, concepto, centro_doc, spic, iva, valor_mercancia) " +
                "VALUES (" + swDoc + ",'" + tipo + "'," + numero + "," + nit + ",'" + sFecha + "','" + sFecha + "'," + vrTotal + "," + vendedor + "," + 0 + ",0 " +
                ",'" + modelo + "','" + notas + "','" + usuario + "','" + pc + "','" + sFecha + "'," + bodega + ",15,0,0,'S'," + vr_iva + "," + vr_tot_mercancia + ")";
        try {
            listSql.add(sql);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        for (int i = 1; i < dt_codigos_valores.getChildCount(); i++) {
            TableRow tableRow = (TableRow) dt_codigos_valores.getChildAt(i);
            String unitarioValor=((TextView) tableRow.getChildAt(4)).getText().toString();
            String kilo_costo=((TextView) tableRow.getChildAt(4)).getText().toString();
            valorUnitario = Double.parseDouble(unitarioValor);
            costo_kilo = Double.parseDouble(kilo_costo);
            seq = i;
            sql = "INSERT INTO documentos_lin(sw,tipo,numero,codigo,seq,fec,nit,cantidad,porcentaje_iva,valor_unitario,porcentaje_descuento " +
                    ",costo_unitario,adicional,vendedor,bodega,und,cantidad_und,cantidad_pedida,maneja_inventario,costo_unitario_sin,cantidad_dos) " +
                    "VALUES(" + swDoc_lin + ",'" + tipo + "'," + numero + ",'" + ((TextView) tableRow.getChildAt(0)).getText().toString() + "'," + seq + ",'" + sFecha + "'," + nit + "," + ((TextView) tableRow.getChildAt(1)).getText().toString() + ",16," + valorUnitario + ",0," +
                    "" + costo_kilo + ",'" + notas + "',0," + bodega + ",'UND',1,0,'S',0.0000000000000000,1) ";
            listSql.add(sql);
            //Script para ingresar a referencias_sto (STOCK)
            listSql.add(actualizarRefSto(Double.parseDouble(((TextView) tableRow.getChildAt(1)).getText().toString()), costo_kilo, ((TextView) tableRow.getChildAt(0)).getText().toString(), dFec, Integer.valueOf(bodega), swDoc));
            listSql.add(sqlActUltEntradaUltSalida(swDoc, ((TextView) tableRow.getChildAt(0)).getText().toString()));
        }

        // Continuar con la implementación del método...

        if (tipo.equals("CMP1") && (modelo.equals("01") || modelo.equals("03"))) {
            listSql.addAll(script_cuentas(tipo, modelo, (double) numero, vr_tot_mercancia, nit_proveedor, sFecha, context));
        }

        return  (listSql);
    }


    private double calcular_costo_total_importacion(TableLayout tableLayout) {
        double costo_total = 0;
        double cantidad = 0;
        double costo_unitario = 0;
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
            cantidad = Double.parseDouble(((TextView) tableRow.getChildAt(1)).getText().toString());
            costo_unitario = Double.parseDouble(((TextView) tableRow.getChildAt(4)).getText().toString());
            costo_total += cantidad * costo_unitario;
        }
        return costo_total;
    }

    public int consultarSwTipo(String tipo,Context context) throws SQLException {
        int sw = 0;
        String swString = String.valueOf(conexion.obtenerconsultaSwTipo(context,tipo));
        if (swString != null && !swString.isEmpty()) {
            sw = Integer.parseInt(swString);
        }
        return sw;
    }

    /*Se comenta ese codigo ya que se modifica debido a un error en contabilidad que ocurrio al hacer el descargue de alambron
    private List<Object> script_cuentas(String tipo, String modelo, Double numero, Double costo_total, Double nit, String fec,Context context) {
        List<Object> listSql = new ArrayList<Object>();
        String sql_consulta_tipo_transacciones_mod = "SELECT cta1,cta2,cta3 FROM tipo_transacciones_mod WHERE tipo ='" + tipo + "' AND modelo= '" + modelo + "'";
        List<Object> dt_cuentas = Collections.singletonList(conexion.lista_consulta_tipo_transacciones(context, sql_consulta_tipo_transacciones_mod));
        String sql_movimientos = "";
        String sql = "";
        Integer seq = 1;
        Double valor = 0.0;
        Double iva = conexion.obtenerIvaPorc(context);
        Double valor_iva = costo_total * iva;
        Double centro = 0.0;
        for (int j = 0; j < dt_cuentas.size(); j++) {
            Object item = dt_cuentas.get(j);
            switch (item.toString()) {
                case "220501":
                    valor = costo_total + valor_iva;
                    valor *= -1;
                    sql_movimientos = "INSERT INTO movimiento (tipo, numero,seq, nit, fec,  cuenta, centro, valor) VALUES ('" + tipo + "', " + numero + "," + seq + ", " + nit + ",'" + fec + "', " + item + ", " + centro + ", " + valor + ")";
                    listSql.add(sql_movimientos);
                    seq += 1;
                case "14050100" :
                    valor = costo_total;
                    sql_movimientos = "INSERT INTO movimiento (tipo, numero,seq, nit, fec,  cuenta, centro, valor) VALUES ('" + tipo + "', " + numero + "," + seq + ", " + nit + ", '" + fec + "', " + item + ", " + centro + ", " + valor + ")";
                    listSql.add(sql_movimientos);
                    seq += 1;
                case "24080505":
                    valor = valor_iva;
                    sql_movimientos = "INSERT INTO movimiento (tipo, numero,seq, nit, fec,  cuenta, centro, valor,base) VALUES ('" + tipo + "', " + numero + "," + seq + ", " + nit + ", '" + fec + "', " + item + ", " + centro + ", " + valor + "," + costo_total + ")";
                    listSql.add(sql_movimientos);
                    seq += 1;
            }
        }
        return listSql;
    }*/

    private List<Object> script_cuentas(String tipo, String modelo, Double numero, Double costo_total, Double nit, String fec,Context context) {
        if (modelo.equals("1")) {
            modelo = "01";
        }
        List<Object> listSql = new ArrayList<Object>();
        String sql_consulta_tipo_transacciones_mod = "SELECT cta1,cta2,cta3 FROM tipo_transacciones_mod WHERE tipo ='" + tipo + "' AND modelo= " + modelo + "";
        List<Object> dt_cuentas = Collections.singletonList(conexion.lista_consulta_tipo_transacciones(context, sql_consulta_tipo_transacciones_mod));
        String sql_movimientos = "";
        String sql = "";
        Integer seq = 1;
        Double valor = 0.0;
        Double iva = conexion.obtenerIvaPorc(context);
        Double valor_iva = costo_total * iva;
        Double centro = 0.0;

        String[] resultado = new String[4];
        try {
            Statement st = conexion.conexionBD("CORSAN", context).createStatement();
            ResultSet rs = st.executeQuery(sql_consulta_tipo_transacciones_mod);
            while (rs.next()){
                resultado[0] = rs.getString("cta1");
                resultado[1] = rs.getString("cta2");
                resultado[2] = rs.getString("cta3");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (!dt_cuentas.isEmpty()) {
            for (int j = 0; j < resultado.length-1; j++) {
                String codigoCuenta = resultado[j].toString();
                System.out.println("cuenta = " + codigoCuenta);
                switch (codigoCuenta) {
                    case "220501":
                        valor = costo_total + valor_iva;
                        valor *= -1;
                        sql_movimientos = "INSERT INTO movimiento (tipo, numero,seq, nit, fec,  cuenta, centro, valor) VALUES ('" + tipo + "', " + numero + "," + seq + ", " + nit + ",'" + fec + "', " + codigoCuenta + ", " + centro + ", " + valor + ")";
                        listSql.add(sql_movimientos);
                        seq += 1;
                        break; // Agrega un break al final de cada caso

                    case "14050100":
                        valor = costo_total;
                        sql_movimientos = "INSERT INTO movimiento (tipo, numero,seq, nit, fec,  cuenta, centro, valor) VALUES ('" + tipo + "', " + numero + "," + seq + ", " + nit + ", '" + fec + "', " + codigoCuenta + ", " + centro + ", " + valor + ")";
                        listSql.add(sql_movimientos);
                        seq += 1;
                        break;

                    case "24080505":
                        valor = valor_iva;
                        sql_movimientos = "INSERT INTO movimiento (tipo, numero,seq, nit, fec,  cuenta, centro, valor,base) VALUES ('" + tipo + "', " + numero + "," + seq + ", " + nit + ", '" + fec + "', " + codigoCuenta + ", " + centro + ", " + valor + "," + costo_total + ")";
                        listSql.add(sql_movimientos);
                        seq += 1;
                        break;
                }
            }
        }
        return listSql;
    }


}
