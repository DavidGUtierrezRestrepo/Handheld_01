package com.example.handheld.modelos;

public class RecoRecepcionModelo {
    String cod_orden;
    String id_detalle;
    String id_rollo;
    String referencia;
    String descripcion;
    String peso;
    String color;

    public RecoRecepcionModelo() {
    }

    public RecoRecepcionModelo(String cod_orden, String id_detalle, String id_rollo, String referencia, String descripcion, String peso, String color) {
        this.cod_orden = cod_orden;
        this.id_detalle = id_detalle;
        this.id_rollo = id_rollo;
        this.referencia = referencia;
        this.descripcion = descripcion;
        this.peso = peso;
        this.color = color;
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

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
