package org.example.dao.mysql;

import org.example.Factory.MySQLDAOFactory;
import org.example.dao.Producto_facturaDAO;
import org.example.entity.Cliente;
import org.example.entity.Factura_producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MySQLProducto_facturaDAO implements Producto_facturaDAO {
    @Override
    public void insertProductoFactura(Factura_producto f) {
        String insert = "INSERT INTO factura_producto (idFactura, idProducto, cantidad) VALUES (?, ?, ?)";

        Connection conn = MySQLDAOFactory.createConnection();

        if (conn == null) return;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {

                ps.setInt(1, f.getIdFactura());
                ps.setInt(2, f.getIdProducto());
                ps.setInt(3, f.getCantidad());

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
    public void insertAll(List<Factura_producto> producto_facturas) {
        String insert = "INSERT INTO factura_producto (idFactura, idProducto, cantidad) VALUES (?, ?, ?)";

        Connection conn = MySQLDAOFactory.createConnection();

        if (conn == null) return;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {

                for (Factura_producto f_p : producto_facturas) {

                    ps.setInt(1, f_p.getIdFactura());
                    ps.setInt(2, f_p.getIdProducto());
                    ps.setInt(3, f_p.getCantidad());

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
