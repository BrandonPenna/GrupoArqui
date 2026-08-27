package org.example;

import Utils.CreacionDb;

public class Main {
    public static void main(String[] args) {

        // 1. Inicializar el esquema de la base de datos
        CreacionDb esquema = new CreacionDb();
        esquema.createTables();

    }
}
