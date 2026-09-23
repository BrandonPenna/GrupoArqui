package org.example.Repository;


import org.example.Entity.Carrera;

public class CarreraRepository extends BaseJPARepository<Carrera, Integer> {

    private static CarreraRepository instance;

    private CarreraRepository() {
        super(Carrera.class, Integer.class);
    }

    public static CarreraRepository getInstance() {
        if (instance == null) {
            instance = new CarreraRepository();
        }
        return instance;
    }

    public boolean existePorId(Integer idCarrera) {
        Long cantidad = entityManager
            .createQuery(
                    "SELECT COUNT(e) FROM Carrera e WHERE e.idCarrera = :idCarrera",
                    Long.class
            )
            .setParameter("idCarrera", idCarrera)
            .getSingleResult();

        return cantidad > 0;
    }
}