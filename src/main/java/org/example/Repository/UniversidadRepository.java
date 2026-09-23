package org.example.Repository;

import org.example.Entity.Universidad;

public class UniversidadRepository extends BaseJPARepository<Universidad, Integer> {

    private static UniversidadRepository instance;

    private UniversidadRepository() {
        super(Universidad.class, Integer.class);
    }

    public static UniversidadRepository getInstance() {
        if (instance == null) {
            instance = new UniversidadRepository();
        }
        return instance;
    }

    public UniversidadRepository getUniversidadRepository() {
        return getInstance();
    }

    public boolean existePorId(Integer idUniversidad) {
        Long cantidad = entityManager
            .createQuery(
                    "SELECT COUNT(e) FROM Universidad e WHERE e.idUniversidad = :idUniversidad",
                    Long.class
            )
            .setParameter("idUniversidad", idUniversidad)
            .getSingleResult();

        return cantidad > 0;
    }
}