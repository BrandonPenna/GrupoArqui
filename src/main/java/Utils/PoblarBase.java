package Utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.example.dao.mysql.MySQLClienteDAO;
import org.example.dao.mysql.MySQLFacturaDAO;
import org.example.dao.mysql.MySQLProducto_facturaDAO;
import org.example.dao.mysql.MySQLProductoDAO;
import org.example.Entity.Estudiante;
import org.example.Entity.Universidad;
import org.example.Entity.Inscripcion;
import org.example.Entity.Carrera;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PoblarBase {

    private Iterable<CSVRecord> getData(String archivo) throws IOException {
        String path = "src\\main\\java\\Utils\\" + archivo;
        Reader in = new FileReader(path);

        CSVFormat format = CSVFormat.EXCEL.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        return format.parse(in).getRecords();
    }

    public void populateDB() throws SQLException, IOException {
        List<Estudiante> clientes = new ArrayList<>();
        List<Universidad> universidads = new ArrayList<>();
        List<Carrera> productos = new ArrayList<>();
        List<Inscripcion> facturaProductos = new ArrayList<>();
        try {
            System.out.println("Poblando base");
            for (CSVRecord row : getData("clientes.csv")) {
                if (row.size() >= 3) {
                    String idString = row.get(0);
                    String name = row.get(1);
                    String email = row.get(2);
                    if (!idString.isEmpty() && !name.isEmpty() && !email.isEmpty()) {
                        try {
                            int id = Integer.parseInt(idString);
                            Estudiante cliente = new Estudiante(id, name, email);
                            clientes.add(cliente);
                        } catch (NumberFormatException e) {
                            System.err.println("Error de formato en datos de persona: " + e.getMessage());
                        }
                    }
                }


            }

            for (CSVRecord row : getData("facturas.csv")) {
                if (row.size() >= 2) { // Verificar que hay al menos 4 campos en el CSVRecord
                    String idFactura = row.get(0);
                    String idCliente = row.get(1);

                    if (!idFactura.isEmpty() && !idCliente.isEmpty()) {
                        try {
                            int idF = Integer.parseInt(idFactura);
                            int idC = Integer.parseInt(idCliente);

                            Universidad universidad = new Universidad(idF, idC);
                            universidads.add(universidad);
                        } catch (NumberFormatException e) {
                            System.err.println("Error de formato en datos de factura: " + e.getMessage());
                        }
                    }
                }

            }

            for (CSVRecord row : getData("productos.csv")) {
                if (row.size() >= 3) {
                    String idProducto = row.get(0);
                    String nombre = row.get(1);
                    String valor = row.get(2);

                    if (!idProducto.isEmpty() && !nombre.isEmpty() && !valor.isEmpty()) {
                        try {
                            int idP = Integer.parseInt(idProducto);
                            float val = Float.parseFloat(valor);

                            Carrera Producto = new Carrera(idP, nombre, val);
                            productos.add(Producto);
                        } catch (NumberFormatException e) {
                            System.err.println("Error de formato en datos de persona: " + e.getMessage());
                        }
                    }
                }
            }

            for (CSVRecord row : getData("facturas-productos.csv")) {
                if (row.size() >= 3) { // Verificar que hay al menos 4 campos en el CSVRecord
                    String idFactura = row.get(0);
                    String idProducto = row.get(1);
                    String cantidad = row.get(2);

                    if (!idFactura.isEmpty() && !idProducto.isEmpty() && !cantidad.isEmpty()) {
                        try {
                            int idF = Integer.parseInt(idFactura);
                            int idP = Integer.parseInt(idProducto);
                            int cant = Integer.parseInt(cantidad);

                            Inscripcion facturaProducto = new Inscripcion(idF, idP, cant);
                            facturaProductos.add(facturaProducto);
                        } catch (NumberFormatException e) {
                            System.err.println("Error de formato en datos de persona: " + e.getMessage());
                        }
                    }
                }


            }
            MySQLClienteDAO clienteDAO = new MySQLClienteDAO();
            clienteDAO.insertAll(clientes);

            MySQLProductoDAO productoDAO = new MySQLProductoDAO();
            productoDAO.insertAll(productos);

            MySQLFacturaDAO facturaDAO = new MySQLFacturaDAO();
            facturaDAO.insertAll(universidads);

            MySQLProducto_facturaDAO producto_facturaDAO = new MySQLProducto_facturaDAO();
            producto_facturaDAO.insertAll(facturaProductos);

        } catch (IOException | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}