package org.example.Services;

import org.example.dao.ClienteDAO;
import org.example.Entity.Estudiante;

import java.util.Map;

public class ClienteServices {
    private final ClienteDAO clienteDAO;

    public ClienteServices() {
        DAOFactory daoFactory = DAOFactory.getDAOFactory(1);
        this.clienteDAO = daoFactory.getClienteDAO();
    }

    public Map<Estudiante, Double> getClientesOrdenadosPorFacturacion() {
        return clienteDAO.getClientesOrdenadosPorFacturacion();
    }
}