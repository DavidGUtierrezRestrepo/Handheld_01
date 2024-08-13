package com.example.handheld.modelos;

public class DetalleTranModeloTref {
    String numero;
    String Num_transaccion;
    String tipo;
    String peso;
    String codigo;
    String id_rollo;
    String id_detalle;
    String descripcion;
    String costo_unit;

    public DetalleTranModeloTref(String numero, String num_transaccion, String tipo, String peso, String codigo, String id_rollo, String id_detalle, String descripcion, String costo_unit) {
        this.numero = numero;
        Num_transaccion = num_transaccion;
        this.tipo = tipo;
        this.peso = peso;
        this.codigo = codigo;
        this.id_rollo = id_rollo;
        this.id_detalle = id_detalle;
        this.descripcion = descripcion;
        this.costo_unit = costo_unit;
    }

    public DetalleTranModeloTref() {
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNum_transaccion() {
        return Num_transaccion;
    }

    public void setNum_transaccion(String num_transaccion) {
        Num_transaccion = num_transaccion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public String getId_rollo() {
        return id_rollo;
    }

    public void setId_rollo(String id_rollo) {
        this.id_rollo = id_rollo;
    }

    public String getId_detalle() {
        return id_detalle;
    }

    public void setId_detalle(String id_detalle) {
        this.id_detalle = id_detalle;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Comparable<String> getCosto_unit() {
        return costo_unit;
    }

    public void setCosto_unit(String costo_unit) {
        this.costo_unit = costo_unit;
    }
}
