package com.example.handheld.ClasesOperativas;

import android.content.Context;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Ing_prod_ad {
    /* Se comenta el codigo original sin limite de tiempo
    public Boolean ExecuteSqlTransaction(List<Object> listSql , String db, Context context) {
        boolean resp = false;
        String ip="10.10.10.246", port="1433", username = "Practicante.sistemas", password = "+Psis.*";
        String connectionUrl= "jdbc:jtds:sqlserver://"+ip+":"+port+";databasename="+ db +";User="+username+";password="+password+";";

        try (Connection con = DriverManager.getConnection(connectionUrl)) {
            con.setAutoCommit(false);

            try (Statement stmt = con.createStatement()) {
                for (Object consultaSQL : listSql) {
                    stmt.executeUpdate((String) consultaSQL);
                }
                con.commit();
                resp = true;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        return resp;

    }*/
    //Transaccion con limite de tiempo - 1 minuto
    public String ExecuteSqlTransaction(List<Object> listSql, String db, Context context) {
        AtomicBoolean isError = new AtomicBoolean(false); // Usamos AtomicBoolean para manejar la bandera isError
        AtomicReference<String> errorMessage = new AtomicReference<>(null); // Almacenar el mensaje de error específico

        String ip = "10.10.10.246", port = "1433", username = "Practicante.sistemas", password = "+Psis.*";
        String connectionUrl = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databasename=" + db + ";User=" + username + ";password=" + password + ";";

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            try (Connection con = DriverManager.getConnection(connectionUrl)) {
                con.setAutoCommit(false);

                try (Statement stmt = con.createStatement()) {
                    for (Object consultaSQL : listSql) {
                        stmt.executeUpdate((String) consultaSQL);
                    }
                    con.commit();
                } catch (SQLException ex) {
                    con.rollback();
                    isError.set(true); // Establecer isError en true si ocurre algún error
                    errorMessage.set(ex.getMessage()); // Almacenar el mensaje de error específico-
                }
            } catch (SQLException ex) {
                isError.set(true); // Establecer isError en true si ocurre algún error
                errorMessage.set(ex.getMessage()); // Almacenar el mensaje de error específico
            }
            return isError.get() ? errorMessage.get() : ""; // Retorna el mensaje de error específico o cadena vacía
        });

        int timeoutSeconds = 60; // Establecer el tiempo de espera en 1 minuto (60 segundos)
        try {
            future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return "La transacción ha excedido el tiempo límite.";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "Error inesperado.";
        } finally {
            executor.shutdownNow();
        }

        if (isError.get()) {
            return errorMessage.get(); // Retornar el mensaje de error específico si isError es true
        } else {
            return ""; // Retornar un string vacío si isError es false (sin errores)
        }
    }

    /*
    public Boolean ExecuteSqlTransaction(List<Object> listSql , String db, Context context) {
        boolean resp = false;
        String ip="10.10.10.246", port="1433", username = "Practicante.sistemas", password = "+Psis.*";
        String connectionUrl= "jdbc:jtds:sqlserver://"+ip+":"+port+";databasename="+ db +";User="+username+";password="+password+";";

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> {
            try (Connection con = DriverManager.getConnection(connectionUrl)) {
                con.setAutoCommit(false);

                try (Statement stmt = con.createStatement()) {
                    for (Object consultaSQL : listSql) {
                        stmt.executeUpdate((String) consultaSQL);
                    }
                    con.commit();
                    return true;
                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        });

        int timeoutSeconds = 60; // Establecer el tiempo de espera en 1 minuto (60 segundos)
        try {
            resp = future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            Toast.makeText(context, "La transacción ha excedido el tiempo límite.", Toast.LENGTH_LONG).show();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }

        return resp;

    }*/

    /* Se comenta el codigo que se utilizo para hacer que se genere el error y hacer la prueba
    public Boolean ExecuteSqlTransaction(List<Object> listSql, String db, Context context) {
        boolean resp = false;
        String ip = "10.10.10.246", port = "1433", username = "Practicante.sistemas", password = "+Psis.*";
        String connectionUrl = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databasename=" + db + ";User=" + username + ";password=" + password + ";";

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> {
            try (Connection con = DriverManager.getConnection(connectionUrl)) {
                con.setAutoCommit(false);

                try (Statement stmt = con.createStatement()) {
                    for (Object consultaSQL : listSql) {
                        stmt.executeUpdate((String) consultaSQL);
                        sleep(90000); // Retraso de 90 segundos entre consultas
                    }
                    con.commit();
                    return true;
                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        });

        int timeoutSeconds = 60; // Tiempo de espera de 60 segundos
        try {
            resp = future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            Toast.makeText(context, "La transacción ha excedido el tiempo límite.", Toast.LENGTH_LONG).show();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }

        return resp;
    }

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

}
