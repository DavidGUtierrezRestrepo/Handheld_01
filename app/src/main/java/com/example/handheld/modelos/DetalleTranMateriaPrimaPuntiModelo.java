package com.example.handheld.modelos;

public class DetalleTranMateriaPrimaPuntiModelo {
    String consecutivo;
    String idDetalle;
    String numRollo;
    String codigo;
    String peso;
    String tipoTransa;
    String numTransa;

    public DetalleTranMateriaPrimaPuntiModelo() {
    }

    public DetalleTranMateriaPrimaPuntiModelo(String consecutivo, String idDetalle, String numRollo, String codigo, String peso, String tipoTransa, String numTransa) {
        this.consecutivo = consecutivo;
        this.idDetalle = idDetalle;
        this.numRollo = numRollo;
        this.codigo = codigo;
        this.peso = peso;
        this.tipoTransa = tipoTransa;
        this.numTransa = numTransa;
    }

    public String getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(String consecutivo) {
        this.consecutivo = consecutivo;
    }

    public String getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(String idDetalle) {
        this.idDetalle = idDetalle;
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
