package org.example.dao.mysql;

import org.example.Factory.MySQLDAOFactory;
import org.example.dao.ProductoDAO;
import org.example.entity.Cliente;
import org.example.entity.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
}
