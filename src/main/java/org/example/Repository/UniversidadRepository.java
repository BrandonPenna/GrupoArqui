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
}