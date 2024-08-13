package com.example.handheld.modelos;

public class PedidoGalvModelo {

    private Integer numero;
    private Integer idDetalle;
    private String fecha;
    private String codigo;
    private String pendiente;
    private String descripcion;

    public PedidoGalvModelo(Integer numero, Integer idDetalle, String fecha, String codigo, String pendiente, String descripcion) {
        this.numero = numero;
        this.idDetalle = idDetalle;
        this.fecha = fecha;
        this.codigo = codigo;
        this.pendiente = pendiente;
        this.descripcion = descripcion;
    }

    public PedidoGalvModelo() {
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getPendiente() {
        return pendiente;
    }

    public void setPendiente(String pendiente) {
        this.pendiente = pendiente;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
