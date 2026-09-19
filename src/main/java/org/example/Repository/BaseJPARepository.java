package org.example.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

public class BaseJPARepository<Entity, ID extends Serializable> implements Repository<Entity, ID> {

    protected EntityManager entityManager;


    private Class<Entity> entityClass;
    private Class<ID> idClass;

    BaseJPARepository(Class<Entity> entityClass, Class<ID> idClass) {
        this.entityClass = entityClass;
        this.idClass = idClass;
    }

    @Override
    public Entity findById(ID id) {
        if (id == null) {
            return null;
        }
        return entityManager.find(entityClass, id);
    }

    @Override
    public Entity persist(Entity entity) {
        entityManager.getTransaction().begin();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
        return entity;
    }

    @Override
    public Entity delete(ID id) {
        if (id == null) {
            return null;
        }

        // 1. Buscamos primero fuera de la transacción
        Entity entity = entityManager.find(entityClass, id);

        // 2. Si no existe, salimos rápido sin abrir transacciones innecesarias
        if (entity == null) {
            return null;
        }

        // 3. Si existe, recién ahí abrimos la transacción para borrar
        entityManager.getTransaction().begin();
        entityManager.remove(entity);
        entityManager.getTransaction().commit();

        return entity;
    }

    @Override
    public List<Entity> findAll() {
        // Creamos una consulta JPQL dinámica para traer todos los registros de la entidad
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return entityManager.createQuery(jpql, entityClass).getResultList();

    }
}
