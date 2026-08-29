package org.example.dao.mysql;

import org.example.Factory.MySQLDAOFactory;
import org.example.dao.ClienteDAO;
import org.example.entity.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MySQLClienteDAO implements ClienteDAO {

    @Override
    public void insertCliente(Cliente cliente) {
        String insert = "INSERT INTO cliente (idCliente, nombre, email) VALUES (?, ?, ?)";

        Connection conn = MySQLDAOFactory.createConnection();

        if (conn == null) return;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {

                ps.setInt(1, cliente.getIdCliente());
                ps.setString(2, cliente.getNombre());
                ps.setString(3, cliente.getEmail());

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
    public void insertAll(List<Cliente> clientes) {
        String insert = "INSERT INTO cliente (idCliente, nombre, email) VALUES (?, ?, ?)";

        Connection conn = MySQLDAOFactory.createConnection();

        if (conn == null) return;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {

                for (Cliente cliente : clientes) {

                    ps.setInt(1, cliente.getIdCliente());
                    ps.setString(2, cliente.getNombre());
                    ps.setString(3, cliente.getEmail());

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
    public Map<Cliente, Double> getClientesOrdenadosPorFacturacion() {
        String query = "SELECT c.idCliente, c.nombre, c.email, SUM(fp.cantidad * p.valor) AS total " +
                "FROM cliente c " +
                "JOIN factura f ON f.idCliente = c.idCliente " +
                "JOIN factura_producto fp ON fp.idFactura = f.idFactura " +
                "JOIN producto p ON p.idProducto = fp.idProducto " +
                "GROUP BY c.idCliente, c.nombre, c.email " +
                "ORDER BY total DESC";

        Connection conn = MySQLDAOFactory.createConnection();
        if (conn == null) return null;

        Map<Cliente, Double> resultado = new LinkedHashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente c = new Cliente(rs.getInt("idCliente"), rs.getString("nombre"), rs.getString("email"));
                resultado.put(c, rs.getDouble("total"));
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

        return resultado;
    }
}