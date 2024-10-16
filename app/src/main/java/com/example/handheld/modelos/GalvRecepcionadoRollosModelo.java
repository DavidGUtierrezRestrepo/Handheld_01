package com.example.handheld.modelos;

public class GalvRecepcionadoRollosModelo {
    Double peso;
    Double promedio;
    Double costo_unitario;
    String referencia;

    String nro_orden;

    String nro_rollo;

    public GalvRecepcionadoRollosModelo() {
    }

    public GalvRecepcionadoRollosModelo(Double peso, Double promedio, Double costo_unitario, String referencia, String nro_orden, String nro_rollo) {
        this.peso = peso;
        this.promedio = promedio;
        this.costo_unitario = costo_unitario;
        this.referencia = referencia;
        this.nro_orden = nro_orden;
        this.nro_rollo = nro_rollo;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getPromedio() {
        return promedio;
    }

    public void setPromedio(Double promedio) {
        this.promedio = promedio;
    }

    public Double getCosto_unitario() {
        return costo_unitario;
    }

    public void setCosto_unitario(Double costo_unitario) {
        this.costo_unitario = costo_unitario;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getNro_orden() {
        return nro_orden;
    }

    public void setNro_orden(String nro_orden) {
        this.nro_orden = nro_orden;
    }

    public String getNro_rollo() {
        return nro_rollo;
    }

    public void setNro_rollo(String nro_rollo) {
        this.nro_rollo = nro_rollo;
    }
}

