package com.example.handheld.modelos;

public class ValidarTrasladoModelo {
    String traslado;
    String destino;
    String anular;

    public ValidarTrasladoModelo() {
    }

    public ValidarTrasladoModelo(String traslado, String destino, String anular) {
        this.traslado = traslado;
        this.destino = destino;
        this.anular = anular;
    }

    public String getTraslado() {
        return traslado;
    }

    public void setTraslado(String traslado) {
        this.traslado = traslado;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getAnular() {
        return anular;
    }

    public void setAnular(String anular) {
        this.anular = anular;
    }
}
