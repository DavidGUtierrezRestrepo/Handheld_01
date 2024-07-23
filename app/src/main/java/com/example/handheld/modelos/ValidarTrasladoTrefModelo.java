package com.example.handheld.modelos;

public class ValidarTrasladoTrefModelo {
    String scal;
    String sav;
    String sar;
    String traslado;
    String anular;

    public ValidarTrasladoTrefModelo() {
    }

    public ValidarTrasladoTrefModelo(String scal, String sav, String sar, String traslado, String anular) {
        this.scal = scal;
        this.sav = sav;
        this.sar = sar;
        this.traslado = traslado;
        this.anular = anular;
    }

    public String getScal() {
        return scal;
    }

    public void setScal(String scal) {
        this.scal = scal;
    }

    public String getSav() {
        return sav;
    }

    public void setSav(String sav) {
        this.sav = sav;
    }

    public String getSar() {
        return sar;
    }

    public void setSar(String sar) {
        this.sar = sar;
    }

    public String getTraslado() {
        return traslado;
    }

    public void setTraslado(String traslado) {
        this.traslado = traslado;
    }

    public String getAnular() {
        return anular;
    }

    public void setAnular(String anular) {
        this.anular = anular;
    }
}
