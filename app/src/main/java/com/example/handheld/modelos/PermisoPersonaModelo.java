package com.example.handheld.modelos;

public class PermisoPersonaModelo {
    String nit;
    String permiso;

    public PermisoPersonaModelo() {
    }

    public PermisoPersonaModelo(String nit, String permiso) {
        this.nit = nit;
        this.permiso = permiso;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getPermiso() {
        return permiso;
    }

    public void setPermiso(String permiso) {
        this.permiso = permiso;
    }
}
