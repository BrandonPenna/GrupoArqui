package Utils;

import org.example.Factory.MySQLDAOFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreacionDb {

    private Connection conn;

    public CreacionDb() {
        conn = MySQLDAOFactory.createConnection();
    }

    public void createTables() {

        String cliente = "CREATE TABLE IF NOT EXISTS cliente (" +
                "idCliente INT, " +
                "nombre VARCHAR(500), " +
                "email VARCHAR(150), " +
                "PRIMARY KEY (idCliente))";

        String producto = "CREATE TABLE IF NOT EXISTS producto (" +
                "idProducto INT, " +
                "nombre VARCHAR(45), " +
                "valor FLOAT, " +
                "PRIMARY KEY (idProducto))";

        String factura = "CREATE TABLE IF NOT EXISTS factura (" +
                "idFactura INT, " +
                "idCliente INT, " +
                "PRIMARY KEY (idFactura), " +
                "FOREIGN KEY (idCliente) REFERENCES cliente(idCliente))";

        String facturaProducto = "CREATE TABLE IF NOT EXISTS factura_producto (" +
                "idFactura INT, " +
                "idProducto INT, " +
                "cantidad INT, " +
                "PRIMARY KEY (idFactura, idProducto), " +
                "FOREIGN KEY (idFactura) REFERENCES factura(idFactura), " +
                "FOREIGN KEY (idProducto) REFERENCES producto(idProducto))";

        if (conn == null) {
            return;
        }

        try {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement psCliente = conn.prepareStatement(cliente);
                    PreparedStatement psProducto = conn.prepareStatement(producto);
                    PreparedStatement psFactura = conn.prepareStatement(factura);
                    PreparedStatement psFacturaProducto = conn.prepareStatement(facturaProducto)
            ) {

                psCliente.executeUpdate();
                psProducto.executeUpdate();
                psFactura.executeUpdate();
                psFacturaProducto.executeUpdate();

                conn.commit();

                System.out.println("Tablas creadas correctamente.");

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}