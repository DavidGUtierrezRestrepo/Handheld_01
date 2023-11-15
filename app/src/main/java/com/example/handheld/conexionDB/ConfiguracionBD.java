package com.example.handheld.conexionDB;

public class ConfiguracionBD {
    private static String nombreBDCorsan = "CORSAN"; // Nombre de la base de datos real
    private static String nombreBDProduccion = "PRGPRODUCCION"; // Nombre de la base de datos real

    private static String nombreBDPruebaCorsan = "JJVDMSCIERREAGOSTO"; // Nombre de la base de datos de prueba
    private static String nombreBDPruebaProduccion = "JJVPRGPRODUCCION"; // Nombre de la base de datos de prueba

    private static boolean modoPrueba = true; // Indica si estamos en modo prueba o no

    // Método para obtener el nombre de la base de datos según el modo
    public static String obtenerNombreBD(int numeroBD) {
        if (modoPrueba) {
            switch (numeroBD) {
                case 1:
                    return nombreBDPruebaCorsan;
                case 2:
                    return nombreBDPruebaProduccion;
                default:
                    return "";
            }
        } else {
            switch (numeroBD) {
                case 1:
                    return nombreBDCorsan;
                case 2:
                    return nombreBDProduccion;
                default:
                    return "";
            }
        }
    }

    public static boolean isModoPrueba() {
        return modoPrueba;
    }

    // Método para cambiar entre modo prueba y modo real
    public static void cambiarModo() {
        modoPrueba = !modoPrueba;
    }
}
