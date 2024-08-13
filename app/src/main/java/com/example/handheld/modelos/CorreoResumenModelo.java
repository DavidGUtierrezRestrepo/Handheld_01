package com.example.handheld.modelos;

public class CorreoResumenModelo {

    String mail;
    String nit;

    public CorreoResumenModelo(String mail) {
        this.mail = mail;
        this.nit = nit;
    }

    public CorreoResumenModelo() {
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String Correo) {
        this.mail = Correo;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }
}
