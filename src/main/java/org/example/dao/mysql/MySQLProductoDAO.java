package org.example.dao.mysql;

import org.example.Factory.MySQLDAOFactory;
import org.example.dao.ProductoDAO;
import org.example.entity.Cliente;
import org.example.entity.Producto;
import org.example.entity.ProductoRecaudado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MySQLProductoDAO implements ProductoDAO {
    @Override
    public void insertProducto(Producto p) {
        String insert = "INSERT INTO producto (idProducto, nombre, valor) VALUES (?, ?, ?)";

        Connection conn = MySQLDAOFactory.createConnection();

        if (conn == null) return;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {

                ps.setInt(1, p.getIdProducto());
                ps.setString(2, p.getNombre());
                ps.setFloat(3, p.getValor());

                ps.executeUpdate();

                conn.commit();

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

    @Override
    public void insertAll(List<Producto> productos) {
        String insert = "INSERT INTO producto (idProducto, nombre, valor) VALUES (?, ?, ?)";

        Connection conn = MySQLDAOFactory.createConnection();

        if (conn == null) return;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {

                for (Producto producto : productos) {

                    ps.setInt(1, producto.getIdProducto());
                    ps.setString(2, producto.getNombre());
                    ps.setFloat(3, producto.getValor());

                    ps.addBatch();
                }

                ps.executeBatch();

                conn.commit();

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

    @Override
        public ProductoRecaudado getProductoMasRecaudado() {
            // 1. Agregamos el SUM con un alias en el SELECT
            String query = "SELECT p.idProducto, p.nombre, p.valor, SUM(fp.cantidad * p.valor) AS recaudado " +
                    "FROM producto p " +
                    "JOIN factura_producto fp ON p.idProducto = fp.idProducto " +
                    "GROUP BY p.idProducto, p.nombre, p.valor " +
                    "ORDER BY recaudado DESC " +
                    "LIMIT 1";

            Connection conn = MySQLDAOFactory.createConnection();
            if (conn == null) return null;

            ProductoRecaudado productoRecaudado = null;

            try (PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    int id = rs.getInt("idProducto");
                    String nombre = rs.getString("nombre");
                    float valor = rs.getFloat("valor");

                    // 2. Leemos el valor recaudado del ResultSet
                    // (Usa getInt() si tu atributo 'recaudado' es int, o getFloat()/getDouble() si maneja decimales)
                    int recaudado = rs.getInt("recaudado");

                    // 3. Instanciamos tu objeto pasándole también el valor recaudado
                    productoRecaudado = new ProductoRecaudado(id, nombre, valor, recaudado);
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

            return productoRecaudado;
    }
}
