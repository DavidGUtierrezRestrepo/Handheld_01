package com.example.handheld.modelos;

public class RollosMPGalvInven extends RolloGalvInventario {
    String cod_orden;
    String id_detalle;
    String id_rollo;
    String peso;
    String codigo;
    String nombre;
    String fecha_hora;
    String destino;
    String traslado;
    String anulado;

    String manuales;
    String saga;

    public RollosMPGalvInven(String cod_orden, String id_detalle, String id_rollo, String peso, String codigo, String nombre, String fecha_hora, String destino, String traslado, String anulado, String manuales, String saga) {
        this.cod_orden = cod_orden;
        this.id_detalle = id_detalle;
        this.id_rollo = id_rollo;
        this.peso = peso;
        this.codigo = codigo;
        this.nombre = nombre;
        this.fecha_hora = fecha_hora;
        this.destino = destino;
        this.traslado = traslado;
        this.anulado = anulado;
        this.manuales = manuales;
        this.saga = saga;
    }

    public RollosMPGalvInven() {
    }

    public String getCod_orden() {
        return cod_orden;
    }

    public void setCod_orden(String cod_orden) {
        this.cod_orden = cod_orden;
    }

    public String getId_detalle() {
        return id_detalle;
    }

    public void setId_detalle(String id_detalle) {
        this.id_detalle = id_detalle;
    }

    public String getId_rollo() {
        return id_rollo;
    }

    public void setId_rollo(String id_rollo) {
        this.id_rollo = id_rollo;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFecha_hora() {
        return fecha_hora;
    }

    public void setFecha_hora(String fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getTraslado() {
        return traslado;
    }

    public void setTraslado(String traslado) {
        this.traslado = traslado;
    }

    public String getAnulado() {
        return anulado;
    }

    public void setAnulado(String anulado) {
        this.anulado = anulado;
    }

    public String getManuales() {
        return manuales;
    }

    public void setManuales(String manuales) {
        this.manuales = manuales;
    }

    public String getSaga() {
        return saga;
    }

    public void setSaga(String saga) {
        this.saga = saga;
    }
}
