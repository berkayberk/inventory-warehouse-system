package com.warehouse.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract for all DAO implementations.
 *
 * @param <T>  entity type
 * @param <ID> primary-key type
 */
public interface BaseDAO<T, ID> {

    /** Persist a new entity and return it with the generated ID populated. */
    T save(T entity);

    /** Update an existing entity. */
    void update(T entity);

    /** Delete the entity with the given primary key. */
    void delete(ID id);

    /** Find by primary key; returns empty Optional if not found. */
    Optional<T> findById(ID id);

    /** Return all rows of this entity type. */
    List<T> findAll();
}
