package com.example.handheld.modelos;

public class RecoRecepcionadoRollosModelo {
    Double peso;
    Double promedio;
    Double costo_unitario;
    String referencia;

    public RecoRecepcionadoRollosModelo(Double peso, Double promedio, Double costo_unitario, String referencia) {
        this.peso = peso;
        this.promedio = promedio;
        this.costo_unitario = costo_unitario;
        this.referencia = referencia;
    }

    public RecoRecepcionadoRollosModelo() {
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
}
