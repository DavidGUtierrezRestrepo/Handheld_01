package com.example.handheld.ClasesOperativas;

import android.content.Context;
import android.widget.Toast;

import com.example.handheld.conexionDB.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class objOperacionesDb {
    public Integer ejecutarUpdate(String Sql, Context context) throws SQLException {
        int resp = 0;
        Connection cnn = new Conexion().conexionBD("JJVDMSCIERREAGOSTO",context);
        try {
            if (cnn != null){
                PreparedStatement stm = cnn.prepareStatement(Sql);
                stm.executeUpdate();
                resp = 1;
                //Toast.makeText(context, "REGISTRO MODIFICADO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        assert cnn != null;
        cnn.close();
        return resp;
    }

    public Integer ejecutarInsertJjprgproduccion(String Sql, Context context) throws SQLException {
        int resp = 0;
        Connection cnn = new Conexion().conexionBD("JJVPRGPRODUCCION",context);
        try {
            if (cnn != null){
                PreparedStatement stm = cnn.prepareStatement(Sql);
                stm.executeUpdate();
                resp = 1;
                //Toast.makeText(context, "REGISTRO MODIFICADO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        assert cnn != null;
        cnn.close();
        return resp;
    }

    public Integer ejecutarUpdateDbProduccion(String Sql, Context context) throws SQLException {
        int resp = 0;
        Connection cnn = new Conexion().conexionBD("JJVPRGPRODUCCION",context);
        try {
            if (cnn != null){
                PreparedStatement stm = cnn.prepareStatement(Sql);
                stm.executeUpdate();
                resp = 1;
                //Toast.makeText(context, "REGISTRO MODIFICADO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        assert cnn != null;
        cnn.close();
        return resp;
    }

    public Integer ejecutarUpdateProduccion(String Sql, Context context) throws SQLException {
        int resp = 0;
        Connection cnn = new Conexion().conexionBD("JJVDMSCIERREAGOSTO",context);
        try {
            if (cnn != null){
                PreparedStatement stm = cnn.prepareStatement(Sql);
                stm.executeUpdate();
                resp = 1;
                //Toast.makeText(context, "REGISTRO MODIFICADO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        assert cnn != null;
        cnn.close();
        return resp;
    }


    public ArrayList<HashMap<String, Object>> listadoDatosProduccionHasp(String Sql, Context context) throws SQLException {
        ArrayList<HashMap<String, Object>> resultados = new ArrayList<>();
        Connection cnn = new Conexion().conexionBD("JJVPRGPRODUCCION",context);
        try {
            if (cnn != null){
                PreparedStatement stm = cnn.prepareStatement(Sql);
                ResultSet rs = stm.executeQuery();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while(rs.next()) {
                    HashMap<String, Object> fila = new HashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object columnValue = rs.getObject(i);
                        fila.put(columnName, columnValue);
                    }

                    resultados.add(fila);
                }
            }

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        assert cnn != null;
        cnn.close();
        return resultados;
    }



    public Integer ejecutarInsertCorsan(String Sql, Context context) throws SQLException {
        int resp = 0;
        Connection cnn = new Conexion().conexionBD("JJVDMSCIERREAGOSTO",context);
        try {
            if (cnn != null){
                PreparedStatement stm = cnn.prepareStatement(Sql);
                stm.executeUpdate();
                resp = 1;
                //Toast.makeText(context, "REGISTRO MODIFICADO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        assert cnn != null;
        cnn.close();
        return resp;
    }


}
