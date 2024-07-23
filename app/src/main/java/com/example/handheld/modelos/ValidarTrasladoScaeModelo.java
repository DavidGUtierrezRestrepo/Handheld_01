package com.example.handheld.modelos;

public class ValidarTrasladoScaeModelo {
    String scae;
    String tipo_trans;

    public ValidarTrasladoScaeModelo() {
    }

    public ValidarTrasladoScaeModelo(String scae, String tipo_trans) {
        this.scae = scae;
        this.tipo_trans = tipo_trans;
    }

    public String getScae() {
        return scae;
    }

    public void setScae(String scae) {
        this.scae = scae;
    }

    public String getTipo_trans() {
        return tipo_trans;
    }

    public void setTipo_trans(String tipo_trans) {
        this.tipo_trans = tipo_trans;
    }
}
