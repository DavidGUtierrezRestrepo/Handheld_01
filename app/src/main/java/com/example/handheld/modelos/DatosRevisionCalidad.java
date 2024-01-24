package com.example.handheld.modelos;

public class DatosRevisionCalidad {
    String fecha_revision;
    String revisor;
    String estado;

    public DatosRevisionCalidad() {
    }

    public DatosRevisionCalidad(String fecha_revision, String revisor, String estado) {
        this.fecha_revision = fecha_revision;
        this.revisor = revisor;
        this.estado = estado;
    }

    public String getFecha_revision() {
        return fecha_revision;
    }

    public void setFecha_revision(String fecha_revision) {
        this.fecha_revision = fecha_revision;
    }

    public String getRevisor() {
        return revisor;
    }

    public void setRevisor(String revisor) {
        this.revisor = revisor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
