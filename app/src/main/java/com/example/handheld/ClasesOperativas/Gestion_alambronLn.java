package com.example.handheld.ClasesOperativas;

public class Gestion_alambronLn {

    public String extraerDatoCodigoBarras(String dato, String codigoBarra){
        Integer numSeparador = 0;
        int contSeparador = 0;
        StringBuilder respuesta = new StringBuilder();
        switch (dato){
            case "proveedor":
                numSeparador = 0;
                break;
            case "num_importacion":
                numSeparador = 1;
                break;
            case "detalle":
                numSeparador = 2;
                break;
            case "num_rollo":
                numSeparador = 3;
                break;
        }
        for (int i = 0; i <= codigoBarra.length() - 1; i++){
            if (numSeparador.equals(contSeparador)){
                if (codigoBarra.charAt(i) != '-'){
                    respuesta.append(codigoBarra.charAt(i));
                }
            }
            if (codigoBarra.charAt(i) == '-'){
                contSeparador += 1;
            }
        }
        return respuesta.toString();
    }

    public String extraerDatoCodigoBarrasTrefilacion(String dato, String codigoBarra){
        Integer numSeparador = 0;
        int contSeparador = 0;
        StringBuilder respuesta = new StringBuilder();
        switch (dato){
            case "cod_orden":
                numSeparador = 0;
                break;
            case "id_detalle":
                numSeparador = 1;
                break;
            case "id_rollo":
                numSeparador = 2;
                break;
        }
        for (int i = 0; i <= codigoBarra.length() - 1; i++){
            if (numSeparador.equals(contSeparador)){
                if (codigoBarra.charAt(i) != '-'){
                    respuesta.append(codigoBarra.charAt(i));
                }
            }
            if (codigoBarra.charAt(i) == '-'){
                contSeparador += 1;
            }
        }
        return respuesta.toString();
    }
}
