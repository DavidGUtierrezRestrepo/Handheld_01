package com.example.handheld.modelos;

public class DetalleTranMateriaPrimaScalModelo {

    String nro_orden;
    String id_detalle;
    String numRollo;
    String codigo;
    String peso;
    String tipoTransa;
    String numTransa;

    public DetalleTranMateriaPrimaScalModelo() {
    }

    public DetalleTranMateriaPrimaScalModelo(String nro_orden, String id_detalle, String numRollo, String codigo, String peso, String tipoTransa, String numTransa) {
        this.nro_orden = nro_orden;
        this.id_detalle = id_detalle;
        this.numRollo = numRollo;
        this.codigo = codigo;
        this.peso = peso;
        this.tipoTransa = tipoTransa;
        this.numTransa = numTransa;
    }

    public String getNro_orden() {
        return nro_orden;
    }

    public void setNro_orden(String nro_orden) {
        this.nro_orden = nro_orden;
    }

    public String getId_detalle() {
        return id_detalle;
    }

    public void setId_detalle(String id_detalle) {
        this.id_detalle = id_detalle;
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
