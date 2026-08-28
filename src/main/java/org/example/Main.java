package org.example;

import Utils.CreacionDb;
import Utils.PoblarBase;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        // 1. Inicializar el esquema de la base de datos
        CreacionDb esquema = new CreacionDb();
        esquema.createTables();


        /* Ejercicio 2
        PoblarBase poblar = new PoblarBase();
        try {
            poblar.populateDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

    }
}
