package org.example;

import Utils.CreacionDb;
import Utils.PoblarBase;
import org.example.Services.ProductoServices;
import org.example.Services.ClienteServices;
import org.example.entity.Producto;
import org.example.entity.Cliente;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

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
        }
        */
        /* Ejercicio 3 */
        ProductoServices productoServices = new ProductoServices();
        Producto masRecaudado = productoServices.getProductoMasRecaudado();
        if (masRecaudado != null) {
            System.out.println("Producto que más recaudó:");
            System.out.println("ID: " + masRecaudado.getIdProducto());
            System.out.println("Nombre: " + masRecaudado.getNombre());
            System.out.println("Valor unitario: " + masRecaudado.getValor());
        } else {
            System.out.println("No se encontraron productos o registros de ventas.");
        }

        /* Ejercicio 4 */
        ClienteServices clienteServices = new ClienteServices();
        Map<Cliente, Double> ranking = clienteServices.getClientesOrdenadosPorFacturacion();
        System.out.println("Clientes ordenados por facturación:");
        for (Map.Entry<Cliente, Double> entry : ranking.entrySet()) {
            System.out.println(entry.getKey().getNombre() + " - Total facturado: " + entry.getValue());
        }

    }
}