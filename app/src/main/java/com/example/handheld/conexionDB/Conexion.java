package com.example.handheld.conexionDB;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import com.example.handheld.modelos.BodegasModelo;
import com.example.handheld.modelos.CajasReceModelo;
import com.example.handheld.modelos.CajasRefeModelo;
import com.example.handheld.modelos.CentrosModelo;
import com.example.handheld.modelos.CodigoGalvModelo;
import com.example.handheld.modelos.CorreoModelo;
import com.example.handheld.modelos.CuentasModelo;
import com.example.handheld.modelos.EmpRecepcionadoCajasModelo;
import com.example.handheld.modelos.GalvRecepcionModelo;
import com.example.handheld.modelos.GalvRecepcionadoRollosModelo;
import com.example.handheld.modelos.InventarioModelo;
import com.example.handheld.modelos.LectorCodCargueModelo;
import com.example.handheld.modelos.MesasModelo;
import com.example.handheld.modelos.PedidoModelo;
import com.example.handheld.modelos.PermisoPersonaModelo;
import com.example.handheld.modelos.Persona;
import com.example.handheld.modelos.PersonaModelo;
import com.example.handheld.modelos.RolloGalvTransa;
import com.example.handheld.modelos.RolloTrefiRevisionModelo;
import com.example.handheld.modelos.RolloterminadoModelo;
import com.example.handheld.modelos.TipotransModelo;
import com.example.handheld.modelos.TrefiRecepcionModelo;
import com.example.handheld.modelos.TrefiRecepcionadoRollosModelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Conexion {

    @SuppressLint("NewApi")
    public Connection conexionBD(String dbname, Context Context){
        Connection cnn = null;
        String ip="10.10.10.246", port="1433", username = "Practicante.sistemas", password = "+Psis.*";
        StrictMode.ThreadPolicy politica = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(politica);
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            String connectionUrl= "jdbc:jtds:sqlserver://"+ip+":"+port+";databasename="+ dbname +";User="+username+";password="+password+";";
            cnn = DriverManager.getConnection(connectionUrl);
        }catch (Exception e){
            Toast.makeText(Context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return cnn;
    }
// obtiene la lista de muestras tomadas en galvanizado para ajustar traccion y zaba
    public List<Persona> obtenerListaMuestreo(Context context, String nit){
        List<Persona> persona = new ArrayList<>();
        Persona modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT Codigo, Diametro_inicial, Nro_bobina, Velocidad_bobina, Longitud,traccion, recubrimiento_zinc FROM F_det_muestreo_galvanizado WHERE nit = " + nit + " \n" +
                    "  AND CAST(fecha_hora AS DATE) = CAST(GETDATE() AS DATE)\n" +
                    "ORDER BY fecha_hora DESC; ");
            while (rs.next()){
                modelo = new Persona();
                modelo.setCodigo(rs.getString(1));
                modelo.setDiametro(rs.getDouble(2));
                modelo.setBobina(rs.getInt(3));
                modelo.setVelocidad_bobina(rs.getDouble(4));
                modelo.setLongitud(rs.getDouble(5));
                // Verificar si traccion es nulo o vacío y establecerlo en 0 si es así
                if (rs.getString(6) == null || rs.getString(6).isEmpty()) {
                    modelo.setTraccion(0);
                } else {
                    modelo.setTraccion(rs.getDouble(6));
                }

                // Verificar si recubrimiento_zinc es nulo o vacío y establecerlo en 0 si es así
                if (rs.getString(7) == null || rs.getString(7).isEmpty()) {
                    modelo.setZaba(0);
                } else {
                    modelo.setZaba(rs.getDouble(7));
                }
                persona.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return persona;
    }
    //metodo para obtener todos los datos de la bd y especificar 1
    public String valorTodo(Context context, String sql){
        String valor = "";
        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                valor = rs.getString(1);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return valor;
    }

    //obtener codigos y descripcion de la bd
    public ArrayList<CodigoGalvModelo> obtenerCodigos(Context context){
        ArrayList<CodigoGalvModelo> tipos = new ArrayList<>();
        CodigoGalvModelo Tipo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select r.descripcion,c.*\n" +
                    "from F_galv_longitud_codigo C INNER JOIN CORSAN.dbo.referencias r on r.codigo = c.codigo");
            while (rs.next()){
                Tipo = new CodigoGalvModelo();
                Tipo.setDescripcion(rs.getString("descripcion"));
                Tipo.setCodigo(rs.getString("codigo"));
                Tipo.setLongitud(rs.getString("longitud"));
                tipos.add(Tipo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return tipos;
    }

    //Obtiene datos de una persona en la BD
    public PersonaModelo obtenerPersona(Context context, String cedula){
        PersonaModelo persona = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT nombres, nit, centro, cargo FROM V_nom_personal_Activo_con_maquila " +
                    "WHERE nit = '" + cedula + "'");
            if (rs.next()){
                persona = new PersonaModelo(rs.getString("nombres"), rs.getString("nit"), rs.getString("centro"), rs.getString("cargo"));
            }else{
                persona = new PersonaModelo("", "","", "");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return persona;
    }

    //Obtiene datos del correo empresarial en la BD
    public CorreoModelo obtenerCorreo(Context context){
        CorreoModelo correo = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("select email, passwordApp from J_spic_servidores_correo where descripcion='EntranteG'");
            if (rs.next()){
                correo = new CorreoModelo(rs.getString("email"), rs.getString("passwordApp"));
            }else{
                correo = new CorreoModelo("", "");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return correo;
    }

    //Obtiene datos de una persona en la BD
    public PermisoPersonaModelo obtenerPermisoPersonaAlambron(Context context, String cedula, String permiso){
        PermisoPersonaModelo persona = null;
        ResultSet rs;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            if (permiso.equals("entrega")){
                rs = st.executeQuery("SELECT p.nit, p.permiso, p.modulo \n" +
                        "FROM jd_permisos_traslado_alambron p inner join CORSAN.dbo.V_nom_personal_Activo_con_maquila c on p.nit = c.nit\n" +
                        "where p.permiso = 'E' and p.nit = '" + cedula + "' and p.modulo ='mod_traslado_alambron_bod1_a_bod2'");
            }else{
                rs = st.executeQuery("SELECT p.nit, p.permiso, p.modulo  \n" +
                        "FROM jd_permisos_traslado_alambron p inner join CORSAN.dbo.V_nom_personal_Activo_con_maquila c on p.nit = c.nit\n" +
                        "where p.permiso = 'R' and p.nit = '" + cedula + "' and p.modulo ='mod_traslado_alambron_bod1_a_bod2'");
            }

            if (rs.next()){
                persona = new PermisoPersonaModelo(rs.getString("nit"), rs.getString("permiso"));
            }else{
                persona = new PermisoPersonaModelo("", "");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return persona;
    }

    public PermisoPersonaModelo obtenerPermisoPersona(Context context, String cedula, String modulo){
        PermisoPersonaModelo persona = null;
        ResultSet rs;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            rs = st.executeQuery("SELECT p.nit, p.permiso \n" +
                    "FROM jd_permisos_traslado_alambron p \n" +
                    "where p.nit = '" + cedula + "' and p.modulo ='" + modulo + "'");

            if (rs.next()){
                persona = new PermisoPersonaModelo(rs.getString("nit"), rs.getString("permiso"));
            }else{
                persona = new PermisoPersonaModelo("", "");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return persona;
    }

    //Obtiene un dato
    public String obtenerIdAlamImport(Context context, String sql){
        String id = "";
        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                id = rs.getString("id");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public Integer obtenerIdInv(Context context, String sql){
        int id = 0;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                id = Integer.parseInt(rs.getString("id"));
            }

        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    //Obtiene un dato
    public String obtenerPesoAlamImport(Context context, String sql){
        String peso = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                peso = rs.getString("peso");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return peso;
    }

    //Obtiene un dato
    public String obtenerCodigoAlamImport(Context context, String sql){
        String codigo = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                codigo = rs.getString("codigo");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return codigo;
    }

    //Obtiene un dato
    public String obtenerCodigo(Context context, String sql){
        String codigo = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                codigo = rs.getString("codigo");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return codigo;
    }

    //Obtiene un dato
    public String consultarStock(Context context,String codigo, String bodega){
        String Stock = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT stock,bodega FROM v_referencias_sto_hoy WHERE codigo = '" + codigo + "' and bodega = " + bodega + " ");
            if (rs.next()){
                Stock = rs.getString("stock");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return Stock;
    }

    //Obtiene un dato
    public String obtenerConsecutivo(Context context, String sql){
        String numero = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                numero = rs.getString("numero");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return numero;
    }

    //Obtiene un dato
    public String obtenerDescripcionCodigo(Context context, String sql){
        String descripcion = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                descripcion = rs.getString("descripcion");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return descripcion;
    }

    //Obtiene un dato
    public String obtenerGenericoCodigo(Context context, String sql){
        String generico = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                generico = rs.getString("generico");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return generico;
    }

    //Obtiene un dato
    public String obtenerCodigoReferencias(Context context, String sql){
        String codigo = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                codigo = rs.getString("codigo");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return codigo;
    }

    //Obtiene un dato
    public String obtenerDescripcionReferencias(Context context, String referencia){
        String descripcion = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("select descripcion from referencias where codigo='"+ referencia +"' and ref_anulada = 'N' and grupo IN ('311','312')");
            if (rs.next()){
                descripcion = rs.getString("descripcion");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return descripcion;
    }

    //Obtiene un dato
    public String obtenerConversionReferencias(Context context, String referencia){
        String conversion = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("select conversion from referencias where codigo='"+ referencia +"' and ref_anulada = 'N'");
            if (rs.next()){
                conversion = rs.getString("conversion");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return conversion;
    }


    //Obtiene un dato
    public String obtenerMes(Context context, String sql){
        String mes = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                mes = rs.getString("mes");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return mes;
    }

    //Obtiene un dato
    public String obtenerCostoUnit(Context context, String sql){
        String costo_kilo = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                costo_kilo = rs.getString("costo_kilo");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return costo_kilo;
    }

    //Obtiene un dato
    public Double obtenerIvaReferencia(Context context, String cod){
        Double iva = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("select porcentaje_iva from referencias where codigo = '"+ cod +"'");
            if (rs.next()){
                iva = rs.getDouble("porcentaje_iva");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return iva;
    }

    public String obtenerEmpresa(Context context, String fecha){
        String Operario = null;
        String Empresa = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select OPERARIO from F_Recepcion_puntilleria where FECHA_RECEPCIONADO = '"+ fecha +"' group by OPERARIO");
            if (rs.next()){
                Operario = rs.getString("OPERARIO");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("select Empresa from V_nom_personal_Activo_con_maquila where nit = '"+ Operario +"'");
            if (rs.next()){
                Empresa = rs.getString("Empresa");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return Empresa;
    }

    //Obtiene un dato
    public String obtenerCantidadPedido(Context context, String sql){
        String cantidad = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                cantidad = rs.getString("pendiente");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return cantidad;
    }

    //Obtiene un dato
    public String obtenerNumTranAlamImport(Context context, String sql){
        String numImport = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                numImport = rs.getString("num_importacion");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return numImport;
    }

    //Obtiene un dato
    public boolean existeCodigo(Context context, String codigo){
        String Pcodigo;
        boolean resp = false;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT codigo FROM referencias WHERE codigo = '" + codigo + "'");
            if (rs.next()){
                Pcodigo = rs.getString("codigo");
                if (!Pcodigo.equals("")){
                    resp = true;
                }
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return resp;
    }

    //Obtiene un dato
    public boolean existeTipoTransaccion(Context context, String tipoSpinner){
        String tipo;
        boolean resp = false;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT tipo FROM tipo_transacciones WHERE tipo = '" + tipoSpinner + "'");
            if (rs.next()){
                tipo = rs.getString("tipo");
                if (!tipo.equals("")){
                    resp = true;
                }
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return resp;
    }

    public ArrayList<TipotransModelo> obtenerTipos(Context context){
        ArrayList<TipotransModelo> tipos = new ArrayList<>();
        TipotransModelo Tipo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT T.tipo,T.sw FROM  tipo_transacciones T WHERE T.tipo = 'TRB1' ");
            while (rs.next()){
                Tipo = new TipotransModelo();
                Tipo.setTipo(rs.getString("tipo"));
                Tipo.setSw(rs.getString("sw"));
                tipos.add(Tipo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return tipos;
    }

    public ArrayList<InventarioModelo> obtenerInven(Context context, String sql){
        ArrayList<InventarioModelo> inventarios = new ArrayList<>();
        InventarioModelo Inventario;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                Inventario = new InventarioModelo();
                Inventario.setId(rs.getString("id"));
                Inventario.setCodigo(rs.getString("codigo"));
                Inventario.setBodega(rs.getString("bodega"));
                inventarios.add(Inventario);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return inventarios;
    }

    public ArrayList<RolloterminadoModelo> obtenerRollosTerm(Context context, String sql){
        ArrayList<RolloterminadoModelo> terminados = new ArrayList<>();
        RolloterminadoModelo Terminado;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                Terminado = new RolloterminadoModelo();
                Terminado.setCod_orden(rs.getString("cod_orden"));
                Terminado.setId_detalle(rs.getString("id_detalle"));
                Terminado.setId_rollo(rs.getString("id_rollo"));
                terminados.add(Terminado);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return terminados;
    }

    public ArrayList<CentrosModelo> obtenerCentros(Context context){
        ArrayList<CentrosModelo> centros = new ArrayList<>();
        CentrosModelo Centro;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT centro,(CONVERT(varchar, centro) + '--' + descripcion) AS descripcion FROM centros WHERE centro IN (2100,2200,2300,5200,6400)");
            while (rs.next()){
                Centro = new CentrosModelo();
                Centro.setCentro(rs.getString("centro"));
                Centro.setDescripcion(rs.getString("descripcion"));
                centros.add(Centro);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return centros;
    }

    public boolean eliminarTiqueteUnico(Context context, String num_importacion, String num_rollo, String nit_proveedor, String detalle){
        boolean resp = false;
        Connection connection = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context);
        try {
            if (connection != null){
                PreparedStatement stm = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).prepareStatement("DELETE FROM  J_alambron_importacion_det_rollos WHERE num_importacion =" + num_importacion + " AND numero_rollo = " + num_rollo + " AND nit_proveedor=" + nit_proveedor + " AND id_solicitud_det =" + detalle);
                stm.executeQuery();
                resp = true;
                Toast.makeText(context, "Tiquete borrado", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return resp;
    }

    public List<PedidoModelo> obtenerPedidos(Context context){
        List<PedidoModelo> pedidos = new ArrayList<>();
        PedidoModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT E.numero,D.id_detalle,E.fecha,D.codigo,(D.cantidad - (SELECT COUNT(numero) FROM J_salida_alambron_transaccion  WHERE numero = D.numero AND id_detalle = D.id_detalle))As pendiente,R.descripcion \n" +
                    "                               FROM J_salida_alambron_enc E ,J_salida_alambron_det D, CORSAN.dbo.referencias R \n" +
                    "                                  WHERE E.anulado is null AND  R.codigo = D.codigo AND D.numero = E.numero AND (D.cantidad - (SELECT COUNT(numero) FROM J_salida_alambron_transaccion  WHERE numero = D.numero AND id_detalle = D.id_detalle)) > 0 AND (e.devolver = 'N' OR e.devolver IS NULL ) \n" +
                    "                                    ORDER BY E.fecha");
            while (rs.next()){
                modelo = new PedidoModelo();
                modelo.setNumero(Integer.valueOf(rs.getString("numero")));
                modelo.setIdDetalle(Integer.valueOf(rs.getString("id_detalle")));
                modelo.setFecha(rs.getString("fecha"));
                modelo.setCodigo(rs.getString("codigo"));
                modelo.setPendiente(rs.getString("pendiente"));
                modelo.setDescripcion(rs.getString("descripcion"));
                pedidos.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return pedidos;
    }

    public List<MesasModelo> obtenerMesas(Context context, String sql){
        List<MesasModelo> mesas = new ArrayList<>();
        MesasModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                modelo = new MesasModelo();
                modelo.setMesa(rs.getString("mesa"));
                modelo.setCantidad(rs.getString("cantidad"));
                mesas.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return mesas;
    }

    public List<GalvRecepcionModelo> obtenerGalvTerminado(Context context){
        List<GalvRecepcionModelo> galvTerminado = new ArrayList<>();
        GalvRecepcionModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT R.nro_orden,R.consecutivo_rollo as nro_rollo,S.final_galv,ref.descripcion,R.peso  \n" +
                                                "FROM D_rollo_galvanizado_f R, D_orden_pro_galv_enc S,CORSAN.dbo.referencias ref,CORSAN.dbo.V_nom_personal_Activo_con_maquila ter \n" +
                                                "where R.nro_orden = S.consecutivo_orden_G And ref.codigo = S.final_galv and ter.nit=R.nit_operario AND R.no_conforme is null and R.anular is null and R.recepcionado is null and R.trb1 is null and S.final_galv LIKE '33G%' and R.tipo_transacion is null\n" +
                                                "order by ref.descripcion");
            while (rs.next()){
                modelo = new GalvRecepcionModelo();
                modelo.setNro_orden(rs.getString("nro_orden"));
                modelo.setNro_rollo(rs.getString("nro_rollo"));
                modelo.setReferencia(rs.getString("final_galv"));
                modelo.setDescripcion(rs.getString("descripcion"));
                modelo.setPeso(String.valueOf(rs.getInt("peso")));
                modelo.setColor("RED");
                galvTerminado.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return galvTerminado;
    }

    public List<GalvRecepcionModelo> consultarGalvIncomple(Context context){
        List<GalvRecepcionModelo> galvTerminado = new ArrayList<>();
        GalvRecepcionModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT R.nro_orden,R.consecutivo_rollo as nro_rollo,S.final_galv,ref.descripcion,R.peso \n" +
                    "FROM D_rollo_galvanizado_f R, D_orden_pro_galv_enc S,CORSAN.dbo.referencias ref,CORSAN.dbo.V_nom_personal_Activo_con_maquila ter \n" +
                    "where R.nro_orden = S.consecutivo_orden_G And ref.codigo = S.final_galv and ter.nit=R.nit_operario AND R.no_conforme is null and R.anular is null and R.recepcionado is not null and trb1 is null and R.tipo_transacion is null and S.final_galv LIKE '33G%' \n" +
                    "order by ref.descripcion");
            while (rs.next()){
                modelo = new GalvRecepcionModelo();
                modelo.setNro_orden(rs.getString("nro_orden"));
                modelo.setNro_rollo(rs.getString("nro_rollo"));
                modelo.setReferencia(rs.getString("final_galv"));
                modelo.setDescripcion(rs.getString("descripcion"));
                modelo.setPeso(String.valueOf(rs.getInt("peso")));
                modelo.setColor("GREEN");
                galvTerminado.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return galvTerminado;
    }

    public Integer consultarReviTrefiIncomple(Context context){
        int id_revision = 0;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select id_revision from jd_revision_calidad_trefilacion where estado='R' and num_transa is null");
            if(rs.next()){
                id_revision = rs.getInt("id_revision");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return id_revision;
    }

    public List<TrefiRecepcionModelo> consultarTrefiIncomple(Context context){
        List<TrefiRecepcionModelo> trefiTerminado = new ArrayList<>();
        TrefiRecepcionModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select R.cod_orden,R.id_detalle,R.id_rollo, O.prod_final,Ref.descripcion, R.peso\n" +
                    "from J_rollos_tref R inner join J_orden_prod_tef O on R.cod_orden = O.consecutivo inner join CORSAN.dbo.referencias Ref on O.prod_final = Ref.codigo inner join jd_revision_calidad_trefilacion Rev on R.id_revision = Rev.id_revision\n" +
                    "where O.prod_final like '33%' and R.recepcionado is not null and R.trb1 is null and R.id_revision is not null and R.anulado is null and R.no_conforme is null  and R.motivo is null and R.traslado is null and\n" +
                    "R.saga is null and R.bobina is null and R.scla is null and R.destino is null and R.srec is null and R.scal is null and R.scae is null and R.sar is null and R.sav is null and Rev.estado='A'");
            while (rs.next()){
                modelo = new TrefiRecepcionModelo();
                modelo.setCod_orden(rs.getString("cod_orden"));
                modelo.setId_detalle(rs.getString("id_detalle"));
                modelo.setId_rollo(rs.getString("id_rollo"));
                modelo.setReferencia(rs.getString("prod_final"));
                modelo.setDescripcion(rs.getString("descripcion"));
                modelo.setPeso(String.valueOf(rs.getInt("peso")));
                modelo.setColor("RED");
                trefiTerminado.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return trefiTerminado;
    }

    public List<TrefiRecepcionModelo> obtenerTrefiRevision(Context context){
        List<TrefiRecepcionModelo> trefiTerminado = new ArrayList<>();
        TrefiRecepcionModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select R.cod_orden,R.id_detalle,R.id_rollo, O.prod_final,Ref.descripcion, R.peso\n" +
                    "from J_rollos_tref R inner join J_orden_prod_tef O on R.cod_orden = O.consecutivo inner join CORSAN.dbo.referencias Ref on O.prod_final = Ref.codigo\n" +
                    "where O.prod_final like '33%' and R.recepcionado is null and R.id_revision is null and R.anulado is null and R.no_conforme is null  and R.motivo is null and R.traslado is null and\n" +
                    "R.saga is null and R.bobina is null and R.scla is null and R.destino is null and R.srec is null and R.scal is null and R.scae is null and R.sar is null and R.sav is null and R.manuales is null");
            while (rs.next()){
                modelo = new TrefiRecepcionModelo();
                modelo.setCod_orden(rs.getString("cod_orden"));
                modelo.setId_detalle(rs.getString("id_detalle"));
                modelo.setId_rollo(rs.getString("id_rollo"));
                modelo.setReferencia(rs.getString("prod_final"));
                modelo.setDescripcion(rs.getString("descripcion"));
                modelo.setPeso(String.valueOf(rs.getInt("peso")));
                modelo.setColor("RED");
                trefiTerminado.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return trefiTerminado;
    }

    public RolloTrefiRevisionModelo obtenerRolloRevision(Context context, String cod_orden,String id_detalle, String id_rollo){
        RolloTrefiRevisionModelo modelo;
        modelo = new RolloTrefiRevisionModelo("","");

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select R.id_revision, C.estado\n" +
                    "from J_rollos_tref R inner join J_orden_prod_tef O on R.cod_orden = O.consecutivo inner join jd_revision_calidad_trefilacion C on C.id_revision = R.id_revision\n" +
                    "where O.prod_final like '33%' and R.recepcionado is null and R.id_revision is not null and R.anulado is null \n" +
                    "and R.cod_orden = '" + cod_orden + "' and R.id_detalle = '" + id_detalle + "' and R.id_rollo = '" + id_rollo + "'");
            if (rs.next()){
                modelo.setId_revision(rs.getString("id_revision"));
                modelo.setEstado(rs.getString("estado"));
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return modelo;
    }

    public RolloGalvTransa obtenerRolloTransGalv(Context context, String nro_orden, String consecutivo_rollo){
        RolloGalvTransa modelo;
        modelo = new RolloGalvTransa("","","","","");

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT R.nro_orden,R.consecutivo_rollo as nro_rollo,S.final_galv as referencia,R.fecha_recepcion, R.trb1\n" +
                    "FROM D_rollo_galvanizado_f R, D_orden_pro_galv_enc S\n" +
                    "where R.nro_orden = S.consecutivo_orden_G and R.no_conforme is null and R.anular is null \n" +
                    "and R.recepcionado is not null and R.trb1 is not null and S.final_galv LIKE '33G%' and R.tipo_transacion is not null and R.nro_orden='" + nro_orden + "' and R.consecutivo_rollo='" + consecutivo_rollo + "'");
            if (rs.next()){
                modelo.setNro_orden(rs.getString("nro_orden"));
                modelo.setNro_rollo(rs.getString("nro_rollo"));
                modelo.setReferencia(rs.getString("referencia"));
                modelo.setFecha_recepcion(rs.getString("fecha_recepcion"));
                modelo.setTrb1(rs.getString("trb1"));
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return modelo;
    }

    public List<TrefiRecepcionModelo> obtenerReviTrefiTerminado(Context context, Integer id_revision){
        List<TrefiRecepcionModelo> trefiTerminado = new ArrayList<>();
        TrefiRecepcionModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select R.cod_orden,R.id_detalle,R.id_rollo, O.prod_final,Ref.descripcion, R.peso\n" +
                    "from J_rollos_tref R inner join J_orden_prod_tef O on R.cod_orden = O.consecutivo inner join CORSAN.dbo.referencias Ref on O.prod_final = Ref.codigo\n" +
                    "where O.prod_final like '33%' and R.recepcionado is null and R.id_revision = " + id_revision + " and R.anulado is null and R.no_conforme is null  and R.motivo is null and R.traslado is null and\n" +
                    "R.saga is null and R.bobina is null and R.scla is null and R.destino is null and R.srec is null and R.scal is null and R.scae is null and R.sar is null and R.sav is null");
            while (rs.next()){
                modelo = new TrefiRecepcionModelo();
                modelo.setCod_orden(rs.getString("cod_orden"));
                modelo.setId_detalle(rs.getString("id_detalle"));
                modelo.setId_rollo(rs.getString("id_rollo"));
                modelo.setReferencia(rs.getString("prod_final"));
                modelo.setDescripcion(rs.getString("descripcion"));
                modelo.setPeso(String.valueOf(rs.getInt("peso")));
                modelo.setColor("GREEN");
                trefiTerminado.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return trefiTerminado;
    }

    public List<TrefiRecepcionModelo> obtenerTrefiTerminado(Context context){
        List<TrefiRecepcionModelo> trefiTerminado = new ArrayList<>();
        TrefiRecepcionModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select R.cod_orden,R.id_detalle,R.id_rollo, O.prod_final,Ref.descripcion, R.peso\n" +
                    "from J_rollos_tref R inner join J_orden_prod_tef O on R.cod_orden = O.consecutivo inner join CORSAN.dbo.referencias Ref on O.prod_final = Ref.codigo inner join jd_revision_calidad_trefilacion Rev on R.id_revision = Rev.id_revision\n" +
                    "where O.prod_final like '33%' and R.recepcionado is null and R.trb1 is null and R.id_revision is not null and R.anulado is null and R.no_conforme is null  and R.motivo is null and R.traslado is null and\n" +
                    "R.saga is null and R.bobina is null and R.scla is null and R.destino is null and R.srec is null and R.scal is null and R.scae is null and R.sar is null and R.sav is null and Rev.estado='A'");
            while (rs.next()){
                modelo = new TrefiRecepcionModelo();
                modelo.setCod_orden(rs.getString("cod_orden"));
                modelo.setId_detalle(rs.getString("id_detalle"));
                modelo.setId_rollo(rs.getString("id_rollo"));
                modelo.setReferencia(rs.getString("prod_final"));
                modelo.setDescripcion(rs.getString("descripcion"));
                modelo.setPeso(String.valueOf(rs.getInt("peso")));
                modelo.setColor("RED");
                trefiTerminado.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return trefiTerminado;
    }

    public List<CajasRefeModelo> obtenerCajasRefe(Context context, String mesa, String referencia){
        List<CajasRefeModelo> cajasRefe = new ArrayList<>();
        CajasRefeModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select FECHA,REFERENCIA,MESA,CANTIDAD from F_Recepcion_puntilleria where MESA = '"+ mesa +"' and REFERENCIA='"+ referencia +"' and RECEPCIONADO is null order by cantidad desc");
            while (rs.next()){
                modelo = new CajasRefeModelo();
                modelo.setFecha(rs.getString("FECHA"));
                modelo.setReferencia(rs.getString("REFERENCIA"));
                modelo.setMesa(rs.getString("MESA"));
                modelo.setCantidad(rs.getInt("CANTIDAD"));
                cajasRefe.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return cajasRefe;
    }

    public List<GalvRecepcionModelo> obtenerRefeCajasTermi(Context context, String fecha_inicio, String fecha_final){
        List<GalvRecepcionModelo> galvTerminado = new ArrayList<>();
        GalvRecepcionModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT R.nro_orden,R.consecutivo_rollo as nro_rollo,S.final_galv,ref.descripcion,R.peso  \n" +
                    "FROM D_rollo_galvanizado_f R, D_orden_pro_galv_enc S,CORSAN.dbo.referencias ref,CORSAN.dbo.V_nom_personal_Activo_con_maquila ter \n" +
                    "where R.nro_orden = S.consecutivo_orden_G And ref.codigo = S.final_galv and ter.nit=R.nit_operario AND R.fecha_hora >= '"+ fecha_inicio +"' AND  R.fecha_hora  <= '"+ fecha_final +"' and R.no_conforme is null and R.anular is null and R.recepcionado is null and S.final_galv LIKE '33G%'\n" +
                    "order by ref.descripcion");
            while (rs.next()){
                modelo = new GalvRecepcionModelo();
                modelo.setNro_orden(rs.getString("nro_orden"));
                modelo.setNro_rollo(rs.getString("nro_rollo"));
                modelo.setReferencia(rs.getString("final_galv"));
                modelo.setDescripcion(rs.getString("descripcion"));
                modelo.setPeso(String.valueOf(rs.getInt("peso")));
                modelo.setColor("RED");
                galvTerminado.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return galvTerminado;
    }

    public List<CajasReceModelo> obtenerCajasRecepcionar(Context context, String sql){
        List<CajasReceModelo> cajasRecep = new ArrayList<>();
        CajasReceModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                modelo = new CajasReceModelo();
                modelo.setReferencia(rs.getString("REFERENCIA"));
                modelo.setCantidad(rs.getString("cantidad"));
                cajasRecep.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return cajasRecep;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //
    public List<GalvRecepcionadoRollosModelo> galvRefeRecepcionados(Context context, String fecha_recepcion, String month, String year){
        List<GalvRecepcionadoRollosModelo> refeRecepcionados = new ArrayList<>();
        GalvRecepcionadoRollosModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select sum(R.peso) as peso, (SELECT p.promedio from corsan.dbo.v_promedio p where codigo = O.final_galv and P.ano = "+ year +" and P.mes = "+ month +")  as promedio, (select costo_unitario from CORSAN.dbo.referencias R where codigo = O.final_galv) as costo_unitario , O.final_galv " +
                    "from D_rollo_galvanizado_f R inner join D_orden_pro_galv_enc O on O.consecutivo_orden_G = R.nro_orden " +
                    "where R.recepcionado is not null and R.fecha_recepcion = '"+ fecha_recepcion +"' and R.no_conforme is null and O.final_galv like '33%' " +
                    "group by O.final_galv");
            while (rs.next()){
                modelo = new GalvRecepcionadoRollosModelo();
                modelo.setPeso(rs.getDouble("peso"));
                modelo.setPromedio(rs.getDouble("promedio"));
                modelo.setCosto_unitario(rs.getDouble("costo_unitario"));
                modelo.setReferencia(rs.getString("final_galv"));
                refeRecepcionados.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return refeRecepcionados;
    }

    public List<TrefiRecepcionadoRollosModelo> trefiRefeRecepcionados(Context context, String fecha_recepcion, String month, String year){
        List<TrefiRecepcionadoRollosModelo> refeRecepcionados = new ArrayList<>();
        TrefiRecepcionadoRollosModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select sum(R.peso) as peso, (SELECT p.promedio from corsan.dbo.v_promedio p where codigo = O.prod_final and P.ano = "+ year +" and P.mes = "+ month +")  as promedio, (select costo_unitario from CORSAN.dbo.referencias R where codigo = O.prod_final) as costo_unitario , O.prod_final " +
                    "from J_rollos_tref R inner join J_orden_prod_tef O on O.consecutivo = R.cod_orden " +
                    "where R.recepcionado is not null and R.fecha_recepcion = '"+ fecha_recepcion +"' and R.no_conforme is null and O.prod_final like '33%' " +
                    "group by O.prod_final");

            while (rs.next()){
                modelo = new TrefiRecepcionadoRollosModelo();
                modelo.setPeso(rs.getDouble("peso"));
                modelo.setPromedio(rs.getDouble("promedio"));
                modelo.setCosto_unitario(rs.getDouble("costo_unitario"));
                modelo.setReferencia(rs.getString("prod_final"));
                refeRecepcionados.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return refeRecepcionados;
    }

    public List<TrefiRecepcionadoRollosModelo> trefiRefeRevisados(Context context, Integer numero_revision, String month, String year){
        List<TrefiRecepcionadoRollosModelo> refeRecepcionados = new ArrayList<>();
        TrefiRecepcionadoRollosModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select sum(R.peso) as peso, (SELECT p.promedio from corsan.dbo.v_promedio p where codigo = O.prod_final and P.ano = "+ year +" and P.mes = "+ month +")  as promedio, (select costo_unitario from CORSAN.dbo.referencias R where codigo = O.prod_final) as costo_unitario , O.prod_final " +
                    "from J_rollos_tref R inner join J_orden_prod_tef O on O.consecutivo = R.cod_orden " +
                    "where R.recepcionado is null and R.id_revision = '"+ numero_revision.toString() +"' and R.no_conforme is null and O.prod_final like '33%' " +
                    "group by O.prod_final");

            while (rs.next()){
                modelo = new TrefiRecepcionadoRollosModelo();
                modelo.setPeso(rs.getDouble("peso"));
                modelo.setPromedio(rs.getDouble("promedio"));
                modelo.setCosto_unitario(rs.getDouble("costo_unitario"));
                modelo.setReferencia(rs.getString("prod_final"));
                refeRecepcionados.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return refeRecepcionados;
    }

    public List<EmpRecepcionadoCajasModelo> empaRefeRecepcionados(Context context, String fecha_recepcion, String month, String year){
        List<EmpRecepcionadoCajasModelo> refeRecepcionados = new ArrayList<>();
        EmpRecepcionadoCajasModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery("select sum(R.CANTIDAD) as cantidad, (SELECT p.promedio from corsan.dbo.v_promedio p where codigo = REFERENCIA and P.ano = "+ year +" and P.mes = "+ month +")  as promedio, (select costo_unitario from CORSAN.dbo.referencias R where codigo = REFERENCIA) as costo_unitario , REFERENCIA " +
                    "from F_Recepcion_puntilleria R " +
                    "where R.RECEPCIONADO is not null and R.FECHA_RECEPCIONADO = '"+ fecha_recepcion +"' " +
                    "group by REFERENCIA");
            while (rs.next()){
                modelo = new EmpRecepcionadoCajasModelo();
                modelo.setCantidad(rs.getDouble("cantidad"));
                modelo.setPromedio(rs.getDouble("promedio"));
                modelo.setCosto_unitario(rs.getDouble("costo_unitario"));
                modelo.setREFERENCIA(rs.getString("REFERENCIA"));
                refeRecepcionados.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return refeRecepcionados;
    }


    // Consultas agregadas para Descargue de Alambron

    //Obtener dato de IdRequisición

    public String obtenerIdAlamRequesicion(Context context, String sql){
        String id_Inirequisicion = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            //ResultSet rs = st.executeQuery("SELECT (CASE WHEN MAX(id) IS NULL THEN 1 ELSE MAX(id)+1 END) as id FROM J_alambron_requisicion");
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                id_Inirequisicion = rs.getString("id");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return id_Inirequisicion;
    }

    //Obtener nombre del proveedor
    public String obtenerNombreProveedor(Context context, String sql){
        String nombreProveedor = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                nombreProveedor = rs.getString("nombres");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return nombreProveedor;
    }

    public Double obtenerIvaPorc(Context context){
        Double porcentaje = null;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT porcentaje FROM J_iva_porcentaje");
            if (rs.next()){
                porcentaje = rs.getDouble("porcentaje");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return porcentaje;
    }



    public int obtenerconsultaSwTipo(Context context, String tipo){
        int sw = 0;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery("SELECT sw FROM tipo_transacciones WHERE tipo = '" + tipo + "'");
            if (rs.next()){
                sw = rs.getInt("sw");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return sw;
    }

    public List<CuentasModelo> lista_consulta_tipo_transacciones(Context context, String sql){
        List<CuentasModelo> consulta_tipo_transacciones = new ArrayList<>();
        CuentasModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                modelo = new CuentasModelo();
                modelo.setCta1(rs.getString("cta1"));
                modelo.setCta2(rs.getString("cta2"));
                modelo.setCta3(rs.getString("cta3"));
                consulta_tipo_transacciones.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return consulta_tipo_transacciones;
    }


    //Obtiene los datos de las requisiciones iniciadas y que no fueron cerradas
    public ArrayList<LectorCodCargueModelo> lista_pendientes_requisicion(Context context, String sql){
        ArrayList<LectorCodCargueModelo> consulta_pendientes_requision = new ArrayList<>();
        LectorCodCargueModelo modelo;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){

                modelo = new LectorCodCargueModelo();
                modelo.setNit_proveedor(rs.getString("nit_proveedor"));
                modelo.setNum_imp(rs.getString("numero_importacion"));
                modelo.setDetalle(rs.getString("id_det"));
                modelo.setNum_rolloAlambron(rs.getString("numero_rollo"));
                modelo.setNumero_transaccion(rs.getString("id_requisicion"));
                modelo.setConsecutivo(rs.getString("id_requisicion"));
                modelo.setPesoAlambron(rs.getString("peso"));
                modelo.setCodigoalambron(rs.getString("codigo"));
                modelo.setCosto_unitario_alambron(rs.getString("costo_kilo"));
                modelo.setEstado_muestra("0");
                modelo.setNumero_rollos_descargar(rs.getString("num_rollos"));
                consulta_pendientes_requision.add(modelo);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return consulta_pendientes_requision;
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////// OBTENER DATOS PARA CONSULTAS EN INVENTARIOS //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public List<BodegasModelo> listarBodegas(Context context, String sql){
        List<BodegasModelo> consulta_bodegas = new ArrayList<>();
        BodegasModelo modelo;


        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(1), context).createStatement();
            ResultSet rs = st.executeQuery(sql);

            /*modelo.setBodega("7");
            modelo.setDescripcion("(2)-BODEGA (BRILLANTE,ESPECIAL,RECOCIDO)");
            consulta_bodegas.add(modelo);*/
            while (rs.next()){
                modelo = new BodegasModelo();
                modelo.setBodega(rs.getString("bodega"));
                modelo.setDescripcion(rs.getString("descripcion"));
                consulta_bodegas.add(modelo);
            }
            modelo = new BodegasModelo();
            modelo.setBodega("7");
            modelo.setDescripcion("(2)-BODEGA (BRILLANTE,ESPECIAL,RECOCIDO)");
            consulta_bodegas.add(modelo);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return consulta_bodegas;
    }


    //Obtener dato de Id_inventario

    public String obtenerIdInventario(Context context, String sql){
        String id_inventario = "";

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            //ResultSet rs = st.executeQuery("SELECT (CASE WHEN MAX(id) IS NULL THEN 1 ELSE MAX(id)+1 END) as id FROM J_alambron_requisicion");
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                id_inventario = rs.getString("id");
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return id_inventario;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///Obtener datos para revision calidad
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public Integer obtenerIdRevision(Context context, String sql){
        int id = 0;

        try {
            Statement st = conexionBD(ConfiguracionBD.obtenerNombreBD(2), context).createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                id = rs.getInt("id_revision");
            }

        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return id;
    }




}
