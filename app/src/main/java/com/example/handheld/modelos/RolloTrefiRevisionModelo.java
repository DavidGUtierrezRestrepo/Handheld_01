package com.example.handheld.modelos;

public class RolloTrefiRevisionModelo {
    String id_revision;
    String fecha_hora;
    String estado;

    public RolloTrefiRevisionModelo() {
    }

    public RolloTrefiRevisionModelo(String id_revision, String fecha_hora, String estado) {
        this.id_revision = id_revision;
        this.fecha_hora = fecha_hora;
        this.estado = estado;
    }

    public String getId_revision() {
        return id_revision;
    }

    public void setId_revision(String id_revision) {
        this.id_revision = id_revision;
    }

    public String getFecha_hora() {
        return fecha_hora;
    }

    public void setFecha_hora(String fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
