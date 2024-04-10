package com.example.handheld.modelos;

public class ReferenciasPuasRecepcionModelo {

    String codigo;
    String descripcion;
    String cantidad;

    public ReferenciasPuasRecepcionModelo() {
    }

    public ReferenciasPuasRecepcionModelo(String codigo, String descripcion, String cantidad) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }
}
