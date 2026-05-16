package com.suraksha.setu.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic abstract DAO — demonstrates Unit III Generics.
 * Type parameter T is the domain model (Worker, WorkHistory, etc.)
 *
 * Usage:
 *   public class WorkerDAO extends GenericDAO<Worker> { ... }
 */
public abstract class GenericDAO<T> {

    /** Insert entity into DB. */
    public abstract void save(T entity) throws SQLException;

    /** Find entity by primary key. */
    public abstract T findById(int id) throws SQLException;

    /** Retrieve all entities. */
    public abstract List<T> findAll() throws SQLException;

    /** Update existing entity. */
    public abstract void update(T entity) throws SQLException;

    /** Delete entity by primary key. */
    public abstract void delete(int id) throws SQLException;

    /**
     * Generic filtering method with Predicate — demonstrates generics + lambda.
     * Filters a list using any Predicate<T> (functional interface).
     */
    public <E> List<E> filterList(List<E> list, java.util.function.Predicate<E> condition) {
        List<E> result = new java.util.ArrayList<>();
        java.util.Iterator<E> it = list.iterator();  // Iterator (Unit III)
        while (it.hasNext()) {
            E item = it.next();
            if (condition.test(item)) {
                result.add(item);
            }
        }
        return result;
    }
}
