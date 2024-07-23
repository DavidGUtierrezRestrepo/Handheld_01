package com.example.handheld.modelos;

public class DetalleTranMateriaPrimaPuasModelo {
    String Nro_orden;
    String numRollo;
    String codigo;
    String peso;
    String tipoTransa;
    String numTransa;

    public DetalleTranMateriaPrimaPuasModelo() {
    }

    public DetalleTranMateriaPrimaPuasModelo(String nro_orden, String numRollo, String codigo, String peso, String tipoTransa, String numTransa) {
        this.Nro_orden = nro_orden;
        this.numRollo = numRollo;
        this.codigo = codigo;
        this.peso = peso;
        this.tipoTransa = tipoTransa;
        this.numTransa = numTransa;
    }

    public String getNro_orden() {
        return Nro_orden;
    }

    public void setNro_orden(String nro_orden) {
        Nro_orden = nro_orden;
    }

    public String getNumRollo() {
        return numRollo;
    }

    public void setNumRollo(String numRollo) {
        this.numRollo = numRollo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getTipoTransa() {
        return tipoTransa;
    }

    public void setTipoTransa(String tipoTransa) {
        this.tipoTransa = tipoTransa;
    }

    public String getNumTransa() {
        return numTransa;
    }

    public void setNumTransa(String numTransa) {
        this.numTransa = numTransa;
    }
}
