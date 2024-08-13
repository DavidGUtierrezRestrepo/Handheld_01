package com.example.handheld.modelos;

public class RollosAlambronInven{

    String nit_proveedor;
    String num_importacion;
    String id_solicitud_det;
    String numero_rollo;
    String peso;
    String codigo;
    String costo_kilo;
    String tipo_salida;

    public RollosAlambronInven(String nit_proveedor, String num_importacion, String id_solicitud_det, String numero_rollo, String peso, String codigo, String costo_kilo, String tipo_salida) {
        this.nit_proveedor = nit_proveedor;
        this.num_importacion = num_importacion;
        this.id_solicitud_det = id_solicitud_det;
        this.numero_rollo = numero_rollo;
        this.peso = peso;
        this.codigo = codigo;
        this.costo_kilo = costo_kilo;
        this.tipo_salida = tipo_salida;
    }

    public RollosAlambronInven() {
    }

    public String getNit_proveedor() {
        return nit_proveedor;
    }

    public void setNit_proveedor(String nit_proveedor) {
        this.nit_proveedor = nit_proveedor;
    }

    public String getNum_importacion() {
        return num_importacion;
    }

    public void setNum_importacion(String num_importacion) {
        this.num_importacion = num_importacion;
    }

    public String getId_solicitud_det() {
        return id_solicitud_det;
    }

    public void setId_solicitud_det(String id_solicitud_det) {
        this.id_solicitud_det = id_solicitud_det;
    }

    public String getNumero_rollo() {
        return numero_rollo;
    }

    public void setNumero_rollo(String numero_rollo) {
        this.numero_rollo = numero_rollo;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCosto_kilo() {
        return costo_kilo;
    }

    public void setCosto_kilo(String costo_kilo) {
        this.costo_kilo = costo_kilo;
    }

    public String getTipo_salida() {
        return tipo_salida;
    }

    public void setTipo_salida(String tipo_salida) {
        this.tipo_salida = tipo_salida;
    }
}

